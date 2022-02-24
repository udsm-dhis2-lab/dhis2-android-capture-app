package org.dhis2.usescases.searchTrackEntity

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagedList
import com.mapbox.geojson.FeatureCollection
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import org.dhis2.commons.data.SearchTeiModel
import org.dhis2.commons.data.uids
import org.dhis2.commons.network.NetworkUtils
import org.dhis2.data.search.SearchParametersModel
import org.dhis2.form.model.RowAction
import org.dhis2.maps.geometry.mapper.featurecollection.MapCoordinateFieldToFeatureCollection
import org.dhis2.maps.geometry.mapper.featurecollection.MapTeiEventsToFeatureCollection
import org.dhis2.maps.geometry.mapper.featurecollection.MapTeisToFeatureCollection
import org.dhis2.maps.mapper.EventToEventUiComponent
import org.dhis2.maps.utils.DhisMapUtils
import org.dhis2.usescases.searchTrackEntity.adapters.uids
import org.dhis2.utils.customviews.navigationbar.NavigationPageConfigurator
import org.hisp.dhis.android.core.program.Program

class SearchTEIViewModel(
    initialProgramUid: String?,
    initialQuery: MutableMap<String, String>?,
    private val presenter: SearchTEContractsModule.Presenter,
    private val searchRepository: SearchRepository,
    private val searchNavPageConfigurator: SearchPageConfigurator,
    private val mapTeisToFeatureCollection: MapTeisToFeatureCollection,
    private val mapTeiEventsToFeatureCollection: MapTeiEventsToFeatureCollection,
    private val mapCoordinateFieldToFeatureCollection: MapCoordinateFieldToFeatureCollection,
    private val eventToEventUiComponent: EventToEventUiComponent,
    private val mapUtils: DhisMapUtils,
    private val networkUtils: NetworkUtils
) : ViewModel() {

    private val _pageConfiguration = MutableLiveData<NavigationPageConfigurator>()
    val pageConfiguration: LiveData<NavigationPageConfigurator> = _pageConfiguration

    init {
        viewModelScope.launch {
            val result = async(Dispatchers.IO) {
                searchNavPageConfigurator.initVariables()
            }
            try {
                _pageConfiguration.postValue(result.await())
            } catch (e: Exception) {
                _pageConfiguration.postValue(searchNavPageConfigurator)
            }
        }
    }

    val queryData = mutableMapOf<String, String>().apply {
        initialQuery?.let { putAll(it) }
    }
    private val _selectedProgram = MutableLiveData(searchRepository.getProgram(initialProgramUid))
    val selectedProgram: LiveData<Program?> = _selectedProgram

    private val _refreshData = MutableLiveData(Unit)
    val refreshData: LiveData<Unit> = _refreshData

    private val _mapResults = MutableLiveData<TrackerMapData>()
    val mapResults: LiveData<TrackerMapData> = _mapResults

    private val _screenState = MutableLiveData<SearchTEScreenState>()
    val screenState: LiveData<SearchTEScreenState> = _screenState

    val createButtonScrollVisibility = MutableLiveData(true)

    val allowCreateWithoutSearch = false // init from configuration
    private var searching: Boolean = false

    init {
        SearchPageConfigurator(searchRepository)
    }

    fun setListScreen() {
        val displayFrontPageList = _selectedProgram.value?.displayFrontPageList() ?: true
        val shouldOpenSearch = !displayFrontPageList &&
            !allowCreateWithoutSearch &&
            !searching
        _screenState.value = when {
            shouldOpenSearch ->
                SearchForm(
                    previousSate = _screenState.value?.screenState ?: SearchScreenState.LIST,
                    queryHasData = queryData.isNotEmpty(),
                    minAttributesToSearch = _selectedProgram.value?.minAttributesRequiredToSearch()
                        ?: 1
                )
            else ->
                SearchList(
                    previousSate = _screenState.value?.screenState ?: SearchScreenState.NONE,
                    listType = SearchScreenState.LIST,
                    displayFrontPageList = _selectedProgram.value?.displayFrontPageList() ?: false,
                    canCreateWithoutSearch = allowCreateWithoutSearch,
                    queryHasData = queryData.isNotEmpty(),
                    minAttributesToSearch = _selectedProgram.value?.minAttributesRequiredToSearch()
                        ?: 0,
                    isSearching = searching
                )
        }
    }

    fun setMapScreen() {
        _screenState.value = SearchMap(
            previousSate = _screenState.value?.screenState ?: SearchScreenState.NONE,
            mapType = SearchScreenState.MAP,
            canCreateWithoutSearch = allowCreateWithoutSearch
        )
    }

    fun setAnalyticsScreen() {
        _screenState.value = SearchAnalytics(
            previousSate = _screenState.value?.screenState ?: SearchScreenState.NONE
        )
    }

    fun setSearchScreen(isLandscapeMode: Boolean) {
        if (isLandscapeMode) {
            setListScreen()
        } else {
            _screenState.value = SearchForm(
                previousSate = _screenState.value?.screenState ?: SearchScreenState.NONE,
                queryHasData = queryData.isNotEmpty(),
                minAttributesToSearch = _selectedProgram.value?.minAttributesRequiredToSearch() ?: 0
            )
        }
    }

    fun setPreviousScreen(isLandscapeMode: Boolean) {
        when (_screenState.value?.previousSate) {
            SearchScreenState.LIST -> setListScreen()
            SearchScreenState.MAP -> setMapScreen()
            SearchScreenState.SEARCHING -> setSearchScreen(isLandscapeMode)
            SearchScreenState.ANALYTICS -> setAnalyticsScreen()
            else -> {
            }
        }
    }

    fun refreshData() {
        onSearchClick()
    }

    fun updateQueryData(rowAction: RowAction) {
        if (rowAction.value != null) {
            queryData[rowAction.id] = rowAction.value!!
        } else {
            queryData.remove(rowAction.id)
        }
        updateSearch()
    }

    fun clearQueryData() {
        queryData.clear()
        updateSearch()
    }

    private fun updateSearch() {
        if (_screenState.value is SearchForm) {
            _screenState.value =
                (_screenState.value as SearchForm).copy(queryHasData = queryData.isNotEmpty())
        }
    }

    fun fetchListResults(): LiveData<PagedList<SearchTeiModel>>? {
        return when {
            searching -> {
                searchRepository.searchTrackedEntities(
                    SearchParametersModel(
                        selectedProgram = _selectedProgram.value,
                        queryData = queryData
                    ),
                    searching && networkUtils.isOnline()
                )
            }
            displayFrontPageList() -> {
                searchRepository.searchTrackedEntities(
                    SearchParametersModel(
                        selectedProgram = _selectedProgram.value,
                        queryData = queryData
                    ),
                    false
                )
            }
            else -> {
                null
            }
        }
    }

    fun fetchGlobalResults(): LiveData<PagedList<SearchTeiModel>>? {
        return if (searching) {
            searchRepository.searchTrackedEntities(
                SearchParametersModel(
                    selectedProgram = null,
                    queryData = queryData
                ),
                searching && networkUtils.isOnline()
            )
        } else {
            null
        }
    }

    fun fetchMapResults() {
        viewModelScope.launch {
            val result = async(context = Dispatchers.IO) {
                val teis = searchRepository.searchTeiForMap(
                    SearchParametersModel(
                        _selectedProgram.value,
                        _selectedProgram.value?.trackedEntityType()?.uid(),
                        queryData
                    ),
                    true
                ).blockingFirst()
                val events = searchRepository.getEventsForMap(teis)
                val dataElements = mapCoordinateFieldToFeatureCollection.map(
                    mapUtils.getCoordinateDataElementInfo(events.uids())
                )
                val attributes = mapCoordinateFieldToFeatureCollection.map(
                    mapUtils.getCoordinateAttributeInfo(teis.uids())
                )
                val coordinateFields = mutableMapOf<String, FeatureCollection>().apply {
                    putAll(dataElements)
                    putAll(attributes)
                }
                val eventsUi = eventToEventUiComponent.mapList(events, teis)
                val teiFeatureCollection =
                    mapTeisToFeatureCollection.map(teis, _selectedProgram.value != null)
                val eventsByProgramStage =
                    mapTeiEventsToFeatureCollection.map(eventsUi).component1()
                TrackerMapData(
                    teiModels = teis,
                    eventFeatures = eventsByProgramStage,
                    teiFeatures = teiFeatureCollection.first,
                    teiBoundingBox = teiFeatureCollection.second,
                    eventModels = eventsUi.toMutableList(),
                    dataElementFeaturess = coordinateFields
                )
            }
            try {
                _mapResults.value = result.await()
            } catch (e: Exception) {
            }
            searching = false
        }
    }

    fun onSearchClick(onMinAttributes: (Int) -> Unit = {}) {
        viewModelScope.launch {
            if (canPerformSearch()) {
                searching = queryData.isNotEmpty()
                val currentScreenState = if (_screenState.value is SearchForm) {
                    _screenState.value?.previousSate
                } else {
                    _screenState.value?.screenState
                }

                when (currentScreenState) {
                    SearchScreenState.LIST -> {
                        setListScreen()
                        _refreshData.value = Unit
                    }
                    SearchScreenState.MAP -> {
                        setMapScreen()
                        fetchMapResults()
                    }
                    else -> searching = false
                }
            } else {
                onMinAttributes(_selectedProgram.value?.minAttributesRequiredToSearch() ?: 0)
            }
        }
    }

    private fun canPerformSearch(): Boolean {
        return minAttributesToSearchCheck() || displayFrontPageList()
    }

    private fun minAttributesToSearchCheck(): Boolean {
        return _selectedProgram.value?.let { program ->
            program.minAttributesRequiredToSearch() ?: 0 <= queryData.size
        } ?: true
    }

    private fun displayFrontPageList(): Boolean {
        return _selectedProgram.value?.let { program ->
            program.displayFrontPageList() == true && queryData.isEmpty()
        } ?: true
    }

    fun canDisplayResult(itemCount: Int): Boolean {
        return _selectedProgram.value?.maxTeiCountToReturn()?.let { maxTeiCount ->
            itemCount <= maxTeiCount
        } ?: true
    }

    fun queryDataByProgram(programUid: String?): MutableMap<String, String> {
        return searchRepository.filterQueryForProgram(queryData, programUid)
    }

    fun onEnrollClick() {
        presenter.onEnrollClick()
    }

    fun onAddRelationship(teiUid: String, relationshipTypeUid: String?, online: Boolean) {
        presenter.addRelationship(teiUid, relationshipTypeUid, online)
    }

    fun onSyncIconClick(teiUid: String) {
        presenter.onSyncIconClick(teiUid)
    }

    fun onDownloadTei(teiUid: String, enrollmentUid: String?) {
        presenter.downloadTei(teiUid, enrollmentUid)
    }

    fun onTeiClick(teiUid: String, enrollmentUid: String?, online: Boolean) {
        presenter.onTEIClick(teiUid, enrollmentUid, online)
    }
}

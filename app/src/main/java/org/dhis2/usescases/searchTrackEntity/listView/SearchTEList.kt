package org.dhis2.usescases.searchTrackEntity.listView

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.activityViewModels
import androidx.paging.PagedList
import androidx.recyclerview.widget.ConcatAdapter
import com.google.android.material.composethemeadapter.MdcTheme
import java.io.File
import javax.inject.Inject
import org.dhis2.usescases.general.FragmentGlobalAbstract
import org.dhis2.usescases.searchTrackEntity.SearchList
import org.dhis2.usescases.searchTrackEntity.SearchTEActivity
import org.dhis2.usescases.searchTrackEntity.SearchTEIViewModel
import org.dhis2.usescases.searchTrackEntity.SearchTeiViewModelFactory
import org.dhis2.usescases.searchTrackEntity.adapters.SearchTeiLiveAdapter
import org.dhis2.usescases.searchTrackEntity.ui.SearchTEListScreen
import org.dhis2.utils.customviews.ImageDetailBottomDialog
import org.dhis2.utils.isLandscape

const val ARG_FROM_RELATIONSHIP = "ARG_FROM_RELATIONSHIP"

class SearchTEList : FragmentGlobalAbstract() {

    @Inject
    lateinit var viewModelFactory: SearchTeiViewModelFactory

    private val viewModel by activityViewModels<SearchTEIViewModel> { viewModelFactory }

    private val initialLoadingAdapter by lazy {
        SearchListResultAdapter { }
    }

    private val liveAdapter by lazy {
        SearchTeiLiveAdapter(
            fromRelationship,
            onAddRelationship = viewModel::onAddRelationship,
            onSyncIconClick = viewModel::onSyncIconClick,
            onDownloadTei = viewModel::onDownloadTei,
            onTeiClick = viewModel::onTeiClick,
            onImageClick = ::displayImageDetail
        )
    }

    private val globalAdapter by lazy {
        SearchTeiLiveAdapter(
            fromRelationship,
            onAddRelationship = viewModel::onAddRelationship,
            onSyncIconClick = viewModel::onSyncIconClick,
            onDownloadTei = viewModel::onDownloadTei,
            onTeiClick = viewModel::onTeiClick,
            onImageClick = ::displayImageDetail
        )
    }

    private val resultAdapter by lazy {
        SearchListResultAdapter {
            initGlobalData()
        }
    }

    private val listAdapter by lazy {
        ConcatAdapter(initialLoadingAdapter, liveAdapter, globalAdapter, resultAdapter)
    }

    private val fromRelationship by lazy {
        arguments?.getBoolean(ARG_FROM_RELATIONSHIP) ?: false
    }

    companion object {
        fun get(fromRelationships: Boolean): SearchTEList {
            return SearchTEList().apply {
                arguments = bundleArguments(fromRelationships)
            }
        }
    }

    private fun bundleArguments(fromRelationships: Boolean): Bundle {
        return Bundle().apply {
            putBoolean(ARG_FROM_RELATIONSHIP, fromRelationships)
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        (context as SearchTEActivity).searchComponent.plus(
            SearchTEListModule()
        ).inject(this)
    }

    @ExperimentalAnimationApi
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(
                ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed
            )
            setContent {
                MdcTheme {
                    SearchTEListScreen(
                        viewModel = viewModel,
                        listAdapter = listAdapter
                    )
                }
            }
        }.also {
            observeNewData()
        }
    }

    private fun displayImageDetail(imagePath: String) {
        ImageDetailBottomDialog(null, File(imagePath))
            .show(childFragmentManager, ImageDetailBottomDialog.TAG)
    }

    private fun observeNewData() {
        viewModel.refreshData.observe(viewLifecycleOwner) {
            restoreAdapters()
            initData()
        }
    }

    private fun restoreAdapters() {
        initialLoadingAdapter.submitList(null)
        liveAdapter.clearList()
        globalAdapter.clearList()
        resultAdapter.submitList(null)
    }

    private val initResultCallback = object : PagedList.Callback() {
        override fun onChanged(position: Int, count: Int) {
        }

        override fun onInserted(position: Int, count: Int) {
            onInitDataLoaded()
        }

        override fun onRemoved(position: Int, count: Int) {
        }
    }

    private val globalResultCallback = object : PagedList.Callback() {
        override fun onChanged(position: Int, count: Int) {
        }

        override fun onInserted(position: Int, count: Int) {
            onGlobalDataLoaded()
        }

        override fun onRemoved(position: Int, count: Int) {
        }
    }

    private fun initData() {
        displayLoadingData()
        viewModel.fetchListResults()?.let {
            it.removeObservers(viewLifecycleOwner)
            it.observe(viewLifecycleOwner) { results ->
                liveAdapter.submitList(results) {
                    onInitDataLoaded()
                }
                results.addWeakCallback(results.snapshot(), initResultCallback)
            }
        }
    }

    private fun onInitDataLoaded() {
        onDataLoaded(
            canDisplayResults = viewModel.canDisplayResult(liveAdapter.itemCount),
            hasProgramResults = liveAdapter.itemCount > 0
        )
    }

    private fun onGlobalDataLoaded() {
        onDataLoaded(
            canDisplayResults = viewModel.canDisplayResult(liveAdapter.itemCount),
            hasProgramResults = liveAdapter.itemCount > 0,
            hasGlobalResults = globalAdapter.itemCount > 0
        )
    }

    private fun initGlobalData() {
        displayLoadingData()
        viewModel.fetchGlobalResults()?.let {
            it.removeObservers(viewLifecycleOwner)
            it.observe(viewLifecycleOwner) { results ->
                globalAdapter.submitList(results) {
                    onGlobalDataLoaded()
                }
                results.addWeakCallback(results.snapshot(), globalResultCallback)
            }
        }
    }

    private fun displayLoadingData() {
        if (listAdapter.itemCount == 0) {
            initialLoadingAdapter.submitList(
                listOf(SearchResult(SearchResult.SearchResultType.LOADING))
            )
        } else {
            resultAdapter.submitList(
                listOf(SearchResult(SearchResult.SearchResultType.LOADING))
            )
        }
    }

    private fun onDataLoaded(
        canDisplayResults: Boolean = true,
        hasProgramResults: Boolean,
        hasGlobalResults: Boolean? = null
    ) {
        initialLoadingAdapter.submitList(emptyList())

        val isSearching = viewModel.screenState.value.takeIf { it is SearchList }?.let {
            (it as SearchList).isSearching
        } ?: false

        if (isSearching) {
            handleSearchResult(
                canDisplayResults,
                hasProgramResults,
                hasGlobalResults
            )
        } else {
            handleDisplayInListResult(hasProgramResults)
        }
    }

    private fun handleDisplayInListResult(hasProgramResults: Boolean) {
        val result = when {
            hasProgramResults ->
                listOf(SearchResult(SearchResult.SearchResultType.NO_MORE_RESULTS))
            !hasProgramResults && viewModel.allowCreateWithoutSearch ->
                listOf(SearchResult(SearchResult.SearchResultType.SEARCH_OR_CREATE))
            else -> listOf()
        }

        if (result.isEmpty()) {
            viewModel.setSearchScreen(isLandscape())
        }

        resultAdapter.submitList(result)
    }

    private fun handleSearchResult(
        canDisplayResults: Boolean,
        hasProgramResults: Boolean,
        hasGlobalResults: Boolean?
    ) {
        val result = when {
            !canDisplayResults -> {
                liveAdapter.clearList()
                globalAdapter.clearList()
                listOf(SearchResult(SearchResult.SearchResultType.TOO_MANY_RESULTS))
            }
            hasGlobalResults == null -> {
                globalAdapter.clearList()
                listOf(SearchResult(SearchResult.SearchResultType.SEARCH_OUTSIDE))
            }
            hasProgramResults || hasGlobalResults ->
                listOf(SearchResult(SearchResult.SearchResultType.NO_MORE_RESULTS))
            else ->
                listOf(SearchResult(SearchResult.SearchResultType.NO_RESULTS))
        }
        resultAdapter.submitList(result)
    }
}

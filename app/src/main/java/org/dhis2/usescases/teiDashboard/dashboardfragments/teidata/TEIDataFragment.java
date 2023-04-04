package org.dhis2.usescases.teiDashboard.dashboardfragments.teidata;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.databinding.ObservableBoolean;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.DividerItemDecoration;

import org.dhis2.App;
import org.dhis2.Bindings.ExtensionsKt;
import org.dhis2.Bindings.ViewExtensionsKt;
import org.dhis2.R;
import org.dhis2.commons.animations.ViewAnimationsKt;
import org.dhis2.commons.data.SearchTeiModel;
import org.dhis2.commons.dialogs.imagedetail.ImageDetailBottomDialog;
import org.dhis2.commons.resources.ObjectStyleUtils;
import org.dhis2.commons.sync.ConflictType;
import org.dhis2.commons.data.EventViewModel;
import org.dhis2.commons.data.StageSection;
import org.dhis2.commons.dialogs.CustomDialog;
import org.dhis2.commons.dialogs.DialogClickListener;
import org.dhis2.databinding.FragmentTeiDataBinding;
import org.dhis2.usescases.eventsWithoutRegistration.eventInitial.EventInitialActivity;
import org.dhis2.usescases.general.FragmentGlobalAbstract;
import org.dhis2.commons.orgunitselector.OUTreeFragment;
import org.dhis2.commons.orgunitselector.OnOrgUnitSelectionFinished;
import org.dhis2.usescases.programStageSelection.ProgramStageSelectionActivity;
import org.dhis2.usescases.teiDashboard.DashboardProgramModel;
import org.dhis2.usescases.teiDashboard.DashboardViewModel;
import org.dhis2.usescases.teiDashboard.TeiDashboardMobileActivity;
import org.dhis2.usescases.teiDashboard.dashboardfragments.teidata.teievents.EventAdapter;
import org.dhis2.commons.data.EventViewModelType;
import org.dhis2.commons.Constants;
import org.dhis2.usescases.teiDashboard.ui.DetailsButtonKt;
import org.dhis2.usescases.teiDashboard.ui.FollowupButtonKt;
import org.dhis2.usescases.teiDashboard.ui.LockButtonKt;
import org.dhis2.utils.CustomComparator;
import org.dhis2.utils.DateUtils;
import org.dhis2.commons.data.EventCreationType;
import org.dhis2.utils.OrientationUtilsKt;
import org.dhis2.utils.category.CategoryDialog;
import org.dhis2.utils.dialFloatingActionButton.DialItem;
import org.dhis2.commons.filters.FilterItem;
import org.dhis2.commons.filters.FilterManager;
import org.dhis2.commons.filters.FiltersAdapter;
import org.dhis2.utils.granularsync.SyncStatusDialog;
import org.hisp.dhis.android.core.enrollment.Enrollment;
import org.hisp.dhis.android.core.enrollment.EnrollmentStatus;
import org.hisp.dhis.android.core.event.Event;
import org.hisp.dhis.android.core.organisationunit.OrganisationUnit;
import org.hisp.dhis.android.core.program.Program;
import org.hisp.dhis.android.core.program.ProgramStage;
import org.hisp.dhis.android.core.program.ProgramTrackedEntityAttribute;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityAttributeValue;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityInstance;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.inject.Inject;

import io.reactivex.Flowable;
import io.reactivex.Single;
import io.reactivex.functions.Consumer;
import kotlin.Unit;
import timber.log.Timber;

import static android.app.Activity.RESULT_OK;
import static com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade;
import static org.dhis2.commons.Constants.ENROLLMENT_UID;
import static org.dhis2.commons.Constants.EVENT_CREATION_TYPE;
import static org.dhis2.commons.Constants.EVENT_PERIOD_TYPE;
import static org.dhis2.commons.Constants.EVENT_REPEATABLE;
import static org.dhis2.commons.Constants.EVENT_SCHEDULE_INTERVAL;
import static org.dhis2.commons.Constants.ORG_UNIT;
import static org.dhis2.commons.Constants.PROGRAM_UID;
import static org.dhis2.commons.Constants.TRACKED_ENTITY_INSTANCE;
import static org.dhis2.utils.analytics.AnalyticsConstants.CREATE_EVENT_TEI;
import static org.dhis2.utils.analytics.AnalyticsConstants.TYPE_EVENT_TEI;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;

public class TEIDataFragment extends FragmentGlobalAbstract implements TEIDataContracts.View, OnOrgUnitSelectionFinished {

    private static final int REQ_DETAILS = 1001;
    private static final int REQ_EVENT = 2001;

    private static final int RC_GENERATE_EVENT = 1501;
    private static final int RC_EVENTS_COMPLETED = 1601;

    private static final int REFERAL_ID = 3;
    private static final int ADD_NEW_ID = 2;
    private static final int SCHEDULE_ID = 1;

    private static final String PREF_COMPLETED_EVENT = "COMPLETED_EVENT";

    private FragmentTeiDataBinding binding;

    @Inject
    TEIDataContracts.Presenter presenter;

    @Inject
    FilterManager filterManager;

    @Inject
    FiltersAdapter filtersAdapter;


    private EventAdapter adapter;
    private CustomDialog dialog;
    private ProgramStage programStageFromEvent;
    private final ObservableBoolean followUp = new ObservableBoolean(false);

    private boolean hasCatComb;
    private final ArrayList<Event> catComboShowed = new ArrayList<>();
    private Context context;
    private DashboardViewModel dashboardViewModel;
    private DashboardProgramModel dashboardModel;
    private TeiDashboardMobileActivity activity;
    private PopupMenu popupMenu;
    public SearchTeiModel teiModel;
    List<ProgramTrackedEntityAttribute> programTrackedEntityAttributes;
    List<TrackedEntityAttributeValue> attributeValues;

    public static TEIDataFragment newInstance(String programUid, String teiUid, String enrollmentUid) {
        TEIDataFragment fragment = new TEIDataFragment();
        Bundle args = new Bundle();
        args.putString("PROGRAM_UID", programUid);
        args.putString("TEI_UID", teiUid);
        args.putString("ENROLLMENT_UID", enrollmentUid);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(@NotNull Context context) {
        super.onAttach(context);
        this.context = context;
        activity = (TeiDashboardMobileActivity) context;
        ((App) context.getApplicationContext())
                .dashboardComponent()
                .plus(new TEIDataModule(this,
                        getArguments().getString("PROGRAM_UID"),
                        getArguments().getString("TEI_UID"),
                        getArguments().getString("ENROLLMENT_UID")
                ))
                .inject(this);
    }

    @Override
    public void onStart() {
        super.onStart();
        dashboardViewModel = ViewModelProviders.of(activity).get(DashboardViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        if (this.teiModel == null) {
            this.teiModel = new SearchTeiModel();
        }
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_tei_data, container, false);
        binding.setPresenter(presenter);
        activity.observeGrouping().observe(getViewLifecycleOwner(), group -> {
            showLoadingProgress(true);
            binding.setIsGrouping(group);
            presenter.onGroupingChanged(group);
        });
        activity.observeFilters().observe(getViewLifecycleOwner(), this::showHideFilters);
        activity.updatedEnrollment().observe(getViewLifecycleOwner(), this::updateEnrollment);

        try {
            binding.filterLayout.setAdapter(filtersAdapter);
        } catch (Exception e) {
            Timber.e(e);
        }

        System.out.println("on create view");
        System.out.println(this.teiModel.getAttributeValues());
        System.out.println(this.teiModel.getSelectedEnrollment());

//        binding.cardFront.setFollowUp(false);
        if (OrientationUtilsKt.isLandscape()) {

            binding.cardFrontLand.setAttributeListOpened(false);
            binding.cardFrontLand.showAttributesButton.setOnClickListener((event) -> {

                if (binding.cardFrontLand.getAttributeListOpened()) {

//                    showAttributeList();
//                    binding.cardFrontLand.setAttributeListOpened(true);
                } else {
//                    hideAttributeList();
//                    binding.cardFrontLand.setAttributeListOpened(true);

                }

            });

            ViewExtensionsKt.clipWithAllRoundedCorners(binding.sectionSelectedMark, ExtensionsKt.getDp(2));
//            binding.cardLayout.getBackground().setAlpha(11);

        }

        return binding.getRoot();
    }

    private void updateEnrollment(String enrollmentUid) {
        presenter.getEnrollment(enrollmentUid);
    }

    private void updateFabItems() {
        List<DialItem> dialItems = new ArrayList<>();
        dialItems.add(
                new DialItem(REFERAL_ID, getString(R.string.referral), R.drawable.ic_arrow_forward)
        );
        dialItems.add(
                new DialItem(ADD_NEW_ID, getString(R.string.add_new), R.drawable.ic_note_add)
        );
        dialItems.add(
                new DialItem(SCHEDULE_ID, getString(R.string.schedule_new), R.drawable.ic_date_range)
        );
        binding.dialFabLayout.addDialItems(dialItems, clickedId -> {
            switch (clickedId) {
                case REFERAL_ID:
                    createEvent(EventCreationType.REFERAL, 0);
                    break;
                case ADD_NEW_ID:
                    createEvent(EventCreationType.ADDNEW, 0);
                    break;
                case SCHEDULE_ID:
                    createEvent(EventCreationType.SCHEDULE, 0);
                    break;
                default:
                    break;
            }
            return Unit.INSTANCE;
        });
    }

    @Override
    public void setEnrollment(Enrollment enrollment) {
        binding.setEnrollment(enrollment);
        dashboardViewModel.updateDashboard(dashboardModel);
        if (adapter != null) {
            adapter.setEnrollment(enrollment);
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        System.out.println("TEI DATA : onResume()");
        System.out.println(dashboardModel);
        System.out.println(dashboardViewModel);
        System.out.println(dashboardViewModel.dashboardModel());
        System.out.println(dashboardViewModel.eventUid());

        presenter.init();
        dashboardViewModel.dashboardModel().observe(this, this::setData);
        dashboardViewModel.eventUid().observe(this, this::displayGenerateEvent);
    }

    @Override
    public void onPause() {
        presenter.setOpeningFilterToNone();
        presenter.onDettach();
        super.onPause();
    }

    @Override
    public void setEnrollmentData(Program program, Enrollment enrollment) {

        System.out.println("on set enr data");
        System.out.println(enrollment);
        System.out.println(program);

        if (adapter != null) {
            adapter.setEnrollment(enrollment);
        }
        binding.setProgram(program);
        binding.setEnrollment(enrollment);
        if (enrollment != null) {
            followUp.set(enrollment.followUp() != null ? enrollment.followUp() : false);
        }
        binding.setFollowup(followUp);


        if (this.teiModel == null) {
            this.teiModel = new SearchTeiModel();
        }
        this.teiModel.setCurrentEnrollment(enrollment);

        System.out.println("on set enr data 22222222222222222222222222222");
        System.out.println(this.teiModel.getAttributeValues());
        System.out.println(this.teiModel.getSelectedEnrollment());
    }

    TrackedEntityAttributeValue getAttributeValue(String attributeUid) {
        List<TrackedEntityAttributeValue> filteredValue = this.attributeValues.stream().filter(value -> {
            System.out.println(value.trackedEntityAttribute().toString() + "===================" + attributeUid.toString());
            return value.trackedEntityAttribute().equals(attributeUid);
        }).collect(Collectors.toList());

        System.out.println("attribute getting");
        System.out.println(this.attributeValues.size());
        System.out.println(filteredValue);

        return filteredValue.size() > 0 ? filteredValue.get(0) : null;

    }


    public void setAttributesAndValues(List<TrackedEntityAttributeValue> trackedEntityAttributeValues, List<ProgramTrackedEntityAttribute> programTrackedEntityAttributes) {

        System.out.println("hellooooooooooo????");
        System.out.println(trackedEntityAttributeValues);
        System.out.println(programTrackedEntityAttributes);

        LinkedHashMap<String, TrackedEntityAttributeValue> linkedHashMapOfAttrValues = new LinkedHashMap<>();

        int teiAttributesLoopCounter = 0;
        while (teiAttributesLoopCounter < programTrackedEntityAttributes.size()) {

            System.out.println("-------------------------");
            System.out.println(programTrackedEntityAttributes.get(teiAttributesLoopCounter).uid());
            TrackedEntityAttributeValue value = getAttributeValue(programTrackedEntityAttributes.get(teiAttributesLoopCounter).trackedEntityAttribute().uid());

            linkedHashMapOfAttrValues.put(programTrackedEntityAttributes.get(teiAttributesLoopCounter).displayShortName().replace("Mother program ", "").replace("Newborn program ", ""), value);
            teiAttributesLoopCounter++;
        }

        this.teiModel.setAttributeValues(linkedHashMapOfAttrValues);

        System.out.println("kkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkk");
        System.out.println(this.teiModel.getAttributeValues().keySet());
        System.out.println(this.teiModel.getAttributeValues().values().stream().collect(Collectors.toList()));

        if (OrientationUtilsKt.isLandscape()) {
            binding.cardFrontLand.setAttributeNames(this.teiModel.getAttributeValues().keySet());
            binding.cardFrontLand.setAttribute(this.teiModel.getAttributeValues().values().stream().collect(Collectors.toList()));
        }

    }

    @Override
    public void setTrackedEntityInstance(TrackedEntityInstance trackedEntityInstance, OrganisationUnit organisationUnit, List<TrackedEntityAttributeValue> trackedEntityAttributeValues) {

        System.out.println("on set tei data 1111111111111111111111111");
        System.out.println(trackedEntityInstance);
        System.out.println(organisationUnit);

        if (OrientationUtilsKt.isLandscape()) {
            binding.cardFrontLand.setOrgUnit(organisationUnit.name());
            this.attributeValues = trackedEntityAttributeValues;

            if (this.programTrackedEntityAttributes != null) {

                setAttributesAndValues(this.attributeValues, this.programTrackedEntityAttributes);

            }
//            binding.cardFrontLand.setAttribute(trackedEntityAttributeValues);

//            Set<String> attributeNames = new HashSet<>();
//
//            for (TrackedEntityAttributeValue attributeValue : trackedEntityAttributeValues) {
//                attributeNames.add(attributeValue.trackedEntityAttribute());
//
//            }
//
//            System.out.println("namesssssssssssssss");
//            System.out.println(attributeNames);
//
//            binding.cardFrontLand.setAttributeNames(attributeNames);

//            binding.cardFrontLand.set
        }

        binding.setTrackEntity(trackedEntityInstance);
//        binding.cardFront.orgUnit.setText(organisationUnit.displayName());

        if (this.teiModel == null) {
            this.teiModel = new SearchTeiModel();
        }

        this.teiModel.setTei(trackedEntityInstance);
        this.teiModel.setEnrolledOrgUnit(organisationUnit.displayName());

        if (teiModel.getSelectedEnrollment() != null) {
            System.out.println("on set tei 333333333333333333333333333333");
            System.out.println(this.teiModel.getAttributeValues());
            System.out.println(this.teiModel.getSelectedEnrollment());
        }

        System.out.println("on set tei data");
        System.out.println(this.teiModel.getAttributeValues());
        System.out.println(this.teiModel.getSelectedEnrollment());


    }

    @Override
    public void setAttributeValues(List<TrackedEntityAttributeValue> attributeValues) {


    }

    public void setData(DashboardProgramModel nprogram) {
        this.dashboardModel = nprogram;

        if (nprogram != null && nprogram.getCurrentEnrollment() != null) {
            binding.dialFabLayout.setFabVisible(true);
            presenter.setDashboardProgram(this.dashboardModel);
            SharedPreferences prefs = context.getSharedPreferences(Constants.SHARE_PREFS, Context.MODE_PRIVATE);
            hasCatComb = nprogram.getCurrentProgram() != null && !nprogram.getCurrentProgram().categoryComboUid().equals(prefs.getString(Constants.DEFAULT_CAT_COMBO, ""));
            binding.setDashboardModel(nprogram);
            updateFabItems();
        } else if (nprogram != null) {
            binding.dialFabLayout.setFabVisible(false);
            binding.teiRecycler.setAdapter(new DashboardProgramAdapter(presenter, nprogram));
            binding.teiRecycler.addItemDecoration(new DividerItemDecoration(getAbstracContext(), DividerItemDecoration.VERTICAL));
            binding.setDashboardModel(nprogram);
            showLoadingProgress(false);
        }

        System.out.println("TrackedEntityInstance:::" + binding.getTrackEntity()   );

        if(OrientationUtilsKt.isPortrait()) {

            DetailsButtonKt.setButtonContent(
                    binding.cardFront.detailsButton,
                    activity.presenter.getTEType(),
                    () -> {
                        presenter.seeDetails(binding.cardFront.cardData, dashboardModel);
                        return Unit.INSTANCE;
                    }
            );

            FollowupButtonKt.setFollowupButtonContent(binding.cardFront.followupButton ,activity.presenter.getTEType(), followUp.get(), () -> {
                presenter.onFollowUp(dashboardModel);
                return Unit.INSTANCE;
            });

            LockButtonKt.setLockButtonContent(binding.cardFront.lockButton, activity.presenter.getTEType(), () -> {
//                presenter.onFollowUp(dashboardModel);
                return Unit.INSTANCE;
            });

        }else{

            System.out.println("******************************************************************");
            System.out.println(binding.getProgram());

            DetailsButtonKt.setButtonContent(
                    binding.cardFrontLand.detailsButton,
                    activity.presenter.getTEType(),
                    () -> {
                        presenter.seeDetails(binding.cardFrontLand.detailsButton, dashboardModel);
                        return Unit.INSTANCE;
                    }
            );


            FollowupButtonKt.setFollowupButtonContent(binding.cardFrontLand.followupButton, activity.presenter.getTEType(), followUp.get(),() -> {
                presenter.onFollowUp(dashboardModel);
                presenter.init();
                return Unit.INSTANCE;
            });

            LockButtonKt.setLockButtonContent(binding.cardFrontLand.lockButton, activity.presenter.getTEType(), () -> {
                presenter.onFollowUp(dashboardModel);
                return Unit.INSTANCE;
            });

        }

        binding.executePendingBindings();

        if (getSharedPreferences().getString(PREF_COMPLETED_EVENT, null) != null) {
            presenter.displayGenerateEvent(getSharedPreferences().getString(PREF_COMPLETED_EVENT, null));
            getSharedPreferences().edit().remove(PREF_COMPLETED_EVENT).apply();
        }


    }

    @SuppressLint("CheckResult")
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == REQ_DETAILS) {
                activity.getPresenter().init();
            }
        }
    }

    @Override
    public void setFilters(List<FilterItem> filterItems) {
        filtersAdapter.submitList(filterItems);
    }

    @Override
    public void hideFilters() {
        activity.hideFilter();
    }

    @Override
    public Flowable<StageSection> observeStageSelection(Program currentProgram, Enrollment currentEnrollment) {

        System.out.println("observing stage selector :: ");
        System.out.println("xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx");

        if (adapter == null) {
            adapter = new EventAdapter(presenter, currentProgram, "", "");
            adapter.setEnrollment(currentEnrollment);
            binding.teiRecycler.setAdapter(adapter);
        }
        return adapter.stageSelector();
    }

    @Override
    public void setEvents(List<EventViewModel> events, boolean canAddEvents) {

        System.out.println("setting events ::: ");

        binding.setCanAddEvents(canAddEvents);

        if (events.isEmpty()) {

            System.out.println("empty events");
            binding.emptyTeis.setVisibility(View.VISIBLE);
            if (binding.dialFabLayout.isFabVisible()) {
                binding.emptyTeis.setText(R.string.empty_tei_add);
            } else {
                binding.emptyTeis.setText(R.string.empty_tei_no_add);
            }
        } else {

            System.out.println("there is events");
            System.out.println(events.size());
            System.out.println("vvvvvvvvvvvvvvvvvvvvvvvv");

            binding.emptyTeis.setVisibility(View.GONE);
            adapter.submitList(events);

            List<Event> currentSectionEvents = events.stream()
                    .filter(eventViewModel -> eventViewModel.getType() == EventViewModelType.EVENT)
                    .map(EventViewModel::getEvent)
                    .collect(Collectors.toList());

            // TODO: work on showing event form on tei activity page
            /*
            if (currentSectionEvents.size() > 0 && OrientationUtilsKt.isLandscape()) {

                System.out.println("i do get in here ");

                ((TeiDashboardMobileActivity) getActivity()).openEventForm(currentSectionEvents.get(0).uid());

            } else { */

            System.out.println("i do not get in here ");

            for (EventViewModel eventViewModel : events) {
                if (eventViewModel.getType() == EventViewModelType.EVENT) {
                    Event event = eventViewModel.getEvent();

                    System.out.println(event.eventDate());
                    System.out.println(event.completedDate());
                    System.out.println("ddddddddddddddddddddddddddd");

                    if (event.eventDate() != null) {
                        if (event.eventDate().after(DateUtils.getInstance().getToday()))
                            binding.teiRecycler.scrollToPosition(events.indexOf(event));
                    }
                    if (hasCatComb && event.attributeOptionCombo() == null && !catComboShowed.contains(event)) {
                        presenter.getCatComboOptions(event);
                        catComboShowed.add(event);
                    } else if (!hasCatComb && event.attributeOptionCombo() == null)
                        presenter.setDefaultCatOptCombToEvent(event.uid());
                }
            }
//            }
        }
        showLoadingProgress(false);
    }

    private void showLoadingProgress(boolean showProgress) {
        if (showProgress) {
            binding.loadingProgress.getRoot().setVisibility(View.VISIBLE);
        } else {
            binding.loadingProgress.getRoot().setVisibility(View.GONE);
        }
    }

    @Override
    public Consumer<ProgramStage> displayGenerateEvent() {
        return programStageModel -> {
            this.programStageFromEvent = programStageModel;
            if (programStageModel.displayGenerateEventBox() || programStageModel.allowGenerateNextVisit()) {
                dialog = new CustomDialog(
                        getContext(),
                        getString(R.string.dialog_generate_new_event),
                        getString(R.string.message_generate_new_event),
                        getString(R.string.button_ok),
                        getString(R.string.cancel),
                        RC_GENERATE_EVENT,
                        new DialogClickListener() {
                            @Override
                            public void onPositive() {
                                createEvent(EventCreationType.SCHEDULE, programStageFromEvent.standardInterval() != null ? programStageFromEvent.standardInterval() : 0);
                            }

                            @Override
                            public void onNegative() {
                                if (programStageFromEvent.remindCompleted())
                                    presenter.areEventsCompleted();
                            }
                        });
                dialog.show();
            } else if (programStageModel.remindCompleted())
                showDialogCloseProgram();
        };
    }

    private void showDialogCloseProgram() {

        dialog = new CustomDialog(
                getContext(),
                getString(R.string.event_completed),
                getString(R.string.complete_enrollment_message),
                getString(R.string.button_ok),
                getString(R.string.cancel),
                RC_EVENTS_COMPLETED,
                new DialogClickListener() {
                    @Override
                    public void onPositive() {
                        presenter.completeEnrollment();
                    }

                    @Override
                    public void onNegative() {
                    }
                });
        dialog.show();
    }

    @Override
    public Consumer<Single<Boolean>> areEventsCompleted() {
        return eventsCompleted -> {
            if (eventsCompleted.blockingGet()) {
                dialog = new CustomDialog(
                        getContext(),
                        getString(R.string.event_completed_title),
                        getString(R.string.event_completed_message),
                        getString(R.string.button_ok),
                        getString(R.string.cancel),
                        RC_EVENTS_COMPLETED,
                        new DialogClickListener() {
                            @Override
                            public void onPositive() {
                                presenter.completeEnrollment();
                            }

                            @Override
                            public void onNegative() {
                            }
                        });
                dialog.show();
            }

        };
    }

    @Override
    public Consumer<EnrollmentStatus> enrollmentCompleted() {
        return enrollmentStatus -> {
            if (enrollmentStatus == EnrollmentStatus.COMPLETED)
                activity.updateStatus();
        };
    }

    private void createEvent(EventCreationType eventCreationType, Integer scheduleIntervalDays) {
        if (isAdded()) {
            analyticsHelper().setEvent(TYPE_EVENT_TEI, eventCreationType.name(), CREATE_EVENT_TEI);
            Bundle bundle = new Bundle();
            bundle.putString(PROGRAM_UID, dashboardModel.getCurrentEnrollment().program());
            bundle.putString(TRACKED_ENTITY_INSTANCE, dashboardModel.getTei().uid());
            if (presenter.enrollmentOrgUnitInCaptureScope(dashboardModel.getCurrentOrgUnit().uid())) {
                bundle.putString(ORG_UNIT, dashboardModel.getCurrentOrgUnit().uid());
            }
            bundle.putString(ENROLLMENT_UID, dashboardModel.getCurrentEnrollment().uid());
            bundle.putString(EVENT_CREATION_TYPE, eventCreationType.name());
            bundle.putInt(EVENT_SCHEDULE_INTERVAL, scheduleIntervalDays);
            Intent intent = new Intent(getContext(), ProgramStageSelectionActivity.class);
            intent.putExtras(bundle);
            startActivityForResult(intent, REQ_EVENT);
        }
    }

    @Override
    public void showCatComboDialog(String eventId, Date eventDate, String categoryComboUid) {
        CategoryDialog categoryDialog = new CategoryDialog(
                CategoryDialog.Type.CATEGORY_OPTION_COMBO,
                categoryComboUid,
                true,
                eventDate,
                selectedCatOptComboUid -> {
                    presenter.changeCatOption(eventId, selectedCatOptComboUid);
                    return null;
                }
        );
        categoryDialog.setCancelable(false);
        categoryDialog.show(getChildFragmentManager(), CategoryDialog.Companion.getTAG());
    }

    @Override
    public void switchFollowUp(boolean followUp) {
        this.followUp.set(followUp);
    }

    @Override
    public void displayGenerateEvent(String eventUid) {
        if (eventUid != null) {
            presenter.displayGenerateEvent(eventUid);
            dashboardViewModel.updateEventUid(null);
        }
    }

    @Override
    public void restoreAdapter(String programUid, String teiUid, String enrollmentUid) {
        activity.startActivity(TeiDashboardMobileActivity.intent(activity, teiUid, programUid, enrollmentUid));
        activity.finish();
    }

    @Override
    public void seeDetails(Intent intent, Bundle bundle) {
        this.startActivityForResult(intent, REQ_DETAILS, bundle);
    }

    @Override
    public void openEventDetails(Intent intent, Bundle bundle) {
        this.startActivityForResult(intent, REQ_EVENT, bundle);
    }

    @Override
    public void openEventInitial(Intent intent) {
        this.startActivityForResult(intent, REQ_EVENT, null);
    }

    @Override
    public void openEventCapture(Intent intent) {

        System.out.println("event capture to open");

        this.startActivityForResult(intent, REQ_EVENT, null);
    }

    @Override
    public void showTeiImage(String filePath, String defaultIcon) {
//        if (filePath.isEmpty() && defaultIcon.isEmpty()) {
//            if (OrientationUtilsKt.isPortrait()) {
//                binding.cardFront.teiImage.setVisibility(View.GONE);
//            }
//        } else {
//
//            if (OrientationUtilsKt.isLandscape()) {
//                binding.cardFrontLand.trackedEntityImage.setVisibility(View.VISIBLE);
//                Glide.with(this)
//                        .load(new File(filePath))
//                        .error(
//                                ObjectStyleUtils.getIconResource(context, defaultIcon, R.drawable.photo_temp_gray)
//                        )
//                        .transition(withCrossFade())
//                        .transform(new CircleCrop())
//                        .into(binding.cardFrontLand.trackedEntityImage);
//                binding.cardFrontLand.trackedEntityImage.setOnClickListener(view -> {
//                    File fileToShow = new File(filePath);
//                    if (fileToShow.exists()) {
//                        new ImageDetailBottomDialog(
//                                null,
//                                fileToShow
//                        ).show(getChildFragmentManager(), ImageDetailBottomDialog.TAG);
//                    }
//                });
//            } else {
//                binding.cardFront.teiImage.setVisibility(View.VISIBLE);
//                Glide.with(this)
//                        .load(new File(filePath))
//                        .error(
//                                ObjectStyleUtils.getIconResource(context, defaultIcon, R.drawable.photo_temp_gray)
//                        )
//                        .transition(withCrossFade())
//                        .transform(new CircleCrop())
//                        .into(binding.cardFront.teiImage);
//                binding.cardFront.teiImage.setOnClickListener(view -> {
//                    File fileToShow = new File(filePath);
//                    if (fileToShow.exists()) {
//                        new ImageDetailBottomDialog(
//                                null,
//                                fileToShow
//                        ).show(getChildFragmentManager(), ImageDetailBottomDialog.TAG);
//                    }
//                });
//            }
//        }
    }

    @Override
    public void showNewEventOptions(View anchor, ProgramStage stage) {
        popupMenu = new PopupMenu(context, anchor);
        popupMenu.inflate(R.menu.dashboard_event_creation);

        popupMenu.setOnMenuItemClickListener(item -> {
            switch (item.getItemId()) {
                case R.id.schedulenew:
                    goToEventInitial(EventCreationType.SCHEDULE, stage);
                    break;
                case R.id.addnew:
                    goToEventInitial(EventCreationType.ADDNEW, stage);
                    break;
                case R.id.referral:
                    goToEventInitial(EventCreationType.REFERAL, stage);
                    break;
            }
            return true;
        });
        popupMenu.show();

    }

    @Override
    public void hideDueDate() {
        popupMenu.getMenu().findItem(R.id.schedulenew).setVisible(false);
    }

    private void goToEventInitial(EventCreationType eventCreationType, ProgramStage programStage) {

        // TODO: fetch and softcode thw attribute names

        ArrayList<String> f = new ArrayList<>();
        f.add("Unique ID");
        f.add("Given name");
        f.add("Family name");
        f.add("Date of birth");
        f.add("Age");
        f.add("Mobile number");
        f.add("Woman wants to receive reminders during pregnancy");

//        Set<String> attributeNames = new HashSet<>(listOfAttributeNames);
        Set<String> attributeNames = new HashSet<>(f);

        System.out.println("event initial class");
        System.out.println("enr :: " + dashboardModel.getCurrentEnrollment().uid());
        System.out.println("tei :: " + dashboardModel.getTei().uid());

        Intent intent = new Intent(activity, EventInitialActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString(PROGRAM_UID, dashboardModel.getCurrentProgram().uid());
        bundle.putString(TRACKED_ENTITY_INSTANCE, dashboardModel.getTei().uid());
        if (presenter.enrollmentOrgUnitInCaptureScope(dashboardModel.getCurrentOrgUnit().uid())) {
            bundle.putString(ORG_UNIT, dashboardModel.getCurrentOrgUnit().uid());
        }

        bundle.putStringArrayList("ATTRIBUTE_NAMES", f);
        bundle.putString(ENROLLMENT_UID, dashboardModel.getCurrentEnrollment().uid());
        bundle.putString(EVENT_CREATION_TYPE, eventCreationType.name());
        bundle.putBoolean(EVENT_REPEATABLE, programStage.repeatable());
        bundle.putSerializable(EVENT_PERIOD_TYPE, programStage.periodType());
        bundle.putString(Constants.PROGRAM_STAGE_UID, programStage.uid());
        bundle.putInt(EVENT_SCHEDULE_INTERVAL, programStage.standardInterval() != null ? programStage.standardInterval() : 0);
        intent.putExtras(bundle);
        startActivityForResult(intent, REQ_EVENT);
    }

    private void showHideFilters(boolean showFilters) {
        if (showFilters) {
            ViewAnimationsKt.expand(binding.filterLayout, false, () -> {
                binding.teiData.setVisibility(View.GONE);
                binding.filterLayout.setVisibility(View.VISIBLE);
                return Unit.INSTANCE;
            });

        } else {
            ViewAnimationsKt.collapse(binding.filterLayout, () -> {
                binding.teiData.setVisibility(View.VISIBLE);
                binding.filterLayout.setVisibility(View.GONE);
                return Unit.INSTANCE;
            });


        }
    }

    @Override
    public void showPeriodRequest(FilterManager.PeriodRequest periodRequest) {
        if (periodRequest == FilterManager.PeriodRequest.FROM_TO) {
            DateUtils.getInstance().fromCalendarSelector(
                    activity,
                    FilterManager.getInstance()::addPeriod);
        } else {
            DateUtils.getInstance().showPeriodDialog(
                    activity,
                    FilterManager.getInstance()::addPeriod,
                    true);
        }
    }

    @Override
    public void openOrgUnitTreeSelector(String programUid) {
        OUTreeFragment ouTreeFragment = OUTreeFragment.Companion.newInstance(true, FilterManager.getInstance().getOrgUnitUidsFilters());
        ouTreeFragment.setSelectionCallback(this);
        ouTreeFragment.show(getChildFragmentManager(), "OUTreeFragment");
    }

    @Override
    public void showSyncDialog(String uid) {
        SyncStatusDialog dialog = new SyncStatusDialog.Builder()
                .setConflictType(ConflictType.TEI)
                .setUid(uid)
                .onDismissListener(hasChanged -> {
                    if (hasChanged)
                        FilterManager.getInstance().publishData();

                })
                .build();

        dialog.show(getChildFragmentManager(), uid);
    }

    @Override
    public void setRiskColor(String risk) {

        System.out.println("999999999999999999999999999999999");
        System.out.println(risk);

        if (risk == "High Risk") {
            System.out.println("High");
            binding.setHighRisk(true);
            binding.setLowRisk(false);
        }

        if (risk == "Low Risk") {

            System.out.println("Low");
            binding.setLowRisk(true);
            binding.setHighRisk(false);
        }

    }

    @Override
    public void setProgramAttributes(List<ProgramTrackedEntityAttribute> programTrackedEntityAttributes) {

        System.out.println("attributes size");
        System.out.println(programTrackedEntityAttributes.size());

        this.programTrackedEntityAttributes = programTrackedEntityAttributes.stream()
                .filter(attr -> attr.displayInList())
                .collect(Collectors.toList());

        Collections.sort(this.programTrackedEntityAttributes, new CustomComparator());

        if (OrientationUtilsKt.isLandscape()) {

            System.out.println("do i have attr values");
            if (this.attributeValues != null) {

                setAttributesAndValues(this.attributeValues, this.programTrackedEntityAttributes);

            }

//            Set<String> attributeNames = new HashSet<>();
//
//            for (ProgramTrackedEntityAttribute attributeValue : this.programTrackedEntityAttributes) {
//
//                attributeNames.add(attributeValue.displayShortName().replace("Mother program ","").replace("Newborn program ", ""));
//
//                System.out.println("-----------------------------------");
//                System.out.println(attributeValue.displayShortName());
//
//
//
//            }
//
//            System.out.println("namesssssssssssssss");
//            System.out.println(attributeNames);
//
//            binding.cardFrontLand.setAttributeNames(attributeNames);
        }

    }

    @Override
    public void onSelectionFinished(@NotNull List<? extends OrganisationUnit> selectedOrgUnits) {
        presenter.setOrgUnitFilters((List<OrganisationUnit>) selectedOrgUnits);
    }


    private void showAttributeList() {
        binding.cardFrontLand.attributeBName.setVisibility(View.GONE);
        binding.cardFrontLand.enrolledOrgUnit.setVisibility(View.GONE);
        binding.cardFrontLand.sortingFieldName.setVisibility(View.GONE);
        binding.cardFrontLand.entityAttribute2.setVisibility(View.GONE);
        binding.cardFrontLand.entityOrgUnit.setVisibility(View.GONE);
        binding.cardFrontLand.sortingFieldValue.setVisibility(View.GONE);
        binding.cardFrontLand.attributeList.setVisibility(View.VISIBLE);
    }

    private void hideAttributeList() {
        binding.cardFrontLand.attributeList.setVisibility(View.GONE);
        binding.cardFrontLand.attributeBName.setVisibility(View.VISIBLE);
        binding.cardFrontLand.enrolledOrgUnit.setVisibility(View.VISIBLE);
        binding.cardFrontLand.sortingFieldName.setVisibility(View.VISIBLE);
        binding.cardFrontLand.entityAttribute2.setVisibility(View.VISIBLE);
        binding.cardFrontLand.entityOrgUnit.setVisibility(View.VISIBLE);
        binding.cardFrontLand.sortingFieldValue.setVisibility(View.VISIBLE);
    }
}
package org.dhis2.usescases.teiDashboard.dashboardfragments.teidata;

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

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;
import android.view.Gravity;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.databinding.ObservableBoolean;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.DividerItemDecoration;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;

import org.dhis2.App;
import org.dhis2.R;
import org.dhis2.commons.Constants;
import org.dhis2.commons.animations.ViewAnimationsKt;
import org.dhis2.commons.data.SearchTeiModel;
import org.dhis2.commons.dialogs.imagedetail.ImageDetailBottomDialog;
import org.dhis2.commons.popupmenu.AppMenuHelper;
import org.dhis2.commons.resources.ObjectStyleUtils;
import org.dhis2.commons.sync.ConflictType;
import org.dhis2.commons.data.EventCreationType;
import org.dhis2.commons.data.EventViewModel;
import org.dhis2.commons.data.StageSection;
import org.dhis2.commons.dialogs.CustomDialog;
import org.dhis2.commons.dialogs.DialogClickListener;
import org.dhis2.commons.dialogs.imagedetail.ImageDetailBottomDialog;
import org.dhis2.commons.filters.FilterItem;
import org.dhis2.commons.filters.FilterManager;
import org.dhis2.commons.filters.FiltersAdapter;
import org.dhis2.commons.orgunitselector.OUTreeFragment;
import org.dhis2.commons.resources.ColorUtils;
import org.dhis2.commons.resources.ObjectStyleUtils;
import org.dhis2.commons.sync.SyncContext;
import org.dhis2.databinding.FragmentTeiDataBinding;
import org.dhis2.usescases.eventsWithoutRegistration.eventInitial.EventInitialActivity;
import org.dhis2.usescases.general.FragmentGlobalAbstract;
import org.dhis2.commons.orgunitselector.OUTreeFragment;
import org.dhis2.commons.orgunitselector.OnOrgUnitSelectionFinished;
import org.dhis2.usescases.programStageSelection.ProgramStageSelectionActivity;
import org.dhis2.usescases.teiDashboard.DashboardProgramModel;
import org.dhis2.usescases.teiDashboard.DashboardViewModel;
import org.dhis2.usescases.teiDashboard.TeiDashboardMobileActivity;
import org.dhis2.usescases.teiDashboard.dashboardfragments.teidata.teievents.CategoryDialogInteractions;
import org.dhis2.usescases.teiDashboard.dashboardfragments.teidata.teievents.EventAdapter;
import org.dhis2.commons.data.EventViewModelType;
import org.dhis2.commons.Constants;
import org.dhis2.usescases.teiDashboard.ui.DetailsButtonKt;
import org.dhis2.usescases.teiDashboard.ui.FollowupButtonKt;
import org.dhis2.usescases.teiDashboard.ui.LockButtonKt;
import org.dhis2.utils.CustomComparator;
import org.dhis2.usescases.teiDashboard.dashboardfragments.teidata.teievents.EventCatComboOptionSelector;
import org.dhis2.usescases.teiDashboard.ui.DetailsButtonKt;
import org.dhis2.utils.DateUtils;
import org.dhis2.commons.data.EventCreationType;
import org.dhis2.utils.OrientationUtilsKt;
import org.dhis2.utils.category.CategoryDialog;
import org.dhis2.commons.dialogs.imagedetail.ImageDetailBottomDialog;
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
import kotlin.jvm.functions.Function1;
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
import static org.dhis2.utils.analytics.AnalyticsConstants.CLICK;
import static org.dhis2.utils.analytics.AnalyticsConstants.CREATE_EVENT_TEI;
import static org.dhis2.utils.analytics.AnalyticsConstants.SHOW_HELP;
import static org.dhis2.utils.analytics.AnalyticsConstants.TYPE_EVENT_TEI;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;

public class TEIDataFragment extends FragmentGlobalAbstract implements TEIDataContracts.View {

    private static final int REQ_DETAILS = 1001;
    private static final int REQ_EVENT = 2001;

    private static final int RC_GENERATE_EVENT = 1501;
    private static final int RC_EVENTS_COMPLETED = 1601;

    protected static final int REFERAL_ID = 3;
    protected static final int ADD_NEW_ID = 2;
    protected static final int SCHEDULE_ID = 1;

    private static final String PREF_COMPLETED_EVENT = "COMPLETED_EVENT";

    private FragmentTeiDataBinding binding;

    @Inject
    TEIDataPresenter presenter;

    @Inject
    FilterManager filterManager;

    @Inject
    FiltersAdapter filtersAdapter;

    @Inject
    ColorUtils colorUtils;

    private EventAdapter adapter;
    private CustomDialog dialog;
    private ProgramStage programStageFromEvent;
    private final ObservableBoolean followUp = new ObservableBoolean(false);
    private EventCatComboOptionSelector eventCatComboOptionSelector;

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

            binding.cardFrontLand.entityAttribute1.setGravity(Gravity.END);
            binding.cardFrontLand.entityAttribute2.setGravity(Gravity.END);
            binding.cardFrontLand.entityAttribute3.setGravity(Gravity.END);
            binding.cardFrontLand.entityAttribute4.setGravity(Gravity.END);

            binding.cardFrontLand.setAttributeListOpened(false);
            binding.cardFrontLand.showAttributesButton.setOnClickListener((event) -> {

                System.out.println("I got click on Tei");
//                binding.cardFrontLand.setAttributeListOpened(true);
                if (binding.cardFrontLand.getAttributeListOpened()) {
                    binding.cardFrontLand.showAttributesButton.setImageResource(R.drawable.ic_arrow_up);
                    binding.cardFrontLand.setAttributeListOpened(false);
//                    binding.cardFrontLand.setGravityPosition("start");
                } else {
                    binding.cardFrontLand.showAttributesButton.setImageResource(R.drawable.ic_arrow_down);
                    binding.cardFrontLand.setAttributeListOpened(true);
                    binding.cardFrontLand.entityAttribute1.setGravity(Gravity.END);
                    binding.cardFrontLand.entityAttribute2.setGravity(Gravity.END);
                    binding.cardFrontLand.entityAttribute3.setGravity(Gravity.END);
                    binding.cardFrontLand.entityAttribute4.setGravity(Gravity.END);

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
        List<DialItem> dialItems = presenter.getNewEventOptionsByTimeline();
        binding.dialFabLayout.addDialItems(dialItems, clickedId -> {
            switch (clickedId) {
                case REFERAL_ID -> createEvent(EventCreationType.REFERAL, 0);
                case ADD_NEW_ID -> createEvent(EventCreationType.ADDNEW, 0);
                case SCHEDULE_ID -> createEvent(EventCreationType.SCHEDULE, 0);
                default -> {
                }
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
    }

    TrackedEntityAttributeValue getAttributeValue(String attributeUid) {
        List<TrackedEntityAttributeValue> filteredValue = this.attributeValues.stream().filter(value -> {
            System.out.println(value.trackedEntityAttribute().toString() + "===================" + attributeUid.toString());
            return value.trackedEntityAttribute().equals(attributeUid);
        }).collect(Collectors.toList());

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

        if (OrientationUtilsKt.isLandscape()) {
            binding.cardFrontLand.setAttributeNames(this.teiModel.getAttributeValues().keySet());
            binding.cardFrontLand.setAttribute(this.teiModel.getAttributeValues().values().stream().collect(Collectors.toList()));
        }

    }

    @Override
    public void setTrackedEntityInstance(TrackedEntityInstance trackedEntityInstance, OrganisationUnit organisationUnit, List<TrackedEntityAttributeValue> trackedEntityAttributeValues) {

        if (OrientationUtilsKt.isLandscape()) {
            binding.cardFrontLand.setOrgUnit(organisationUnit.name());
            this.attributeValues = trackedEntityAttributeValues;

            if (this.programTrackedEntityAttributes != null) {

                setAttributesAndValues(this.attributeValues, this.programTrackedEntityAttributes);

            }
        }

        binding.setTrackEntity(trackedEntityInstance);

        if (this.teiModel == null) {
            this.teiModel = new SearchTeiModel();
        }

        this.teiModel.setTei(trackedEntityInstance);
        this.teiModel.setEnrolledOrgUnit(organisationUnit.displayName());

        if (teiModel.getSelectedEnrollment() != null) {

        }
    }

    @Override
    public void setAttributeValues(List<TrackedEntityAttributeValue> attributeValues) {


    }

    public void setData(DashboardProgramModel nprogram) {
        this.dashboardModel = nprogram;

        if (nprogram != null && nprogram.getCurrentEnrollment() != null) {
            binding.dialFabLayout.setFabVisible(true);
            presenter.setDashboardProgram(this.dashboardModel);
            eventCatComboOptionSelector = new EventCatComboOptionSelector(nprogram.getCurrentProgram().categoryComboUid(),
                    getChildFragmentManager(),
                    new CategoryDialogInteractions() {
                        @Override
                        public void showDialog(
                                @NonNull String categoryComboUid,
                                @Nullable Date dateControl,
                                @NonNull FragmentManager fragmentManager,
                                @NonNull Function1<? super String, Unit> onItemSelected) {
                            CategoryDialogInteractions.DefaultImpls.showDialog(this,
                                    categoryComboUid,
                                    dateControl,
                                    fragmentManager,
                                    onItemSelected);
                        }
                    });
            binding.setDashboardModel(nprogram);
            updateFabItems();
        } else if (nprogram != null) {
            binding.dialFabLayout.setFabVisible(false);
            binding.teiRecycler.setAdapter(new DashboardProgramAdapter(presenter, nprogram));
            binding.teiRecycler.addItemDecoration(new DividerItemDecoration(getAbstracContext(), DividerItemDecoration.VERTICAL));
            binding.setDashboardModel(nprogram);
            showLoadingProgress(false);
        }


        if (OrientationUtilsKt.isPortrait()) {

            DetailsButtonKt.setButtonContent(
                    binding.cardFront.detailsButton,
                    activity.presenter.getTEType(),
                    () -> {
                        presenter.seeDetails(binding.cardFront.cardData, dashboardModel);
                        return Unit.INSTANCE;
                    }
            );

            FollowupButtonKt.setFollowupButtonContent(binding.cardFront.followupButton, activity.presenter.getTEType(), followUp.get(), () -> {
                presenter.onFollowUp(dashboardModel);
                return Unit.INSTANCE;
            });

            LockButtonKt.setLockButtonContent(binding.cardFront.lockButton, activity.presenter.getTEType(), () -> {
//                presenter.onFollowUp(dashboardModel);
                return Unit.INSTANCE;
            });

        } else {
            DetailsButtonKt.setButtonContent(
                    binding.cardFrontLand.detailsButton,
                    activity.presenter.getTEType(),
                    () -> {
//                        presenter.seeDetails(binding.cardFrontLand.detailsButton, dashboardModel);
                        return Unit.INSTANCE;
                    }
            );


            FollowupButtonKt.setFollowupButtonContent(binding.cardFrontLand.followupButton, activity.presenter.getTEType(), followUp.get(), () -> {

                presenter.onFollowUp(dashboardModel);
                presenter.init();
                return Unit.INSTANCE;
            });

            LockButtonKt.setLockButtonContent(binding.cardFrontLand.lockButton, activity.presenter.getTEType(), () -> {
//                presenter.onFollowUp(dashboardModel);
                showEnrollmentStatusOptions();
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


    public void showEnrollmentStatusOptions() {

        int menu;

        if (teiModel.getSelectedEnrollment().status() == EnrollmentStatus.ACTIVE) {
            menu = R.menu.tei_detail_options_active;
        } else if (teiModel.getSelectedEnrollment().status() == EnrollmentStatus.COMPLETED) {
            menu = R.menu.tei_detail_options_completed;
        } else {
            menu = R.menu.tei_detail_options_cancelled;
        }

        new AppMenuHelper.Builder()
                .anchor(binding.teiData)
                .menu(activity, menu)
                .onMenuInflated(popupMenu -> {
                            return Unit.INSTANCE;
                        }
                )
                .onMenuItemClicked(itemId -> {
                    switch (itemId) {
                        case R.id.complete:
                            activity.getPresenter().updateEnrollmentStatus(activity.getEnrollmentUid(), EnrollmentStatus.COMPLETED);
                            break;
                        case R.id.deactivate:
                            activity.getPresenter().updateEnrollmentStatus(activity.getEnrollmentUid(), EnrollmentStatus.CANCELLED);
                            break;
                        case R.id.reOpen:
                            activity.getPresenter().updateEnrollmentStatus(activity.getEnrollmentUid(), EnrollmentStatus.ACTIVE);
                            break;
                    }
                    return true;
                })
                .build().show();

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
        if (adapter == null) {
            adapter = new EventAdapter(presenter, currentProgram, colorUtils);
            adapter.setEnrollment(currentEnrollment);
            binding.teiRecycler.setAdapter(adapter);
        }
        return adapter.stageSelector();
    }

    @Override
    public void setEvents(List<EventViewModel> events, boolean canAddEvents) {

        binding.setCanAddEvents(canAddEvents);

        if (events.isEmpty()) {
            binding.emptyTeis.setVisibility(View.VISIBLE);
            if (binding.dialFabLayout.isFabVisible()) {
                binding.emptyTeis.setText(R.string.empty_tei_add);
            } else {
                binding.emptyTeis.setText(R.string.empty_tei_no_add);
            }
        } else {
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



            for (EventViewModel eventViewModel : events) {
                if (eventViewModel.isAfterToday(DateUtils.getInstance().getToday())) {
                    binding.teiRecycler.scrollToPosition(events.indexOf(eventViewModel));
                }
            }
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
                                if (Boolean.TRUE.equals(programStageFromEvent.remindCompleted()))
                                    presenter.areEventsCompleted();
                            }
                        });
                dialog.show();
            } else if (Boolean.TRUE.equals(programStageModel.remindCompleted()))
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
        this.startActivityForResult(intent, REQ_EVENT, null);
    }

    @Override
    public void showTeiImage(String filePath, String defaultIcon) {
        if (filePath.isEmpty() && defaultIcon.isEmpty()) {
            binding.cardFront.teiImage.setVisibility(View.GONE);
        } else {
            binding.cardFront.teiImage.setVisibility(View.VISIBLE);
            Glide.with(this)
                    .load(new File(filePath))
                    .error(
                            ObjectStyleUtils.getIconResource(context, defaultIcon, R.drawable.photo_temp_gray, colorUtils)
                    )
                    .transition(withCrossFade())
                    .transform(new CircleCrop())
                    .into(binding.cardFront.teiImage);
            binding.cardFront.teiImage.setOnClickListener(view -> {
                File fileToShow = new File(filePath);
                if (fileToShow.exists()) {
                    new ImageDetailBottomDialog(
                            null,
                            fileToShow
                    ).show(getChildFragmentManager(), ImageDetailBottomDialog.TAG);
                }
            });
        }
    }

    public void goToEventInitial(EventCreationType eventCreationType, ProgramStage programStage) {
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
        new OUTreeFragment.Builder()
                .showAsDialog()
                .withPreselectedOrgUnits(
                        FilterManager.getInstance().getOrgUnitUidsFilters()
                )
                .onSelection(selectedOrgUnits -> {
                    presenter.setOrgUnitFilters((List<OrganisationUnit>) selectedOrgUnits);
                    return Unit.INSTANCE;
                })
                .build().show(getChildFragmentManager(), "OUTreeFragment");
    }

    @Override
    public void showSyncDialog(String eventUid, String enrollmentUid) {
        new SyncStatusDialog.Builder()
                .withContext(this, null)
                .withSyncContext(
                        new SyncContext.EnrollmentEvent(eventUid, enrollmentUid)
                )
                .onDismissListener(hasChanged -> {
                    if (hasChanged)
                        FilterManager.getInstance().publishData();

                }).show(enrollmentUid);
    }

    @Override
    public void displayCatComboOptionSelectorForEvents(List<EventViewModel> data) {
        eventCatComboOptionSelector.setEventsWithoutCatComboOption(data);
        eventCatComboOptionSelector.requestCatComboOption(
                (eventUid, categoryOptionComboUid) -> {
                    presenter.changeCatOption(eventUid, categoryOptionComboUid);
                    return null;
                }
        );
    }

    @Override
    public void showProgramRuleErrorMessage(@NonNull String message) {
        activity.runOnUiThread(() -> showDescription(message));
    }

    @Override
    public void setRiskColor(String risk) {

        if (risk == "High Risk") {
            binding.setHighRisk(true);
            binding.setLowRisk(false);
        }

        if (risk == "Low Risk") {
            binding.setLowRisk(true);
            binding.setHighRisk(false);
        }

    }

    @Override
    public void setProgramAttributes(List<ProgramTrackedEntityAttribute> programTrackedEntityAttributes) {

        this.programTrackedEntityAttributes = programTrackedEntityAttributes.stream()
                .filter(attr -> attr.displayInList())
                .collect(Collectors.toList());

        Collections.sort(this.programTrackedEntityAttributes, new CustomComparator());

        if (OrientationUtilsKt.isLandscape()) {
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
    public void showCatOptComboDialog(String catComboUid) {
        new CategoryDialog(
                CategoryDialog.Type.CATEGORY_OPTION_COMBO,
                catComboUid,
                false,
                null,
                selectedCatOptionCombo -> {
                    presenter.filterCatOptCombo(selectedCatOptionCombo);
                    return null;
                }
        ).show(
                getChildFragmentManager(),
                CategoryDialog.Companion.getTAG()
        );
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
package org.dhis2.usescases.eventsWithoutRegistration.eventCapture;

import static org.dhis2.usescases.teiDashboard.dashboardfragments.indicators.IndicatorsFragmentKt.VISUALIZATION_TYPE;
import static org.dhis2.commons.Constants.PROGRAM_UID;

import android.os.Bundle;

import androidx.annotation.IntegerRes;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import org.dhis2.R;
import org.dhis2.usescases.eventsWithoutRegistration.eventCapture.eventCaptureFragment.EventCaptureFormFragment;
import org.dhis2.usescases.eventsWithoutRegistration.eventDetails.ui.EventDetailsFragment;
import org.dhis2.usescases.notes.NotesFragment;
import org.dhis2.usescases.teiDashboard.dashboardfragments.indicators.IndicatorsFragment;
import org.dhis2.usescases.teiDashboard.dashboardfragments.indicators.VisualizationType;
import org.dhis2.usescases.teiDashboard.dashboardfragments.relationships.RelationshipFragment;
import org.dhis2.commons.Constants;

import java.util.ArrayList;
import java.util.List;

import kotlin.Unit;

public class EventCapturePagerAdapter extends FragmentStateAdapter {

    private final String programUid;
    private final String eventUid;
    private final List<EventPageType> pages;
    private EventCaptureFormFragment formFragment;

    public int getNavigationPagePosition(int navigationId) {

        int i = navigationId;
        EventPageType pageType =
                i == R.id.navigation_details ? EventPageType.DETAILS :
                        i == R.id.navigation_analytics ? EventPageType.ANALYTICS :
                                i == R.id.navigation_relationships ? EventPageType.RELATIONSHIPS :
                                        i == R.id.navigation_notes ? EventPageType.NOTES :
                                                i == R.id.navigation_data_entry ? EventPageType.DATA_ENTRY :
                                                        null;

        return pages.indexOf(pageType);

    }


    private enum EventPageType {
        DETAILS, DATA_ENTRY, ANALYTICS, RELATIONSHIPS, NOTES
    }

    public EventCapturePagerAdapter(FragmentActivity fragmentActivity,
                                    String programUid,
                                    String eventUid,
                                    boolean displayAnalyticScreen,
                                    boolean displayRelationshipScreen,
                                    boolean displayDataEntryScreen

    ) {
        super(fragmentActivity);
        this.programUid = programUid;
        this.eventUid = eventUid;
        pages = new ArrayList<>();
        pages.add(EventPageType.DETAILS);

        if (displayDataEntryScreen) {
            pages.add(EventPageType.DATA_ENTRY);
        }

        if (displayAnalyticScreen) {
            pages.add(EventPageType.ANALYTICS);
        }

        if (displayRelationshipScreen) {
            pages.add(EventPageType.RELATIONSHIPS);
        }
        pages.add(EventPageType.NOTES);
    }

    public int getDynamicTabIndex(@IntegerRes int tabClicked) {


        if (tabClicked == R.id.navigation_details) {

            System.out.println("kkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkk1");
            System.out.println(R.id.navigation_details);
            System.out.println(tabClicked);

            return pages.indexOf(EventPageType.DETAILS);
        } else if (tabClicked == R.id.navigation_data_entry) {

            System.out.println("kkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkk2");
            System.out.println(R.id.navigation_data_entry);
            System.out.println(tabClicked);
            return pages.indexOf(EventPageType.DATA_ENTRY);
        } else if (tabClicked == R.id.navigation_analytics) {

            System.out.println("kkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkk3");
            System.out.println(R.id.navigation_analytics);
            System.out.println(tabClicked);
            return pages.indexOf(EventPageType.ANALYTICS);
        } else if (tabClicked == R.id.navigation_relationships) {


            System.out.println("kkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkk4");
            System.out.println(R.id.navigation_relationships);
            System.out.println(tabClicked);
            return pages.indexOf(EventPageType.RELATIONSHIPS);
        } else if (tabClicked == R.id.navigation_notes) {


            System.out.println("kkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkk5");
            System.out.println(R.id.navigation_notes);
            System.out.println(tabClicked);
            return pages.indexOf(EventPageType.NOTES);
        }
        return 0;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (pages.get(position)) {
            default:
            case DETAILS:
                Bundle bundle = new Bundle();
                bundle.putString(Constants.EVENT_UID, eventUid);
                bundle.putString(PROGRAM_UID, programUid);
                EventDetailsFragment eventDetailsFragment = new EventDetailsFragment();
                eventDetailsFragment.setArguments(bundle);
                eventDetailsFragment.setOnEventReopened(() -> {
                    if (formFragment != null) {
                        formFragment.onReopen();
                    }
                    return Unit.INSTANCE;
                });
                return eventDetailsFragment;
            case DATA_ENTRY:
                formFragment = EventCaptureFormFragment.newInstance(eventUid);
                return formFragment;
            case ANALYTICS:
                Fragment indicatorFragment = new IndicatorsFragment();
                Bundle arguments = new Bundle();
                arguments.putString(VISUALIZATION_TYPE, VisualizationType.EVENTS.name());
                indicatorFragment.setArguments(arguments);
                return indicatorFragment;
            case RELATIONSHIPS:
                Fragment relationshipFragment = new RelationshipFragment();
                relationshipFragment.setArguments(
                        RelationshipFragment.withArguments(programUid,
                                null,
                                null,
                                eventUid
                        )
                );
                return relationshipFragment;
            case NOTES:
                return NotesFragment.newEventInstance(programUid, eventUid);
        }
    }

    @Override
    public int getItemCount() {
        return pages.size();
    }
}

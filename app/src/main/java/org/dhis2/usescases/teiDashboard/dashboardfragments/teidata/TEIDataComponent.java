package org.dhis2.usescases.teiDashboard.dashboardfragments.teidata;

import org.dhis2.commons.di.dagger.PerFragment;
import org.dhis2.usescases.eventsWithoutRegistration.eventTEIDetails.EventTeiDetailsFragment;

import dagger.Subcomponent;

/**
 * QUADRAM. Created by ppajuelo on 09/04/2019.
 */
@PerFragment
@Subcomponent(modules = TEIDataModule.class)
public interface TEIDataComponent {

    void inject(TEIDataFragment notesFragment);

    void inject(EventTeiDetailsFragment eventTeiDetailsFragment);

}

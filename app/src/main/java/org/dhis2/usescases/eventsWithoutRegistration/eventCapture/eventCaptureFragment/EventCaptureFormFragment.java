package org.dhis2.usescases.eventsWithoutRegistration.eventCapture.eventCaptureFragment;

import static org.dhis2.commons.extensions.ViewExtensionsKt.closeKeyboard;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.FragmentTransaction;

import org.dhis2.R;
import org.dhis2.data.forms.dataentry.FormView;
import org.dhis2.data.location.LocationProvider;
import org.dhis2.databinding.SectionSelectorFragmentBinding;
import org.dhis2.form.data.FormRepository;
import org.dhis2.form.model.DispatcherProvider;
import org.dhis2.form.model.FieldUiModel;
import org.dhis2.usescases.eventsWithoutRegistration.eventCapture.EventCaptureActivity;
import org.dhis2.usescases.general.FragmentGlobalAbstract;
import org.dhis2.utils.Constants;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import javax.inject.Inject;

import kotlin.Unit;

public class EventCaptureFormFragment extends FragmentGlobalAbstract implements EventCaptureFormView,
        OnEditionListener {

    @Inject
    EventCaptureFormPresenter presenter;

    @Inject
    FormRepository formRepository;

    @Inject
    LocationProvider locationProvider;

    @Inject
    DispatcherProvider coroutineDispatcher;

    private EventCaptureActivity activity;
    private SectionSelectorFragmentBinding binding;
    private FormView formView;

    public static EventCaptureFormFragment newInstance(String eventUid) {
        EventCaptureFormFragment fragment = new EventCaptureFormFragment();
        Bundle args = new Bundle();
        args.putString(Constants.EVENT_UID, eventUid);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(@NotNull Context context) {
        super.onAttach(context);
        this.activity = (EventCaptureActivity) context;
        activity.eventCaptureComponent.plus(
                new EventCaptureFormModule(
                        this,
                        getArguments().getString(Constants.EVENT_UID))
        ).inject(this);
        setRetainInstance(true);
    }

    @Override
    public void onCreate(@Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        formView = new FormView.Builder()
                .repository(formRepository)
                .locationProvider(locationProvider)
                .dispatcher(coroutineDispatcher)
                .onLoadingListener(loading -> {
                    if (loading) {
                        activity.showProgress();
                    } else {
                        activity.hideProgress();
                    }
                    return Unit.INSTANCE;
                })
                .onFocused(() -> {
                    activity.hideNavigationBar();
                    return Unit.INSTANCE;
                })
                .onPercentageUpdate(percentage -> {
                    activity.updatePercentage(percentage);
                    return Unit.INSTANCE;
                })
                .onDataIntegrityResult(result -> {
                    presenter.handleDataIntegrityResult(result);
                    return Unit.INSTANCE;
                })
                .factory(activity.getSupportFragmentManager())
                .build();
        activity.setFormEditionListener(this);
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.section_selector_fragment, container, false);
        binding.setPresenter(activity.getPresenter());
        binding.actionButton.setOnClickListener(view -> {
            closeKeyboard(view);
            performSaveClick();
        });

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
        transaction.replace(R.id.formViewContainer, formView).commit();
        formView.setScrollCallback(isSectionVisible -> {
            animateFabButton(isSectionVisible);
            return Unit.INSTANCE;
        });
        presenter.showOrHideSaveButton();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void animateFabButton(boolean sectionIsVisible) {
        int translationX = 1000;
        if (sectionIsVisible) translationX = 0;

        binding.actionButton.animate().translationX(translationX).setDuration(500).start();
    }

    @Override
    public void performSaveClick() {
        if (activity.getCurrentFocus() instanceof EditText) {
            activity.getCurrentFocus().clearFocus();
        } else {
            formView.requestDataIntegrityCheck();
        }
    }

    @Override
    public void onEditionListener() {
        formView.onEditionFinish();
    }

    @Override
    public void hideSaveButton() {
        binding.actionButton.setVisibility(View.GONE);
    }

    @Override
    public void showSaveButton() {
        binding.actionButton.setVisibility(View.VISIBLE);
    }
}
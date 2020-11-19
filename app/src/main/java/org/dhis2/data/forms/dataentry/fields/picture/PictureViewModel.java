package org.dhis2.data.forms.dataentry.fields.picture;

import androidx.annotation.NonNull;

import com.google.auto.value.AutoValue;

import org.dhis2.R;
import org.dhis2.data.forms.dataentry.DataEntryViewHolderTypes;
import org.dhis2.data.forms.dataentry.fields.FieldViewModel;
import org.dhis2.data.forms.dataentry.fields.RowAction;
import org.hisp.dhis.android.core.common.ObjectStyle;

import io.reactivex.processors.FlowableProcessor;

@AutoValue
public abstract class PictureViewModel extends FieldViewModel {

    public boolean isBackgroundTransparent;

    public static PictureViewModel create(String id, String label, Boolean mandatory, String value, String section, Boolean editable, String description, ObjectStyle objectStyle) {
        return new AutoValue_PictureViewModel(id, label, mandatory, value, section, null, editable, null, null, null, description, objectStyle, null, DataEntryViewHolderTypes.PICTURE, null);
    }

    public static PictureViewModel create(String id, String label, Boolean mandatory, String value, String section, Boolean editable, String description, ObjectStyle objectStyle, FlowableProcessor<RowAction> processor) {
        return new AutoValue_PictureViewModel(id, label, mandatory, value, section, null, editable, null, null, null, description, objectStyle, null, DataEntryViewHolderTypes.PICTURE, processor);
    }

    @Override
    public PictureViewModel setMandatory() {
        return new AutoValue_PictureViewModel(uid(), label(), true, value(), programStageSection(), null, editable(), null, warning(), error(), description(), objectStyle(), null, DataEntryViewHolderTypes.PICTURE, processor());
    }

    @NonNull
    @Override
    public PictureViewModel withWarning(@NonNull String warning) {
        return new AutoValue_PictureViewModel(uid(), label(), mandatory(), value(), programStageSection(), null, editable(), null, warning, error(), description(), objectStyle(), null, DataEntryViewHolderTypes.PICTURE, processor());
    }

    @NonNull
    @Override
    public PictureViewModel withError(@NonNull String error) {
        return new AutoValue_PictureViewModel(uid(), label(), mandatory(), value(), programStageSection(), null, editable(), null, warning(), error, description(), objectStyle(), null, DataEntryViewHolderTypes.PICTURE, processor());
    }

    @NonNull
    @Override
    public PictureViewModel withValue(String data) {
        return new AutoValue_PictureViewModel(uid(), label(), mandatory(), data, programStageSection(), null, false, null, warning(), error(), description(), objectStyle(), null, DataEntryViewHolderTypes.PICTURE, processor());
    }

    @NonNull
    @Override
    public PictureViewModel withEditMode(boolean isEditable) {
        return new AutoValue_PictureViewModel(uid(), label(), mandatory(), value(), programStageSection(), null, isEditable, null, warning(), error(), description(), objectStyle(), null, DataEntryViewHolderTypes.PICTURE, processor());
    }

    @Override
    public int getLayoutId() {
        return R.layout.custom_form_picture;
    }

    public void onClearValue() {
        processor().onNext(RowAction.create(uid(), null, getAdapterPosition()));
    }
}

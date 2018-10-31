package org.dhis2.usescases.datasets.dataSetTable;

import android.support.annotation.NonNull;

import org.dhis2.data.tuples.Pair;
import org.dhis2.usescases.datasets.dataSetTable.dataSetSection.DataSetSectionFragment;
import org.dhis2.usescases.general.AbstractActivityContracts;
import org.hisp.dhis.android.core.category.CategoryModel;
import org.hisp.dhis.android.core.category.CategoryOptionModel;
import org.hisp.dhis.android.core.dataelement.DataElementModel;
import org.hisp.dhis.android.core.dataset.DataSetModel;

import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

public class DataSetTableContract {

    public interface View extends AbstractActivityContracts.View {

        void setDataElements(Map<String, List<DataElementModel>> data, Map<String, List<List<Pair<CategoryOptionModel, CategoryModel>>>> stringListMap);

        void setDataSet(DataSetModel data);

        void setDataValue(List<DataSetTableModel> data);
    }

    public interface Presenter extends AbstractActivityContracts.Presenter {
        void onBackClick();

        void init(View view, String orgUnitUid, String periodTypeName, String periodInitialDate, String catCombo);

        void getData(@NonNull DataSetSectionFragment dataSetSectionFragment, @Nullable String sectionUid);

        Map<String, List<List<CategoryOptionModel>>> transformCategories(@NonNull Map<String, List<List<Pair<CategoryOptionModel, CategoryModel>>>> map);
    }

}

package org.dhis2.usescases.teiDashboard.dashboardfragments.teidata.teievents

import android.view.View
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.composethemeadapter.MdcTheme
import io.reactivex.processors.FlowableProcessor
import org.dhis2.R
import org.dhis2.commons.data.EventViewModel
import org.dhis2.commons.data.StageSection
import org.dhis2.commons.resources.ColorType
import org.dhis2.commons.resources.ColorUtils
import org.dhis2.commons.resources.ResourceManager
import org.dhis2.databinding.ItemStageSectionBinding
import org.dhis2.ui.MetadataIconData
import org.dhis2.ui.setUpMetadataIcon
import org.dhis2.usescases.teiDashboard.dashboardfragments.teidata.TEIDataPresenter
import org.dhis2.usescases.teiDashboard.ui.NewEventOptions

internal class StageViewHolder(
    private val binding: ItemStageSectionBinding,
    private val stageSelector: FlowableProcessor<StageSection>,
    private val presenter: TEIDataPresenter,
    private val colorUtils: ColorUtils,
) : RecyclerView.ViewHolder(binding.root) {

    init {
        binding.composeProgramStageIcon.setViewCompositionStrategy(
            ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed,
        )
    }

    fun bind(eventItem: EventViewModel) {
        val stage = eventItem.stage!!

        binding.programStageName.text = stage.displayName()
        binding.programStageName.post {
            binding.programStageName.isSelected = true
        }

        val color = colorUtils.getColorFrom(
            stage.style().color(),
            colorUtils.getPrimaryColor(
                itemView.context,
                ColorType.PRIMARY_LIGHT,
            ),
        )

        val resourceManager = ResourceManager(itemView.context, colorUtils)

        val iconResource = resourceManager
            .getObjectStyleDrawableResource(
                stage.style().icon(),
                R.drawable.ic_default_outline,
            )

        binding.composeProgramStageIcon.setUpMetadataIcon(
            MetadataIconData(
                programColor = color,
                iconResource = iconResource,
                sizeInDp = 40,
            ),
            false,
        )

        binding.addStageButton.visibility =
            if (eventItem.canShowAddButton()) {
                View.VISIBLE
            } else {
                View.GONE
            }

        binding.addStageButton.setContent {
            MdcTheme {
                NewEventOptions(presenter.getNewEventOptionsByStages(stage)) {
                    presenter.onAddNewEventOptionSelected(it, stage)
                }
            }
        }
        binding.addStageButton.setOnClickListener {
            stageSelector.onNext(
                StageSection(
                    stageUid = stage.uid(),
                    showOptions = true,
                    showAllEvents = false,
                ),
            )
        }

        binding.programNoStageText.visibility =
            if (eventItem.eventCount < 1) {
                View.VISIBLE
            } else {
                View.GONE
            }

        if (eventItem.isSelected) {
            eventItem.isSelected = false
        }
    }
}

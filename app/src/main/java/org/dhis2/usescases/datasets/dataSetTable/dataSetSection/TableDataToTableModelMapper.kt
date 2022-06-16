package org.dhis2.usescases.datasets.dataSetTable.dataSetSection

import org.dhis2.compose_table.model.TableCell
import org.dhis2.compose_table.model.TableHeader
import org.dhis2.compose_table.model.TableHeaderRow
import org.dhis2.compose_table.model.TableModel
import org.dhis2.compose_table.model.TableRowModel
import java.util.SortedMap

class TableDataToTableModelMapper {
    fun map(tableData: TableData): TableModel {
        val tableHeader = TableHeader(
            rows = tableData.columnHeaders()?.map { catOptions ->
                TableHeaderRow(
                    cells = catOptions.distinctBy { it.uid() }
                        .filter { it.uid() != null && it.uid().isNotEmpty() }
                        .map { categoryOption ->
                            TableCell(value = categoryOption.displayName()!!)
                        }
                )
            } ?: emptyList(),
            hasTotals = tableData.showRowTotals
        )

        val tableRows = tableData.rows()?.mapIndexed { rowIndex, dataElement ->
            TableRowModel(
                rowHeader = dataElement.displayName()!!,
                values = tableData.fieldViewModels[rowIndex].mapIndexed { columnIndex, field ->
                    columnIndex to TableCell(
                        id = field.uid(),
                        row = rowIndex,
                        column = columnIndex,
                        value = field.value()
                    )
                }.toMap()
            )
        } ?: emptyList()

        return TableModel(
            id = tableData.catCombo()?.uid(),
            tableHeaderModel = tableHeader,
            tableRows = tableRows
        )
    }

    fun map(tableData: SortedMap<String?, String>): TableModel {
        val tableHeader = TableHeader(
            rows = listOf(
                TableHeaderRow(
                    cells = listOf(
                        TableCell(value = "Value")
                    )
                )
            )
        )

        val tableRows = tableData.map { (indicatorName, indicatorValue) ->
            TableRowModel(
                rowHeader = indicatorName!!,
                values = mapOf(Pair(0, TableCell(value = indicatorValue)))
            )
        }

        return TableModel(
            tableHeaderModel = tableHeader,
            tableRows = tableRows
        )
    }
}

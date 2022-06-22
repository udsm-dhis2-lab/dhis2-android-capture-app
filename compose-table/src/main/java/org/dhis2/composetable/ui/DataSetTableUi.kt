package org.dhis2.composetable.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import kotlinx.coroutines.launch
import org.dhis2.composetable.model.TableCell
import org.dhis2.composetable.model.TableHeader
import org.dhis2.composetable.model.TableModel
import org.dhis2.composetable.model.TableRowModel

@Composable
fun TableHeader(
    modifier: Modifier,
    tableHeaderModel: TableHeader,
    horizontalScrollState: ScrollState
) {
    Row(modifier = modifier.horizontalScroll(state = horizontalScrollState)) {
        Column {
            tableHeaderModel.rows.forEachIndexed { rowIndex, tableHeaderRow ->
                Row {
                    val totalColumns = tableHeaderModel.numberOfColumns(rowIndex)
                    val rowOptions = tableHeaderRow.cells.size
                    repeat(
                        times = totalColumns,
                        action = { columnIndex ->
                            val cellIndex = columnIndex % rowOptions
                            HeaderCell(
                                cellIndex,
                                headerCell = tableHeaderRow.cells[cellIndex],
                                headerWidth = tableHeaderModel.cellWidth(rowIndex)
                            )
                        }
                    )
                }
            }
        }
        if (tableHeaderModel.hasTotals) {
            HeaderCell(
                columnIndex = tableHeaderModel.rows.size,
                headerCell = TableCell(value = "Total"),
                headerWidth = tableHeaderModel.defaultCellWidth
            )
        }
    }
}

@Composable
fun HeaderCell(columnIndex: Int, headerCell: TableCell, headerWidth: Dp) {
    Text(
        modifier = Modifier
            .width(headerWidth)
            .background(
                if (columnIndex % 2 == 0) {
                    Color.Gray
                } else {
                    Color.LightGray
                }
            ),
        text = headerCell.value ?: ""
    )
}

@Composable
fun TableHeaderRow(
    tableModel: TableModel,
    horizontalScrollState: ScrollState
) {
    Row {
        TableCorner(tableModel)
        TableHeader(
            modifier = Modifier,
            tableHeaderModel = tableModel.tableHeaderModel,
            horizontalScrollState = horizontalScrollState
        )
    }
}

@Composable
fun TableItemRow(
    tableModel: TableModel,
    horizontalScrollState: ScrollState,
    dataElementLabel: String,
    dataElementValues: Map<Int, TableCell>,
    onValueChange: (TableCell) -> Unit
) {
    Row {
        ItemHeader(dataElementLabel)
        ItemValues(
            horizontalScrollState = horizontalScrollState,
            columnCount = tableHeaderModel.tableMaxColumns(),
            cellValues = dataElementValues,
            defaultHeight = tableModel.tableHeaderModel.defaultCellHeight,
            defaultWidth = tableModel.tableHeaderModel.defaultCellWidth,
            onValueChange = onValueChange
        )
    }
}

@Composable
fun TableCorner(tableModel: TableModel) {
    Box(
        modifier = Modifier
            .height(tableModel.tableHeaderModel.defaultCellHeight)
            .width(tableRows.defaultWidth)
    )
}

@Composable
fun ItemHeader(dataElementLabel: String) {
    Text(
        modifier = Modifier
            .height(tableModel.tableHeaderModel.defaultCellHeight)
            .width(tableModel.tableHeaderModel.defaultCellWidth),
        text = dataElementLabel
    )
}

@Composable
fun ItemValues(
    horizontalScrollState: ScrollState,
    columnCount: Int,
    cellValues: Map<Int, TableCell>,
    defaultHeight: Dp,
    defaultWidth: Dp,
    onValueChange: (TableCell) -> Unit
) {
    val focusRequester = LocalFocusManager.current
    val coroutineScope = rememberCoroutineScope()
    val defaultWidthPx = LocalDensity.current.run { defaultWidth.toPx() }
    Row(
        modifier = Modifier
            .horizontalScroll(state = horizontalScrollState)
    ) {
        repeat(
            times = columnCount,
            action = { columnIndex ->
                TableCell(
                    modifier = Modifier
                        .width(defaultWidth)
                        .height(defaultHeight),
                    cell = cellValues[columnIndex] ?: TableCell(value = ""),
                    focusRequester = focusRequester,
                    onNext = {
                        coroutineScope.launch {
                            horizontalScrollState.scrollTo(
                                (columnIndex + 1) * defaultWidthPx.toInt()
                            )
                        }
                    },
                    onValueChange = onValueChange
                )
            }
        )
    }
}

@Composable
fun TableCell(
    modifier: Modifier,
    cell: TableCell,
    focusRequester: FocusManager,
    onNext: () -> Unit,
    onValueChange: (TableCell) -> Unit
) {
    if (cell.isReadOnly) {
        ClickableText(
            modifier = modifier,
            text = AnnotatedString(cell.value ?: ""),
            onClick = {
                onValueChange(cell)
            }
        )
    } else {
        var value by remember { mutableStateOf(cell.value) }
        BasicTextField(
            modifier = modifier,
            value = value ?: "",
            onValueChange = { newValue ->
                value = newValue
                onValueChange(cell.copy(value = value))
            },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
            keyboardActions = KeyboardActions(
                onNext = {
                    onNext()
                    focusRequester.moveFocus(
                        FocusDirection.Right
                    )
                }
            )
        )
    }
}

private val tableHeaderModel = TableHeader(
    rows = listOf(
        org.dhis2.composetable.model.TableHeaderRow(
            cells = listOf(
                TableCell(value = "<18"),
                TableCell(value = ">18 <65"),
                TableCell(value = ">65")
            )
        ),
        org.dhis2.composetable.model.TableHeaderRow(
            cells = listOf(
                TableCell(value = "Male"),
                TableCell(value = "Female")
            )
        ),
        org.dhis2.composetable.model.TableHeaderRow(
            cells = listOf(
                TableCell(value = "Fixed"),
                TableCell(value = "Outreach")
            )
        )
    ),
    hasTotals = true
)

private val tableRows = TableRowModel(
    rowHeader = "Data Element",
    values = mapOf(
        Pair(2, TableCell(value = "12")),
        Pair(4, TableCell(value = "55"))
    )
)

private val tableModel = TableModel(
    tableHeaderModel = tableHeaderModel,
    tableRows = listOf(tableRows, tableRows, tableRows, tableRows)
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TableList(
    tableList: List<TableModel>,
    onCellChange: (TableCell) -> Unit
) {
    val horizontalScrollStates = tableList.map { rememberScrollState() }
    LazyColumn {
        tableList.forEachIndexed { index, currentTableModel ->
            stickyHeader {
                TableHeaderRow(
                    tableModel = currentTableModel,
                    horizontalScrollState = horizontalScrollStates[index]
                )
            }
            items(items = currentTableModel.tableRows) { tableRowModel ->
                TableItemRow(
                    tableModel = tableModel,
                    horizontalScrollState = horizontalScrollStates[index],
                    dataElementLabel = tableRowModel.rowHeader,
                    dataElementValues = tableRowModel.values,
                    onValueChange = onCellChange
                )
            }
        }
    }
}

@Preview
@Composable
fun TableListPreview() {
    val tableList = listOf(tableModel, tableModel, tableModel, tableModel, tableModel, tableModel)
    TableList(tableList) {}
}

package org.dhis2.composetable

import androidx.compose.ui.unit.dp
import org.dhis2.composetable.model.TableCell
import org.dhis2.composetable.model.TableHeader
import org.dhis2.composetable.model.TableHeaderRow
import org.junit.Assert.assertTrue
import org.junit.Ignore
import org.junit.Test

class TableHeaderTest {

    val tableHeaderModel = TableHeader(
        rows = listOf(
            TableHeaderRow(
                cells = listOf(
                    TableCell(value = "<18"),
                    TableCell(value = ">18 <65"),
                    TableCell(value = ">65")
                )
            ),
            TableHeaderRow(
                cells = listOf(
                    TableCell(value = "Male"),
                    TableCell(value = "Female")
                )
            ),
            TableHeaderRow(
                cells = listOf(
                    TableCell(value = "Fixed"),
                    TableCell(value = "Outreach")
                )
            )
        )
    )

    @Test
    fun numberOfCellsInHeaderRow() {
        assertTrue(tableHeaderModel.numberOfColumns(0) == 3)
        assertTrue(tableHeaderModel.numberOfColumns(1) == 6)
        assertTrue(tableHeaderModel.numberOfColumns(2) == 12)
    }

    @Ignore
    @Test
    fun widthCellInHeaderRow() {
        assertTrue(tableHeaderModel.cellWidth(0) == 200.dp)
        assertTrue(tableHeaderModel.cellWidth(1) == 100.dp)
        assertTrue(tableHeaderModel.cellWidth(2) == 50.dp)
    }
}

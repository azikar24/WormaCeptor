package com.azikar24.wormaceptor.feature.database.ui.theme

import androidx.compose.ui.graphics.Color
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorColors

/**
 * Database feature colors - delegates to centralized colors.
 * @see WormaCeptorColors.Database
 */
object DatabaseDesignSystem {

    /** Colors representing SQLite column data types. */
    object DataTypeColors {
        /** Color for INTEGER column type. */
        val integer = WormaCeptorColors.Database.Integer

        /** Color for REAL column type. */
        val real = WormaCeptorColors.Database.Real

        /** Color for TEXT column type. */
        val text = WormaCeptorColors.Database.Text

        /** Color for BLOB column type. */
        val blob = WormaCeptorColors.Database.Blob

        /** Color for NULL values. */
        val nullValue = WormaCeptorColors.Database.NullValue

        /** Color for primary key columns. */
        val primaryKey = WormaCeptorColors.Database.PrimaryKey

        /** Returns the color for the given SQLite column type name. */
        fun forType(type: String): Color = WormaCeptorColors.Database.forDataType(type)
    }

    /** Colors for SQL syntax highlighting in the query editor. */
    object SqlSyntaxColors {
        /** Color for SQL keywords (SELECT, FROM, WHERE, etc.). */
        val keyword = WormaCeptorColors.Database.SqlKeyword

        /** Color for SQL string literals. */
        val string = WormaCeptorColors.Database.SqlString

        /** Color for SQL numeric literals. */
        val number = WormaCeptorColors.Database.SqlNumber

        /** Color for SQL built-in functions. */
        val function = WormaCeptorColors.Database.SqlFunction

        /** Color for SQL operators. */
        val operator = WormaCeptorColors.Database.SqlOperator

        /** Color for SQL comments. */
        val comment = WormaCeptorColors.Database.SqlComment

        /** Color for SQL table name references. */
        val table = WormaCeptorColors.Database.SqlTable
    }
}

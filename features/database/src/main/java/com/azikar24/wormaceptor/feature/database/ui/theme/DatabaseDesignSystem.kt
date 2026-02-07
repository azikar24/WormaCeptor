package com.azikar24.wormaceptor.feature.database.ui.theme

import androidx.compose.ui.graphics.Color
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorColors

/**
 * Database feature colors - delegates to centralized colors.
 * @see WormaCeptorColors.Database
 */
object DatabaseDesignSystem {

    object DataTypeColors {
        val integer = WormaCeptorColors.Database.Integer
        val real = WormaCeptorColors.Database.Real
        val text = WormaCeptorColors.Database.Text
        val blob = WormaCeptorColors.Database.Blob
        val nullValue = WormaCeptorColors.Database.NullValue
        val primaryKey = WormaCeptorColors.Database.PrimaryKey

        fun forType(type: String): Color = WormaCeptorColors.Database.forDataType(type)
    }

    object SqlSyntaxColors {
        val keyword = WormaCeptorColors.Database.SqlKeyword
        val string = WormaCeptorColors.Database.SqlString
        val number = WormaCeptorColors.Database.SqlNumber
        val function = WormaCeptorColors.Database.SqlFunction
        val operator = WormaCeptorColors.Database.SqlOperator
        val comment = WormaCeptorColors.Database.SqlComment
        val table = WormaCeptorColors.Database.SqlTable
    }
}

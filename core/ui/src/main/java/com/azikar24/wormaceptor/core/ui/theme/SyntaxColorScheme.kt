package com.azikar24.wormaceptor.core.ui.theme

/**
 * Color scheme for syntax highlighting.
 * Colors are represented as ARGB Long values (e.g., 0xFF0033B3).
 * Implementations provide both light and dark theme variants.
 */
interface SyntaxColors {
    val keyword: Long
    val string: Long
    val number: Long
    val comment: Long
    val property: Long
    val tag: Long
    val operator: Long
    val punctuation: Long
    val boolean: Long
    val typeAnnotation: Long
    val default: Long
}

/** Light theme syntax colors - inspired by IntelliJ IDEA light theme. */
@Suppress("MagicNumber")
object LightSyntaxColors : SyntaxColors {
    override val keyword = 0xFF0033B3L
    override val string = 0xFF067D17L
    override val number = 0xFF1750EBL
    override val comment = 0xFF8C8C8CL
    override val property = 0xFF871094L
    override val tag = 0xFF0033B3L
    override val operator = 0xFF000000L
    override val punctuation = 0xFF000000L
    override val boolean = 0xFF0033B3L
    override val typeAnnotation = 0xFF008080L
    override val default = 0xFF000000L
}

/** Dark theme syntax colors - inspired by IntelliJ IDEA dark theme. */
@Suppress("MagicNumber")
object DarkSyntaxColors : SyntaxColors {
    override val keyword = 0xFFCC7832L
    override val string = 0xFF6A8759L
    override val number = 0xFF6897BBL
    override val comment = 0xFF808080L
    override val property = 0xFF9876AAL
    override val tag = 0xFFE8BF6AL
    override val operator = 0xFFA9B7C6L
    override val punctuation = 0xFFA9B7C6L
    override val boolean = 0xFFCC7832L
    override val typeAnnotation = 0xFF6897BBL
    override val default = 0xFFA9B7C6L
}

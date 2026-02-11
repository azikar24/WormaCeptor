package com.azikar24.wormaceptor.core.ui.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Formats a timestamp as short time: "HH:mm:ss".
 */
fun formatTimestamp(timestamp: Long): String = SimpleDateFormat("HH:mm:ss", Locale.US).format(Date(timestamp))

/**
 * Formats a timestamp as full date+time: "MMM d, yyyy 'at' HH:mm:ss".
 */
fun formatTimestampFull(timestamp: Long): String =
    SimpleDateFormat("MMM d, yyyy 'at' HH:mm:ss", Locale.US).format(Date(timestamp))

/**
 * Formats a timestamp as compact date+time+millis: "MMM d, HH:mm:ss.SSS".
 */
fun formatTimestampCompact(timestamp: Long): String =
    SimpleDateFormat("MMM d, HH:mm:ss.SSS", Locale.US).format(Date(timestamp))

/**
 * Formats a timestamp as short date+time: "MMM d, HH:mm".
 */
fun formatDateShort(timestamp: Long): String = SimpleDateFormat("MMM d, HH:mm", Locale.US).format(Date(timestamp))

/**
 * Formats a timestamp as ISO-style date+time: "yyyy-MM-dd HH:mm:ss".
 */
fun formatDateFull(timestamp: Long): String =
    SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date(timestamp))

/**
 * Formats a timestamp as short date: "MMM d, yyyy".
 */
fun formatDateOnly(timestamp: Long): String = SimpleDateFormat("MMM d, yyyy", Locale.US).format(Date(timestamp))

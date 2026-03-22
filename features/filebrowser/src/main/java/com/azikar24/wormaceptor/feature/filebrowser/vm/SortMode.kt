package com.azikar24.wormaceptor.feature.filebrowser.vm

/** Determines how files are sorted in the file browser listing. */
enum class SortMode {
    /** Sort alphabetically by file name. */
    NAME,

    /** Sort by file size. */
    SIZE,

    /** Sort by last-modified date. */
    DATE,
}

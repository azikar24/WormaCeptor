package com.azikar24.wormaceptor.core.engine

object CoreHolder {
    @Volatile
    var captureEngine: CaptureEngine? = null
    
    @Volatile
    var queryEngine: QueryEngine? = null
}

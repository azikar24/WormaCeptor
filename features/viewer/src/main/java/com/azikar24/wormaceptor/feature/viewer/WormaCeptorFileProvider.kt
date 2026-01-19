package com.azikar24.wormaceptor.feature.viewer

import androidx.core.content.FileProvider

/**
 * Custom FileProvider subclass for WormaCeptor library.
 * This avoids manifest merger conflicts with apps that have their own FileProvider.
 */
class WormaCeptorFileProvider : FileProvider()

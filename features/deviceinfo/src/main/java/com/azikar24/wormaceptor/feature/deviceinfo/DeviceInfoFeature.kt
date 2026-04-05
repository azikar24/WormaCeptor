package com.azikar24.wormaceptor.feature.deviceinfo

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.azikar24.wormaceptor.feature.deviceinfo.vm.DeviceInfoViewModel

/** Entry point for creating device-info ViewModel dependencies. */
object DeviceInfoFeature {

    /** Creates a [DeviceInfoViewModelFactory] for the given [application]. */
    fun createViewModelFactory(application: Application): DeviceInfoViewModelFactory {
        return DeviceInfoViewModelFactory(application)
    }
}

/** Factory that provides [DeviceInfoViewModel] instances. */
class DeviceInfoViewModelFactory(
    private val application: Application,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DeviceInfoViewModel::class.java)) {
            return DeviceInfoViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}

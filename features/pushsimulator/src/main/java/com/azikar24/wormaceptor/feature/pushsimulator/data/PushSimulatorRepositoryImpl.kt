package com.azikar24.wormaceptor.feature.pushsimulator.data

import com.azikar24.wormaceptor.core.engine.PushSimulatorEngine
import com.azikar24.wormaceptor.domain.contracts.PushSimulatorRepository
import com.azikar24.wormaceptor.domain.entities.NotificationChannelInfo
import com.azikar24.wormaceptor.domain.entities.NotificationTemplate
import kotlinx.coroutines.flow.Flow

/**
 * Implementation of PushSimulatorRepository using DataStore and PushSimulatorEngine.
 */
class PushSimulatorRepositoryImpl(
    private val dataSource: PushSimulatorDataSource,
    private val engine: PushSimulatorEngine,
) : PushSimulatorRepository {

    override fun getTemplates(): Flow<List<NotificationTemplate>> {
        return dataSource.observeTemplates()
    }

    override suspend fun saveTemplate(template: NotificationTemplate) {
        dataSource.saveTemplate(template)
    }

    override suspend fun deleteTemplate(id: String) {
        dataSource.deleteTemplate(id)
    }

    override suspend fun getNotificationChannels(): List<NotificationChannelInfo> {
        // Ensure default channel exists
        engine.createDefaultChannel()

        val channels = engine.getNotificationChannels()

        // If no channels (shouldn't happen after creating default), return default
        return channels.ifEmpty {
            listOf(
                NotificationChannelInfo(
                    id = PushSimulatorEngine.DEFAULT_CHANNEL_ID,
                    name = PushSimulatorEngine.DEFAULT_CHANNEL_NAME,
                    description = PushSimulatorEngine.DEFAULT_CHANNEL_DESCRIPTION,
                    importance = android.app.NotificationManager.IMPORTANCE_DEFAULT,
                ),
            )
        }
    }
}

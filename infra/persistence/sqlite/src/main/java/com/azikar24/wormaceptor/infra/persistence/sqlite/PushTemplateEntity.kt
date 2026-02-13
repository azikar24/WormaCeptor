package com.azikar24.wormaceptor.infra.persistence.sqlite

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.azikar24.wormaceptor.domain.entities.NotificationAction
import com.azikar24.wormaceptor.domain.entities.NotificationPriority
import com.azikar24.wormaceptor.domain.entities.NotificationTemplate
import com.azikar24.wormaceptor.domain.entities.SimulatedNotification
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Entity(tableName = "push_templates")
data class PushTemplateEntity(
    @PrimaryKey val id: String,
    val name: String,
    val notificationId: String,
    val title: String,
    val body: String,
    val channelId: String,
    val smallIconRes: Int = 0,
    val largeIconUri: String? = null,
    val priority: String = "DEFAULT",
    val actionsJson: String = "[]",
    val extrasJson: String = "{}",
    val timestamp: Long = 0L,
) {
    fun toDomain(): NotificationTemplate {
        val actions = try {
            json.decodeFromString<List<SerializedAction>>(actionsJson)
                .map { NotificationAction(it.title, it.actionId) }
        } catch (_: Exception) {
            emptyList()
        }
        val extras = try {
            json.decodeFromString<Map<String, String>>(extrasJson)
        } catch (_: Exception) {
            emptyMap()
        }
        return NotificationTemplate(
            id = id,
            name = name,
            notification = SimulatedNotification(
                id = notificationId,
                title = title,
                body = body,
                channelId = channelId,
                smallIconRes = smallIconRes,
                largeIconUri = largeIconUri,
                priority = NotificationPriority.valueOf(priority),
                actions = actions,
                extras = extras,
                timestamp = timestamp,
            ),
        )
    }

    companion object {
        private val json = Json { ignoreUnknownKeys = true }

        fun fromDomain(template: NotificationTemplate): PushTemplateEntity {
            val notification = template.notification
            val actionsStr = json.encodeToString(
                notification.actions.map { SerializedAction(it.title, it.actionId) },
            )
            val extrasStr = json.encodeToString(notification.extras)
            return PushTemplateEntity(
                id = template.id,
                name = template.name,
                notificationId = notification.id,
                title = notification.title,
                body = notification.body,
                channelId = notification.channelId,
                smallIconRes = notification.smallIconRes,
                largeIconUri = notification.largeIconUri,
                priority = notification.priority.name,
                actionsJson = actionsStr,
                extrasJson = extrasStr,
                timestamp = notification.timestamp,
            )
        }
    }
}

@Serializable
private data class SerializedAction(
    val title: String,
    val actionId: String,
)

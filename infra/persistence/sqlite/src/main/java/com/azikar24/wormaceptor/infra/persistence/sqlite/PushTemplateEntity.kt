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

/**
 * Room entity representing a saved push notification template for the push simulator.
 *
 * @property id Unique identifier for the template.
 * @property name User-visible display name of the template.
 * @property notificationId Identifier assigned to the simulated notification.
 * @property title Notification title text.
 * @property body Notification body text.
 * @property channelId Android notification channel identifier.
 * @property smallIconRes Resource ID for the notification small icon.
 * @property largeIconUri Optional URI string for the notification large icon.
 * @property priority Stringified [NotificationPriority] level.
 * @property actionsJson JSON-serialised list of notification action buttons.
 * @property extrasJson JSON-serialised map of extra key-value data.
 * @property timestamp Epoch millis when the template was created or last used.
 */
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
    /** Converts this entity to a domain [NotificationTemplate] model. */
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

    /** JSON serialization and domain-entity conversion factory. */
    companion object {
        private val json = Json { ignoreUnknownKeys = true }

        /** Creates a [PushTemplateEntity] from a domain [NotificationTemplate] model. */
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

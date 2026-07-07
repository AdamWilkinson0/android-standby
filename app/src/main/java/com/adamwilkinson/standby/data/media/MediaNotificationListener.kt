package com.adamwilkinson.standby.data.media

import android.service.notification.NotificationListenerService

/**
 * Intentionally empty. The service existing and the user granting
 * Notification Access is what authorizes MediaSessionManager.getActiveSessions;
 * we never read notifications themselves.
 */
class MediaNotificationListener : NotificationListenerService()

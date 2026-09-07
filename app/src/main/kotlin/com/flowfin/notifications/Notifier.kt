package com.flowfin.notifications

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.annotation.StringRes
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.navigation3.runtime.NavKey
import com.flowfin.MainActivity
import com.flowfin.core.navigation.AccountDetailRoute
import com.flowfin.core.navigation.AccountsRoute
import com.flowfin.core.navigation.AddTransactionRoute
import com.flowfin.core.navigation.HomeRoute
import com.flowfin.core.navigation.RecurringRoute
import com.flowfin.core.resources.R

/**
 * The three channels, one per kind of interruption. Deliberately not one: muting
 * the evening nudge must not also mute "rent is three days overdue".
 *
 * **The ids are permanent.** Android keeps a channel — and the user's importance,
 * sound and Do-Not-Disturb choices for it — for the life of the install, and a
 * renamed id is a new channel with the user's settings silently reset. Change the
 * labels freely; never the ids.
 */
enum class NotificationKind(
  val channelId: String,
  @param:StringRes val channelName: Int,
  @param:StringRes val channelDescription: Int,
) {
  DAILY_REMINDER("daily_reminder", R.string.channel_reminder, R.string.channel_reminder_sub),
  PAYMENT_ALERT("payment_alerts", R.string.channel_payments, R.string.channel_payments_sub),
  BUDGET_ALERT("budget_alerts", R.string.channel_budgets, R.string.channel_budgets_sub),
}

/** Where tapping a notification lands. Every notification goes somewhere specific. */
enum class NotificationTarget { ADD_TRANSACTION, RECURRING, ACCOUNT }

/**
 * Posts FlowFin's notifications. Thin over [NotificationManagerCompat] — it owns
 * the channels, the small icon, and the tap intent, and nothing else.
 */
class Notifier(private val context: Context) {

  private val manager = NotificationManagerCompat.from(context)

  /**
   * Whether posting would actually show anything — `POST_NOTIFICATIONS` from API
   * 33, and the user not having muted FlowFin from Android's own settings. Both
   * are the same question from the app's side, and the answer is checked at post
   * time rather than remembered: it can change while the app isn't running.
   */
  val enabled: Boolean get() = manager.areNotificationsEnabled()

  fun createChannels() {
    manager.createNotificationChannelsCompat(
      NotificationKind.entries.map { kind ->
        NotificationChannelCompat.Builder(kind.channelId, NotificationManagerCompat.IMPORTANCE_DEFAULT)
          .setName(context.getString(kind.channelName))
          .setDescription(context.getString(kind.channelDescription))
          .build()
      },
    )
  }

  /**
   * [tag] plus [id] identify the notification: same pair replaces, different pair
   * stacks. Kinds use different tags so a bill and a budget can't collide on a
   * hashed id.
   */
  @SuppressLint("MissingPermission") // guarded by [enabled]
  fun post(
    kind: NotificationKind,
    tag: String,
    id: Int,
    title: String,
    body: String,
    target: NotificationTarget,
    targetId: String? = null,
  ) {
    if (!enabled) return
    manager.notify(
      tag,
      id,
      NotificationCompat.Builder(context, kind.channelId)
        .setSmallIcon(com.flowfin.R.drawable.ic_notification)
        .setContentTitle(title)
        .setContentText(body)
        .setStyle(NotificationCompat.BigTextStyle().bigText(body))
        .setAutoCancel(true)
        .setContentIntent(pendingIntent(id, target, targetId))
        .build(),
    )
  }

  private fun pendingIntent(id: Int, target: NotificationTarget, targetId: String?): PendingIntent {
    val intent = Intent(context, MainActivity::class.java)
      .addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP)
      .putExtra(EXTRA_TARGET, target.name)
      .putExtra(EXTRA_TARGET_ID, targetId)
    // The request code has to vary with the notification, or two pending intents
    // that differ only in extras are treated as one and the first one's extras win.
    return PendingIntent.getActivity(
      context,
      id,
      intent,
      PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
    )
  }
}

/**
 * The back stack a tapped notification should land on, or null if this intent
 * didn't come from one. A tab plus (where it applies) the screen inside it, so
 * back from a deep-linked detail goes somewhere sensible rather than out.
 */
fun Intent.notificationBackStack(): List<NavKey>? {
  val target = getStringExtra(EXTRA_TARGET) ?: return null
  val id = getStringExtra(EXTRA_TARGET_ID)
  return when (runCatching { NotificationTarget.valueOf(target) }.getOrNull()) {
    NotificationTarget.ADD_TRANSACTION -> listOf(HomeRoute, AddTransactionRoute)
    NotificationTarget.RECURRING -> listOf(RecurringRoute)
    NotificationTarget.ACCOUNT ->
      if (id == null) listOf(AccountsRoute) else listOf(AccountsRoute, AccountDetailRoute(id))
    null -> null
  }
}

private const val EXTRA_TARGET = "com.flowfin.notification.target"
private const val EXTRA_TARGET_ID = "com.flowfin.notification.targetId"

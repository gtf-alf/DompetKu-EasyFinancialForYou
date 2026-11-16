package com.example.notifications

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.example.model.RecurringTransactionModel
import com.example.model.TransactionType
import com.example.util.CurrencyUtils
import com.example.util.DateUtils

object ReminderScheduler {

    fun syncReminders(context: Context, items: List<RecurringTransactionModel>) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val now = System.currentTimeMillis()

        items.forEach { item ->
            val requestCode = item.id.hashCode()
            val intent = buildIntent(context, item, requestCode)
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                requestCode,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            if (!item.enabled) {
                alarmManager.cancel(pendingIntent)
                return@forEach
            }

            val triggerAt = DateUtils.computeReminderTriggerMillis(
                dueIsoDate = item.nextDueDate,
                daysBefore = item.reminderDaysBefore.toInt(),
                hour = item.reminderHour.toInt(),
                minute = item.reminderMinute.toInt(),
            )

            if (triggerAt > now) {
                alarmManager.setAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerAt,
                    pendingIntent
                )
            } else {
                alarmManager.cancel(pendingIntent)
            }
        }
    }

    fun cancelReminder(context: Context, recurringId: String) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val requestCode = recurringId.hashCode()
        val intent = Intent(context, ReminderReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent)
        }
    }

    fun sendTestNotification(context: Context, item: RecurringTransactionModel? = null) {
        val title = if (item != null) {
            val hLabel = if (item.reminderDaysBefore == 0L) "Hari ini" else "H-${item.reminderDaysBefore}"
            "Pengingat ($hLabel): ${item.title}"
        } else {
            "Pengingat DompetKu Aktif"
        }
        val body = if (item != null) {
            val typeLabel = TransactionType.fromKey(item.type).labelId
            "$typeLabel sebesar ${CurrencyUtils.formatIdr(item.amount)} (${item.categoryName}) jatuh tempo ${DateUtils.formatShortIndonesianDate(item.nextDueDate)}."
        } else {
            "Notifikasi pengingat transaksi berulang DompetKu siap mengingatkan tagihan dan pemasukan rutin Anda."
        }
        ReminderReceiver.showReminderNotification(
            context = context,
            title = title,
            body = body,
            notifId = (item?.id?.hashCode() ?: 9999)
        )
    }

    private fun buildIntent(
        context: Context,
        item: RecurringTransactionModel,
        notifId: Int,
    ): Intent {
        val hLabel = if (item.reminderDaysBefore == 0L) "Hari-H" else "H-${item.reminderDaysBefore}"
        val typeLabel = TransactionType.fromKey(item.type).labelId
        val formattedAmount = CurrencyUtils.formatIdr(item.amount)
        val formattedDue = DateUtils.formatShortIndonesianDate(item.nextDueDate)
        return Intent(context, ReminderReceiver::class.java).apply {
            putExtra(ReminderReceiver.EXTRA_TITLE, "Pengingat $hLabel: ${item.title}")
            putExtra(
                ReminderReceiver.EXTRA_BODY,
                "$typeLabel $formattedAmount (${item.categoryName}) dijadwalkan pada $formattedDue."
            )
            putExtra(ReminderReceiver.EXTRA_NOTIF_ID, notifId)
        }
    }
}

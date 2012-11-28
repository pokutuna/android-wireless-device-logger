package com.pokutuna.n7logger

import com.pokutuna.n7logger.action._
import _root_.android.app._
import _root_.android.content._

class AlarmReceiver extends BroadcastReceiver {
  override def onReceive(context: Context, intent: Intent) {
    if (AlarmReceiver.defaultAction == intent.getAction) {
      val intent = LogService.getIntent(context)
      context.startService(intent)
    }
  }
}

object AlarmReceiver extends ReceiverTrait {
  override val companionClass = classOf[AlarmReceiver]
  override val defaultAction  = LoggerAction.alarmFired
}


object Alarm {

  def startAlarm(context: Context) {
    if (AlarmReceiver.isRegisteredBroadcast(context)) {
      stopAlarm(context)
      Util.log(context, "stop!")
    }

    val alarm: AlarmManager =
      context.getSystemService(Context.ALARM_SERVICE).asInstanceOf[AlarmManager]
    val intent: Intent = AlarmReceiver.getIntent(context)
    val pending: PendingIntent =
      PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
    alarm.setInexactRepeating(
      AlarmManager.RTC, System.currentTimeMillis, 20 * 1000, pending //TODO logger interval
    )
  }

  def stopAlarm(context: Context) {
    val alarm: AlarmManager =
      context.getSystemService(Context.ALARM_SERVICE).asInstanceOf[AlarmManager]
    val intent: Intent = AlarmReceiver.getIntent(context)
    val pending: PendingIntent =
      PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT)
    alarm.cancel(pending)
  }

}

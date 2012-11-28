package com.pokutuna.n7logger

import com.pokutuna.n7logger._
import _root_.android.app._
import _root_.android.content._
import _root_.android.content.res.Resources

object NotificationUtil {

  def getScanningNotification(context: Context): Notification = {
    val intent = new Intent(context, classOf[MainActivity])
    val pending =
      PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)

    val notification: Notification = new Notification.Builder(context)
      .setSmallIcon(R.drawable.scan)
      .setContentTitle(context.getString(R.string.app_name))
      .setWhen(System.currentTimeMillis)
      .setContentIntent(pending)
      .getNotification
    notification.flags = Notification.FLAG_NO_CLEAR ^ Notification.FLAG_FOREGROUND_SERVICE
    return notification
  }

}

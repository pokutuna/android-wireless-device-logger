package com.pokutuna.n7logger

import com.pokutuna.n7logger.uri._
import _root_.android.app._
import _root_.android.content._
import scala.collection.JavaConversions._

trait ReceiverTrait {

  val companionClass: Class[_]
  val defaultAction: String

  def getIntent(context: Context): Intent = {
    val intent = new Intent(context, companionClass)
    intent.setAction(defaultAction)
    intent.setData(LoggerUri.get(companionClass.getSimpleName))
    return intent
  }

  def isRegisteredBroadcast(context: Context): Boolean = {
    val intent = getIntent(context)
    val pending =
      PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_NO_CREATE)
    return pending != null
  }

  def isRunningService(context: Context): Boolean = {
    val manager = context.getSystemService(Context.ACTIVITY_SERVICE).asInstanceOf[ActivityManager]
    for ( service <- manager.getRunningServices(Int.MaxValue) ) {
      if (service.service.getClassName == companionClass.getName) return true
    }
    return false
  }

}

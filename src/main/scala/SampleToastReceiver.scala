package com.pokutuna.n7logger

import com.pokutuna.n7logger.action._
import com.pokutuna.n7logger.uri._
import _root_.android.app._
import _root_.android.content._
import _root_.android.widget.Toast
import java.util.Date
import scala.collection.JavaConversions._

class SampleToastReceiver extends BroadcastReceiver {

  def message: String = {
    return "[" + DateFormatter.formatTime(new Date()) + "] Toast!! Yeah!!"
  }

  override def onReceive(context: Context, intent: Intent) {
    Toast.makeText(context, message, Toast.LENGTH_LONG).show()
    Util.log(this, message)
    if (LoggerAction.alarmFired == intent.getAction) {
      val intent =
        (new Intent(context, classOf[LogService])).setAction(LoggerAction.scanRequest)
      context.startService(intent)
    }
  }
}


object SampleToastReceiver extends ReceiverTrait {
  override val companionClass = classOf[SampleToastReceiver]
  override val defaultAction = LoggerAction.alarmFired
}

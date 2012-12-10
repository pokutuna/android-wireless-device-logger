package com.pokutuna.n7logger

import com.pokutuna.n7logger.action._
import _root_.android.app._
import _root_.android.content._
import _root_.android.os.IBinder
import _root_.android.util.Log
import _root_.android.app.Notification
import java.util.Date
import java.lang.Thread

class LogService extends Service {

  var btScanner: BtScanner = null
  var btLogfile = new LogFileWriter(DeviceType.Bluetooth)
  var wfScanner: WifiScanner = null
  var wfLogfile = new LogFileWriter(DeviceType.WiFi)

  override def onBind(intent: Intent): IBinder = {
    return null;
  }

  override def onCreate() {
    super.onCreate()

    startForeground(1, NotificationUtil.getScanningNotification(this))

    btScanner = new BtScanner(this) {
      override def onDetected(device: BtDevice) = {
        Util.log(this, "BtScanner " + device.toLog)
        btLogfile.puts(device.toLog)
        sendToMain(device)
      }
      override def onScanned(success: Boolean) = {
        if (success) {
          val btScanLog = EventLogProducer.getBtScanEventLog
          btLogfile.puts(btScanLog)
          btLogfile.flush
          sendToMain(btScanLog, DeviceType.Bluetooth)
        }
      }
    }

    wfScanner = new WifiScanner(this) {
      override def onDetected(device: WifiDevice) = {
        Util.log(this, "WifiScanner " + device.toLog)
        wfLogfile.puts(device.toLog)
        sendToMain(device)
      }
      override def onScanned(success: Boolean) = {
        if (success) {
          val wfScanLog = EventLogProducer.getWfScanEventLog
          wfLogfile.puts(wfScanLog)
          wfLogfile.flush()
          sendToMain(wfScanLog, DeviceType.WiFi)
        }
      }
    }

    sendToMain(EventLogProducer.getStartEventLog)
  }

  override def onStartCommand(intent: Intent, flags: Int, startId: Int): Int = {
    if (intent == null) return Service.START_STICKY;

    if (LogService.defaultAction == intent.getAction) {
      checkRotate()
      btScanner.scan()
      wfScanner.scan()
    }

    return Service.START_STICKY;
  }

  override def onDestroy() {
    btScanner.destroy()
    wfScanner.destroy()
    btLogfile.close()
    wfLogfile.close()
    stopForeground(true)
    sendToMain(EventLogProducer.getStopEventLog)
    super.onDestroy()
  }

  def sendToMain(device: AnyDevice) = {
    sendBroadcast(device.toIntent.setAction(LoggerAction.updateRequest))
  }

  def sendToMain(log: String, logType: DeviceType.Value = DeviceType.Other) = {
    val intent = (new Intent()).putExtra("type", logType.id).putExtra("log", log)
    sendBroadcast(intent.setAction(LoggerAction.updateRequest))
  }

  def checkRotate() = {
    if (btLogfile.isOld) {
      btLogfile.close()
      btLogfile = new LogFileWriter(DeviceType.Bluetooth)
      sendToMain("bt log rotate: " + LogFileWriter.today)
    }

    if (wfLogfile.isOld) {
      wfLogfile.close()
      wfLogfile = new LogFileWriter(DeviceType.WiFi)
      sendToMain("wifi log rotate: " + LogFileWriter.today)
    }
  }
}

object LogService extends ReceiverTrait {
  override val companionClass = classOf[LogService]
  override val defaultAction  = LoggerAction.scanRequest

  def start(context: Activity) =
    context.startService(new Intent(context.getApplicationContext, classOf[LogService]))

  def stop(context: Activity) =
    context.stopService(new Intent(context.getApplicationContext, classOf[LogService]))
}

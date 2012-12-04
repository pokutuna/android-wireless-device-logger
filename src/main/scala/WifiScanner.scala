package com.pokutuna.n7logger

import _root_.android.util.Log
import _root_.android.content._
import _root_.android.net.wifi._
import _root_.android.app.Activity
import scala.collection.JavaConversions._
import java.util.Date

class WifiScanner(context: Context) extends ScannerTrait[WifiDevice] {

  lazy val wifi: WifiManager =
    context.getSystemService(Context.WIFI_SERVICE).asInstanceOf[WifiManager]

  override def init() = {
    interval = Some(20)
  }

  override def scanAction(): Boolean = synchronized {
    if (wifi.getWifiState == WifiManager.WIFI_STATE_ENABLED) {
      Util.log(this, "start wifi scan")
      wifi.startScan()
      val devices = wifi.getScanResults
      for (d <- devices) onDetected(new WifiDevice(d))
      return true
    }
    return false
  }

  init()
}

object WifiScanner {

  object Status extends Enumeration {
    val Working, NotEnabled = Value
  }

  def checkStatus(context: Context): WifiScanner.Status.Value = {
    val wifi: WifiManager =
      context.getSystemService(Context.WIFI_SERVICE).asInstanceOf[WifiManager]

    if (!wifi.isWifiEnabled) return Status.NotEnabled

    return Status.Working
  }

}

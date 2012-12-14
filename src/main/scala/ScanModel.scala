package com.pokutuna.wdlogger

import _root_.android.content.Intent
import _root_.android.bluetooth.BluetoothDevice
import _root_.android.net.wifi.ScanResult
import java.util.Date

object DeviceType extends Enumeration {
  val Bluetooth, WiFi, Other = Value
}

trait AnyDevice {
  def getType: DeviceType.Value = DeviceType.Other
  def getId: String
  def getName: String
  def getSignal: Int
  def toLog: String

  override def toString: String = List(getType, ":", getId, getName, getSignal).mkString(" ")
  val timestamp: String = DateFormatter.formatTime(new Date())

  def toIntent: Intent = {
    val intent = new Intent()
    intent.putExtra("type", getType.id)
    intent.putExtra("log", toLog)
    return intent
  }
}

class BtDevice(device: BluetoothDevice) extends AnyDevice {
  override def getType: DeviceType.Value = DeviceType.Bluetooth
  override def getId: String = device.getAddress().toUpperCase
  override def getName: String = device.getName()
  override def getSignal: Int = -1
  override def toLog: String =
    List(timestamp, getName, getId).mkString("\t").replaceAll("\n", "")
}

class WifiDevice(device: ScanResult) extends AnyDevice {
  override def getType: DeviceType.Value = DeviceType.WiFi
  override def getId: String = device.BSSID.toUpperCase
  override def getName: String = device.SSID
  override def getSignal: Int = device.level
  override def toLog: String =
    List(timestamp, getName, getId, getSignal).mkString("\t").replaceAll("\n", "")
}

object EventLogProducer {
  def getLoggerVersionLog = "[LOGGER_VERSION]" + Logger.versionName
  def getLoggerBDALog(bda: String): String = "[LOGGER_BDA]" + bda
  def getStartEventLog: String = "[LOGGER_START]" + DateFormatter.formatTime(new Date())
  def getStopEventLog: String = "[LOGGER_STOP]" + DateFormatter.formatTime(new Date())
  def getBtScanEventLog: String = "[BT_SCAN]" + DateFormatter.formatTime(new Date())
  def getWfScanEventLog: String = "[WIFI_SCAN]" + DateFormatter.formatTime(new Date())
  def getStartSyncEventLog: String = "[SYNC_START]" + DateFormatter.formatTime(new Date())
  def getSucecssSyncEventLog: String = "[SYNC_SUCCESS]" + DateFormatter.formatTime(new Date())
  def getFailSyncEventLog: String = "[SYNC_FAILED]" + DateFormatter.formatTime(new Date())
}

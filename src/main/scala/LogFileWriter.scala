package com.pokutuna.n7logger

import _root_.android.os.Environment
import _root_.android.bluetooth.BluetoothAdapter
import _root_.android.content.res.Resources
import java.io._
import java.util.Date

object LogFileWriter {

  // /sdcard/<root>/<device_name>/<bda>/<date>/logs

  val ext = ".tsv"
  lazy val logRoot: File = new File(
    Environment.getExternalStorageDirectory().getPath,
    Util.getString(R.string.rootDirName)
  )
  lazy val bt = BluetoothAdapter.getDefaultAdapter
  lazy val deviceName: String = bt.getName
  lazy val deviceAddress: String = bt.getAddress
  lazy val dirParentDates: File =
    joinPath(logRoot, deviceName, deviceAddress.replaceAll(":", ""))

  def joinPath(root: File, dirs: String*): File = {
    new File(root, dirs.mkString(File.separator))
  }

  def today: String = DateFormatter.formatDateDigits(new Date())

  def getLogFilename(date: String, deviceType: DeviceType.Value): String = {
    List(typeToString(deviceType), date, ext).mkString
  }

  def getLogPath(day: String, deviceType: DeviceType.Value): File = {
    val file = joinPath(dirParentDates, day, getLogFilename(day, deviceType))
    val parent = file.getParentFile
    if (!parent.exists) parent.mkdirs
    return file
  }

  def typeToString(deviceType: DeviceType.Value): String = deviceType match {
    case DeviceType.Bluetooth => "bda"
    case DeviceType.WiFi      => "wifi"
    case _ => throw new RuntimeException("other log")
  }

  def logFileNamePattern = """(bda|wifi).*\.tsv""".r

}

class LogFileWriter(deviceType: DeviceType.Value, day: String = LogFileWriter.today) {

  val logFile: File = LogFileWriter.getLogPath(day, deviceType)
  val fos = new FileOutputStream(logFile, true) // 追記
  val osw = new OutputStreamWriter(fos, "UTF-8")
  val bw  = new BufferedWriter(osw)

  init()

  def init() = synchronized {
    puts(EventLogProducer.getLoggerVersionLog)
    puts(EventLogProducer.getLoggerBDALog(LogFileWriter.deviceAddress))
  }

  def puts(log: String) = synchronized {
    bw.write(log)
    bw.newLine()
  }

  def flush() = {
    bw.flush()
  }

  def close() = {
    bw.flush()
    bw.close()
    osw.close()
    fos.close()
  }

  def isOld: Boolean = day != LogFileWriter.today
}

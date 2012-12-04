package com.pokutuna.n7logger

import _root_.android.util.Log
import _root_.android.bluetooth._
import _root_.android.app._
import _root_.android.content._
import java.util.Date

class BtScanner(context: Context) extends ScannerTrait[BtDevice] {

  lazy val bt = BluetoothAdapter.getDefaultAdapter()

  val filter = new IntentFilter()
  filter.addAction(BluetoothDevice.ACTION_FOUND)

  val receiver = new BtReceiver()

  override def init() = {
    interval = Some(20)

    try { // 複数のレシーバ登録を防止するために消してから登録
      context.unregisterReceiver(receiver)
    } catch {
      case e: IllegalArgumentException => Util.log(this, "not registered yet")
    } finally {
      context.registerReceiver(receiver, filter)
    }
  }

  override def scanAction(): Boolean = synchronized {
    if (bt.isEnabled && !bt.isDiscovering) {
      Util.log(this, "start bt scan")
      bt.startDiscovery()
      return true
    }
    return false
  }

  override def destroy() = {
    Util.log(this, "destroy")
    try {
      context.unregisterReceiver(receiver)
    } catch {
      case e: IllegalArgumentException => Util.log(this, "not registered yet")
    }
  }

  class BtReceiver extends BroadcastReceiver {
    override def onReceive(context: Context, intent: Intent) {
      if (BluetoothDevice.ACTION_FOUND == intent.getAction) {
        val device: BluetoothDevice =
          intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
        onDetected(new BtDevice(device))
      }
    }
  }

  init()
}

object BtScanner {

  object Status extends Enumeration {
    val Working, NotEnabled, NotDiscoverable = Value
  }

  def checkStatus: BtScanner.Status.Value = {
    val bt = BluetoothAdapter.getDefaultAdapter

    if (!bt.isEnabled) return Status.NotEnabled

    if (bt.getScanMode != BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE)
      return Status.NotDiscoverable

    return Status.Working
  }

}

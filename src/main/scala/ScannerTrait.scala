package com.pokutuna.n7logger

import java.util.Date

trait ScannerTrait[T <: AnyDevice] {

  // should override
  var interval: Option[Int] = None
  def init(): Unit = {}
  def scanAction(): Boolean
  def onDetected(device: T): Unit = Util.log(this, "ScanEventHandler:" + device.toString)
  def destroy(): Unit = {}
  def onScanSuccess: Unit = Util.log(this, "ScanSuccess")

  // members
  var lastScanned: Date = new Date(0)

  def scan(): Unit = interval match {
    case Some(gap) => {
      if ( lastScanned.getTime + gap < (new Date().getTime) ) scanAndUpdate()
    }
    case None           => scanAndUpdate()
  }

  def scanAndUpdate() = {
    new Thread(new Runnable() {
      override def run(): Unit = {
        val scanned = scanAction()
        if (scanned) lastScanned = new Date()
      }
    }).start()
  }

  def setInterval(interval: Int): Unit = {
    this.interval = Some(interval)
  }

}

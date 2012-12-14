package com.pokutuna.wdlogger

import java.text.SimpleDateFormat
import java.util.Date

object DateFormatter {
  val sdfTime = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss")
  val sdfDate = new SimpleDateFormat("yyyy/MM/dd")
  val sdfDateDigits = new SimpleDateFormat("yyyyMMdd")

  def formatTime(date: Date): String = synchronized {
    sdfTime.format(date)
  }

  def formatDate(date: Date): String = synchronized {
    sdfDate.format(date)
  }

  def formatDateDigits(date: Date): String = synchronized {
    sdfDateDigits.format(date)
  }
}

package com.pokutuna.n7logger

import _root_.android.util.Log
import java.util.Date

object Util {

  val logTag = "N7Logger"

  def log(from: Object, msg: String): Unit = {
    Log.d(logTag, List(DateFormatter.formatTime(new Date()), "[" + getClassName(from) + "]", msg).mkString(" "))
  }

  def getClassName(obj: Object): String = obj.getClass.getSimpleName

  def getString(id: Int): String = App.getContext.getString(id)
}

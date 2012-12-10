package com.pokutuna.n7logger

import _root_.android.util.Log
import java.util.Date

object Util {

  def log(from: Object, msg: String): Unit = {
    if (Logger.isDebugging) {
      Log.d(Logger.logTag, List(DateFormatter.formatTime(new Date()),"[" + getClassName(from) + "]", msg).mkString(" "))
    }
  }

  def getClassName(obj: Object): String = obj.getClass.getSimpleName

  def getString(id: Int): String = App.getContext.getString(id)
}

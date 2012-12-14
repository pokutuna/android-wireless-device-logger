package com.pokutuna.wdlogger

import _root_.android.widget.Toast
import _root_.android.view.Gravity

object ToastUtil {
  def say(message: String) = {
    val toast = Toast.makeText(App.getContext, message, Toast.LENGTH_LONG)
    toast.setGravity(Gravity.AXIS_CLIP, 0, 0)
    toast.show()
  }
}

package com.pokutuna.n7logger

import _root_.android.widget.Toast

object ToastUtil {
  def say(message: String) = Toast.makeText(App.getContext, message, Toast.LENGTH_LONG).show()
}

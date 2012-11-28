package com.pokutuna.n7logger

object Logger {
  import _root_.android.content.pm._

  val packageName = "com.pokutuna.n7logger"
  val versionName = {
    val pm = App.getContext.getPackageManager
    pm.getPackageInfo(packageName, PackageManager.GET_META_DATA).versionName
  }
}

package action {
  object LoggerAction {
    val packageName   = Logger.packageName + ".action"
    val alarmFired    = packageName + ".ALARM_FIRED"
    val scanRequest   = packageName + ".SCAN_REQUEST"
    val updateRequest = packageName + ".UPDATE_REQUEST"
  }
}

package uri {
  import _root_.android.net.Uri

  object LoggerUri {
    val schema = "n7logger://"
    def get(str: String): Uri = Uri.parse(schema + str)
  }
}

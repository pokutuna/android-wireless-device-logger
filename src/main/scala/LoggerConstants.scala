package com.pokutuna.n7logger

object Logger {
  import _root_.android.content.pm._

  val packageName = "com.pokutuna.n7logger"

  val logTag = "N7Logger"

  lazy val packageManager = App.getContext.getPackageManager

  lazy val appInfo = packageManager.getApplicationInfo(App.getContext.getPackageName, 0)

  lazy val versionName =
    packageManager.getPackageInfo(packageName, PackageManager.GET_META_DATA).versionName

  lazy val isDebugging: Boolean = {
    val info = appInfo
    (info.flags & ApplicationInfo.FLAG_DEBUGGABLE) == ApplicationInfo.FLAG_DEBUGGABLE
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

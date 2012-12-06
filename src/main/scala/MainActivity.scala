package com.pokutuna.n7logger

import com.pokutuna.n7logger.action._
import _root_.android.app._
import _root_.android.content._
import _root_.android.view._
import _root_.android.view.View._
import _root_.android.widget._
import _root_.android.graphics._
import _root_.android.os._
import _root_.android.util.Log
import _root_.android.webkit.WebView
import java.lang.System

class MainActivity extends Activity with TypedActivity with OnClickListener {

  // view
  lazy val btlog  = findView(TR.btlog)
  lazy val wflog  = findView(TR.wflog)
  lazy val syslog = findView(TR.syslog)
  lazy val logToggle = findView(TR.logToggle)
  lazy val clearBtn = findView(TR.logClear)
  lazy val btBtn = findView(TR.btButton)
  lazy val wfBtn = findView(TR.wifiButton)
  lazy val syncBtn = findView(TR.syncButton)

  override def onCreate(savedInstanceState: Bundle) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.main)

    initView()
    registerUpdateReceiver()
    startLogging()

    checkBluetooth()
    checkWiFi()

    syslog.append(EventLogProducer.getLoggerVersionLog + "\n")
  }

  override def onCreateOptionsMenu(menu: Menu): Boolean = {
    menu.add(Menu.NONE, 1, Menu.NONE, getString(R.string.info))
    menu.add(Menu.NONE, 2, Menu.NONE, getString(R.string.deauthorize))
    super.onCreateOptionsMenu(menu)
  }

  override def onOptionsItemSelected(item: MenuItem): Boolean = item.getItemId match {
    case id if id == 1 => openInfoDialog(); true
    case id if id == 2 => deauthorizeDropbox(); true
    case _ => false
  }

  override def onResume() {
    super.onResume()

    checkBluetooth()
    checkWiFi()

    if (Dropbox.isAuthed) {
      try {
        Dropbox.Api.getSession.finishAuthentication();
        val token = Dropbox.Api.getSession.getAccessTokenPair;
        Dropbox.putDropboxToken(token)
      } catch {
        case e: IllegalStateException => ToastUtil.say(getString(R.string.authFail))
      }
    }
  }

  override def onDestroy() {
    unregisterReceiver(updateReceiver)
    super.onDestroy()
  }

  def initView() {
    List(logToggle, clearBtn, btBtn, wfBtn, syncBtn).foreach(_.setOnClickListener(this))
  }

  override def onClick(view: View) {
    view.getId match {
      case id if id == logToggle.getId => toggleLogging()
      case id if id == clearBtn.getId  => clearLog()
      case id if id == btBtn.getId     => checkBluetoothAction()
      case id if id == wfBtn.getId     => checkWiFiAction()
      case id if id == syncBtn.getId   => syncLogdata()
      case _ => Util.log(this, "unexpected click: " + view.toString)
    }
  }

  def toggleLogging() {
    if (LogService.isRunningService(this)) stopLogging() else startLogging()
  }

  def startLogging() {
    Alarm.startAlarm(this)
    LogService.start(this)
    logToggle.setImageResource(R.drawable.stop)
    logToggle.getBackground.setColorFilter(Color.BLUE, PorterDuff.Mode.DARKEN);
  }

  def stopLogging() {
    Alarm.stopAlarm(this)
    LogService.stop(this)
    logToggle.setImageResource(R.drawable.play)
    logToggle.getBackground.setColorFilter(null);
  }

  def clearLog() {
    openYNDialogSimply(
      R.string.clearDialogTitle,
      R.string.clearDialogMessage,
      () => {
        List(btlog, wflog, syslog).foreach(_.setText(""))
        ToastUtil.say(getString(R.string.clearDialogComplete))
      }
    )
  }

  def checkBluetooth() {
    BtScanner.checkStatus match {
      case s if s == BtScanner.Status.Working => btBtn.hideErrorBadge()
      case _                                  => btBtn.showErrorBadge()
    }
  }

  def checkBluetoothAction() {
    openYNDialogSimply(
      R.string.checkBtTitle,
      btStatusToStringId(BtScanner.checkStatus),
      () => openBluetoothConfig)
  }

  def checkWiFi() {
    WifiScanner.checkStatus(this) match {
      case s if s == WifiScanner.Status.Working => wfBtn.hideErrorBadge()
      case _                                    => wfBtn.showErrorBadge()
    }
  }

  def checkWiFiAction() {
    openYNDialogSimply(
      R.string.checkWfTitle,
      wfStatusToStringId(WifiScanner.checkStatus(this)),
      () => openWifiConfig
    )
  }

  def syncLogdata() {
    Dropbox.getDropboxToken match {
      case Some(token) => Dropbox.startSync()
      case None => Dropbox.Api.getSession().startAuthentication(MainActivity.this);
    }
  }

  def updateFromService(intent: Intent) = intent.getIntExtra("type", -1) match {
    case t if t == DeviceType.Bluetooth.id =>
      btlog.setText(intent.getStringExtra("log") + "\n" + btlog.getText)
    case t if t == DeviceType.WiFi.id      =>
      wflog.setText(intent.getStringExtra("log") + "\n" + wflog.getText)
    case t if t == DeviceType.Other.id     =>
      syslog.setText(intent.getStringExtra("log") + "\n" + syslog.getText)
    case _ =>
  }


  // udpate ui receiver
  lazy val updateReceiver = new BroadcastReceiver {
    override def onReceive(context: Context, intent: Intent) {
      if (intent == null) return;
      if (LoggerAction.updateRequest == intent.getAction) updateFromService(intent)
    }
  }

  def registerUpdateReceiver() = {
    val filter = new IntentFilter()
    filter.addAction(LoggerAction.updateRequest)

    try {
      unregisterReceiver(updateReceiver)
    } catch {
      case e: IllegalArgumentException => Util.log(this, "not registered update receiver")
    } finally {
      registerReceiver(updateReceiver, filter)
    }
  }

  def openYNDialogSimply(titleId: Int, messageId: Int, yesAction: () => Unit) = {
    val alert = new AlertDialog.Builder(this)
    alert.setTitle(getString(titleId))
    alert.setMessage(getString(messageId))
    alert.setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
      override def onClick(dialog: DialogInterface, which: Int) {
        yesAction()
      }
    })
    alert.setNegativeButton(getString(R.string.no), new DialogInterface.OnClickListener() {
      override def onClick(dialog: DialogInterface, which: Int) {}
    })
    alert.show()
  }

  def openBluetoothConfig = {
    val intent = new Intent(android.provider.Settings.ACTION_BLUETOOTH_SETTINGS)
    startActivity(intent)
  }

  def btStatusToStringId(status: BtScanner.Status.Value): Int = status match {
    case s if s == BtScanner.Status.Working         => R.string.checkBtWorking
    case s if s == BtScanner.Status.NotEnabled      => R.string.checkBtNotEnabled
    case s if s == BtScanner.Status.NotDiscoverable => R.string.checkBtNotDiscoverable
  }

  def openWifiConfig = {
    val intent = new Intent(android.provider.Settings.ACTION_WIFI_SETTINGS)
    startActivity(intent);
  }

  def wfStatusToStringId(status: WifiScanner.Status.Value): Int = status match {
    case s if s == WifiScanner.Status.Working         => R.string.checkWfWorking
    case s if s == WifiScanner.Status.NotEnabled      => R.string.checkWfNotEnabled
  }


  // menu action
  def openInfoDialog() {
    val dialog = new AlertDialog.Builder(this)
    val webView = new WebView(this)
    webView.loadData(getString(R.string.infoHtml), "text/html", "UTF-8")
    dialog.setView(webView)
    dialog.setPositiveButton("close", new DialogInterface.OnClickListener() {
      override def onClick(dialog: DialogInterface, whitch: Int) = dialog.dismiss()
    })
    dialog.show()
  }

  def deauthorizeDropbox() {
    Dropbox.dropDropboxToken()
    ToastUtil.say(getString(R.string.deauthorizeComplete))
  }

}

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

    LogService.start(this)
    Alarm.startAlarm(this)
    registerUpdateReceiver()
    logToggle.setImageResource(R.drawable.stop)
    logToggle.setState(ViewState.Active)

    checkBluetooth()
    checkWiFi()

    syslog.append(EventLogProducer.getLoggerVersionLog + "\n")
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
    if (LogService.isRunningService(this)) {
      Alarm.stopAlarm(this)
      LogService.stop(this)
      logToggle.setImageResource(R.drawable.play)
      logToggle.setState(ViewState.Default)
    } else {
      Alarm.startAlarm(this)
      LogService.start(this)
      logToggle.setImageResource(R.drawable.stop)
      logToggle.setState(ViewState.Active)
    }
  }

  def clearLog() {
    val alert = new AlertDialog.Builder(this)
    openDialogSimply(
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
      case s if s == BtScanner.Status.Working => btBtn.setState(ViewState.Working)
      case _ => btBtn.setState(ViewState.Error)
    }
  }

  def checkBluetoothAction() {
    val messageId = BtScanner.checkStatus match {
      case s if s == BtScanner.Status.Working         => R.string.checkBtWorking
      case s if s == BtScanner.Status.NotEnabled      => R.string.checkBtNotEnabled
      case s if s == BtScanner.Status.NotDiscoverable => R.string.checkBtNotDiscoverable
    }
    openDialogSimply(
      R.string.checkBtTitle,
      messageId,
      () => BtScanner.openConfig(this)
    )
  }

  def checkWiFi() {
    WifiScanner.checkStatus(this) match {
      case s if s == WifiScanner.Status.Working => wfBtn.setState(ViewState.Working)
      case _ => wfBtn.setState(ViewState.Error)
    }
  }

  def checkWiFiAction() {
    val messageId = WifiScanner.checkStatus(this) match {
      case s if s == WifiScanner.Status.Working         => R.string.checkWfWorking
      case s if s == WifiScanner.Status.NotEnabled      => R.string.checkWfNotEnabled
    }
    openDialogSimply(
      R.string.checkWfTitle,
      messageId,
      () => WifiScanner.openConfig(this)
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

  // view util
  object ViewState extends Enumeration {
      val Active, Working, Error, Default = Value
  }

  // TODO
  case class MyButton(button: ImageButton) {
    def setState(state: ViewState.Value) = state match {
      case ViewState.Active   =>
        button.getBackground.setColorFilter(Color.BLUE, PorterDuff.Mode.DARKEN);
      case ViewState.Working  =>
        button.getBackground.setColorFilter(Color.GREEN, PorterDuff.Mode.DARKEN);
      case ViewState.Error    =>
        button.getBackground.setColorFilter(Color.RED, PorterDuff.Mode.DARKEN);
      case ViewState.Default  =>
        button.getBackground.setColorFilter(null);
      case _                  => Util.log(this, "unknown style")
    }
  }

  implicit def buttonToMyButton(button: ImageButton): MyButton = MyButton(button)


  def openDialogSimply(titleId: Int, messageId: Int, yesAction: () => Unit) = {
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

}

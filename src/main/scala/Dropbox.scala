package com.pokutuna.n7logger

import _root_.android.os._
import _root_.android.content._
import com.dropbox.client2._
import com.dropbox.client2.android._
import com.dropbox.client2.session._
import com.dropbox.client2.session.Session._
import com.dropbox.client2.exception._
import com.pokutuna.n7logger.action.LoggerAction
import java.io._
import java.util._

object Dropbox {
  // secret keys are not in the repository
  val appKey     = Util.getString(R.string.DropboxAppKey)
  val appSecret  = Util.getString(R.string.DropboxAppSecret)

  val accessType = AccessType.APP_FOLDER

  lazy val appKeys = new AppKeyPair(appKey, appSecret)
  lazy val session = new AndroidAuthSession(appKeys, accessType)
  lazy val Api = new DropboxAPI[AndroidAuthSession](session)

  updateApiToken()

  def isAuthed: Boolean = Api.getSession.authenticationSuccessful

  def getDropboxToken: Option[AccessTokenPair] = {
    (Preference.getString("AccessKey"), Preference.getString("AccessSecret")) match {
      case (Some(key), Some(secret)) => Some(new AccessTokenPair(key, secret))
      case _                         => None
    }
  }

  def putDropboxToken(token: AccessTokenPair) = {
    Preference.putString("AccessKey", token.key)
    Preference.putString("AccessSecret", token.secret)
    updateApiToken()
  }

  def dropDropboxToken() = {
    Preference.dropString("AccessKey")
    Preference.dropString("AccessSecret")
  }

  def updateApiToken() = {
    getDropboxToken match {
      case Some(token) => Api.getSession.setAccessTokenPair(token)
      case None        =>
    }
  }

  def updateLastSync() = Preference.putLong("LastSynced", (new Date()).getTime)
  def getLastSync: Long = Preference.getLong("LastSynced").getOrElse(0)

  def startSync() = synchronized {
    ToastUtil.say(Util.getString(R.string.startSync))
    sendToMain(EventLogProducer.getStartSyncEventLog)

    val ht = new HandlerThread("syncthread")
    ht.start()
    val handler = new Handler(ht.getLooper)
    handler.post(new Runnable() {
      override def run(): Unit = {
        val last = getLastSync
        val files =
          FileSelector.select(LogFileWriter.logRoot, LogFileWriter.logFileNamePattern)
        val filesToUpload = files.filter( last < _.lastModified() )
        val results = filesToUpload.takeWhile(upload(_) == true)
        if (0 < filesToUpload.length && filesToUpload.length == results.length) {
          updateLastSync()
          ToastUtil.say(Util.getString(R.string.syncSuccess))
          sendToMain(EventLogProducer.getSucecssSyncEventLog)
        } else {
          ToastUtil.say(Util.getString(R.string.syncFail))
          sendToMain(EventLogProducer.getFailSyncEventLog)
        }
      }
    })
  }

  def sendToMain(log: String) = {
    val intent = (new Intent()).putExtra("type", DeviceType.Other.id).putExtra("log", log)
    App.getContext.sendBroadcast(intent.setAction(LoggerAction.updateRequest))
  }

  def upload(file :File): Boolean = synchronized {
    val inputStream = new FileInputStream(file)
    try {
      val entry = Api.putFileOverwrite(
        file.getAbsolutePath.replaceFirst(LogFileWriter.logRoot.getPath, ""),
        inputStream,
        file.length,
        null
      )
      return true
    } catch {
      case e: DropboxUnlinkedException => e.printStackTrace(); dropDropboxToken(); return false;
      case e: Exception => e.printStackTrace(); return false
    } finally {
      if (inputStream != null) inputStream.close()
    }
  }

}

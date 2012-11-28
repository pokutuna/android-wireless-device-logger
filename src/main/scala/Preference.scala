package com.pokutuna.n7logger

import _root_.android.content._
import _root_.android.content.SharedPreferences
import com.dropbox.client2.session.AccessTokenPair

object Preference {

  lazy val pref =
    App.getContext.getSharedPreferences(Logger.packageName, Context.MODE_PRIVATE)

  def getString(key: String): Option[String] = pref.getString(key, null) match {
    case value: String => Some(value)
    case _             => None
  }
  def putString(key: String, value: String) = pref.edit().putString(key, value).commit()
  def dropString(key: String) = pref.edit().remove(key).commit()

  def getLong(key: String): Option[Long] = pref.getLong(key, -1) match {
    case value if value != -1 => Some(value)
    case _                    => None
  }
  def putLong(key: String, value: Long) = pref.edit().putLong(key, value).commit()

}

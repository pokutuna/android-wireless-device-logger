package com.pokutuna.wdlogger.view

import _root_.android.app._
import _root_.android.content._
import _root_.android.view._
import _root_.android.widget._
import _root_.android.util.AttributeSet

class AutoScrollView(context: Context, attrs: AttributeSet)
  extends ScrollView(context, attrs) {

  def this(context: Context) = this(context, null)

  def scrollToTop()    = post(new Runnable() {
    override def run() = fullScroll(View.FOCUS_UP)
  })
  def scrollToBottom() = post(new Runnable() {
    override def run() = fullScroll(View.FOCUS_DOWN)
  })

 def isReachedBottom: Boolean = {
   if (getChildCount < 1) return false
   val diff = getChildAt(getChildCount - 1).getBottom - (getHeight + getScrollY)
   return if (diff <= 0) true else false
 }

  def scrollIfFullScrolled(func: => Unit): Boolean = {
    val reached = isReachedBottom
    try {
      func
    } finally {
      if (isReachedBottom) scrollToBottom()
    }
    return reached
  }
}

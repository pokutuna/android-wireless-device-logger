package com.pokutuna.wdlogger.view

import _root_.android.app._
import _root_.android.content._
import _root_.android.widget.ImageButton
import _root_.android.util.AttributeSet
import com.readystatesoftware.viewbadger._

class ImageButtonWithBadge(context: Context, attrs: AttributeSet)
  extends ImageButton(context, attrs) {

  def this(context: Context) = this(context, null)

  lazy val errBadge: BadgeView = {
    val b = new BadgeView(context, this)
    b.setText(" ! ")
    b
  }

  def showErrorBadge() = {
    errBadge.show
  }

  def hideErrorBadge() = {
    errBadge.hide
  }

}

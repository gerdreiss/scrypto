package com.jscriptive.scrypto

import android.content.{ClipData, ClipboardManager, Context}
import android.support.v7.app.AppCompatActivity
import android.graphics.drawable.Animatable
import android.os.Bundle
import android.text.{Editable, TextWatcher}
import android.util.Base64
import android.view.View
import android.widget.{EditText, Toast}

class MainActivity extends AppCompatActivity {
  // allows accessing `.value` on TR.resource.constants
  implicit val context = this

  override def onCreate(savedInstanceState: Bundle): Unit = {
    super.onCreate(savedInstanceState)
    // type ascription is required due to SCL-10491
    val vh: TypedViewHolder.main = TypedViewHolder.setContentView(this, TR.layout.main)
    vh.input.addTextChangedListener(new TextWatcher {
      override def beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int): Unit = {}

      override def afterTextChanged(s: Editable): Unit = {}

      override def onTextChanged(s: CharSequence, start: Int, before: Int, count: Int): Unit = {
        vh.output.setText(Base64.encodeToString(s.toString.getBytes("UTF-8"), Base64.DEFAULT))
      }
    })
    vh.image.getDrawable match {
      case a: Animatable => a.start()
      case _ =>
    }
  }

  def send(view: View): Unit = {
    val outputText: EditText = context.findViewById(R.id.output).asInstanceOf[EditText]
    if (outputText.length() == 0) {
      Toast.makeText(context, "Nothing to send", Toast.LENGTH_SHORT).show()
    } else {
      Toast.makeText(context, "This button does nothing yet...", Toast.LENGTH_SHORT).show()
    }
  }

  def copy(view: View): Unit = {
    val outputText: EditText = context.findViewById(R.id.output).asInstanceOf[EditText]
    if (outputText.length() == 0) {
      Toast.makeText(context, "Nothing to copy", Toast.LENGTH_SHORT).show()
    } else {
      Option(context.getSystemService(Context.CLIPBOARD_SERVICE)) match {
        case Some(manager: ClipboardManager) =>
          val clip = ClipData.newPlainText(outputText.getText.toString, outputText.getText.toString)
          manager.setPrimaryClip(clip)
          Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show()
        case _ =>
      }
    }
  }
}
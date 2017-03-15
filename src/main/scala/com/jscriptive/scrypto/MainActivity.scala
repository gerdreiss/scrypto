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
  implicit val context: MainActivity = this

  override def onCreate(savedInstanceState: Bundle): Unit = {
    super.onCreate(savedInstanceState)
    // type ascription is required due to SCL-10491
    val vh = TypedViewHolder.setContentView(this, TR.layout.main).asInstanceOf[TypedViewHolder.main]
    vh.input.addTextChangedListener(InputTextWatcher(vh.output))
    vh.image.getDrawable match {
      case a: Animatable => a.start()
      case _ =>
    }
  }

  def send(view: View): Unit = {
    val outputText = findViewById(R.id.output).asInstanceOf[EditText]
    if (outputText.length() == 0) {
      Toast.makeText(this, "Nothing to send", Toast.LENGTH_SHORT).show()
    } else {
      Toast.makeText(this, "This button does nothing yet...", Toast.LENGTH_SHORT).show()
    }
  }

  def copy(view: View): Unit = {
    val outputText = findViewById(R.id.output).asInstanceOf[EditText]
    if (outputText.length() == 0) {
      Toast.makeText(this, "Nothing to copy", Toast.LENGTH_SHORT).show()
    } else {
      Option(getSystemService(Context.CLIPBOARD_SERVICE)) match {
        case Some(manager: ClipboardManager) =>
          val clip = ClipData.newPlainText(outputText.getText.toString, outputText.getText.toString)
          manager.setPrimaryClip(clip)
          Toast.makeText(this, "Copied to clipboard", Toast.LENGTH_SHORT).show()
        case _ =>
      }
    }
  }
}

case class InputTextWatcher(output: EditText) extends TextWatcher {
  override def beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int): Unit = {}
  override def afterTextChanged(s: Editable): Unit = {}
  override def onTextChanged(s: CharSequence, start: Int, before: Int, count: Int): Unit = {
    output.setText(Base64.encodeToString(s.toString.getBytes("UTF-8"), Base64.DEFAULT))
  }
}
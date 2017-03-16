package com.jscriptive.scrypto

import android.content.ClipData.newPlainText
import android.content.{ClipboardManager, Context}
import android.graphics.drawable.Animatable
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.text.{Editable, TextWatcher}
import android.util.Base64
import android.view.View
import android.widget._

class MainActivity extends AppCompatActivity {
  // allows accessing `.value` on TR.resource.constants
  implicit val context: MainActivity = this

  class InputTextWatcher(output: EditText) extends TextWatcher {
    override def beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int): Unit = {}
    override def afterTextChanged(s: Editable): Unit = {}
    override def onTextChanged(s: CharSequence, start: Int, before: Int, count: Int): Unit = {
      output.setText(Base64.encodeToString(s.toString.getBytes("UTF-8"), Base64.DEFAULT))
    }
  }

  class InputHistoryListener(input: EditText) extends AdapterView.OnItemSelectedListener {
    override def onNothingSelected(parent: AdapterView[_ <: Adapter]): Unit = {}
    override def onItemSelected(parent: AdapterView[_ <: Adapter], view: View, position: Int, id: Long): Unit = {
      if (position > 0) {
        input.setText(parent.getItemAtPosition(position).toString)
      }
    }
  }

  override def onCreate(savedInstanceState: Bundle): Unit = {
    super.onCreate(savedInstanceState)
    // type ascription is required due to SCL-10491
    val vh = TypedViewHolder.setContentView(this, TR.layout.main).asInstanceOf[TypedViewHolder.main]
    vh.inputHistory.setOnItemSelectedListener(new InputHistoryListener(vh.input))
    vh.input.addTextChangedListener(new InputTextWatcher(vh.output))
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
          manager.setPrimaryClip(newPlainText(outputText.getText.toString, outputText.getText.toString))
          Toast.makeText(this, "Copied to clipboard", Toast.LENGTH_SHORT).show()
        case _ =>
      }
    }
  }
}
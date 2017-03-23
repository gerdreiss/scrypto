package com.jscriptive.scrypto

import android.content.ClipData.newPlainText
import android.content.{ClipboardManager, Context}
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.text.{Editable, TextWatcher}
import android.util.Base64
import android.view.View
import android.widget._

class MainActivity extends AppCompatActivity {
  // allows accessing `.value` on TR.resource.constants
  implicit val context: MainActivity = this

  private def encodeBase64(input: String): String = {
    Base64.encodeToString(input.getBytes("UTF-8"), Base64.DEFAULT)
  }

  private def encryptInput(input: String, encryption: String): String = {
    val digest = java.security.MessageDigest.getInstance("MD5")
    digest.reset()
    digest.update(input.getBytes("UTF-8"))
    new java.math.BigInteger(1, digest.digest()).toString(16)
  }

  private def updateOutput(input: CharSequence, encryptionMethodIndex: Int, output: EditText) = {
    encryptionMethodIndex match {
      case 0 => output.setText(encodeBase64(input.toString))
      case 1 => output.setText(encryptInput(input.toString, "MD5"))
      case 2 => output.setText(encryptInput(input.toString, "SHA-1"))
      case 3 => output.setText(encryptInput(input.toString, "SHA-256"))
      case _ =>
    }
  }

  class InputTextWatcher(encryptionMethods: Spinner, output: EditText) extends TextWatcher {
    override def beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int): Unit = {}
    override def afterTextChanged(s: Editable): Unit = {}
    override def onTextChanged(s: CharSequence, start: Int, before: Int, count: Int): Unit = {
      updateOutput(s, encryptionMethods.getSelectedItemPosition, output)
    }
  }

  class InputHistoryListener(input: EditText) extends AdapterView.OnItemSelectedListener {
    def onNothingSelected(parent: AdapterView[_]): Unit = {}
    def onItemSelected(parent: AdapterView[_], view: View, position: Int, id: Long): Unit = {
      if (position > 0) {
        input.setText(parent.getItemAtPosition(position).toString)
        parent.setSelection(0)
      }
    }
  }

  class EncryptionMethodListener(input: EditText, output: EditText) extends AdapterView.OnItemSelectedListener {
    def onNothingSelected(parent: AdapterView[_]): Unit = {}
    def onItemSelected(parent: AdapterView[_], view: View, position: Int, id: Long): Unit = {
      if (input.getText.length > 0) {
        updateOutput(input.getText.toString, position, output)
      }
    }
  }

  override def onCreate(savedInstanceState: Bundle): Unit = {
    super.onCreate(savedInstanceState)
    // type ascription is required due to SCL-10491
    val vh = TypedViewHolder.setContentView(this, TR.layout.main).asInstanceOf[TypedViewHolder.main]
    vh.inputHistory.setOnItemSelectedListener(new InputHistoryListener(vh.input))
    vh.input.addTextChangedListener(new InputTextWatcher(vh.encryptionMethods, vh.output))
    vh.encryptionMethods.setOnItemSelectedListener(new EncryptionMethodListener(vh.input, vh.output))
  }

  def share(view: View): Unit = {
    val outputText = findViewById(R.id.output).asInstanceOf[EditText]
    if (outputText.length() == 0) {
      Toast.makeText(this, "Nothing to share", Toast.LENGTH_SHORT).show()
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
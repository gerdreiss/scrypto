package com.jscriptive.scrypto

import java.io._

import android.content.ClipData.newPlainText
import android.content.{ActivityNotFoundException, ClipboardManager, Context, Intent}
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.text.{Editable, TextWatcher}
import android.util.Base64
import android.view.View
import android.widget.{Toast, _}

class MainActivity extends AppCompatActivity {
  // allows accessing `.value` on TR.resource.constants
  implicit val context = this
  val FILE = "scrypto.history"

  private def encodeBase64(input: String): String = {
    Base64.encodeToString(input.getBytes("UTF-8"), Base64.DEFAULT)
  }

  private def encryptInput(input: String, encryption: String): String = {
    val digest = java.security.MessageDigest.getInstance(encryption)
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
    //vh.inputHistory.setAdapter(new ArrayAdapter[String](context, vh.inputHistory.getId, readHistory(FILE).toArray))
    vh.inputHistory.setOnItemSelectedListener(new InputHistoryListener(vh.input))
    vh.input.addTextChangedListener(new InputTextWatcher(vh.encryptionMethods, vh.output))
    vh.encryptionMethods.setOnItemSelectedListener(new EncryptionMethodListener(vh.input, vh.output))
  }

  def share(view: View): Unit = {
    val outputText: EditText = findViewById(R.id.output).asInstanceOf[EditText]
    if (outputText.length() == 0) {
      Toast.makeText(this, "Nothing to share", Toast.LENGTH_SHORT).show()
    } else {
      val sendIntent = new Intent(Intent.ACTION_SEND)
      sendIntent.setType("message/rfc822")
      sendIntent.putExtra(Intent.EXTRA_EMAIL, "recipient@example.com")
      sendIntent.putExtra(Intent.EXTRA_SUBJECT, "Scrypto")
      sendIntent.putExtra(Intent.EXTRA_TEXT, outputText.getText.toString)
      try {
          startActivity(Intent.createChooser(sendIntent, "Share..."))
          saveHistory(outputText.getText.toString)
      } catch {
        case _: ActivityNotFoundException =>
          Toast.makeText(this, "There are no email clients installed.", Toast.LENGTH_SHORT).show()
      }
    }
  }

  def copy(view: View): Unit = {
    val outputText: EditText = findViewById(R.id.output).asInstanceOf[EditText]
    if (outputText.length() == 0) {
      Toast.makeText(this, "Nothing to copy", Toast.LENGTH_SHORT).show()
    } else {
      Option(getSystemService(Context.CLIPBOARD_SERVICE)) match {
        case Some(manager: ClipboardManager) =>
          manager.setPrimaryClip(newPlainText(outputText.getText.toString, outputText.getText.toString))
          Toast.makeText(this, "Copied to clipboard", Toast.LENGTH_SHORT).show()
          saveHistory(outputText.getText.toString)
        case _ =>
      }
    }
  }

  private def saveHistory(input: String): Unit = {
    val lines: List[String] = readHistory(FILE)
    val history = input :: lines.filterNot(_ == input)
    saveNewHistory(FILE, history)
  }

  private def readHistory(filename: String): List[String] = {
    val file = context.getFileStreamPath(filename)
    if (file.exists()) {
      val fileInput = openFileInput(filename)
      val reader = new BufferedReader(new InputStreamReader(fileInput))
      val lines = readLines(reader)
      reader.close()
      lines
    } else Nil
  }

  private def readLines(reader: BufferedReader): List[String] = {
    val line = reader.readLine()
    if (line != null) line :: readLines(reader)
    else Nil
  }

  private def saveNewHistory(filename: String, history: List[String]) = {
    val fos = openFileOutput(filename, Context.MODE_PRIVATE)
    fos.write(history.mkString("\n").getBytes("UTF-8"))
    fos.close()
  }
}

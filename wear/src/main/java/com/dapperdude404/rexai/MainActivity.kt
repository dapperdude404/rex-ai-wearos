package com.dapperdude404.rexai
import android.app.Activity
import android.os.Bundle
import android.content.Intent
import android.speech.RecognizerIntent
import android.speech.tts.TextToSpeech
import android.widget.*
import android.view.Gravity
import android.graphics.Color
import org.json.JSONObject
import java.net.URL
import javax.net.ssl.HttpsURLConnection
class MainActivity : Activity() {
 private lateinit var answer: TextView
 private lateinit var prompt: EditText
 private lateinit var prefs: android.content.SharedPreferences
 private var tts: TextToSpeech? = null
 private var ready = false
 private var busy = false
 override fun onCreate(savedInstanceState: Bundle?) {
 super.onCreate(savedInstanceState)
 prefs = getSharedPreferences("rex", MODE_PRIVATE)
 tts = TextToSpeech(this) { ready = it == TextToSpeech.SUCCESS }
 val scroll = ScrollView(this)
 val layout = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; gravity = Gravity.CENTER_HORIZONTAL; setPadding(24,30,24,30); setBackgroundColor(Color.BLACK) }
 scroll.addView(layout); setContentView(scroll)
 layout.addView(ImageView(this).apply { setImageResource(R.drawable.rex); layoutParams = LinearLayout.LayoutParams(90,90) })
 layout.addView(TextView(this).apply { text = "Rex AI"; textSize = 22f; setTextColor(0xff69df91.toInt()) })
 prompt = EditText(this).apply { hint = "Ask Rex"; setSingleLine(false) }; layout.addView(prompt)
 fun button(label: String, action: () -> Unit) { layout.addView(Button(this).apply { text = label; setOnClickListener { action() } }) }
 button("Speak") { try { startActivityForResult(Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM), 10) } catch(e: Exception) { answer.text = "Voice input unavailable. Type instead." } }
 button("Ask Rex") { ask() }
 answer = TextView(this).apply { text = prefs.getString("last", "Hello! Configure your AI server in Settings."); textSize = 16f; setTextColor(Color.WHITE) }; layout.addView(answer)
 button("Read aloud") { if(ready) tts?.speak(answer.text.toString(), TextToSpeech.QUEUE_FLUSH, null, "rex") }
 button("Settings") { val input = EditText(this).apply { hint = "https://your-server/chat"; setText(prefs.getString("endpoint","")) }; android.app.AlertDialog.Builder(this).setTitle("AI server URL").setView(input).setPositiveButton("Save") { _,_ -> prefs.edit().putString("endpoint",input.text.toString().trim()).apply() }.setNegativeButton("Cancel",null).show() }
 button("Clear history") { prefs.edit().remove("last").apply(); answer.text = "History cleared." }
 }
 override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) { super.onActivityResult(requestCode,resultCode,data); if(requestCode == 10 && resultCode == RESULT_OK) { prompt.setText(data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.firstOrNull().orEmpty()); ask() } }
 private fun ask() {
 val question = prompt.text.toString().trim(); if(question.isEmpty() || busy) return
 val endpoint = prefs.getString("endpoint","").orEmpty()
 if(!endpoint.startsWith("https://")) { answer.text = "Set an HTTPS AI server in Settings."; return }
 busy = true; answer.text = "Rex is thinking…"
 Thread {
 val result = try {
 val connection = URL(endpoint).openConnection() as HttpsURLConnection
 try { connection.instanceFollowRedirects = false; connection.requestMethod = "POST"; connection.connectTimeout = 15000; connection.readTimeout = 45000; connection.doOutput = true; connection.setRequestProperty("Content-Type","application/json"); connection.outputStream.use { it.write(JSONObject().put("message",question).toString().toByteArray(Charsets.UTF_8)) }; if(connection.responseCode != 200) error("Server error"); JSONObject(connection.inputStream.bufferedReader().use { it.readText() }).getString("reply") } finally { connection.disconnect() }
 } catch(e: Exception) { "Unable to reach Rex server. Check settings and connection." }
 runOnUiThread { busy = false; if(!isDestroyed) { answer.text = result; prefs.edit().putString("last",result).apply() } }
 }.start()
 }
 override fun onDestroy() { tts?.shutdown(); super.onDestroy() }
}
package com.noxstore.airtelupi

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.widget.*
import androidx.activity.ComponentActivity

class MainActivity : ComponentActivity() {
    private val prefs by lazy { getSharedPreferences("config", MODE_PRIVATE) }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val root = LinearLayout(this).apply { orientation=LinearLayout.VERTICAL; setPadding(32,32,32,32) }
        val endpoint = EditText(this).apply {
            hint="Bot API URL"
            setText(prefs.getString("endpoint","https://agni.ender.co.in:45850/api/upi/payment"))
        }
        val secret = EditText(this).apply {
            hint="Webhook secret"; setText(prefs.getString("secret",""))
        }
        val save=Button(this).apply{text="Save configuration"}
        val access=Button(this).apply{text="Enable notification access"}
        val status=TextView(this).apply{text="Airtel UPI Listener"}
        save.setOnClickListener {
            prefs.edit().putString("endpoint",endpoint.text.toString().trim())
                .putString("secret",secret.text.toString()).apply()
            status.text="Configuration saved."
        }
        access.setOnClickListener { startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)) }
        root.addView(status); root.addView(endpoint); root.addView(secret); root.addView(save); root.addView(access)
        setContentView(root)
    }
}

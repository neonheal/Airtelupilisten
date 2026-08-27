package com.noxstore.airtelupi

import android.app.Notification
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import kotlinx.coroutines.*
import java.net.HttpURLConnection
import java.net.URL

class PaymentNotificationListener : NotificationListenerService() {
    private val scope=CoroutineScope(SupervisorJob()+Dispatchers.IO)

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        val extras=sbn.notification.extras
        val text=listOf(
            extras.getCharSequence(Notification.EXTRA_TITLE)?.toString(),
            extras.getCharSequence(Notification.EXTRA_TEXT)?.toString(),
            extras.getCharSequence(Notification.EXTRA_BIG_TEXT)?.toString()
        ).filterNotNull().joinToString(" ").trim()
        if(text.isBlank()) return

        val lower=text.lowercase()
        if(!(lower.contains("airtel") || lower.contains("airtel payments"))) return
        if(!(lower.contains("received") || lower.contains("credited") || lower.contains("credit"))) return

        val amount=Regex("""(?:rs\.?|₹)\s*([0-9,]+(?:\.[0-9]{1,2})?)""",RegexOption.IGNORE_CASE)
            .find(text)?.groupValues?.get(1)?.replace(",","")?.toDoubleOrNull() ?: return
        val txid=Regex("""(?:txn(?:\s*id)?|transaction\s*(?:id|no)|utr|upi\s*ref(?:erence)?)[\s:#-]*([A-Za-z0-9]{6,})""",
            RegexOption.IGNORE_CASE).find(text)?.groupValues?.get(1) ?: return
        val sender=Regex("""(?:from|by)\s+([A-Za-z][A-Za-z .'-]{1,60})""",
            RegexOption.IGNORE_CASE).find(text)?.groupValues?.get(1)?.trim() ?: ""

        scope.launch { postPayment(amount,txid,sender) }
    }

    private fun postPayment(amount:Double,txid:String,sender:String) {
        val p=getSharedPreferences("config",MODE_PRIVATE)
        val endpoint=p.getString("endpoint","") ?: return
        val secret=p.getString("secret","") ?: return
        if(endpoint.isBlank() || secret.isBlank()) return
        val body="""{"method":"upi","amount":$amount,"txid":${json(txid)},"sender":${json(sender)}}"""
        try {
            val c=URL(endpoint).openConnection() as HttpURLConnection
            c.requestMethod="POST"; c.connectTimeout=10000; c.readTimeout=15000; c.doOutput=true
            c.setRequestProperty("Content-Type","application/json")
            c.setRequestProperty("Authorization","Bearer $secret")
            c.outputStream.use{it.write(body.toByteArray(Charsets.UTF_8))}
            c.inputStream.close(); c.disconnect()
        } catch(_:Exception) { /* add persistent queue/retry in production */ }
    }
    private fun json(s:String)=""""${s.replace("\","\\").replace(""","\"")}""""
    override fun onDestroy(){scope.cancel();super.onDestroy()}
}

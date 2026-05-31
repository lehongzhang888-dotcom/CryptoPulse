package com.cryptopulse

import android.annotation.SuppressLint
import android.os.Bundle
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import com.google.gson.Gson
import java.io.IOException
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {

    private lateinit var webView: WebView
    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()
    private val gson = Gson()

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        webView = WebView(this).apply {
            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true
            settings.allowFileAccess = true
            settings.allowContentAccess = true
            settings.setSupportZoom(false)
            settings.builtInZoomControls = false
            settings.displayZoomControls = false
            settings.loadWithOverviewMode = true
            settings.useWideViewPort = true
            webViewClient = object : WebViewClient() {
                override fun onPageFinished(view: WebView?, url: String?) {
                    super.onPageFinished(view, url)
                }
            }
            webChromeClient = WebChromeClient()
            addJavascriptInterface(NativeAPI(), "NativeAPI")
        }
        setContentView(webView)
        webView.loadUrl("file:///android_asset/index.html")
    }

    inner class NativeAPI {
        @JavascriptInterface
        fun rawFetch(url: String): String {
            return try {
                val request = Request.Builder().url(url)
                    .header("User-Agent", "Mozilla/5.0")
                    .build()
                val response = client.newCall(request).execute()
                response.body?.string() ?: "{}"
            } catch (e: Exception) {
                "{\"error\":\"${e.message}\"}"
            }
        }

        @JavascriptInterface
        fun aiChat(prompt: String): String {
            return try {
                val body = mapOf(
                    "model" to "deepseek-chat",
                    "messages" to listOf(mapOf("role" to "user", "content" to prompt)),
                    "max_tokens" to 500,
                    "temperature" to 0.7
                )
                val json = gson.toJson(body)
                val requestBody = json.toRequestBody("application/json".toMediaType())

                val request = Request.Builder()
                    .url("https://api.deepseek.com/chat/completions")
                    .header("Authorization", "Bearer sk-placeholder")
                    .header("Content-Type", "application/json")
                    .post(requestBody)
                    .build()

                val response = client.newCall(request).execute()
                val respBody = response.body?.string() ?: "{}"
                // Extract content from DeepSeek response
                val respMap = gson.fromJson(respBody, Map::class.java) as? Map<*, *>
                val choices = respMap?.get("choices") as? List<*>
                val first = choices?.firstOrNull() as? Map<*, *>
                val message = first?.get("message") as? Map<*, *>
                message?.get("content") as? String ?: "AI响应解析失败"
            } catch (e: Exception) {
                "AI调用失败：${e.message}"
            }
        }

        @JavascriptInterface
        fun showToast(msg: String) {
            runOnUiThread {
                Toast.makeText(this@MainActivity, msg, Toast.LENGTH_SHORT).show()
            }
        }
    }
}

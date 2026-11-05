package com.amhetcnaa.fluxion

import android.annotation.SuppressLint
import android.app.Activity
import android.app.DownloadManager
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.view.WindowInsetsController
import android.view.WindowManager
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast

class MainActivity : Activity() {

    private lateinit var webView: WebView
    private var sharedUrl: String = ""

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        try {
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.statusBarColor = Color.parseColor("#09090b")
            window.navigationBarColor = Color.parseColor("#09090b")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                window.insetsController?.setSystemBarsAppearance(
                    0,
                    WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS
                )
            }
        } catch (ignored: Throwable) {}

        try {
            handleIncomingIntent(intent)

            webView = WebView(this).apply {
                setBackgroundColor(Color.parseColor("#09090b"))
                settings.apply {
                    javaScriptEnabled = true
                    domStorageEnabled = true
                    allowFileAccess = true
                    allowContentAccess = true
                    loadWithOverviewMode = true
                    useWideViewPort = true
                    cacheMode = WebSettings.LOAD_DEFAULT
                }

                webViewClient = object : WebViewClient() {
                    override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                        val url = request?.url?.toString() ?: return false
                        if (url.startsWith("file:///android_asset/")) {
                            return false
                        }
                        return try {
                            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                            true
                        } catch (e: Exception) {
                            false
                        }
                    }

                    override fun onPageFinished(view: WebView?, url: String?) {
                        super.onPageFinished(view, url)
                        if (sharedUrl.isNotEmpty()) {
                            evaluateJs("if (window.onSharedUrlReceived) { window.onSharedUrlReceived('${sharedUrl}'); }")
                        }
                    }
                }

                webChromeClient = WebChromeClient()
                addJavascriptInterface(AndroidBridge(this@MainActivity), "Android")
                loadUrl("file:///android_asset/index.html")
            }

            setContentView(webView)
        } catch (e: Throwable) {
            e.printStackTrace()
            Toast.makeText(this, "Fluxion error: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        intent?.let {
            handleIncomingIntent(it)
            if (sharedUrl.isNotEmpty() && this::webView.isInitialized) {
                webView.evaluateJs("if (window.onSharedUrlReceived) { window.onSharedUrlReceived('${sharedUrl}'); }")
            }
        }
    }

    private fun handleIncomingIntent(intent: Intent?) {
        if (intent == null) return
        if (intent.action == Intent.ACTION_SEND && intent.type == "text/plain") {
            val text = intent.getStringExtra(Intent.EXTRA_TEXT) ?: ""
            val urlRegex = Regex("""https?://[^\s]+""")
            val match = urlRegex.find(text)
            sharedUrl = match?.value ?: text.trim()
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        if (this::webView.isInitialized && webView.canGoBack()) {
            webView.goBack()
        } else {
            finish()
        }
    }

    inner class AndroidBridge(private val context: Context) {

        @JavascriptInterface
        fun getClipboard(): String {
            return try {
                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val clip = clipboard.primaryClip
                if (clip != null && clip.itemCount > 0) {
                    clip.getItemAt(0).coerceToText(context).toString()
                } else {
                    ""
                }
            } catch (e: Exception) {
                ""
            }
        }

        @JavascriptInterface
        fun get_clipboard(): String = getClipboard()

        @JavascriptInterface
        fun getSharedUrl(): String {
            return sharedUrl
        }

        @JavascriptInterface
        fun get_download_dir(): String {
            return "/storage/emulated/0/Download/Fluxion"
        }

        @JavascriptInterface
        fun select_directory(): String {
            return "/storage/emulated/0/Download/Fluxion"
        }

        @JavascriptInterface
        fun open_download_folder() {
            try {
                val intent = Intent(DownloadManager.ACTION_VIEW_DOWNLOADS).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(intent)
            } catch (e: Exception) {
                showToast("Download directory: /storage/emulated/0/Download/Fluxion")
            }
        }

        @JavascriptInterface
        fun start_download(url: String, format: String, customDir: String?): String {
            runOnUiThread {
                triggerDownload(url, format)
            }
            return """{"status":"downloading","title":"Fluxion Stream"}"""
        }

        @JavascriptInterface
        fun showToast(message: String) {
            runOnUiThread {
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
            }
        }

        private fun triggerDownload(url: String, format: String) {
            try {
                val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
                val uri = Uri.parse(url)
                val ext = if (format == "mp3") "mp3" else "mp4"
                val filename = "Fluxion_${System.currentTimeMillis()}.$ext"

                val request = DownloadManager.Request(uri).apply {
                    setTitle(filename)
                    setDescription("Fluxion Media Engine ($format)")
                    setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                    setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "Fluxion/$filename")
                    setAllowedOverMetered(true)
                    setAllowedOverRoaming(true)
                }
                downloadManager.enqueue(request)
                showToast("Download queued to Downloads/Fluxion")
            } catch (e: Exception) {
                showToast("Stream request sent: $url")
            }
        }
    }

    private fun WebView.evaluateJs(script: String) {
        post {
            evaluateJavascript(script, null)
        }
    }
}

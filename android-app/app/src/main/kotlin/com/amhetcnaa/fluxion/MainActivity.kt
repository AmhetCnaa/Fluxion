package com.amhetcnaa.fluxion

import android.annotation.SuppressLint
import android.app.Activity
import android.app.DownloadManager
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.media.MediaScannerConnection
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
import com.yausername.ffmpeg.FFmpeg
import com.yausername.youtubedl_android.YoutubeDL
import com.yausername.youtubedl_android.YoutubeDLRequest
import org.json.JSONObject
import java.io.File

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

        // Initialize YoutubeDL and FFmpeg asynchronously in background
        Thread {
            try {
                YoutubeDL.getInstance().init(applicationContext)
                FFmpeg.getInstance().init(applicationContext)
            } catch (e: Throwable) {
                e.printStackTrace()
            }
        }.start()

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
            val publicDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            return File(publicDir, "Fluxion").absolutePath
        }

        @JavascriptInterface
        fun select_directory(): String {
            val dir = get_download_dir()
            showToast("Saved to: $dir")
            return dir
        }

        @JavascriptInterface
        fun open_download_folder() {
            runOnUiThread {
                try {
                    val intent = Intent(DownloadManager.ACTION_VIEW_DOWNLOADS).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                    context.startActivity(intent)
                } catch (e: Exception) {
                    try {
                        val viewIntent = Intent(Intent.ACTION_VIEW).apply {
                            setDataAndType(Uri.parse(get_download_dir()), "*/*")
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        }
                        context.startActivity(viewIntent)
                    } catch (ex: Exception) {
                        showToast("Files saved to Downloads/Fluxion")
                    }
                }
            }
        }

        @JavascriptInterface
        fun get_info(url: String): String {
            return try {
                val info = YoutubeDL.getInstance().getInfo(url)
                val json = JSONObject().apply {
                    put("title", info.title ?: "Media Stream")
                    put("uploader", info.uploader ?: "Creator")
                    put("duration", info.duration)
                    put("thumbnail", info.thumbnail ?: "")
                }
                json.toString()
            } catch (e: Exception) {
                JSONObject().apply {
                    put("error", e.message ?: "Could not resolve metadata")
                }.toString()
            }
        }

        @JavascriptInterface
        fun start_download(url: String, format: String, customDir: String?): String {
            Thread {
                try {
                    val publicDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                    val downloadFolder = File(publicDir, "Fluxion").takeIf { it.exists() || it.mkdirs() }
                        ?: File(context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), "Fluxion").apply { mkdirs() }

                    val request = YoutubeDLRequest(url).apply {
                        addOption("--no-mtime")
                        addOption("-o", "${downloadFolder.absolutePath}/%(title)s.%(ext)s")
                        if (format.equals("mp3", ignoreCase = true)) {
                            addOption("-x")
                            addOption("--audio-format", "mp3")
                            addOption("--audio-quality", "0")
                        } else {
                            addOption("-f", "bestvideo[ext=mp4]+bestaudio[ext=m4a]/best[ext=mp4]/best")
                        }
                    }

                    var lastReport = 0L
                    YoutubeDL.getInstance().execute(request) { progress, etaInSeconds, line ->
                        val now = System.currentTimeMillis()
                        if (now - lastReport > 200 || progress >= 100f) {
                            lastReport = now
                            val pct = progress.coerceIn(0f, 100f)
                            runOnUiThread {
                                webView.evaluateJs(
                                    "if (window.onProgress) { window.onProgress({status: 'downloading', percent: $pct, eta: $etaInSeconds}); }"
                                )
                            }
                        }
                    }

                    // Index newly downloaded files into MediaStore so they instantly appear in Gallery
                    val latestFile = downloadFolder.listFiles()?.maxByOrNull { it.lastModified() }
                    val finalTitle = latestFile?.name ?: "Fluxion_${System.currentTimeMillis()}"

                    if (latestFile != null && latestFile.exists()) {
                        val mimeType = if (format.equals("mp3", ignoreCase = true)) "audio/mpeg" else "video/mp4"
                        MediaScannerConnection.scanFile(
                            context,
                            arrayOf(latestFile.absolutePath),
                            arrayOf(mimeType),
                            null
                        )
                    }

                    val titleJson = JSONObject.quote(finalTitle)
                    runOnUiThread {
                        webView.evaluateJs(
                            "if (window.onDownloadFinished) { window.onDownloadFinished({title: $titleJson}); }"
                        )
                        showToast("Saved to Downloads/Fluxion")
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    val errorMsg = JSONObject.quote(e.message ?: "Download failed")
                    runOnUiThread {
                        webView.evaluateJs(
                            "if (window.onDownloadError) { window.onDownloadError($errorMsg); }"
                        )
                        showToast("Error: ${e.message}")
                    }
                }
            }.start()

            return """{"status":"started"}"""
        }

        @JavascriptInterface
        fun showToast(message: String) {
            runOnUiThread {
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun WebView.evaluateJs(script: String) {
        post {
            evaluateJavascript(script, null)
        }
    }
}

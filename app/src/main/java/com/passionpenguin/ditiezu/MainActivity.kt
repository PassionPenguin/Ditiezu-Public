package com.passionpenguin.ditiezu

import android.Manifest
import android.annotation.TargetApi
import android.app.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.webkit.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.content.FileProvider
import java.io.*
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.nio.charset.Charset
import java.util.concurrent.TimeUnit


class MainActivity : AppCompatActivity() {
    var downloadList: ArrayList<Long> = ArrayList()
    private var uploadMessage: ValueCallback<Uri>? = null
    private var uploadMessageAboveL: ValueCallback<Array<Uri>>? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        registerReceiver(
            onComplete,
            IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE)
        );

        val isDarkMode =
            this.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK === Configuration.UI_MODE_NIGHT_YES
        val webView = findViewById<WebView>(R.id.webview)
        fun webViewAlert(msg: String, id: String, occur: String) {
            webView.post(Runnable {
                webView.evaluateJavascript("pg.alert('$msg','$id','$occur')", null)
            })
        }

        class WebAppInterface(private val mContext: Context) {
            @JavascriptInterface
            fun storeStringPref(name: String, string: String) {
                val sharedPref = this@MainActivity.getPreferences(Context.MODE_PRIVATE) ?: return
                with(sharedPref.edit()) {
                    if (name == "shouldOverrideAds") {
                        if (string == "0")
                            putString(
                                "shouldOverrideAds",
                                (System.currentTimeMillis() + TimeUnit.DAYS.toMillis(3650)).toString()
                            )
                        else
                            putString(
                                "shouldOverrideAds",
                                (System.currentTimeMillis() + TimeUnit.DAYS.toMillis(string.toLong())).toString()
                            )
                    } else
                        putString(name, string)
                    commit()
                }
            }

            @JavascriptInterface
            fun getPref(name: String): String {
                val sharedPref = this@MainActivity.getPreferences(Context.MODE_PRIVATE)
                if (name == "shouldOverrideAds") {
                    return TimeUnit.MILLISECONDS.toDays(
                        sharedPref.getString(
                            "shouldOverrideAds",
                            "-1"
                        )!!.toLong()
                    ).toString()
                }
                if (name === "language") return sharedPref.getString("language", "CHS").toString()
                return sharedPref.getString(name, "").toString()
            }

            @JavascriptInterface
            fun isAdsBlocked(): Boolean {
                val sharedPref = this@MainActivity.getPreferences(Context.MODE_PRIVATE)
                if (sharedPref.getString("shouldOverrideAds", "-1").toString() != "-1") {
                    return sharedPref.getString(
                        "shouldOverrideAds",
                        "-1"
                    )!!.toLong() > TimeUnit.MILLISECONDS.toDays(System.currentTimeMillis())
                }
                return false
            }

            @JavascriptInterface
            fun sharedPreferencesRemove() {
                val sharedPref = this@MainActivity.getPreferences(Context.MODE_PRIVATE) ?: return
                with(sharedPref.edit()) {
                    clear()
                    commit()
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
                    CookieManager.getInstance().removeAllCookies(null)
                    CookieManager.getInstance().flush()
                } else {
                    val cookieSyncMngr =
                        CookieSyncManager.createInstance(this@MainActivity)
                    cookieSyncMngr.startSync()
                    val cookieManager = CookieManager.getInstance()
                    cookieManager.removeAllCookie()
                    cookieManager.removeSessionCookie()
                    cookieSyncMngr.stopSync()
                    cookieSyncMngr.sync()
                }
            }

            @JavascriptInterface
            fun XHR(url: String): String? {
                val cookieManager: CookieManager = CookieManager.getInstance()
                var targetUrl = url;
                targetUrl += if (targetUrl.contains('?')) {
                    "&mobile=no"
                } else "?mobile=no"
                val threadUrl =
                    URL(targetUrl)
                var resStr: String? = null
                var threadEnd = false
                val connThread = Thread {
                    val urlConnection =
                        threadUrl.openConnection() as HttpURLConnection
                    urlConnection.setRequestProperty(
                        "Cookie",
                        cookieManager.getCookie(threadUrl.toString())
                    )
                    urlConnection.requestMethod = "GET"
                    urlConnection.setRequestProperty(
                        "User-Agent",
                        "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/80.0.3987.132 Safari/537.36"
                    )
                    try {
                        val inputStream: InputStream = urlConnection.inputStream
                        val reader = InputStreamReader(inputStream, "GBK")
                        var str = reader.readText()
                        var res = ""
                        while (str != "") {
                            res += str
                            str = reader.readText()
                        }
                        resStr = res
                    } catch (e: Exception) {
                        threadEnd = true
                        resStr = "!null"
                    } finally {
                        urlConnection.disconnect()
                        if (resStr == null || resStr == "null")
                            resStr = "!null"
                        threadEnd = true
                    }
                    val headerFields = urlConnection.headerFields
                    val cookiesHeader = headerFields["Set-Cookie"]
                    if (cookiesHeader != null) {
                        for (cookie in cookiesHeader) {
                            Log.i("Cookie:", cookie)
                            cookieManager.setCookie("http://ditiezu.com", cookie)
                        }
                    }
                    CookieSyncManager.getInstance().sync()
                }
                connThread.start()
                connThread.join()
                while (!threadEnd && resStr != null && resStr != "null") {
                    Log.i(threadEnd.toString(), resStr!!)
                    continue
                }
                return resStr
            }

            @JavascriptInterface
            fun XHRPost(
                type: String,
                message: String,
                subject: String,
                tid: String,
                pid: String,
                formhash: String
            ): String? {
                val cookieManager: CookieManager = CookieManager.getInstance()
                val targetUrl: String;
                if (type == "reply")
                    targetUrl =
                        "http://www.ditiezu.com/forum.php?mod=post&action=reply&tid=$tid&replysubmit=yes"
                else return ""
                val threadUrl =
                    URL(targetUrl)
                var resStr: String? = null
                var threadEnd = false
                val connThread = Thread {
                    val urlConnection =
                        threadUrl.openConnection() as HttpURLConnection
                    urlConnection.setRequestProperty(
                        "Cookie",
                        cookieManager.getCookie(threadUrl.toString())
                    )

                    Log.e("Log", cookieManager.getCookie(threadUrl.toString()))

                    urlConnection.requestMethod = "POST"
                    urlConnection.setRequestProperty(
                        "Referer",
                        "http://www.ditiezu.com"
                    )
                    urlConnection.setRequestProperty(
                        "Content-Type",
                        "application/x-www-form-urlencoded"
                    )
                    val form =
                        ("message=" + URLEncoder.encode(
                            message,
                            "UTF-8"
                        ) + "&subject=" + URLEncoder.encode(
                            subject,
                            "UTF-8"
                        ) + "&formhash=" + URLEncoder.encode(formhash, "UTF-8")).toString()
                            .toByteArray(Charset.forName("UTF-8"))
                    urlConnection.setFixedLengthStreamingMode(form.size)
                    try {
                        val os: OutputStream = urlConnection.outputStream
                        os.write(form)
                    } catch (e: java.lang.Exception) {

                    }

                    try {
                        val inputStream: InputStream = urlConnection.inputStream
                        val reader = InputStreamReader(inputStream, "GBK")
                        var str = reader.readText()
                        var res = ""
                        while (str != "") {
                            res += str
                            str = reader.readText()
                        }
                        resStr = res
                    } catch (e: Exception) {
                        threadEnd = true
                        resStr = "!null"
                    } finally {
                        urlConnection.disconnect()
                        if (resStr == null || resStr == "null")
                            resStr = "!null"
                        threadEnd = true
                    }
                }
                connThread.start()
                connThread.join()
                while (!threadEnd && resStr != null && resStr != "null") {
                    Log.i(threadEnd.toString(), resStr!!)
                    continue
                }
                return resStr
            }

            @JavascriptInterface
            fun downloadFile(url: String, fileName: String) {
                if (ActivityCompat.checkSelfPermission(
                        this@MainActivity,
                        Manifest.permission.READ_EXTERNAL_STORAGE
                    ) != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(
                        this@MainActivity,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    webViewAlert(
                        "无权限读取手机内存\n确认权限后请再次点击",
                        "ERR_NO_PERMISSION_RW_EXT_STORAGE",
                        "WebAppInterface"
                    )
                    // this will request for permission when user has not granted permission for the app
                    ActivityCompat.requestPermissions(
                        this@MainActivity,
                        arrayOf(
                            Manifest.permission.READ_EXTERNAL_STORAGE,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE
                        ),
                        1
                    )
                } else {
                    //Download Script
                    val downloadManager: DownloadManager =
                        getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
                    val uri = Uri.parse(url)
                    val cookieManager: CookieManager = CookieManager.getInstance()
                    val request: DownloadManager.Request = DownloadManager.Request(uri)
                    request.addRequestHeader(
                            "Cookie",
                            cookieManager.getCookie("http://www.ditiezu.com")
                        )
                        .setVisibleInDownloadsUi(true)
                        .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                        .setDestinationInExternalPublicDir(
                            Environment.DIRECTORY_DOWNLOADS,
                            uri.lastPathSegment
                        )
                        .setDestinationInExternalPublicDir(
                            Environment.DIRECTORY_DOWNLOADS,
                            fileName
                        )
                    val refId = downloadManager.enqueue(request)
                    downloadList.add(refId)

                    val manager =
                        getSystemService(NOTIFICATION_SERVICE) as NotificationManager

                    val channelId = "ditiezu"
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        val channel = NotificationChannel(
                            channelId,
                            "ditiezu",
                            NotificationManager.IMPORTANCE_DEFAULT
                        )
                        manager.createNotificationChannel(channel)
                    }

                    val notification: Notification =
                        NotificationCompat.Builder(this@MainActivity, channelId)
                            .setContentTitle("PassionPenguin | 下载管理")
                            .setContentText("开始下载地址$url\n文件名: $fileName")
                            .setWhen(System.currentTimeMillis())
                            .setSmallIcon(R.mipmap.ic_launcher_round)
                            .setPriority(NotificationCompat.PRIORITY_MAX)
                            .setDefaults(NotificationCompat.DEFAULT_SOUND)
                            .setTicker("开始下载")
                            .setAutoCancel(true)
                            .setWhen(System.currentTimeMillis())
                            .setProgress(0, 0, true)
                            .build()
                    manager.notify(1, notification)
                    this@MainActivity.registerReceiver(
                        onComplete,
                        IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE)
                    );
                }
            }

            @JavascriptInterface
            fun changeToMobileUA() {
                webView.post(Runnable {
                    webView.settings.userAgentString =
                        "Mozilla/5.0 (Phone iOS/Android WebViewKit) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/80.0.3987.132 Mobile Safari/537.36"
                })
            }

            @JavascriptInterface
            fun openLoginWindow() {
                webView.post(Runnable {
                    val url =
                        "http://www.ditiezu.com/member.php?mod=logging&action=login&mobile=yes&transfered";
                    val extraHeaders: HashMap<String, String> = HashMap();
                    extraHeaders["Referer"] =
                        "http://www.ditiezu.com/member.php?mod=logging&action=login&mobile=yes";
                    webView.loadUrl(url + "&transfered", extraHeaders);
                })
            }

            @JavascriptInterface
            fun shareImage(url: String) {
                val image = try {
                    val src = URL(url)
                    val connection =
                        src.openConnection() as HttpURLConnection
                    connection.doInput = true
                    connection.connect()
                    val input = connection.inputStream
                    BitmapFactory.decodeStream(input)
                } catch (e: IOException) {
                    e.printStackTrace()
                    null
                }
                // save bitmap to cache directory
                try {
                    val cachePath = File(applicationContext.cacheDir, "images")
                    cachePath.mkdirs() // don't forget to make the directory
                    val stream =
                        FileOutputStream("$cachePath/image.png") // overwrites this image every time
                    image!!.compress(Bitmap.CompressFormat.PNG, 100, stream)
                    stream.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
                val imagePath = File(applicationContext.cacheDir, "images")
                val newFile = File(imagePath, "image.png")
                val contentUri: Uri =
                    FileProvider.getUriForFile(
                        applicationContext,
                        "com.passionpenguin.ditiezu.MainActivity",
                        newFile
                    )

                if (contentUri != null) {
                    val shareIntent = Intent()
                    shareIntent.action = Intent.ACTION_SEND
                    shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION) // temp permission for receiving app to read this file
                    shareIntent.setDataAndType(contentUri, contentResolver.getType(contentUri))
                    shareIntent.putExtra(Intent.EXTRA_STREAM, contentUri)
                    startActivity(Intent.createChooser(shareIntent, "Choose an app"))
                }
            }
        }
        webView.settings.javaScriptEnabled = true
        webView.addJavascriptInterface(WebAppInterface(this), "android")
        if (isDarkMode)
            webView.loadUrl("file:///android_asset/webRes/index.html?dark=true")
        else
            webView.loadUrl("file:///android_asset/webRes/index.html?dark=false")
        webView.settings.userAgentString =
            "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/80.0.3987.132 Safari/537.36"
//        WebView.setWebContentsDebuggingEnabled(true)
        webView.webChromeClient =
            object : WebChromeClient() {
                override fun onShowFileChooser(
                    webView: WebView,
                    filePathCallback: ValueCallback<Array<Uri>>,
                    fileChooserParams: FileChooserParams
                ): Boolean {
                    uploadMessageAboveL = filePathCallback
                    openImageChooserActivity()
                    return true
                }
            }

        webView.webViewClient = (
                object : WebViewClient() {
                    override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
                        if (isDarkMode) {
                            if (url.contains('?')) {
                                view.loadUrl("$url&dark=true&pg__newRelease=true")
                            } else {
                                view.loadUrl(
                                    "$url?dark=true&pg__newRelease=true"
                                )
                            }
                        } else
                            view.loadUrl(url)
                        if (webView.url.contains("http://www.ditiezu.com/android_asset/webRes")) {
                            webView.loadUrl(
                                webView.url.replace(
                                    "http://www.ditiezu.com/",
                                    "file:///"
                                )
                            )
                            Log.i("Log-load:\t", url)
                        }
                        return true
                    }

                    @RequiresApi(21)
                    override fun shouldOverrideUrlLoading(
                        view: WebView?,
                        request: WebResourceRequest?
                    ): Boolean {
                        val url = view!!.url
                        if (isDarkMode) {
                            if (url.contains('?')) {
                                view.loadUrl("$url&dark=true&pg__newRelease=true")
                            } else {
                                view.loadUrl("$url?dark=true&pg__newRelease=true")
                            }
                        } else
                            view.loadUrl(url)
                        return super.shouldOverrideUrlLoading(view, request)
                    }

                    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
                    override fun shouldInterceptRequest(
                        view: WebView?,
                        request: WebResourceRequest?
                    ): WebResourceResponse? {
                        val url = request!!.url
                        if (url.toString()
                                .contains("mod=logging&action=login&mobile=yes") && !url.toString()
                                .contains("transfered")
                        ) {
                            webView.post(Runnable {
                                val extraHeaders: HashMap<String, String> = HashMap();
                                extraHeaders["Referer"] =
                                    "http://www.ditiezu.com/member.php?mod=logging&action=login&mobile=yes&transfered";
                                webView.loadUrl("$url&transfered", extraHeaders);
                            })
                        }
                        return super.shouldInterceptRequest(view, request)
                    }

                    override fun shouldInterceptRequest(
                        view: WebView?,
                        url: String?
                    ): WebResourceResponse? {
                        if (url!!.contains("mod=logging&action=login&mobile=yes") && !url.contains("transfered")) {
                            webView.post(Runnable {
                                val extraHeaders: HashMap<String, String> = HashMap();
                                extraHeaders["Referer"] =
                                    "http://www.ditiezu.com/member.php?mod=logging&action=login&mobile=yes&transfered";
                                webView.loadUrl(url + "&transfered", extraHeaders);
                            })
                        }
                        return super.shouldInterceptRequest(view, url)
                    }
                })
        webView.settings.allowUniversalAccessFromFileURLs = true
        webView.settings.allowFileAccessFromFileURLs = true
        webView.settings.allowFileAccess = true
    }

    override fun onBackPressed() {
        findViewById<WebView>(R.id.webview).evaluateJavascript("history.length") {
            if (it.toInt() > 1) {
                findViewById<WebView>(R.id.webview).evaluateJavascript("history.back()", null)
            } else
                super.onBackPressed()
        }
    }

    private fun openImageChooserActivity() {
        val i = Intent(Intent.ACTION_GET_CONTENT)
        i.addCategory(Intent.CATEGORY_OPENABLE)
        i.type = "image/*"
        startActivityForResult(
            Intent.createChooser(i, "Image Chooser"),
            FILE_CHOOSER_RESULT_CODE
        )
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == FILE_CHOOSER_RESULT_CODE) {
            if (null == uploadMessage && null == uploadMessageAboveL) return
            val result =
                if (data == null || resultCode != Activity.RESULT_OK) null else data.data
            if (uploadMessageAboveL != null) {
                onActivityResultAboveL(requestCode, resultCode, data)
            } else if (uploadMessage != null) {
                uploadMessage!!.onReceiveValue(result)
                uploadMessage = null
            }
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private fun onActivityResultAboveL(requestCode: Int, resultCode: Int, intent: Intent?) {
        if (requestCode != FILE_CHOOSER_RESULT_CODE || uploadMessageAboveL == null)
            return
        var results: Array<Uri>? = null
        if (resultCode == Activity.RESULT_OK) {
            if (intent != null) {
                val dataString = intent.dataString
                val clipData = intent.clipData
                if (clipData != null) {
                    results = Array(clipData.itemCount) { i ->
                        clipData.getItemAt(i).uri
                    }
                }
                if (dataString != null)
                    results = arrayOf(Uri.parse(dataString))
            }
        }
        uploadMessageAboveL!!.onReceiveValue(results)
        uploadMessageAboveL = null
    }

    companion object {
        private const val FILE_CHOOSER_RESULT_CODE = 10000
    }


    private var onComplete: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(ctxt: Context?, intent: Intent) {
            // get the refid from the download manager
            val referenceId =
                intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
            val downloadManager: DownloadManager =
                getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            downloadManager.openDownloadedFile(referenceId)

            val manager =
                getSystemService(NOTIFICATION_SERVICE) as NotificationManager

            val channelId = "ditiezu"
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel = NotificationChannel(
                    channelId,
                    "ditiezu",
                    NotificationManager.IMPORTANCE_DEFAULT
                )
                manager.createNotificationChannel(channel)
            }

            val notification: Notification =
                NotificationCompat.Builder(this@MainActivity, channelId)
                    .setContentTitle("PassionPenguin | 下载管理")
                    .setContentText("下载项目已完成")
                    .setWhen(System.currentTimeMillis())
                    .setSmallIcon(R.mipmap.ic_launcher_round)
                    .setPriority(NotificationCompat.PRIORITY_MAX)
                    .setDefaults(NotificationCompat.DEFAULT_SOUND)
                    .setTicker("下载完成")
                    .setAutoCancel(true)
                    .setWhen(System.currentTimeMillis())
                    .setProgress(0, 0, false)
                    .build()
            manager.notify(1, notification)
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(onComplete)
    }
}
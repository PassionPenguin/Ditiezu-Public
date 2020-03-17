package com.passionpenguin.ditiezu

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.webkit.CookieManager
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL


class Splash : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
    }

    override fun onResume() {
        Handler().postDelayed({ startActivity(Intent(this, MainActivity::class.java)) }, 1000)
        super.onResume()
    }

}
//class Splash : AppCompatActivity() {
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_splash)
//        val cookieManager: CookieManager = CookieManager.getInstance()
//        val url =
//            URL("http://www.ditiezu.com/forum.php?mod=redirect&goto=findpost&ptid=662786&pid=11373827")
//        findViewById<TextView>(R.id.splashText).text =
//            cookieManager.getCookie(url.toString())
//        Log.i("Log", cookieManager.getCookie(url.toString()))
//
//
//        val sendHttpRequestThread = Thread {
//            val urlConnection =
//                url.openConnection() as HttpURLConnection
//            urlConnection.setRequestProperty("Cookie:", cookieManager.getCookie(url.toString()))
//            urlConnection.requestMethod = "GET"
//            try {
//                val inputStream: InputStream = urlConnection.inputStream
//                val result = ByteArrayOutputStream()
//                val buffer = ByteArray(1024)
//                var length: Int
//                while (inputStream.read(buffer).also { length = it } != -1) {
//                    result.write(buffer, 0, length)
//                }
//                Log.i("ResultPage:", result.toString())
//            } finally {
//                urlConnection.disconnect()
//            }
//        }
//        sendHttpRequestThread.start()
//    }
//}
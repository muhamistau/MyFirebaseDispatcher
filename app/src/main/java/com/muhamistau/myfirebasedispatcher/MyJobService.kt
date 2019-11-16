package com.muhamistau.myfirebasedispatcher

import com.firebase.jobdispatcher.JobParameters
import com.firebase.jobdispatcher.JobService
import org.json.JSONObject
import com.loopj.android.http.AsyncHttpResponseHandler
import com.loopj.android.http.AsyncHttpClient
import android.util.Log
import cz.msebera.android.httpclient.Header
import java.text.DecimalFormat
import android.app.NotificationManager
import android.app.NotificationChannel
import android.os.Build
import androidx.core.content.ContextCompat
import android.content.Context
import androidx.core.app.NotificationCompat
import android.media.RingtoneManager

class MyJobService : JobService() {

    companion object {
        val TAG = MyJobService::class.java.simpleName
        val APP_ID = "Masukkan API key anda..."
        var EXTRAS_CITY = "extras_city"
    }

    override fun onStopJob(job: JobParameters?): Boolean {
        return true
    }

    override fun onStartJob(job: JobParameters?): Boolean {
        job?.let { getCurrentWeather(it) }
        return true
    }

    private fun getCurrentWeather(job: JobParameters) {

        val extras = job.extras

        if (extras == null) {
            jobFinished(job, false)
            return
        } else if (extras.isEmpty) {
            jobFinished(job, false)
            return
        }

        val city = extras.getString(EXTRAS_CITY)

        val client = AsyncHttpClient()
        val url = "http://api.openweathermap.org/data/2.5/weather?q=$city&appid=$APP_ID"
        client.get(url, object : AsyncHttpResponseHandler() {
            override fun onSuccess(statusCode: Int, headers: Array<Header>, responseBody: ByteArray) {
                val result = String(responseBody)
                Log.d(TAG, result)
                try {
                    val responseObject = JSONObject(result)
                    val currentWeather =
                        responseObject.getJSONArray("weather").getJSONObject(0).getString("main")
                    val description = responseObject.getJSONArray("weather").getJSONObject(0)
                        .getString("description")
                    val tempInKelvin = responseObject.getJSONObject("main").getDouble("temp")

                    val tempInCelcius = tempInKelvin - 273
                    val temprature = DecimalFormat("##.##").format(tempInCelcius)

                    val title = "Current Weather"
                    val message = "$currentWeather, $description with $temprature celcius"
                    val notifId = 100

                    showNotification(applicationContext, title, message, notifId)

                    jobFinished(job, false)
                } catch (e: Exception) {
                    jobFinished(job, true)
                    e.printStackTrace()
                }

            }

            override fun onFailure(
                statusCode: Int,
                headers: Array<Header>,
                responseBody: ByteArray,
                error: Throwable
            ) {
                jobFinished(job, true)
            }
        })
    }

    private fun showNotification(context: Context, title: String, message: String, notifId: Int) {
        val CHANNEL_ID = "Channel_1"
        val CHANNEL_NAME = "Job service channel"

        val notificationManagerCompat =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentTitle(title)
            .setSmallIcon(R.drawable.ic_replay_30_black)
            .setContentText(message)
            .setColor(ContextCompat.getColor(context, android.R.color.black))
            .setVibrate(longArrayOf(1000, 1000, 1000, 1000, 1000))
            .setSound(alarmSound)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            )

            channel.enableVibration(true)
            channel.vibrationPattern = longArrayOf(1000, 1000, 1000, 1000, 1000)

            builder.setChannelId(CHANNEL_ID)

            notificationManagerCompat.createNotificationChannel(channel)
        }

        val notification = builder.build()

        notificationManagerCompat.notify(notifId, notification)
    }
}
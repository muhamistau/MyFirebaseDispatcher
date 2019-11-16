package com.muhamistau.myfirebasedispatcher

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.firebase.jobdispatcher.*
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity(), View.OnClickListener {

    private val DISPATCHER_TAG = "mydispatcher"
    private val CITY = "Jakarta"
    lateinit var mDispatcher: FirebaseJobDispatcher

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btn_cancel_scheduler.setOnClickListener(this)
        btn_set_scheduler.setOnClickListener(this)

        mDispatcher = FirebaseJobDispatcher(GooglePlayDriver(this))
    }

    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.btn_set_scheduler -> {
                startDispatcher()
                Toast.makeText(this, "Dispatcher Created", Toast.LENGTH_SHORT).show()
            }

            R.id.btn_cancel_scheduler -> {
                cancelDispatcher()
                Toast.makeText(this, "Dispatcher Cancelled", Toast.LENGTH_SHORT).show();
            }
        }
    }

    fun startDispatcher() {

        val myExtrasBundle = Bundle()
        myExtrasBundle.putString(MyJobService.EXTRAS_CITY, CITY)

        val myJob = mDispatcher.newJobBuilder()
            .setService(MyJobService::class.java)
            .setTag(DISPATCHER_TAG)
            .setRecurring(true)
            .setLifetime(Lifetime.UNTIL_NEXT_BOOT)
            .setTrigger(Trigger.executionWindow(0, 60))
            .setReplaceCurrent(true)
            .setRetryStrategy(RetryStrategy.DEFAULT_EXPONENTIAL)
            .setConstraints(
                Constraint.ON_UNMETERED_NETWORK,
                Constraint.DEVICE_CHARGING
            )
            .setExtras(myExtrasBundle)
            .build()

        mDispatcher.mustSchedule(myJob)
    }

    fun cancelDispatcher() {
        mDispatcher.cancel(DISPATCHER_TAG)
    }
}

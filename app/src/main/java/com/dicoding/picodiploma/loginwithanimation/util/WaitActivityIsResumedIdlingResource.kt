package com.dicoding.picodiploma.loginwithanimation.util


import android.app.Activity
import android.app.Application
import android.os.Bundle
import androidx.test.espresso.IdlingResource

class WaitActivityIsResumedIdlingResource(
    application: Application,
    private val activityToWaitClassName: String
) : IdlingResource {

    @Volatile
    private var resourceCallback: IdlingResource.ResourceCallback? = null
    private var resumed: Boolean = false

    private val activityLifecycleCallbacks = object : Application.ActivityLifecycleCallbacks {
        override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {}
        override fun onActivityStarted(activity: Activity) {}
        override fun onActivityResumed(activity: Activity) {
            if (activity.javaClass.name == activityToWaitClassName) {
                resumed = true
                resourceCallback?.onTransitionToIdle()
            }
        }
        override fun onActivityPaused(activity: Activity) {}
        override fun onActivityStopped(activity: Activity) {}
        override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}
        override fun onActivityDestroyed(activity: Activity) {}
    }

    init {
        application.registerActivityLifecycleCallbacks(activityLifecycleCallbacks)
    }

    override fun getName(): String {
        return javaClass.name
    }

    override fun isIdleNow(): Boolean {
        return resumed
    }

    override fun registerIdleTransitionCallback(resourceCallback: IdlingResource.ResourceCallback) {
        this.resourceCallback = resourceCallback
    }
}
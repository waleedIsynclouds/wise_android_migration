package com.example.utils

import android.annotation.SuppressLint
import android.app.Activity
import android.app.ActivityManager
import android.app.Application
import android.app.Application.ActivityLifecycleCallbacks
import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.core.content.FileProvider
import java.lang.reflect.Field
import java.lang.reflect.InvocationTargetException
import java.util.HashMap
import java.util.HashSet
import java.util.Iterator
import java.util.LinkedList
import java.util.Map
import java.util.Set

@SuppressLint("StaticFieldLeak")
object Utils {
    private const val PERMISSION_ACTIVITY_CLASS_NAME =
        "com.blankj.utilcode.util.PermissionUtils\$PermissionActivity"

    private val ACTIVITY_LIFECYCLE = ActivityLifecycleImpl()

    private var sApplication: Application? = null

    fun init(context: Context?) {
        if (context == null) {
            init(getApplicationByReflect())
            return
        }
        init(context.applicationContext as Application)
    }

    fun init(app: Application?) {
        if (sApplication == null) {
            sApplication = app ?: getApplicationByReflect()
            sApplication?.registerActivityLifecycleCallbacks(ACTIVITY_LIFECYCLE)
        } else {
            if (app != null && app::class.java != sApplication!!::class.java) {
                sApplication?.unregisterActivityLifecycleCallbacks(ACTIVITY_LIFECYCLE)
                ACTIVITY_LIFECYCLE.mActivityList.clear()
                sApplication = app
                sApplication?.registerActivityLifecycleCallbacks(ACTIVITY_LIFECYCLE)
            }
        }
    }

    fun getApp(): Application {
        if (sApplication != null) return sApplication as Application
        val app = getApplicationByReflect()
        init(app)
        return app
    }

    private fun getApplicationByReflect(): Application {
        try {
            @Suppress("PrivateApi")
            val activityThread = Class.forName("android.app.ActivityThread")
            val thread = activityThread.getMethod("currentActivityThread").invoke(null)
            val app = activityThread.getMethod("getApplication").invoke(thread)
            if (app == null) {
                throw NullPointerException("u should init first")
            }
            return app as Application
        } catch (e: NoSuchMethodException) {
            e.printStackTrace()
        } catch (e: IllegalAccessException) {
            e.printStackTrace()
        } catch (e: InvocationTargetException) {
            e.printStackTrace()
        } catch (e: ClassNotFoundException) {
            e.printStackTrace()
        }
        throw NullPointerException("u should init first")
    }

    internal fun getActivityLifecycle(): ActivityLifecycleImpl {
        return ACTIVITY_LIFECYCLE
    }

    internal fun getActivityList(): LinkedList<Activity> {
        return ACTIVITY_LIFECYCLE.mActivityList
    }

    internal fun getTopActivityOrApp(): Context {
        return if (isAppForeground()) {
            val topActivity = ACTIVITY_LIFECYCLE.getTopActivity()
            topActivity ?: getApp()
        } else {
            getApp()
        }
    }

    internal fun isAppForeground(): Boolean {
        val am = getApp().getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager
        if (am == null) return false
        val info = am.runningAppProcesses ?: return false
        if (info.isEmpty()) return false
        for (aInfo in info) {
            if (aInfo.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                return aInfo.processName == getApp().packageName
            }
        }
        return false
    }

    class ActivityLifecycleImpl : ActivityLifecycleCallbacks {

        val mActivityList: LinkedList<Activity> = LinkedList()
        val mStatusListenerMap: MutableMap<Any, OnAppStatusChangedListener> = HashMap()
        val mDestroyedListenerMap: MutableMap<Activity, MutableSet<OnActivityDestroyedListener>> = HashMap()

        private var mForegroundCount = 0
        private var mConfigCount = 0
        private var mIsBackground = false

        override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
            setTopActivity(activity)
        }

        override fun onActivityStarted(activity: Activity) {
            if (!mIsBackground) {
                setTopActivity(activity)
            }
            if (mConfigCount < 0) {
                mConfigCount++
            } else {
                mForegroundCount++
            }
        }

        override fun onActivityResumed(activity: Activity) {
            setTopActivity(activity)
            if (mIsBackground) {
                mIsBackground = false
                postStatus(true)
            }
        }

        override fun onActivityPaused(activity: Activity) {}

        override fun onActivityStopped(activity: Activity) {
            if (activity.isChangingConfigurations) {
                mConfigCount--
            } else {
                mForegroundCount--
                if (mForegroundCount <= 0) {
                    mIsBackground = true
                    postStatus(false)
                }
            }
        }

        override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}

        override fun onActivityDestroyed(activity: Activity) {
            mActivityList.remove(activity)
            consumeOnActivityDestroyedListener(activity)
            fixSoftInputLeaks(activity)
        }

        fun getTopActivity(): Activity? {
            if (mActivityList.isNotEmpty()) {
                val topActivity = mActivityList.last()
                return topActivity ?: getTopActivityByReflect()
            }
            val topActivityByReflect = getTopActivityByReflect()
            if (topActivityByReflect != null) {
                setTopActivity(topActivityByReflect)
            }
            return topActivityByReflect
        }

        fun addOnAppStatusChangedListener(`object`: Any, listener: OnAppStatusChangedListener) {
            mStatusListenerMap[`object`] = listener
        }

        fun removeOnAppStatusChangedListener(`object`: Any) {
            mStatusListenerMap.remove(`object`)
        }

        fun removeOnActivityDestroyedListener(activity: Activity?) {
            if (activity == null) return
            mDestroyedListenerMap.remove(activity)
        }

        fun addOnActivityDestroyedListener(activity: Activity?, listener: OnActivityDestroyedListener?) {
            if (activity == null || listener == null) return
            val listeners: MutableSet<OnActivityDestroyedListener> = mDestroyedListenerMap.getOrPut(activity) { HashSet() }
            if (listeners.contains(listener)) return
            listeners.add(listener)
            mDestroyedListenerMap[activity] = listeners
        }

        private fun postStatus(isForeground: Boolean) {
            if (mStatusListenerMap.isEmpty()) return
            for (onAppStatusChangedListener in mStatusListenerMap.values) {
                if (isForeground) {
                    onAppStatusChangedListener.onForeground()
                } else {
                    onAppStatusChangedListener.onBackground()
                }
            }
        }

        private fun setTopActivity(activity: Activity) {
            if (PERMISSION_ACTIVITY_CLASS_NAME == activity.javaClass.name) return
            if (mActivityList.contains(activity)) {
                if (mActivityList.last() != activity) {
                    mActivityList.remove(activity)
                    mActivityList.addLast(activity)
                }
            } else {
                mActivityList.addLast(activity)
            }
        }

        private fun consumeOnActivityDestroyedListener(activity: Activity) {
            val iterator = mDestroyedListenerMap.entries.iterator()
            while (iterator.hasNext()) {
                val entry = iterator.next()
                if (entry.key == activity) {
                    val value = entry.value
                    for (listener in value) {
                        listener.onActivityDestroyed(activity)
                    }
                    iterator.remove()
                }
            }
        }

        private fun getTopActivityByReflect(): Activity? {
            try {
                @Suppress("PrivateApi")
                val activityThreadClass = Class.forName("android.app.ActivityThread")
                val currentActivityThreadMethod = activityThreadClass.getMethod("currentActivityThread").invoke(null)
                val mActivityListField: Field = activityThreadClass.getDeclaredField("mActivityList")
                mActivityListField.isAccessible = true
                val activities = mActivityListField.get(currentActivityThreadMethod) as? Map<*, *>
                if (activities == null) return null
                for (activityRecord in activities.values) {
                    val activityRecordClass = activityRecord!!::class.java
                    val pausedField = activityRecordClass.getDeclaredField("paused")
                    pausedField.isAccessible = true
                    if (!pausedField.getBoolean(activityRecord)) {
                        val activityField = activityRecordClass.getDeclaredField("activity")
                        activityField.isAccessible = true
                        return activityField.get(activityRecord) as Activity
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return null
        }

        private fun fixSoftInputLeaks(activity: Activity?) {
            if (activity == null) return
            val imm = getApp().getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager ?: return
            val leakViews = arrayOf("mLastSrvView", "mCurRootView", "mServedView", "mNextServedView")
            for (leakView in leakViews) {
                try {
                    val leakViewField = InputMethodManager::class.java.getDeclaredField(leakView)
                    if (leakViewField == null) continue
                    if (!leakViewField.isAccessible) {
                        leakViewField.isAccessible = true
                    }
                    val obj = leakViewField.get(imm)
                    if (obj !is View) continue
                    val view = obj
                    if (view.rootView == activity.window.decorView.rootView) {
                        leakViewField.set(imm, null)
                    }
                } catch (ignore: Throwable) {
                }
            }
        }
    }

    class FileProvider4UtilCode : FileProvider() {
        override fun onCreate(): Boolean {
            init(context)
            return true
        }
    }

    interface OnAppStatusChangedListener {
        fun onForeground()
        fun onBackground()
    }

    interface OnActivityDestroyedListener {
        fun onActivityDestroyed(activity: Activity)
    }
}

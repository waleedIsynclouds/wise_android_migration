package com.example.utils

import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import com.example.HxjApp

object HXToast {
    private val TAG: String = HXToast::class.java.simpleName
    private var mToast: Toast? = null
    private val mHandler: Handler = Handler(Looper.getMainLooper())

    fun getMainHandler(): Handler = mHandler

    /**
     * 可以在子线程中调用
     *
     * @param msg toast内容
     */
    fun show(msg: String?) {
        if (!msg.isNullOrEmpty()) {
            runOnUIThread(Runnable {
                mToast?.cancel()
                mToast = Toast.makeText(HxjApp.getAppContext(), msg, Toast.LENGTH_SHORT)
                mToast?.setText(msg)
                Log.d(TAG, "Toast: $msg")
                mToast?.show()
            })
        }
    }

    fun runOnUIThread(run: Runnable) {
        if (isUIThread()) {
            run.run()
        } else {
            mHandler.post(run)
        }
    }

    fun isUIThread(): Boolean = Looper.getMainLooper() == Looper.myLooper()
}

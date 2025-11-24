package com.example.utils

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import androidx.annotation.NonNull
import java.util.Collections
import java.util.HashMap

@SuppressLint("ApplySharedPref")
class SPUtils private constructor(spName: String?, mode: Int?) {

    private var sp: SharedPreferences

    init {
        sp = if (spName == null || spName.isEmpty()) {
            com.example.utils.Utils.getApp().getSharedPreferences("spUtils", Context.MODE_PRIVATE)
        } else {
            com.example.utils.Utils.getApp().getSharedPreferences(spName, mode ?: Context.MODE_PRIVATE)
        }
    }

    companion object {
        private val SP_UTILS_MAP: MutableMap<String, SPUtils> = HashMap()

        @JvmStatic
        fun getInstance(): SPUtils {
            return getInstance("", Context.MODE_PRIVATE)
        }

        @JvmStatic
        fun getInstance(mode: Int): SPUtils {
            return getInstance("", mode)
        }

        @JvmStatic
        fun getInstance(spName: String): SPUtils {
            return getInstance(spName, Context.MODE_PRIVATE)
        }

        @JvmStatic
        fun getInstance(spName: String?, mode: Int): SPUtils {
            var name = spName
            if (isSpace(name)) name = "spUtils"
            var spUtils = SP_UTILS_MAP[name]
            if (spUtils == null) {
                synchronized(SPUtils::class.java) {
                    spUtils = SP_UTILS_MAP[name]
                    if (spUtils == null) {
                        spUtils = SPUtils(name, mode)
                        SP_UTILS_MAP[name!!] = spUtils
                    }
                }
            }
            return spUtils!!
        }

        private fun isSpace(s: String?): Boolean {
            if (s == null) return true
            for (ch in s.toCharArray()) {
                if (!Character.isWhitespace(ch)) {
                    return false
                }
            }
            return true
        }
    }

    fun put(key: String, value: String) {
        put(key, value, false)
    }

    fun put(key: String, value: String, isCommit: Boolean) {
        if (isCommit) {
            sp.edit().putString(key, value).commit()
        } else {
            sp.edit().putString(key, value).apply()
        }
    }

    fun getString(key: String): String? {
        return getString(key, "")
    }

    fun getString(key: String, defaultValue: String): String? {
        return sp.getString(key, defaultValue)
    }

    fun put(key: String, value: Int) {
        put(key, value, false)
    }

    fun put(key: String, value: Int, isCommit: Boolean) {
        if (isCommit) {
            sp.edit().putInt(key, value).commit()
        } else {
            sp.edit().putInt(key, value).apply()
        }
    }

    fun getInt(key: String): Int {
        return getInt(key, -1)
    }

    fun getInt(key: String, defaultValue: Int): Int {
        return sp.getInt(key, defaultValue)
    }

    fun put(key: String, value: Long) {
        put(key, value, false)
    }

    fun put(key: String, value: Long, isCommit: Boolean) {
        if (isCommit) {
            sp.edit().putLong(key, value).commit()
        } else {
            sp.edit().putLong(key, value).apply()
        }
    }

    fun getLong(key: String): Long {
        return getLong(key, -1L)
    }

    fun getLong(key: String, defaultValue: Long): Long {
        return sp.getLong(key, defaultValue)
    }

    fun put(key: String, value: Float) {
        put(key, value, false)
    }

    fun put(key: String, value: Float, isCommit: Boolean) {
        if (isCommit) {
            sp.edit().putFloat(key, value).commit()
        } else {
            sp.edit().putFloat(key, value).apply()
        }
    }

    fun getFloat(key: String): Float {
        return getFloat(key, -1f)
    }

    fun getFloat(key: String, defaultValue: Float): Float {
        return sp.getFloat(key, defaultValue)
    }

    fun put(key: String, value: Boolean) {
        put(key, value, false)
    }

    fun put(key: String, value: Boolean, isCommit: Boolean) {
        if (isCommit) {
            sp.edit().putBoolean(key, value).commit()
        } else {
            sp.edit().putBoolean(key, value).apply()
        }
    }

    fun getBoolean(key: String): Boolean {
        return getBoolean(key, false)
    }

    fun getBoolean(key: String, defaultValue: Boolean): Boolean {
        return sp.getBoolean(key, defaultValue)
    }

    fun put(key: String, value: MutableSet<String>) {
        put(key, value, false)
    }

    fun put(key: String, value: MutableSet<String>, isCommit: Boolean) {
        if (isCommit) {
            sp.edit().putStringSet(key, value).commit()
        } else {
            sp.edit().putStringSet(key, value).apply()
        }
    }

    fun getStringSet(key: String): Set<String>? {
        return getStringSet(key, mutableSetOf())
    }

    fun getStringSet(key: String, defaultValue: Set<String>): Set<String>? {
        return sp.getStringSet(key, defaultValue.toMutableSet())
    }

    fun getAll(): Map<String, *> {
        return sp.getAll()
    }

    fun contains(key: String): Boolean {
        return sp.contains(key)
    }

    fun remove(key: String) {
        remove(key, false)
    }

    fun remove(key: String, isCommit: Boolean) {
        if (isCommit) {
            sp.edit().remove(key).commit()
        } else {
            sp.edit().remove(key).apply()
        }
    }

    fun clear() {
        clear(false)
    }

    fun clear(isCommit: Boolean) {
        if (isCommit) {
            sp.edit().clear().commit()
        } else {
            sp.edit().clear().apply()
        }
    }
}

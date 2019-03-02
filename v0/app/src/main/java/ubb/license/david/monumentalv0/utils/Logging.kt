package ubb.license.david.monumentalv0.utils

import android.util.Log

fun info(tag: String, message: String) {
    Log.i(tag, message)
}

fun warn(tag: String, message: String) {
    Log.w(tag, message)
}

fun debug(tag: String, message: String) {
    Log.d(tag, message)
}
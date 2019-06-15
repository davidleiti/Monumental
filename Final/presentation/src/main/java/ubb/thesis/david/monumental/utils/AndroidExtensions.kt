package ubb.thesis.david.monumental.utils

import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import com.google.android.material.snackbar.Snackbar
import ubb.thesis.david.domain.entities.Landmark
import ubb.thesis.david.monumental.MainApplication

fun <T : Any> MutableLiveData<T>.default(value: T) = apply { setValue(value) }

val Context.appContext: MainApplication
    get() = applicationContext as MainApplication

fun Fragment.requestPermission(permission: String, code: Int) =
    requestPermissions(arrayOf(permission), code)

fun Context.checkPermission(permission: String) =
    checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED

fun Activity.hideSoftKeyboard() {
    val imm = getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
    var view = currentFocus
    if (view == null) view = View(this)
    imm.hideSoftInputFromWindow(view.windowToken, 0)
}

fun Activity.clearFocus() = window.decorView.clearFocus()

fun View.shortSnack(message: String) =
    Snackbar.make(this, message, Snackbar.LENGTH_SHORT).show()

fun View.longSnack(message: String) =
    Snackbar.make(this, message, Snackbar.LENGTH_LONG).show()

fun Context.shortToast(message: String) =
    Toast.makeText(this, message, Toast.LENGTH_SHORT).show()

fun Context.longToast(message: String) =
    Toast.makeText(this, message, Toast.LENGTH_LONG).show()


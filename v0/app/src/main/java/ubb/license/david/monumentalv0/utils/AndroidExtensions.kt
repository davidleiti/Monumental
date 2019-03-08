package ubb.license.david.monumentalv0.utils

import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.android.material.snackbar.Snackbar
import ubb.license.david.monumentalv0.BaseApplication

val Context.appContext: BaseApplication
    get() = applicationContext as BaseApplication

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

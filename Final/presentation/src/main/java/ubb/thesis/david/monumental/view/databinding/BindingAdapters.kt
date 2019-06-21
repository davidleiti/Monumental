package ubb.thesis.david.monumental.view.databinding

import android.transition.TransitionManager
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.databinding.BindingAdapter
import com.google.android.material.textfield.TextInputEditText

@BindingAdapter("app:animatedVisibility")
fun setAnimatedVisibility(view: View, visibility: Int) {
    TransitionManager.beginDelayedTransition(view.rootView as ViewGroup)
    view.visibility = if (visibility == View.VISIBLE) View.VISIBLE else View.GONE
}

@BindingAdapter("app:animatedText")
fun animateTextChange(textView: TextView, text: String) {
    TransitionManager.beginDelayedTransition(textView.rootView as ViewGroup)
    textView.text = text
}

@BindingAdapter("app:errorText")
fun fieldErrorText(editText: TextInputEditText, text: String?) {
    editText.error = text
}
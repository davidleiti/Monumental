package ubb.thesis.david.monumental.view

import android.transition.TransitionManager
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.databinding.BindingAdapter

@BindingAdapter("app:animatedVisibility")
fun setAnimatedVisibility(view: View, visibility: Int) {
    TransitionManager.beginDelayedTransition(view.rootView as ViewGroup)
    view.visibility = if (visibility == View.VISIBLE) View.VISIBLE else View.GONE
}

@BindingAdapter("app:textAnimated")
fun animateTextChange(textView: TextView, text: String) {
    TransitionManager.beginDelayedTransition(textView.rootView as ViewGroup)
    textView.text = text
}
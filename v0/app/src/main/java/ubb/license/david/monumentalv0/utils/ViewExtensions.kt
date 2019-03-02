package ubb.license.david.monumentalv0.utils

import android.animation.Animator
import android.view.View
import android.view.animation.Animation
import android.view.animation.Transformation
import android.widget.LinearLayout


fun View.fadeIn() {
    alpha = 0f
    visibility = View.VISIBLE
    animate().setDuration(500).alpha(1f)
}

fun View.fadeOut() {
    animate().setDuration(500).alpha(0f).setListener(object : Animator.AnimatorListener {
        override fun onAnimationEnd(animation: Animator?) {
            visibility = View.GONE
        }

        override fun onAnimationRepeat(animation: Animator?) = Unit
        override fun onAnimationStart(animation: Animator?) = Unit
        override fun onAnimationCancel(animation: Animator?) = Unit
    })
}

fun View.expand() {
    measure(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)

    layoutParams.height = 1
    visibility = View.VISIBLE

    val animation = expandAnimation(this)
    startAnimation(animation)
}

fun View.collapse(onAnimationEndAction: Runnable? = null) {
    val animation = collapseAnimation(this, onAnimationEndAction)
    startAnimation(animation)
}

private fun expandAnimation(view: View): Animation = object : Animation() {

    private val targetHeight = view.measuredHeight

    init {
        duration = Math.max((targetHeight / view.resources.displayMetrics.density).toInt().toLong(), 300)
    }

    override fun applyTransformation(interpolatedTime: Float, t: Transformation) {
        view.layoutParams.height = if (interpolatedTime == 1f)
            LinearLayout.LayoutParams.WRAP_CONTENT
        else
            (targetHeight * interpolatedTime).toInt()
        view.requestLayout()
    }

    override fun willChangeBounds(): Boolean {
        return true
    }
}

private fun collapseAnimation(view: View, onAnimationEndAction: Runnable?): Animation = object : Animation() {

    private val initialHeight = view.measuredHeight

    init {
        duration = Math.max((initialHeight / view.resources.displayMetrics.density).toInt().toLong(), 300)
        onAnimationEndAction?.let {
            setAnimationListener(object : Animation.AnimationListener {
                override fun onAnimationRepeat(animation: Animation?) = Unit
                override fun onAnimationStart(animation: Animation?) = Unit
                override fun onAnimationEnd(animation: Animation?) = onAnimationEndAction.run()
            })
        }
    }

    override fun applyTransformation(interpolatedTime: Float, t: Transformation) {
        if (interpolatedTime == 1f) {
            view.visibility = View.GONE
        } else {
            view.layoutParams.height = initialHeight - (initialHeight * interpolatedTime).toInt()
        }
        view.requestLayout()
    }

    override fun willChangeBounds(): Boolean {
        return true
    }
}
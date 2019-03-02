package ubb.license.david.monumentalv0.ui

import android.content.Context
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.util.AttributeSet
import android.widget.FrameLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.getColor
import org.jetbrains.anko.textColor
import ubb.license.david.monumentalv0.R

class FlatButton(context: Context, attrs: AttributeSet?, defStyleAttr: Int) :
        FrameLayout(context, attrs, defStyleAttr) {

    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)

    constructor(context: Context) : this(context, null)

    private val mTextView: TextView

    init {
        inflate(context, R.layout.button_flat, this)

        mTextView = findViewById(R.id.text_flat_button)

        val attributes = context.obtainStyledAttributes(attrs, R.styleable.FlatButton)
        val invertColors = attributes.getBoolean(R.styleable.FlatButton_invertColors, false)
        val textColor = if (invertColors) getColor(context, R.color.primary) else getColor(context, R.color.white)

        mTextView.text = attributes.getString(R.styleable.FlatButton_text)
        mTextView.textColor = textColor

        background = if (invertColors) backgroundInverted() else backgroundNormal()
        attributes.recycle()
    }

    fun setText(text: String) {
        mTextView.text = text
    }

    fun setText(res: Int) {
        mTextView.text = context.getString(res)
    }

    private fun backgroundInverted(): Drawable {
        val backgroundColor = ContextCompat.getColor(context, R.color.white)
        return GradientDrawable().apply {
            setColor(backgroundColor)
            setStroke(1, getColor(context, R.color.primary))
        }
    }

    private fun backgroundNormal(): ColorDrawable {
        val backgroundColor = ContextCompat.getColor(context, R.color.primary)
        return ColorDrawable(backgroundColor)
    }
}
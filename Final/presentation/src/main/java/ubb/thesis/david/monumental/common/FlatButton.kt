package ubb.thesis.david.monumental.common

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
import ubb.thesis.david.monumental.R

class FlatButton(context: Context, attrs: AttributeSet?, defStyleAttr: Int) :
    FrameLayout(context, attrs, defStyleAttr) {

    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)

    constructor(context: Context) : this(context, null)

    private val buttonText: TextView
    private var inverted: Boolean

    init {
        inflate(context, R.layout.button_flat, this)

        buttonText = findViewById(R.id.text_flat_button)

        val attributes = context.obtainStyledAttributes(attrs, R.styleable.FlatButton)
        val invertColors = attributes.getBoolean(R.styleable.FlatButton_invertColors, false)
        val textColor = if (invertColors) getColor(context, R.color.primary) else getColor(context, R.color.white)

        inverted = invertColors
        buttonText.text = attributes.getString(R.styleable.FlatButton_text)
        buttonText.textColor = textColor

        background = if (invertColors) backgroundInverted() else backgroundNormal()
        attributes.recycle()
    }

    fun invertColors() {
        inverted = !inverted
        background = if (inverted) backgroundInverted() else backgroundNormal()
        buttonText.textColor = if (inverted) getColor(context, R.color.primary) else getColor(context, R.color.white)
    }

    fun setText(text: String) {
        buttonText.text = text
    }

    fun setText(res: Int) {
        buttonText.text = context.getString(res)
    }

    private fun backgroundInverted(): Drawable =
        GradientDrawable().also {
            val backgroundColor = getColor(context, R.color.white)
            it.setColor(backgroundColor)
            it.setStroke(1, getColor(context, R.color.primary))
        }

    private fun backgroundNormal(): ColorDrawable =
        getColor(context, R.color.primary).run {
            ColorDrawable(this)
        }
}
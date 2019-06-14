package ubb.thesis.david.monumental.common

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.View
import android.view.Window
import kotlinx.android.synthetic.main.simple_dialog.*
import org.jetbrains.anko.backgroundColorResource
import org.jetbrains.anko.textColorResource
import ubb.thesis.david.monumental.R

class SimpleDialog(mContext: Context, private var title: String, private var message: String) : Dialog(mContext) {

    private var positiveButtonModel: ButtonModel? = null
    private var negativeButtonModel: ButtonModel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setCanceledOnTouchOutside(false)
        setCancelable(false)

        setContentView(R.layout.simple_dialog)
        window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        window?.attributes?.windowAnimations = R.style.DialogAnimation

        setupUi()
    }

    fun updatePositiveButton(text: String, action: (() -> Unit)? = null) {
        positiveButtonModel = ButtonModel(text, action)
    }

    fun setupNegativeButton(text: String, action: (() -> Unit)? = null) {
        negativeButtonModel = ButtonModel(text, action)
    }

    private data class ButtonModel(var text: String, var action: (() -> Unit)?)

    private fun setupUi() {
        label_title.text = title
        label_message.text = message

        positiveButtonModel?.let { model -> setupPositiveButton(model) }
            ?: run { button_confirm.setOnClickListener { dismiss() } }

        negativeButtonModel?.let { model -> setupNegativeButton(model) }
    }

    private fun setupPositiveButton(model: ButtonModel) {
        with(button_confirm) {
            text = model.text
            button_confirm.setOnClickListener {
                model.action?.run { this() }
                dismiss()
            }
        }
    }

    private fun setupNegativeButton(model: ButtonModel) {
        with(button_cancel) {
            visibility = View.VISIBLE
            text = model.text
            setOnClickListener {
                model.action?.run { this() }
                dismiss()
            }
        }
        button_confirm.backgroundColorResource = R.color.accent
        button_confirm.textColorResource = R.color.white
    }
}

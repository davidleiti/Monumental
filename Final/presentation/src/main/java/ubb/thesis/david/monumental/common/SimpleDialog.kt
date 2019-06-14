package ubb.thesis.david.monumental.common

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.View
import android.view.Window
import kotlinx.android.synthetic.main.simple_dialog.*
import ubb.thesis.david.monumental.R

class SimpleDialog(mContext: Context, private var title: String, private var message: String) : Dialog(mContext) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setCanceledOnTouchOutside(false)
        setCancelable(false)

        setContentView(R.layout.simple_dialog)
        window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        window?.attributes?.windowAnimations = R.style.DialogAnimation

        label_title.text = title
        label_message.text = message

        button_confirm.setOnClickListener {
            dismiss()
        }
    }

    fun updatePositiveButton(text: String, action: () -> Unit = {}) {
        button_confirm.text = text
        button_confirm.setOnClickListener {
            action()
            dismiss()
        }
    }

    fun setupNegativeButton(text: String, action: () -> Unit = {}) {
        button_cancel.visibility = View.VISIBLE
        button_cancel.text = text
        button_cancel.setOnClickListener {
            action()
            dismiss()
        }
    }
}

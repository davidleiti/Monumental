package ubb.license.david.monumentalv0.ui.home


import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import kotlinx.android.synthetic.main.fragment_details.*
import ubb.license.david.monumentalv0.R
import ubb.license.david.monumentalv0.utils.collapse
import ubb.license.david.monumentalv0.utils.expand

class DetailsFragment : Fragment(), View.OnClickListener {

    private val radiusMultiplier = 500

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? =
        inflater.inflate(R.layout.fragment_details, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        button_decrease.setOnClickListener(this)
        button_increase.setOnClickListener(this)
        button_next.setOnClickListener(this)

        cb_limit.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) container_limit.expand()
            else container_limit.collapse()
        }

        sb_radius.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onStartTrackingTouch(seekBar: SeekBar?) = Unit

            override fun onStopTrackingTouch(seekBar: SeekBar?) = Unit

            @SuppressLint("SetTextI18n")
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                label_radius.text = "${(progress + 1) * radiusMultiplier} m"
            }
        })
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.button_decrease -> decreaseLimit()
            R.id.button_increase -> increaseLimit()
            R.id.button_next -> advance()
        }
    }

    private fun advance() {
        val limitArg = if (cb_limit.isChecked) field_limit.text.toString().toInt() else 0
        val radiusArg = (sb_radius.progress + 1) * radiusMultiplier

        val advanceAction = DetailsFragmentDirections.actionAdvance().apply {
            limit = limitArg
            radius = radiusArg
        }

        Navigation.findNavController(activity!!, R.id.nav_host_fragment).navigate(advanceAction)
    }

    private fun decreaseLimit() {
        val currentValue = field_limit.text.toString().toInt()

        if (currentValue > 5)
            field_limit.setText(Math.max(currentValue - 5, 5).toString())
    }

    private fun increaseLimit() {
        val currentValue = field_limit.text.toString().toInt()

        if (currentValue < 50)
            field_limit.setText(Math.min(currentValue + 5, 50).toString())
    }
}

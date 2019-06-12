package ubb.thesis.david.monumental.session.tracking

import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.Navigation
import kotlinx.android.synthetic.main.fragment_snapshot.*

import ubb.thesis.david.monumental.R
import ubb.thesis.david.monumental.common.BaseFragment
import ubb.thesis.david.monumental.common.FlatButton

class SnapshotFragment : BaseFragment() {

    private lateinit var viewModel: SnapshotViewModel

    override fun usesNavigationDrawer(): Boolean = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_snapshot, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(SnapshotViewModel::class.java)

        button_take_photo.setOnClickListener {
            button_accept_photo.visibility = View.VISIBLE
            (it as FlatButton).invertColors()
        }
    }
}

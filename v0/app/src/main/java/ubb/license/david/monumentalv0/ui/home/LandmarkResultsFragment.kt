package ubb.license.david.monumentalv0.ui.home


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.fragment_landmark_results.*

import ubb.license.david.monumentalv0.R

class LandmarkResultsFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? =
        inflater.inflate(R.layout.fragment_landmark_results, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val args = LandmarkResultsFragmentArgs.fromBundle(arguments!!)

        label_output.text = "Received arguments: Limit=${args.limit}, Radius=${args.radius}, Categories=${args.categories}"
    }
}

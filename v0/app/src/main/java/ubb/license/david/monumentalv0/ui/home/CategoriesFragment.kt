package ubb.license.david.monumentalv0.ui.home


import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import kotlinx.android.synthetic.main.fragment_categories.*
import org.jetbrains.anko.layoutInflater
import ubb.license.david.foursquareapi.FoursquareApi
import ubb.license.david.monumentalv0.R

class CategoriesFragment : Fragment(), View.OnClickListener {

    private lateinit var mAdapter: CategoriesAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? =
        inflater.inflate(R.layout.fragment_categories, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mAdapter = CategoriesAdapter(context!!, getPredefinedCategories())

        list_categories.apply {
            adapter = mAdapter
            setOnItemClickListener { _, viewHolder, position, _ ->
                val cb = viewHolder.findViewById<CheckBox>(R.id.cb_category)
                cb.isChecked = cb.isChecked.not()
                mAdapter.setChecked(cb.isChecked, position)
            }
        }

        button_go.setOnClickListener(this)
        button_select_all.setOnClickListener(this)
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.button_go -> {
                if (mAdapter.itemsChecked() > 0)
                    advance()
                else
                    displayError()
            }
            R.id.button_select_all -> {
                val selection = mAdapter.itemsChecked() < mAdapter.count
                setSelection(selection)
            }
        }
    }

    private fun setSelection(check: Boolean) {
        for (catIndex: Int in 0 until mAdapter.count)
            mAdapter.setChecked(check, catIndex)

        mAdapter.notifyDataSetChanged()
    }

    private fun displayError() {
        val message = getString(R.string.message_none_selected)
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    private fun advance() {
        val sb = StringBuilder()
        for (cat: Category in mAdapter.getItems())
            if (cat.checked)
                sb.append("${cat.id},")

        val categoriesArg = sb.substring(0, sb.length - 1)
        val receivedArgs = CategoriesFragmentArgs.fromBundle(arguments!!)

        val action = CategoriesFragmentDirections.actionAdvance().apply {
            categories = categoriesArg
            limit = receivedArgs.limit
            radius = receivedArgs.radius
        }

        Navigation.findNavController(button_go).navigate(action)
    }

    private fun getPredefinedCategories(): Array<Category> = arrayOf(
        Category("Monuments", R.drawable.cat_government_monument_bg_88, FoursquareApi.ID_MONUMENT),
        Category("Public art", R.drawable.cat_sculpture_bg_88, FoursquareApi.ID_PUBLIC_ART),
        Category("Stadiums", R.drawable.cat_stadium_bg_88, FoursquareApi.ID_STADIUM),
        Category("Bridges", R.drawable.cat_bridge_bg_88, FoursquareApi.ID_BRIDGE),
        Category("Castles", R.drawable.cat_castle_bg_88, FoursquareApi.ID_CASTLE),
        Category("Historic sites", R.drawable.cat_historicsite_bg_88, FoursquareApi.ID_HISTORIC_SITE),
        Category("Museums", R.drawable.cat_museum_bg_88, FoursquareApi.ID_MUSEUMS),
        Category("Opera houses", R.drawable.cat_performingarts_operahouse_bg_88, FoursquareApi.ID_OPERA_HOUSE),
        Category("Theatres", R.drawable.cat_performingarts_theater_bg_88, FoursquareApi.ID_THEATRE)
    )

    private data class Category(val name: String, val iconResource: Int, val id: String, var checked: Boolean = false)

    private inner class CategoriesAdapter(context: Context,
                                          categories: Array<Category>) :
            ArrayAdapter<Category>(context, R.layout.selection_category, R.id.text_dummy, categories) {

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            val viewHolder: View =
                convertView ?: context.layoutInflater.inflate(R.layout.selection_category, parent, false)
            val checkBox = viewHolder.findViewById<CheckBox>(R.id.cb_category)
            val icon = viewHolder.findViewById<ImageView>(R.id.icon_category)

            getItem(position)?.let { item ->
                checkBox.text = item.name
                checkBox.isChecked = item.checked
                icon.setImageResource(item.iconResource)
            }

            return viewHolder
        }

        fun getItems(): Array<Category> = Array(count) { position -> getItem(position) }

        fun itemsChecked(): Int = getItems().filter { cat -> cat.checked }.count()

        fun setChecked(check: Boolean, position: Int) {
            getItem(position)?.checked = check
            setButtonText()
        }

        private fun setButtonText() {
            val res = if (itemsChecked() == count) R.string.select_none else R.string.select_all
            button_select_all.setText(res)
        }
    }
}

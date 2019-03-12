package ubb.license.david.monumentalv0.ui.session.setup


import android.content.Context
import android.os.Bundle
import android.transition.TransitionManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.CheckBox
import android.widget.ImageView
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import kotlinx.android.synthetic.main.fragment_categories.*
import org.jetbrains.anko.layoutInflater
import ubb.license.david.foursquareapi.FoursquareApi
import ubb.license.david.monumentalv0.R
import ubb.license.david.monumentalv0.utils.shortToast

class CategoriesFragment : Fragment(), View.OnClickListener {

    private lateinit var listAdapter: CategoriesAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? =
        inflater.inflate(R.layout.fragment_categories, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        listAdapter = CategoriesAdapter(context!!, getPredefinedCategories())

        list_categories.apply {
            adapter = listAdapter
            setOnItemClickListener { _, viewHolder, position, _ ->
                viewHolder.findViewById<CheckBox>(R.id.cb_category).apply {
                    isChecked = isChecked.not()
                    listAdapter.setChecked(isChecked, position)
                }
            }
        }

        button_next.setOnClickListener(this)
        button_select_all.setOnClickListener(this)
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.button_next -> {
                if (listAdapter.itemsChecked() == 0) {
                    context?.shortToast(getString(R.string.message_none_selected))
                } else {
                    advance()
                }
            }
            R.id.button_select_all -> setSelection(listAdapter.itemsChecked() < listAdapter.count)
        }
    }

    private fun setSelection(check: Boolean) {
        for (catIndex: Int in 0 until listAdapter.count)
            listAdapter.setChecked(check, catIndex)
    }

    private fun advance() {
        val sb = StringBuilder()
        for (cat: Category in listAdapter.getItems())
            if (cat.checked)
                sb.append("${cat.id},")

        val categoriesArg = sb.substring(0, sb.length - 1)
        val receivedArgs = CategoriesFragmentArgs.fromBundle(arguments!!)

        val action = CategoriesFragmentDirections.actionAdvance(
            receivedArgs.location).apply {
            categories = categoriesArg
            limit = receivedArgs.limit
            radius = receivedArgs.radius
        }

        Navigation.findNavController(view!!).navigate(action)
    }

    private fun getPredefinedCategories(): Array<Category> = arrayOf(
        Category(getString(R.string.cat_monument),
            R.drawable.cat_government_monument_bg_88, FoursquareApi.ID_MONUMENT),
        Category(getString(R.string.cat_public_art),
            R.drawable.cat_sculpture_bg_88, FoursquareApi.ID_PUBLIC_ART),
        Category(getString(R.string.cat_castle),
            R.drawable.cat_castle_bg_88, FoursquareApi.ID_CASTLE),
        Category(
            getString(R.string.cat_historic_site), R.drawable.cat_historicsite_bg_88, FoursquareApi.ID_HISTORIC_SITE),
        Category(getString(R.string.cat_museum),
            R.drawable.cat_museum_bg_88, FoursquareApi.ID_MUSEUMS),
        Category(getString(R.string.cat_opera_house),
            R.drawable.cat_performingarts_operahouse_bg_88, FoursquareApi.ID_OPERA_HOUSE),
        Category(getString(R.string.cat_theatre),
            R.drawable.cat_performingarts_theater_bg_88, FoursquareApi.ID_THEATRE),
        Category(getString(R.string.cat_stadium),
            R.drawable.cat_stadium_bg_88, FoursquareApi.ID_STADIUM)
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
            notifyDataSetChanged()
        }

        private fun setButtonText() {
            TransitionManager.beginDelayedTransition(button_select_all)
            val res = if (itemsChecked() == count) R.string.select_none else R.string.select_all
            button_select_all.setText(res)
        }
    }
}

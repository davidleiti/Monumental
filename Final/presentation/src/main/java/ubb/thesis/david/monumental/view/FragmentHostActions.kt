package ubb.thesis.david.monumental.view

interface FragmentHostActions {
    fun setTitle(text: String?)
    fun disableUserNavigation()
    fun enableUserNavigation()

    fun displayProgress()
    fun hideProgress()
}
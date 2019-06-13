package ubb.thesis.david.monumental.view

interface UiActions {
    fun setTitle(text: String?)
    fun disableUserNavigation()
    fun enableUserNavigation()

    fun displayProgress()
    fun hideProgress()
}
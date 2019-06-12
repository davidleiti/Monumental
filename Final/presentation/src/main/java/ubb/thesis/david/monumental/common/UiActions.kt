package ubb.thesis.david.monumental.common

interface UiActions {
    fun setTitle(text: String?)
    fun disableUserNavigation()
    fun enableUserNavigation()

    fun displayProgress()
    fun hideProgress()
}
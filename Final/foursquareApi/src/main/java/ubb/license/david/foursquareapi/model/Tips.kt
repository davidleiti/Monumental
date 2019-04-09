package ubb.license.david.foursquareapi.model

data class Tips(private val count: Int, private val groups: List<TipGroup>) {
    fun get(): List<Tip> = groups.map { group -> group.items }.flatten()
}


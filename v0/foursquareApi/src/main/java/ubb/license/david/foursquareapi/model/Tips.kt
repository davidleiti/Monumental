package ubb.license.david.foursquareapi.model

data class Tips(private val count: Int, private val groups: Array<TipGroup>) {

    fun get(): List<Tip> = groups.map { group -> group.items }.toTypedArray().flatten()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Tips

        if (count != other.count) return false
        if (!groups.contentEquals(other.groups)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = count
        result = 31 * result + groups.contentHashCode()
        return result
    }
}

data class TipGroup(val items: Array<Tip>) {
    override fun toString(): String {
        return "Group(items=$items)"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as TipGroup

        if (!items.contentEquals(other.items)) return false

        return true
    }

    override fun hashCode(): Int {
        return items.contentHashCode()
    }
}

data class Tip(val id: String, val text: String)

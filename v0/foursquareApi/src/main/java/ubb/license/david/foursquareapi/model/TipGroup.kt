package ubb.license.david.foursquareapi.model


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
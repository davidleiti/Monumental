package model

data class Tips(val count: Int, val groups: Array<Group>) {
    inner class Group(val items: Array<Tip>) {
        override fun toString(): String {
            return "Group(items=$items)"
        }
    }
}

data class Tip(val id: String, val text: String)

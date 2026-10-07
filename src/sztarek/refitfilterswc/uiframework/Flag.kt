package sztarek.refitfilterswc.uiframework

data class Flag(var isEnabled: Boolean = true) {
    var isFiltered: Boolean
        get() = !isEnabled
        set(filtered) { isEnabled = !filtered }
}
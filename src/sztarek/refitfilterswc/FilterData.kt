package sztarek.refitfilterswc

import sztarek.refitfilterswc.uiframework.Flag

abstract class FilterData(
    var kineticDamage: Flag = Flag(),
    var heDamage: Flag = Flag(),
    var energyDamage: Flag = Flag(),
    var fragDamage: Flag = Flag(),
    var lowerRange: Float,
    var upperRange: Float,
    var currentSearch: String = ""
) {
    /** Class name -> flag (weapon classes or fighter classes). Entries are created on demand from what the player owns. */
    val weaponClasses: MutableMap<String, Flag> = LinkedHashMap()

    /** Which page of class rows is shown when there are more rows than fit. */
    var classPage: Int = 0

    /** True when at least one class is currently filtered out. */
    val anyWeaponClassFiltered: Boolean
        get() = weaponClasses.values.any { it.isFiltered }

    protected fun resetCommonFields(defaultLower: Float, defaultUpper: Float) {
        kineticDamage = Flag()
        heDamage = Flag()
        energyDamage = Flag()
        fragDamage = Flag()
        lowerRange = defaultLower
        upperRange = defaultUpper
        currentSearch = ""
        weaponClasses.clear()
        classPage = 0
    }

    abstract fun reset()
}

class WeaponFilterData: FilterData(
    lowerRange = RFSettings.weaponMinRange.toFloat(),
    upperRange = RFSettings.weaponMaxRange.toFloat()
) {
    var projectileWeapons: Flag = Flag()
    var beamWeapons: Flag = Flag()
    var pdWeapons: Flag = Flag()
    var nonpdWeapons: Flag = Flag()
    var ammoWeapons: Flag = Flag()
    var nonAmmoWeapons: Flag = Flag()

    override fun reset() {
        resetCommonFields(RFSettings.weaponMinRange.toFloat(), RFSettings.weaponMaxRange.toFloat())
        projectileWeapons = Flag()
        beamWeapons = Flag()
        pdWeapons = Flag()
        nonpdWeapons = Flag()
        ammoWeapons = Flag()
        nonAmmoWeapons = Flag()
    }
}

class FighterFilterData: FilterData(
    lowerRange = RFSettings.fighterMinRange.toFloat(),
    upperRange = RFSettings.fighterMaxRange.toFloat()
) {
    var supportWing: Flag = Flag()
    var fighterWing: Flag = Flag()
    var bomberWing: Flag = Flag()
    var interceptorWing: Flag = Flag()

    override fun reset() {
        resetCommonFields(RFSettings.fighterMinRange.toFloat(), RFSettings.fighterMaxRange.toFloat())
        supportWing = Flag()
        fighterWing = Flag()
        bomberWing = Flag()
        interceptorWing = Flag()
    }
}

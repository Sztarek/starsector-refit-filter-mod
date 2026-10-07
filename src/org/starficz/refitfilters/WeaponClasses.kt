package org.starficz.refitfilters

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.CargoAPI
import com.fs.starfarer.api.impl.campaign.ids.Submarkets
import com.fs.starfarer.api.loading.WeaponSpecAPI
import java.util.TreeMap

/**
 * Groups weapons into "classes" and discovers which classes the player currently owns.
 *
 * Ownership is read from the player fleet's cargo and, when docked at a market that has a
 * storage submarket, from that storage as well (the same two sources the vanilla refit
 * weapon picker draws from).
 */
object WeaponClasses {
    const val GROUP_DESIGN_TYPE = "Design Type"
    const val GROUP_WEAPON_TYPE = "Weapon Type"
    const val GROUP_SOURCE_MOD = "Source Mod"

    private const val VANILLA_NAME = "Starsector"

    fun classOf(spec: WeaponSpecAPI): String {
        return when (RFSettings.weaponClassGrouping) {
            GROUP_WEAPON_TYPE -> spec.type?.displayName ?: "Other"
            GROUP_SOURCE_MOD -> sourceModName(spec)
            else -> spec.manufacturer?.trim()?.takeIf { it.isNotEmpty() } ?: sourceModName(spec)
        }
    }

    private fun sourceModName(spec: WeaponSpecAPI): String = spec.sourceMod?.name ?: VANILLA_NAME

    /** Class name -> number of owned weapons of that class, sorted by name (case-insensitive). */
    fun ownedWeaponCounts(): Map<String, Int> {
        val counts = TreeMap<String, Int>(String.CASE_INSENSITIVE_ORDER)
        addCargo(Global.getSector()?.playerFleet?.cargo, counts)
        addCargo(dockedStorageCargo(), counts)
        return counts
    }

    private fun addCargo(cargo: CargoAPI?, counts: MutableMap<String, Int>) {
        if (cargo == null) return
        for (quantity in cargo.weapons) {
            val spec = try { Global.getSettings().getWeaponSpec(quantity.item) } catch (e: Exception) { null } ?: continue
            val name = classOf(spec)
            counts[name] = (counts[name] ?: 0) + quantity.count
        }
    }

    /** Storage cargo of the market the player is currently docked at, or null when not applicable. */
    fun dockedStorageCargo(): CargoAPI? {
        val market = Global.getSector()?.campaignUI?.currentInteractionDialog?.interactionTarget?.market ?: return null
        return market.getSubmarket(Submarkets.SUBMARKET_STORAGE)?.cargoNullOk
    }
}

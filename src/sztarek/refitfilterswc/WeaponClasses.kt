package sztarek.refitfilterswc

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.CargoAPI
import com.fs.starfarer.api.campaign.econ.MarketAPI
import com.fs.starfarer.api.impl.campaign.ids.Submarkets
import com.fs.starfarer.api.loading.WeaponSpecAPI
import org.json.JSONObject
import java.util.TreeMap

/**
 * Groups weapons into "classes" and discovers which classes the player currently owns.
 *
 * Ownership is read from the player fleet's cargo and, when docked at a market that has a
 * storage submarket, from that storage as well (the same two sources the vanilla refit
 * weapon picker draws from).
 *
 * The grouping (what counts as a class) and whether the class buttons are collapsed are
 * switched from the refit picker and remembered across sessions in saves/common.
 */
object WeaponClasses {
    const val GROUP_DESIGN_TYPE = "Design Type"
    const val GROUP_SOURCE_MOD = "Source Mod"
    val GROUPINGS = listOf(GROUP_DESIGN_TYPE, GROUP_SOURCE_MOD)

    private const val VANILLA_NAME = "Starsector"
    private const val PREFS_FILE = "refitfilters_weaponclass.json"
    private const val KEY_GROUPING = "grouping"
    private const val KEY_COLLAPSED = "collapsed"

    private val log = Global.getLogger(WeaponClasses::class.java)
    private var prefs: JSONObject? = null

    /** The active grouping. Defaults to design type. */
    var grouping: String
        get() = prefs().optString(KEY_GROUPING, GROUP_DESIGN_TYPE).takeIf { it in GROUPINGS } ?: GROUP_DESIGN_TYPE
        set(value) { prefs().put(KEY_GROUPING, value); savePrefs() }

    /** Whether the class buttons are hidden, leaving only the header row. */
    var collapsed: Boolean
        get() = prefs().optBoolean(KEY_COLLAPSED, false)
        set(value) { prefs().put(KEY_COLLAPSED, value); savePrefs() }

    fun cycleGrouping(): String {
        val next = GROUPINGS[(GROUPINGS.indexOf(grouping) + 1) % GROUPINGS.size]
        grouping = next
        return next
    }

    private fun prefs(): JSONObject {
        prefs?.let { return it }
        val loaded = try {
            if (Global.getSettings().fileExistsInCommon(PREFS_FILE))
                Global.getSettings().readJSONFromCommon(PREFS_FILE, false) else JSONObject()
        } catch (e: Exception) {
            log.warn("Could not read $PREFS_FILE, using defaults", e)
            JSONObject()
        }
        prefs = loaded
        return loaded
    }

    private fun savePrefs() {
        try {
            Global.getSettings().writeJSONToCommon(PREFS_FILE, prefs(), false)
        } catch (e: Exception) {
            log.warn("Could not write $PREFS_FILE", e)
        }
    }

    fun classOf(spec: WeaponSpecAPI): String {
        return when (grouping) {
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

    /** The market the player is currently docked at, or null when not docked. */
    fun dockedMarket(): MarketAPI? {
        return Global.getSector()?.campaignUI?.currentInteractionDialog?.interactionTarget?.market
    }

    /** Storage cargo of the market the player is currently docked at, or null when not applicable. */
    fun dockedStorageCargo(): CargoAPI? {
        return dockedMarket()?.getSubmarket(Submarkets.SUBMARKET_STORAGE)?.cargoNullOk
    }
}

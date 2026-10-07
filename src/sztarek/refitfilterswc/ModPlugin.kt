package sztarek.refitfilterswc

import com.fs.starfarer.api.BaseModPlugin
import com.fs.starfarer.api.Global
import lunalib.lunaSettings.LunaSettings

/**
 * This fork has its own mod id. If the original Refit Filters is enabled at the same time,
 * both would inject filter rows into the same picker, so the fork stands down entirely.
 */
object ConflictGuard {
    const val UPSTREAM_MOD_ID = "refitfilters"

    val upstreamEnabled: Boolean by lazy {
        val enabled = Global.getSettings().modManager.isModEnabled(UPSTREAM_MOD_ID)
        if (enabled) {
            Global.getLogger(ConflictGuard::class.java).warn(
                "Refit Filters (Weapon Class): the original Refit Filters is also enabled. " +
                "This fork replaces it, so enable only one of the two. The fork is doing nothing this session.")
        }
        enabled
    }
}

class ModPlugin : BaseModPlugin() {
    override fun onApplicationLoad() {
        if (ConflictGuard.upstreamEnabled) return
        LunaSettings.addSettingsListener(RFSettings)
    }

    override fun onGameLoad(newGame: Boolean) {
        if (ConflictGuard.upstreamEnabled) return
        Global.getSector().addTransientScript(CampaignUIAdderScript())
    }
}

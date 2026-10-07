package sztarek.refitfilterswc

import com.fs.starfarer.api.combat.BaseEveryFrameCombatPlugin
import com.fs.starfarer.api.input.InputEventAPI
import com.fs.starfarer.api.ui.UIPanelAPI
import com.fs.starfarer.title.TitleScreenState
import com.fs.state.AppDriver
import sztarek.refitfilterswc.uiframework.ReflectionUtils.invoke


class CombatUIAdderScript : BaseEveryFrameCombatPlugin() {

    override fun advance(amount: Float, events: MutableList<InputEventAPI>?) {
        if (ConflictGuard.upstreamEnabled) return

        val state = AppDriver.getInstance().currentState
        if (state !is TitleScreenState) return

        val core = state.invoke("getScreenPanel") as? UIPanelAPI ?: return

        FilterPanelCreator.modifyFilterPanels(core, openedFromCampaign = false, docked = false)
    }
}

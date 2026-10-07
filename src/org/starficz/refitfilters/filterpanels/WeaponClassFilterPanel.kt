package org.starficz.refitfilters.filterpanels

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.loading.WeaponSpecAPI
import com.fs.starfarer.api.ui.ButtonAPI
import com.fs.starfarer.api.ui.CustomPanelAPI
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.ui.UIPanelAPI
import com.fs.starfarer.api.util.Misc
import org.starficz.UIFramework.*
import org.starficz.refitfilters.PickerPanelHelpers
import org.starficz.refitfilters.RFSettings
import org.starficz.refitfilters.WeaponClasses
import org.starficz.refitfilters.WeaponFilterData
import java.util.TreeMap

private class ClassButton(val name: String, val owned: Int, val listed: Int, var width: Float)

private val log = Global.getLogger(WeaponClasses::class.java)

/**
 * One button per weapon class, wrapped over as many rows as needed. Classes come from two
 * sources merged together: the weapons the player owns (fleet cargo + local storage when
 * docked) and whatever the vanilla picker is currently listing for this slot.
 * Returns null when fewer than two classes are known, since there would be nothing to filter between.
 */
fun UIPanelAPI.createWeaponClassFilterPanel(
    width: Float,
    rowHeight: Float,
    pickerPanel: UIPanelAPI,
    filterData: WeaponFilterData,
    listedSpecs: List<WeaponSpecAPI>
): CustomPanelAPI? {

    val owned = WeaponClasses.ownedWeaponCounts()

    val listed = TreeMap<String, Int>(String.CASE_INSENSITIVE_ORDER)
    for (spec in listedSpecs) {
        val name = WeaponClasses.classOf(spec)
        listed[name] = (listed[name] ?: 0) + 1
    }

    val allClasses = TreeMap<String, Unit>(String.CASE_INSENSITIVE_ORDER)
    owned.keys.forEach { allClasses[it] = Unit }
    listed.keys.forEach { allClasses[it] = Unit }

    // keep the flag map in sync with the classes known right now
    filterData.weaponClasses.keys.retainAll(allClasses.keys)
    for (name in allClasses.keys) filterData.weaponClasses.getOrPut(name) { Flag() }

    log.info("RF-WC: owned classes = $owned")
    log.info("RF-WC: listed classes = $listed")
    log.info("RF-WC: flags = " + filterData.weaponClasses.entries.joinToString { "${it.key}=${if (it.value.isEnabled) "on" else "OFF"}" })

    if (allClasses.size < 2) return null

    val brightColor = Global.getSettings().basePlayerColor
    val baseColor = brightColor.darker()
    val bgColor = baseColor.darker().darker()
    val pad = 1f

    // measure labels so each button is as wide as its text needs
    val fontPath = getFontPath(Font.VICTOR_14)
    val buttons = allClasses.keys.map { name ->
        val textWidth = Global.getSettings().computeStringWidth(name.uppercase(), fontPath)
        ClassButton(name, owned[name] ?: 0, listed[name] ?: 0, (textWidth + 16f).coerceIn(40f, width))
    }

    // wrap into rows that fit the panel width, then stretch each row to fill it
    val rows = mutableListOf<MutableList<ClassButton>>()
    for (button in buttons) {
        val row = rows.lastOrNull()
        if (row == null || row.sumOf { it.width.toDouble() } + row.size * pad + button.width > width) {
            rows.add(mutableListOf(button))
        } else {
            row.add(button)
        }
    }
    for (row in rows) {
        val slack = width - row.sumOf { it.width.toDouble() }.toFloat() - (row.size - 1) * pad
        if (slack > 0f) row.forEach { it.width += slack / row.size }
    }

    val panelHeight = rows.size * rowHeight + (rows.size - 1) * pad
    val groupingLabel = RFSettings.weaponClassGrouping.lowercase()

    log.info("RF-WC: ${rows.size} row(s), panel height $panelHeight, rows = " +
            rows.joinToString(" | ") { row -> row.joinToString(", ") { "${it.name}:${it.width.toInt()}" } })

    return CustomPanel(width, panelHeight) {
        val classGroup = ButtonGroup()
        var rowStart: ButtonAPI? = null

        rows.forEachIndexed { rowIndex, row ->
            row.forEachIndexed { index, button ->
                val flag = filterData.weaponClasses[button.name] ?: Flag()
                val created = AreaCheckbox(button.name.uppercase(), baseColor, bgColor, brightColor,
                    button.width, rowHeight, font = Font.VICTOR_14, flag = flag, buttonGroup = classGroup) {

                    when {
                        rowIndex == 0 && index == 0 -> anchorInTopLeftOfParent()
                        index == 0 -> position.belowLeft(rowStart, pad)
                        else -> anchorRightOfPreviousMatchingMid(pad)
                    }

                    Tooltip(TooltipMakerAPI.TooltipLocation.ABOVE, 300f) {
                        addPara("Show weapons whose $groupingLabel is %s.", 0f,
                            Misc.getTextColor(), Misc.getHighlightColor(), button.name)
                        addPara("%s in this list, %s owned in cargo and local storage.", 0f,
                            Misc.getTextColor(), Misc.getHighlightColor(),
                            button.listed.toString(), button.owned.toString())
                        addPara("Click to show only this class, Shift/Ctrl + Click to toggle it. " +
                                "These buttons are rebuilt from the weapons you own.", 0f)
                    }
                    onClick {
                        log.info("RF-WC: clicked '${button.name}' (row $rowIndex, index $index)")
                        PickerPanelHelpers.filtersChanged(pickerPanel)
                    }
                }
                if (index == 0) rowStart = created
            }
        }
    }
}

package org.starficz.refitfilters.filterpanels

import com.fs.starfarer.api.Global
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

private class ClassButton(val name: String, val count: Int, var width: Float)

private val log = Global.getLogger(WeaponClasses::class.java)

/**
 * One button per weapon class the player currently owns (cargo + local storage when docked),
 * wrapped over as many rows as needed. Returns null when fewer than two classes are owned,
 * since there would be nothing to filter between.
 */
fun UIPanelAPI.createWeaponClassFilterPanel(
    width: Float,
    rowHeight: Float,
    pickerPanel: UIPanelAPI,
    filterData: WeaponFilterData
): CustomPanelAPI? {

    val owned = WeaponClasses.ownedWeaponCounts()

    // keep the flag map in sync with what is owned right now
    filterData.weaponClasses.keys.retainAll(owned.keys)
    for (name in owned.keys) filterData.weaponClasses.getOrPut(name) { Flag() }

    log.info("RF-WC: owned classes = $owned")
    log.info("RF-WC: flags = " + filterData.weaponClasses.entries.joinToString { "${it.key}=${if (it.value.isEnabled) "on" else "OFF"}" })

    if (owned.size < 2) return null

    val brightColor = Global.getSettings().basePlayerColor
    val baseColor = brightColor.darker()
    val bgColor = baseColor.darker().darker()
    val pad = 1f

    // measure labels so each button is as wide as its text needs
    val fontPath = getFontPath(Font.VICTOR_14)
    val buttons = owned.map { (name, count) ->
        val textWidth = Global.getSettings().computeStringWidth(name.uppercase(), fontPath)
        ClassButton(name, count, (textWidth + 16f).coerceIn(40f, width))
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
                        addPara("You have %s in cargo and local storage.", 0f,
                            Misc.getTextColor(), Misc.getHighlightColor(), button.count.toString())
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

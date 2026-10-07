package sztarek.refitfilterswc.filterpanels

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.loading.WeaponSpecAPI
import com.fs.starfarer.api.ui.ButtonAPI
import com.fs.starfarer.api.ui.CustomPanelAPI
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.ui.UIPanelAPI
import com.fs.starfarer.api.util.Misc
import sztarek.refitfilterswc.uiframework.*
import sztarek.refitfilterswc.PickerPanelHelpers
import sztarek.refitfilterswc.WeaponClasses
import sztarek.refitfilterswc.WeaponFilterData
import java.util.TreeMap

/** One cell in the row layout: either the grouping switch or a class button. */
private class Cell(val label: String, var width: Float, val className: String? = null, val owned: Int = 0, val listed: Int = 0) {
    val isGroupingSwitch get() = className == null
}

/**
 * A grouping switch followed by one button per weapon class, wrapped over as many rows as
 * needed. Classes come from two sources merged together: the weapons the player owns (fleet
 * cargo + local storage when docked) and whatever the vanilla picker is currently listing.
 * Returns null when no class is known at all.
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

    if (allClasses.isEmpty()) return null

    val brightColor = Global.getSettings().basePlayerColor
    val baseColor = brightColor.darker()
    val bgColor = baseColor.darker().darker()
    val pad = 1f
    val fontPath = getFontPath(Font.VICTOR_14)
    fun measure(text: String) = (Global.getSettings().computeStringWidth(text, fontPath) + 16f).coerceIn(40f, width)

    val grouping = WeaponClasses.grouping
    val groupingLabel = grouping.lowercase()

    // the grouping switch comes first, then one cell per class
    val cells = mutableListOf(Cell("BY: ${grouping.uppercase()}", measure("BY: ${grouping.uppercase()}")))
    for (name in allClasses.keys) {
        cells.add(Cell(name.uppercase(), measure(name.uppercase()), name, owned[name] ?: 0, listed[name] ?: 0))
    }

    // wrap into rows that fit the panel width, then stretch each row to fill it
    val rows = mutableListOf<MutableList<Cell>>()
    for (cell in cells) {
        val row = rows.lastOrNull()
        if (row == null || row.sumOf { it.width.toDouble() } + row.size * pad + cell.width > width) {
            rows.add(mutableListOf(cell))
        } else {
            row.add(cell)
        }
    }
    for (row in rows) {
        val slack = width - row.sumOf { it.width.toDouble() }.toFloat() - (row.size - 1) * pad
        if (slack > 0f) row.forEach { it.width += slack / row.size }
    }

    val panelHeight = rows.size * rowHeight + (rows.size - 1) * pad

    return CustomPanel(width, panelHeight) {
        val classGroup = ButtonGroup()
        var rowStart: ButtonAPI? = null

        rows.forEachIndexed { rowIndex, row ->
            row.forEachIndexed { index, cell ->
                val place: ButtonAPI.() -> Unit = {
                    when {
                        rowIndex == 0 && index == 0 -> anchorInTopLeftOfParent()
                        index == 0 -> position.belowLeft(rowStart, pad)
                        else -> anchorRightOfPreviousMatchingMid(pad)
                    }
                }

                val created = if (cell.isGroupingSwitch) {
                    AreaCheckbox(cell.label, baseColor, bgColor, brightColor, cell.width, rowHeight, font = Font.VICTOR_14) {
                        isChecked = true
                        place()
                        Tooltip(TooltipMakerAPI.TooltipLocation.ABOVE, 320f) {
                            addPara("What counts as a weapon's class. Click to switch.", 0f)
                            addSpacer(6f)
                            for (option in WeaponClasses.GROUPINGS) {
                                val mark = if (option == grouping) "[x]" else "[ ]"
                                val what = when (option) {
                                    WeaponClasses.GROUP_SOURCE_MOD -> "The mod the weapon comes from."
                                    else -> "Design type / manufacturer, falling back to the mod it comes from."
                                }
                                addPara("$mark $option - $what", 0f, Misc.getTextColor(), Misc.getHighlightColor(), option)
                            }
                        }
                        onClick {
                            WeaponClasses.cycleGrouping()
                            filterData.weaponClasses.clear() // class names change with the grouping
                            isChecked = true
                            PickerPanelHelpers.filtersChanged(pickerPanel)
                        }
                    }
                } else {
                    val flag = filterData.weaponClasses[cell.className] ?: Flag()
                    AreaCheckbox(cell.label, baseColor, bgColor, brightColor, cell.width, rowHeight,
                        font = Font.VICTOR_14, flag = flag, buttonGroup = classGroup) {
                        place()
                        Tooltip(TooltipMakerAPI.TooltipLocation.ABOVE, 300f) {
                            addPara("Show weapons whose $groupingLabel is %s.", 0f,
                                Misc.getTextColor(), Misc.getHighlightColor(), cell.className!!)
                            addPara("%s in this list, %s owned in cargo and local storage.", 0f,
                                Misc.getTextColor(), Misc.getHighlightColor(),
                                cell.listed.toString(), cell.owned.toString())
                            addPara("Click to show only this class, Shift/Ctrl + Click to toggle it. " +
                                    "These buttons are rebuilt from the weapons you own.", 0f)
                        }
                        onClick { PickerPanelHelpers.filtersChanged(pickerPanel) }
                    }
                }
                if (index == 0) rowStart = created
            }
        }
    }
}

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
import sztarek.refitfilterswc.RFSettings
import sztarek.refitfilterswc.WeaponClasses
import sztarek.refitfilterswc.WeaponFilterData
import java.util.TreeMap
import kotlin.math.max
import kotlin.math.min

/** One class button in the row layout. */
private class ClassCell(val name: String, var width: Float, val owned: Int, val listed: Int)

/**
 * A header row (hide/show, grouping switch, paging) followed by one button per weapon class,
 * wrapped into rows. At most [RFSettings.weaponClassMaxRows] rows are shown at once; the rest
 * are paged with the < > buttons or the mouse wheel. Classes come from the weapons the player
 * owns (fleet cargo + local storage when docked) merged with whatever the picker is listing.
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
    val collapsed = WeaponClasses.collapsed

    // wrap the class buttons into rows that fit the panel width, then stretch each row to fill it
    val rows = mutableListOf<MutableList<ClassCell>>()
    for (name in allClasses.keys) {
        val cell = ClassCell(name, measure(name.uppercase()), owned[name] ?: 0, listed[name] ?: 0)
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

    val maxRows = max(1, RFSettings.weaponClassMaxRows)
    val pageCount = max(1, (rows.size + maxRows - 1) / maxRows)
    filterData.classPage = filterData.classPage.coerceIn(0, pageCount - 1)

    // the panel keeps the same height on every page so the picker layout never shifts
    val visibleRows = if (collapsed) 0 else min(maxRows, rows.size)
    val panelHeight = rowHeight + visibleRows * (rowHeight + pad)

    return CustomPanel(width, panelHeight) { plugin ->
        val panel = this
        var pendingRender = false

        fun render() {
            panel.clearChildren()
            val page = filterData.classPage
            val selected = filterData.weaponClasses.values.count { it.isEnabled }

            // ---- header row ----
            val toggleLabel = if (collapsed) "SHOW" else "HIDE"
            val toggle = AreaCheckbox(toggleLabel, baseColor, bgColor, brightColor, measure(toggleLabel), rowHeight, font = Font.VICTOR_14) {
                isChecked = true
                anchorInTopLeftOfParent()
                Tooltip(TooltipMakerAPI.TooltipLocation.ABOVE, 300f) {
                    addPara(if (collapsed) "Show the weapon class buttons." else "Hide the weapon class buttons.", 0f)
                    addPara("Filters stay active while hidden: %s of %s classes selected.", 0f,
                        Misc.getTextColor(), Misc.getHighlightColor(), selected.toString(), allClasses.size.toString())
                }
                onClick {
                    WeaponClasses.collapsed = !collapsed
                    isChecked = true
                    PickerPanelHelpers.filtersChanged(pickerPanel) // height changes, so the picker re-lays out
                }
            }

            AreaCheckbox("BY: ${grouping.uppercase()}", baseColor, bgColor, brightColor,
                measure("BY: ${grouping.uppercase()}"), rowHeight, font = Font.VICTOR_14) {
                isChecked = true
                anchorRightOfPreviousMatchingMid(pad)
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
                    filterData.classPage = 0
                    isChecked = true
                    PickerPanelHelpers.filtersChanged(pickerPanel)
                }
            }

            if (!collapsed && pageCount > 1) {
                AreaCheckbox("<", baseColor, bgColor, brightColor, 26f, rowHeight, font = Font.VICTOR_14) {
                    isChecked = true
                    anchorRightOfPreviousMatchingMid(pad)
                    Tooltip(TooltipMakerAPI.TooltipLocation.ABOVE, 260f) {
                        addPara("Previous page of classes. The mouse wheel over this row does the same.", 0f)
                    }
                    onClick {
                        isChecked = true
                        if (filterData.classPage > 0) { filterData.classPage--; pendingRender = true }
                    }
                }
                Text(" ${page + 1}/$pageCount ", font = Font.VICTOR_14, color = brightColor) {
                    anchorRightOfPreviousMatchingMid(pad + 3f)
                }
                AreaCheckbox(">", baseColor, bgColor, brightColor, 26f, rowHeight, font = Font.VICTOR_14) {
                    isChecked = true
                    anchorRightOfPreviousMatchingMid(pad + 3f)
                    Tooltip(TooltipMakerAPI.TooltipLocation.ABOVE, 260f) {
                        addPara("Next page of classes. The mouse wheel over this row does the same.", 0f)
                    }
                    onClick {
                        isChecked = true
                        if (filterData.classPage < pageCount - 1) { filterData.classPage++; pendingRender = true }
                    }
                }
            }

            if (collapsed) return

            // ---- class rows for the current page ----
            val classGroup = ButtonGroup()
            var rowStart: ButtonAPI = toggle
            val first = page * maxRows
            val pageRows = rows.subList(first, min(rows.size, first + maxRows))

            // A plain click means "only this class", which has to switch off the classes on the
            // other pages as well, so every off-page flag joins the group before the visible buttons do.
            val visibleNames = pageRows.flatMap { row -> row.map { it.name } }.toHashSet()
            for ((name, flag) in filterData.weaponClasses) {
                if (name !in visibleNames) classGroup.allFlags.add(flag)
            }
            for (row in pageRows) {
                row.forEachIndexed { index, cell ->
                    val flag = filterData.weaponClasses[cell.name] ?: Flag()
                    val created = AreaCheckbox(cell.name.uppercase(), baseColor, bgColor, brightColor, cell.width, rowHeight,
                        font = Font.VICTOR_14, flag = flag, buttonGroup = classGroup) {
                        if (index == 0) position.belowLeft(rowStart, pad) else anchorRightOfPreviousMatchingMid(pad)
                        Tooltip(TooltipMakerAPI.TooltipLocation.ABOVE, 300f) {
                            addPara("Show weapons whose $groupingLabel is %s.", 0f,
                                Misc.getTextColor(), Misc.getHighlightColor(), cell.name)
                            addPara("%s in this list, %s owned in cargo and local storage.", 0f,
                                Misc.getTextColor(), Misc.getHighlightColor(),
                                cell.listed.toString(), cell.owned.toString())
                            addPara("Click to show only this class, Shift/Ctrl + Click to toggle it. " +
                                    "These buttons are rebuilt from the weapons you own.", 0f)
                        }
                        onClick { PickerPanelHelpers.filtersChanged(pickerPanel) }
                    }
                    if (index == 0) rowStart = created
                }
            }
        }

        // mouse wheel over the panel pages through the classes
        plugin.onHover { event ->
            if (event.isMouseScrollEvent && !collapsed && pageCount > 1) {
                val next = (filterData.classPage + if (event.eventValue > 0) -1 else 1).coerceIn(0, pageCount - 1)
                if (next != filterData.classPage) { filterData.classPage = next; pendingRender = true }
                event.consume()
            }
        }

        // re-render outside of input handling, which is where vanilla UI changes are safe
        plugin.advance {
            if (pendingRender) { pendingRender = false; render() }
        }

        render()
    }
}

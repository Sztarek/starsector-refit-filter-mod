package sztarek.refitfilterswc.filterpanels

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.ui.CustomPanelAPI
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.ui.UIPanelAPI
import sztarek.refitfilterswc.uiframework.*
import sztarek.refitfilterswc.PickerPanelHelpers
import sztarek.refitfilterswc.WeaponFilterData

/**
 * One row of three buttons filtering weapons by mount size. Useful on slots that accept
 * several sizes (large slots take small and medium weapons too), where the vanilla list
 * mixes all of them together.
 */
fun UIPanelAPI.createWeaponSizeFilterPanel(
    width: Float,
    height: Float,
    pickerPanel: UIPanelAPI,
    filterData: WeaponFilterData
): CustomPanelAPI {

    val brightColor = Global.getSettings().basePlayerColor
    val baseColor = brightColor.darker()
    val bgColor = baseColor.darker().darker()
    val pad = 1f
    val buttonWidth = (width - 2 * pad) / 3f

    return CustomPanel(width, height) {
        val sizeGroup = ButtonGroup()

        AreaCheckbox("SMALL", baseColor, bgColor, brightColor, buttonWidth, height,
            font = Font.VICTOR_14, flag = filterData.smallWeapons, buttonGroup = sizeGroup) {
            anchorInTopLeftOfParent()
            Tooltip(TooltipMakerAPI.TooltipLocation.ABOVE, 300f) {
                addPara("Show small weapons.", 0f)
            }
            onClick { PickerPanelHelpers.filtersChanged(pickerPanel) }
        }

        AreaCheckbox("MEDIUM", baseColor, bgColor, brightColor, buttonWidth, height,
            font = Font.VICTOR_14, flag = filterData.mediumWeapons, buttonGroup = sizeGroup) {
            anchorRightOfPreviousMatchingMid(pad)
            Tooltip(TooltipMakerAPI.TooltipLocation.ABOVE, 300f) {
                addPara("Show medium weapons.", 0f)
            }
            onClick { PickerPanelHelpers.filtersChanged(pickerPanel) }
        }

        AreaCheckbox("LARGE", baseColor, bgColor, brightColor, buttonWidth, height,
            font = Font.VICTOR_14, flag = filterData.largeWeapons, buttonGroup = sizeGroup) {
            anchorRightOfPreviousMatchingMid(pad)
            Tooltip(TooltipMakerAPI.TooltipLocation.ABOVE, 300f) {
                addPara("Show large weapons.", 0f)
            }
            onClick { PickerPanelHelpers.filtersChanged(pickerPanel) }
        }
    }
}

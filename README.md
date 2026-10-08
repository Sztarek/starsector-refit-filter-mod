# Refit Filters (Weapon Class)

A fork of [Refit Filters](https://fractalsoftworks.com/forum/index.php?topic=29327) by Starficz for Starsector 0.98a.
It keeps everything the original does and adds one more filter row to the refit weapon picker:
**Weapon Class**, with one button per class of weapon you actually own.

## What the Weapon Class row does

- Buttons are built dynamically from your fleet cargo, from local storage when you are docked,
  and from whatever the picker is listing for the slot. Own weapons from three factions, get three buttons.
- Click a button to show only that class. Shift or Ctrl + click adds another class. Clicking the last
  active button turns the whole row back on. Reset Filters, Ctrl + R and middle mouse reset it like the other rows.
- With many mods the list can get long, so only a few rows are shown at once (3 by default, a LunaLib setting). Further rows
  are paged with the < > buttons in the header or the mouse wheel over the row. HIDE / SHOW collapses the buttons to the
  header; filters stay active while hidden.
- Hover a button to see how many of that class are in the list and how many you own.
- The same row appears in the fighter wing picker. Wings are classed by the design type of their hull, or the
  mod they come from, and the buttons are built from the LPCs you own.

The first button of the row switches what counts as a class, and the choice is remembered across sessions:

| Grouping | Meaning |
| --- | --- |
| **Design Type** (default) | The weapon's design type / manufacturer. A weapon with no design type falls back to the mod it comes from. |
| Source Mod | The mod the weapon comes from. |

The row's position (1 to 5, or 0 to disable) is on the mod's LunaLib settings page.

## Requirements

- Starsector 0.98a
- [LazyLib](https://fractalsoftworks.com/forum/index.php?topic=5444) 3.0.0 or newer
- [LunaLib](https://fractalsoftworks.com/forum/index.php?topic=25658) 2.0.0 or newer

## Installing

Extract the zip into your `mods` folder and enable the mod in the launcher or your mod manager.

This fork replaces Refit Filters: enable one or the other, not both. If both are enabled, the fork
detects it, does nothing, and says so in the log. Safe to add to or remove from an existing save.

## Building from source

The mod is written in Kotlin. `build.ps1` compiles `src/` into `jars/RefitFilters.jar` with the
command-line Kotlin compiler, using the Kotlin runtime shipped by LazyLib.

1. Install JDK 17 and put it on `PATH`.
2. Unpack [kotlin-compiler-2.1.20.zip](https://github.com/JetBrains/kotlin/releases/tag/v2.1.20)
   to `%USERPROFILE%\.starsector-tools\kotlinc`, or set `KOTLINC_HOME` to wherever you unpacked it.
3. With the mod folder inside your Starsector `mods` folder, run:

   ```powershell
   powershell -ExecutionPolicy Bypass -File .\build.ps1
   ```

## Credits and license

Refit Filters and its UI framework are by Starficz. The fork renames the packages (`sztarek.refitfilterswc`
and `sztarek.refitfilterswc.uiframework`) so it can coexist with the original; the licenses follow the code.
The filter code is CC0-1.0, the UI framework is LGPL-3.0-only (full texts in `LICENSE-LGPL-3.0.txt` and
`LICENSE-GPL-3.0.txt`); see `LICENSE`. The Weapon Class filter in this fork is released under the same
terms as the code it extends (CC0-1.0). The two icon images are Starficz's artwork and are not covered
by the code licenses.

## AI disclosure

The Weapon Class filter in this fork was vibe coded: I (Sztarek) described what I wanted, an AI coding assistant
(Claude, by Anthropic) wrote the code, and I tested it in-game. Starficz's original code was not written with AI.

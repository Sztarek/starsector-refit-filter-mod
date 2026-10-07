## Version 3.2.3-weaponclass (fork)
- Removed the Weapon Type grouping (Ballistic / Energy / Missile): the vanilla slot filter already covers it. Groupings are now Design Type and Source Mod.

## Version 3.2.2-weaponclass (fork)
- The fork now has its own mod id (refitfilters_weaponclass) instead of reusing the id of Refit Filters. If the original Refit Filters is enabled alongside it, the fork does nothing and logs a warning.
- LunaLib settings for the fork live under the new id; the row position setting resets to its default once.

## Version 3.2.1-weaponclass (fork)
- The grouping switch (Design Type / Weapon Type / Source Mod) moved from the LunaLib settings page into the filter row itself. The choice is remembered in saves/common.

## Version 3.2.0-weaponclass (fork)
- Added a Weapon Class filter row. Buttons are built dynamically from the weapons you own: fleet cargo, plus local storage when docked.
- Class grouping is configurable in LunaLib settings: Design Type (default, falls back to source mod), Weapon Type, or Source Mod.
- Filter panel order settings now go up to 5; the Weapon Class row defaults to position 5 (0 disables it).
- Version checker now points at this fork.
- Included the full LGPL-3.0 and GPL-3.0 license texts for the bundled UI framework, and an AI disclosure (see LICENSE / README).

## Version 3.1.3
- Fixed memory leak

## Version 3.1.2
- Updated filter thresholds to be more strict for fuzzy matching

## Version 3.1.1
- Pinned down and fixed CTD on some machines. *Thanks ANU (anulackk) on Discord!*

## Version 3.1.0
- Added support for Fighter Wings
- Fixed Height position issues

## Version 3.0.1
- Fixed CTRL weapon compare bug with CTD in codex 

## Version 3.0.0
- 0.98 Compatibility
- Rewrote then entire mod again, using my new experience with Starsector UI.
- Added 2 new filters, Ammo and Non-Ammo
- Added the ability to change the range slider ranges arbitrarily
- Added the ability to change the order of the filters, and the ability to disable 2 of them

## Version 2.0.0
- NOT SAVE COMPATIBLE WITH 1.X.X
- Rewrote entire mod, now works off of the UI directly in the refit weapon panel
- Re-Added Utility Mod tag

## Version 1.1.1
- One string change to fix the jar not loading

## Version 1.1.0
- Updated to 0.97

## Version 1.0.1
- Fixed Conflict with Vanilla "Field Repairs" skill
- Fixed Deco/System/BuiltIn weapons being caught in the blacklist
- Fixed Log Spam
- Removed Utility Mod tag

## Version 1.0.0
- Initial Release

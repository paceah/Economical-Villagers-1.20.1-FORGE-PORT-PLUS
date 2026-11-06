- 1.9.0 (3 Jul. 2026):
  - Updated for 26.2
  - Migrated from [Cloth Config](https://modrinth.com/mod/cloth-config) to
    [MidnightLib](https://modrinth.com/mod/midnightlib)
- 1.8.0 (28 Apr. 2026): Updated for 26.1.x
- 1.7.0 (22 Dec. 2025): Updated for 1.21.11
- 1.6.1 (22 Dec. 2025): Ported fix from 1.5.6
- 1.5.6 (22 Dec. 2025): Fixed invisible text in the 'Configuration Unavailable' screen
- 1.6.0 (14 Oct. 2025): Updated for 1.21.9-1.21.10
- 1.5.5 (27 Jul. 2025): Added "Do vanilla restocks" option, defaults to `false`
- 1.5.4 (19 Jul. 2025): Marked as compatible with 1.21.8
- 1.5.3 (2 Jul. 2025): Marked as compatible with 1.21.7
- 1.5.2 (20 Jun. 2025): Marked as compatible with 1.21.6
- 1.5.1 (27 Apr. 2025): Ported 1.4.2 to MC 1.21.5
- 1.4.2 (27 Apr. 2025): Fixed clients seeing incorrect prices when connecting to a server with
"Enable reputation price adjustments" disabled
- 1.5.0 (9 Apr. 2025): Updated for 1.21.5
- 1.4.1 (18 Dec. 2024):
  - Marked as compatible with 1.21.4
  - Minor internal changes
- 1.4.0 (2 Dec. 2024): Updated for 1.21.2-1.21.3
- 1.3.1 (29 Aug. 2024): Marked as compatible with 1.21.1
- 1.3.0 (16 Jul. 2024):
  - Updated for 1.21!
  - Improved mod compatibility potential:
    - Whether a trade is a "buy trade" is now determined by whether the given item is tagged with
    `"economical_villager_trading:buy_trade_currencies"`
    - By default, `"economical_villager_trading:buy_trade_currencies"` contains `"c:villager_currencies"`,
    which contains `"minecraft:emerald"`
  - Replaced bundled [CrowdinTranslate](<https://github.com/gbl/CrowdinTranslate>) with optional
  [SSS Translate](<https://modrinth.com/mod/sss-translate>) dependency;
  install [SSS Translate](<https://modrinth.com/mod/sss-translate>) for automatic translation updates
  - Moderate internal changes (reduced spaghetti content by up to 31% or more!)
- 1.2.14 (9 May 2024):
  - Marked as compatible with 1.20.6
  - Improved [Mod Menu](https://modrinth.com/mod/modmenu) integration
- 1.2.13 (26 Apr. 2024):
  - Marked as compatible with 1.20.5
  - Minor internal changes
- 1.2.12 (1 Feb. 2024):
  - Updated for 1.20.3 and 1.20.4
  - Minor internal changes
- 1.2.11 (13 Nov. 2023): Updated for 1.20.2
- 1.2.10 (19 Jun. 2023): Updated for 1.20 and 1.20.1!
- 1.2.9 (22 Mar. 2023): Updated for 1.19.3 and 1.19.4
- 1.2.8 (7 Aug. 2022): Marked as compatible with 1.19.2
- 1.2.7 (29 Jul. 2022):
  - Marked as compatible with 1.19.1.
  - Minor internal changes.
- 1.2.6 (25 Jun. 2022): Updated for 1.19!
- 1.2.5 (15 Mar. 2022): Updated for 1.18.2
- 1.2.4 (18 Jan. 2022):
  
  - Added option to disable downloading translations on game launch
  - Cloth Config is now optional
  - Possibly fixed a bug where a newly reset trade couldn't be used until the trade gui was re-opened
  
- 1.2.3 (13 Dec. 2021): Marked as compatible with 1.18.1
- 1.2.2 (3 Dec. 2021): Updated for 1.18!
- 1.2.1 (14 Sep. 2021): *Finally* updated for 1.17(.1)!
- 1.2 (21 Mar. 2021): Added new config: 'Enable Reputation Price Adjustments'
  
  This affects things like hurting villagers, curing zombie villagers and just frequent trades.
  
  You can also now disable price adjustments due to usage by setting 'Villager Trade Adjust Balance' to 100 or greater.
  
  These changes were made because the vanilla trade adjustment system just doesn't work well with this mod, 
  
  trades would end up being far too expensive. 
  
- 1.1.1 (23 Feb. 2021): Added translation fetching via [CrowdinTranslate](https://github.com/gbl/CrowdinTranslate). 

  If you'd like to contribute a translation, follow [this link](https://crowdin.com/project/villager-see-villager-do/invite).

- 1.1 (22 Feb. 2021): 
  - Switched from gamrules to standard config (use Mod Menu or edit the json to change settings), new dependencies:
    - [Cloth Config API](https://www.curseforge.com/minecraft/mc-mods/cloth-config/files/all?filter-game-version=1738749986%3a70886)
    - [Auto Config Updated API](https://www.curseforge.com/minecraft/mc-mods/auto-config-updated-api/files/all?filter-game-version=1738749986%3a70886)
    - **both** of the above dependencies *must* be installed
  - Added new options:
    - Villager XP Multiplier: adjust how much xp villagers give when you trade with them, for balance
    - Enable villager levelup while trading: removes vanilla's requirement that you exit a villager's gui before it
    will levelup
- 1.0.3 (16 Jan. 2021): Marked as compatible with 1.16.5.
  Improved 'villagerTradeAdjustBalance' gamerule algorithm to make it more 'fair'.  
- 1.0.2 (3 Nov. 2020): Updated for 1.16.4. 
- 1.0.1-1 (17 Sep. 2020): Now properly marked as compatible with Minecraft 1.16.1-1.16.3. 
- 1.0.1 (17 Sep. 2020): Marked as compatible with Minecraft 1.16.3. Fixed (hopefully, pretty sure) possible error on
game start, [issue #2](https://gitlab.com/supersaiyansubtlety/economical-villager-trading/-/issues/2).  
- 1.0.0 (29 Aug. 2020): Out of beta! Fixes a non-crashing exception that occurred when opening a wandering trader's
inventory. 

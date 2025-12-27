# Supported Launchers

This document lists all launchers supported by the app and their icon pack apply methods.

## Direct Apply Launchers

These launchers support automatic icon pack application via intent/broadcast:

| Launcher | Package Name(s) |
|----------|-----------------|
| Action | `com.actionlauncher.playstore`, `com.chrislacy.actionlauncher.pro` |
| ADW | `org.adw.launcher`, `org.adwfreak.launcher` |
| Apex | `com.anddoes.launcher`, `com.anddoes.launcher.pro` |
| Atom | `com.dlto.atom.launcher` |
| Aviate | `com.tul.aviate` |
| Before | `com.beforesoft.launcher` |
| CM Theme | `org.cyanogenmod.theme.chooser` |
| Flick | `com.universallauncher.universallauncher` |
| GO EX | `com.gau.go.launcherex` |
| Ion Launcher | `one.zagura.IonLauncher` |
| Lawnchair Legacy | `ch.deletescape.lawnchair`, `ch.deletescape.lawnchair.plah`, `ch.deletescape.lawnchair.ci` |
| Lucid | `com.powerpoint45.launcher` |
| mLauncher | `app.mlauncher` |
| Niagara | `bitpit.launcher` |
| Nougat | `me.craftsapp.nlauncher` |
| Nova | `com.teslacoilsw.launcher`, `com.teslacoilsw.launcher.prime` |
| Omega | `com.saggitt.omega` |
| Posidon | `posidon.launcher` |
| Projectivy | `com.spocky.projengmenu` |
| Smart | `ginlemon.flowerfree`, `ginlemon.flowerpro`, `ginlemon.flowerpro.special` |
| Solo | `home.solo.launcher.free` |
| Square | `com.ss.squarehome2` |
| Total | `com.ss.launcher2` |
| ZenUI | `com.asus.launcher` |

## Manual Apply Launchers

These launchers require manual icon pack application through their settings:

| Launcher | Package Name(s) | Settings Activity |
|----------|-----------------|-------------------|
| BlackBerry | `com.blackberry.blackberrylauncher` | `MainActivity` |
| Holo | `com.mobint.hololauncher` | `SettingsActivity` |
| Holo HD | `com.mobint.hololauncher.hd` | `SettingsActivity` |
| Hyperion | `projekt.launcher` | `SettingsActivity` |
| Lawnchair | `app.lawnchair`, `app.lawnchair.play` | `PreferenceActivity` |
| Moto | `com.motorola.launcher3` | `IconPacksActivity` |
| TinyBit | `rocks.tbog.tblauncher` | `SettingsActivity` |

## Manual Apply (No Settings Activity)

These launchers support icon packs but require manual steps without a direct settings link:

| Launcher | Package Name(s) | Notes |
|----------|-----------------|-------|
| ColorOS | `com.oppo.launcher` | Apply via system wallpaper settings (Android 11+) |
| HiOS | `com.transsion.hilauncher` | Apply via launcher settings |
| KISS | `fr.neamar.kiss` | Apply via launcher settings |
| Kvaesitso | `de.mm20.launcher2.release` | Apply via launcher settings |
| Nothing | `com.nothing.launcher` | Apply via launcher settings |
| OxygenOS | `net.oneplus.launcher` | Apply via system settings (Android 9+) |
| Samsung OneUI | `com.sec.android.app.launcher` | Requires Theme Park app (Android 12+) |
| Stock Legacy | `com.android.launcher` | ColorOS/OxygenOS/realme UI merged launcher |

## Incompatible Launchers

These launchers do not support third-party icon packs:

| Launcher | Package Name(s) |
|----------|-----------------|
| LG Home | `com.lge.launcher2`, `com.lge.launcher3` |
| Pixel | `com.google.android.apps.nexuslauncher` |

## Root-Only Launchers

These launchers require root access to apply icon packs:

| Launcher | Package Name(s) |
|----------|-----------------|
| Evie | `is.shortcut` |
| Microsoft | `com.microsoft.launcher` |

## Other Supported Launchers

| Launcher | Package Name(s) | Apply Method |
|----------|-----------------|--------------|
| ABC | `com.abclauncher.launcher` | Direct (broadcast) |
| M Launcher | `com.uprui.launcher.marshmallow` | Direct |
| Next | `com.gtp.nextlauncher`, `com.gtp.nextlauncher.trial` | Direct (broadcast) |
| Open Launcher | `com.benny.openlauncher` | Unknown |
| POCO | `com.mi.android.globallauncher` | Manual |
| Rootless | `amirz.rootless.nexuslauncher` | Unknown |
| V Launcher | `com.vivid.launcher` | Direct (broadcast) |
| Zero | `com.zeroteam.zerolauncher` | Direct (broadcast) |

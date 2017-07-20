package dev.ukanth.iconmgr;


import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;

/*
 * CandyBar - Material Dashboard
 *
 * Copyright (c) 2014-2016 Dani Mahardhika
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

public class LauncherHelper {

    private static final int UNKNOWN = -1;
    private static final int ACTION = 1;
    private static final int ADW = 2;
    private static final int APEX = 3;
    private static final int ATOM = 4;
    private static final int AVIATE = 5;
    private static final int CMTHEME = 6;
    private static final int GO = 7;
    private static final int HOLO = 8;
    private static final int HOLOHD = 9;
    private static final int LGHOME = 10;
    private static final int LGHOME3 = 11;
    private static final int LUCID = 12;
    private static final int MINI = 13;
    private static final int NEXT = 14;
    private static final int NOVA = 15;
    private static final int SMART = 16;
    private static final int SOLO = 17;
    private static final int ZENUI = 18;
    private static final int NOUGAT = 19;
    private static final int M = 20;
    private static final int ZERO = 21;
    private static final int V = 22;
    private static final int ABC = 23;
    private static final int EVIE = 24;

    private static int getLauncherId(String packageName) {
        if (packageName == null) return UNKNOWN;
        switch (packageName) {
            case "com.actionlauncher.playstore":
            case "com.chrislacy.actionlauncher.pro":
                return ACTION;
            case "org.adw.launcher":
            case "org.adwfreak.launcher":
                return ADW;
            case "com.anddoes.launcher":
            case "com.anddoes.launcher.pro":
                return APEX;
            case "com.dlto.atom.launcher":
                return ATOM;
            case "com.tul.aviate":
                return AVIATE;
            case "org.cyanogenmod.theme.chooser":
                return CMTHEME;
            case "com.gau.go.launcherex":
                return GO;
            case "com.mobint.hololauncher":
                return HOLO;
            case "com.mobint.hololauncher.hd":
                return HOLOHD;
            case "com.lge.launcher2":
                return LGHOME;
            case "com.lge.launcher3":
                return LGHOME3;
            case "com.powerpoint45.launcher":
                return LUCID;
            case "com.jiubang.go.mini.launcher":
                return MINI;
            case "com.gtp.nextlauncher":
            case "com.gtp.nextlauncher.trial":
                return NEXT;
            case "com.teslacoilsw.launcher":
            case "com.teslacoilsw.launcher.prime":
                return NOVA;
            case "ginlemon.flowerfree":
            case "ginlemon.flowerpro":
            case "ginlemon.flowerpro.special":
                return SMART;
            case "home.solo.launcher.free":
                return SOLO;
            case "com.asus.launcher":
                return ZENUI;
            case "me.craftsapp.nlauncher":
                return NOUGAT;
            case "com.uprui.launcher.marshmallow":
                return M;
            case "com.zeroteam.zerolauncher":
                return ZERO;
            case "com.vivid.launcher":
                return V;
            case "com.abclauncher.launcher":
                return ABC;
            case "is.shortcut":
                return EVIE;
            default:
                return UNKNOWN;
        }
    }

    public static void apply(@NonNull Context context, String packageName, String launcherName) {
        applyLauncher(context, packageName, launcherName, getLauncherId(launcherName));
    }

    private static void applyLauncher(@NonNull Context context, String launcherPackage, String launcherName, int id) {
        switch (id) {
            case ACTION:
                try {
                    final Intent action = context.getPackageManager().getLaunchIntentForPackage(
                            launcherPackage);
                    action.putExtra("apply_icon_pack", launcherPackage);
                    action.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(action);

                } catch (ActivityNotFoundException | NullPointerException e) {
                    openGooglePlay(context, launcherPackage, launcherName);
                }
                break;
            case ADW:
                try {
                    final Intent adw = new Intent("org.adw.launcher.SET_THEME");
                    adw.putExtra("org.adw.launcher.theme.NAME", launcherPackage);
                    adw.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(adw);

                } catch (ActivityNotFoundException | NullPointerException e) {
                    openGooglePlay(context, launcherPackage, launcherName);
                }
                break;
            case APEX:
                try {
                    final Intent apex = new Intent("com.anddoes.launcher.SET_THEME");
                    apex.putExtra("com.anddoes.launcher.THEME_PACKAGE_NAME", launcherPackage);
                    apex.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(apex);

                } catch (ActivityNotFoundException | NullPointerException e) {
                    openGooglePlay(context, launcherPackage, launcherName);
                }
                break;
            case ATOM:
                try {
                    final Intent atom = new Intent("com.dlto.atom.launcher.intent.action.ACTION_VIEW_THEME_SETTINGS");
                    atom.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    atom.putExtra("packageName", launcherPackage);
                    context.startActivity(atom);

                } catch (ActivityNotFoundException | NullPointerException e) {
                    openGooglePlay(context, launcherPackage, launcherName);
                }
                break;
            case AVIATE:
                try {
                    final Intent aviate = new Intent("com.tul.aviate.SET_THEME");
                    aviate.putExtra("THEME_PACKAGE", launcherPackage);
                    aviate.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(aviate);

                } catch (ActivityNotFoundException | NullPointerException e) {
                    openGooglePlay(context, launcherPackage, launcherName);
                }
                break;
            case CMTHEME:
                try {
                    final Intent cmtheme = new Intent("android.intent.action.MAIN");
                    cmtheme.setComponent(new ComponentName(launcherPackage,
                            "org.cyanogenmod.theme.chooser.ChooserActivity"));
                    cmtheme.putExtra("pkgName", launcherPackage);
                    context.startActivity(cmtheme);
                } catch (ActivityNotFoundException | NullPointerException e) {
                    /*Toast.makeText(context, R.string.apply_cmtheme_not_available,
                            Toast.LENGTH_LONG).show();*/
                } catch (SecurityException | IllegalArgumentException e) {
                    /*Toast.makeText(context, R.string.apply_cmtheme_failed,
                            Toast.LENGTH_LONG).show();*/
                }
                break;
            case GO:
                try {
                    final Intent goex = context.getPackageManager().getLaunchIntentForPackage(
                            "com.gau.go.launcherex");
                    final Intent go = new Intent("com.gau.go.launcherex.MyThemes.mythemeaction");
                    go.putExtra("type", 1);
                    go.putExtra("pkgname", launcherPackage);
                    goex.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.sendBroadcast(go);
                    context.startActivity(goex);

                } catch (ActivityNotFoundException | NullPointerException e) {
                    openGooglePlay(context, launcherPackage, launcherName);
                }
                break;
            case HOLO:
                applyManual(context, launcherPackage, launcherName, "com.mobint.hololauncher.SettingsActivity");
                break;
            case HOLOHD:
                applyManual(context, launcherPackage, launcherName, "com.mobint.hololauncher.SettingsActivity");
                break;
            case LUCID:
                try {
                    final Intent lucid = new Intent("com.powerpoint45.action.APPLY_THEME", null);
                    lucid.putExtra("icontheme", launcherPackage);
                    context.startActivity(lucid);

                } catch (ActivityNotFoundException | NullPointerException e) {
                    openGooglePlay(context, launcherPackage, launcherName);
                }
                break;
            case MINI:
                applyManual(context, launcherPackage, launcherName,
                        "com.jiubang.go.mini.launcher.setting.MiniLauncherSettingActivity");
                break;
            case NEXT:
                try {
                    Intent next = context.getPackageManager().getLaunchIntentForPackage("com.gtp.nextlauncher");
                    if (next == null) {
                        next = context.getPackageManager().getLaunchIntentForPackage("com.gtp.nextlauncher.trial");
                    }
                    final Intent next2 = new Intent("com.gau.go.launcherex.MyThemes.mythemeaction");
                    next2.putExtra("type", 1);
                    next2.putExtra("pkgname", launcherPackage);
                    next.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.sendBroadcast(next2);
                    context.startActivity(next);

                } catch (ActivityNotFoundException | NullPointerException e) {
                    openGooglePlay(context, launcherPackage, launcherName);
                }
                break;
            case NOVA:
                try {
                    final Intent nova = new Intent("com.teslacoilsw.launcher.APPLY_ICON_THEME");
                    nova.setPackage("com.teslacoilsw.launcher");
                    nova.putExtra("com.teslacoilsw.launcher.extra.ICON_THEME_TYPE", "GO");
                    nova.putExtra("com.teslacoilsw.launcher.extra.ICON_THEME_PACKAGE", launcherPackage);
                    nova.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(nova);
                    //restart NOVA
                } catch (ActivityNotFoundException | NullPointerException e) {
                    openGooglePlay(context, launcherPackage, launcherName);
                }
                break;
            case SMART:
                try {
                    final Intent smart = new Intent("ginlemon.smartlauncher.setGSLTHEME");
                    smart.putExtra("package", launcherPackage);
                    smart.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(smart);

                } catch (ActivityNotFoundException | NullPointerException e) {
                    openGooglePlay(context, launcherPackage, launcherName);
                }
                break;
            case SOLO:
                try {
                    final Intent solo = context.getPackageManager().getLaunchIntentForPackage(
                            "home.solo.launcher.free");
                    final Intent soloAction = new Intent("home.solo.launcher.free.APPLY_THEME");
                    soloAction.putExtra("EXTRA_THEMENAME", context.getResources().getString(
                            R.string.app_name));
                    soloAction.putExtra("EXTRA_PACKAGENAME", launcherPackage);
                    solo.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.sendBroadcast(soloAction);
                    context.startActivity(solo);

                } catch (ActivityNotFoundException | NullPointerException e) {
                    openGooglePlay(context, launcherPackage, launcherName);
                }
                break;
            case ZENUI:
                try {
                    final Intent asus = new Intent("com.asus.launcher");
                    asus.setAction("com.asus.launcher.intent.action.APPLY_ICONPACK");
                    asus.addCategory(Intent.CATEGORY_DEFAULT);
                    asus.putExtra("com.asus.launcher.iconpack.PACKAGE_NAME", launcherPackage);
                    asus.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(asus);

                } catch (ActivityNotFoundException | NullPointerException e) {
                    openGooglePlay(context, launcherPackage, launcherName);
                }
                break;
            case NOUGAT:
                try {
                    /*
                     * Just want to let anyone who is going to copy
                     * It's not easy searching for this
                     * I will be grateful if you take this with a proper credit
                     * Thank you
                     */
                    final Intent nougat = new Intent("me.craftsapp.nlauncher");
                    nougat.setAction("me.craftsapp.nlauncher.SET_THEME");
                    nougat.putExtra("me.craftsapp.nlauncher.theme.NAME", launcherPackage);
                    nougat.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(nougat);

                } catch (ActivityNotFoundException | NullPointerException e) {
                    openGooglePlay(context, launcherPackage, launcherName);
                }
                break;
            case M:
                try {
                    /*
                     * Just want to let anyone who is going to copy
                     * It's not easy searching for this
                     * I will be grateful if you take this with a proper credit
                     * Thank you
                     */
                    final Intent newLauncher = new Intent("com.uprui.launcher.marshmallow");
                    newLauncher.setAction("com.uprui.launcher.marshmallow.SET_THEME");
                    newLauncher.putExtra("com.uprui.launcher.marshmallow.theme.NAME", launcherPackage);
                    newLauncher.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(newLauncher);

                } catch (ActivityNotFoundException | NullPointerException e) {
                    openGooglePlay(context, launcherPackage, launcherName);
                }
                break;
            case ZERO:
                try {
                     /*
                     * Just want to let anyone who is going to copy
                     * It's not easy searching for this
                     * I will be grateful if you take this with a proper credit
                     * Thank you
                     */
                    final Intent zero = context.getPackageManager().getLaunchIntentForPackage(
                            "com.zeroteam.zerolauncher");
                    final Intent zero1 = new Intent("com.zeroteam.zerolauncher.MyThemes.mythemeaction");
                    zero1.putExtra("type", 1);
                    zero1.putExtra("pkgname", launcherPackage);
                    zero.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.sendBroadcast(zero1);
                    context.startActivity(zero);

                } catch (ActivityNotFoundException | NullPointerException e) {
                    openGooglePlay(context, launcherPackage, launcherName);
                }
                break;
            case V:
                try {
                     /*
                     * Just want to let anyone who is going to copy
                     * It's not easy searching for this
                     * I will be grateful if you take this with a proper credit
                     * Thank you
                     */
                    final Intent v = context.getPackageManager().getLaunchIntentForPackage(
                            "com.vivid.launcher");
                    final Intent v1 = new Intent("com.vivid.launcher.MyThemes.mythemeaction");
                    v1.putExtra("type", 1);
                    v1.putExtra("pkgname", launcherPackage);
                    v.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.sendBroadcast(v1);
                    context.startActivity(v);

                } catch (ActivityNotFoundException | NullPointerException e) {
                    openGooglePlay(context, launcherPackage, launcherName);
                }
                break;
            case ABC:
                try {
                     /*
                     * Just want to let anyone who is going to copy
                     * It's not easy searching for this
                     * I will be grateful if you take this with a proper credit
                     * Thank you
                     */
                    final Intent abc = context.getPackageManager().getLaunchIntentForPackage(
                            "com.abclauncher.launcher");
                    final Intent abc1 = new Intent("com.abclauncher.launcher.themes.themeaction");
                    abc1.putExtra("theme_package_name", launcherPackage);
                    abc.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.sendBroadcast(abc1);
                    context.startActivity(abc);

                } catch (ActivityNotFoundException | NullPointerException e) {
                    openGooglePlay(context, launcherPackage, launcherName);
                }
                break;
            case EVIE:
                applyEvie(context, launcherPackage, launcherName);
                break;
        }
    }

    private static void applyManual(final Context context, final String launcherPackage, final String launcherName, final String activity) {
        new MaterialDialog.Builder(context)
                .typeface("Font-Medium.ttf", "Font-Regular.ttf")
                .title(launcherName)
                .content(context.getResources().getString(R.string.apply_manual,
                        launcherName,
                        context.getResources().getString(R.string.app_name)))
                .positiveText(context.getResources().getString(R.string.ok))
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        try {
                            final Intent intent = new Intent(Intent.ACTION_MAIN);
                            intent.setComponent(new ComponentName(launcherPackage,
                                    activity));
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            context.startActivity(intent);

                        } catch (ActivityNotFoundException | NullPointerException e) {
                            openGooglePlay(context, launcherPackage, launcherName);
                        } catch (SecurityException | IllegalArgumentException e) {
                            Toast.makeText(context, String.format(context.getResources().getString(
                                    R.string.apply_launch_failed), launcherName),
                                    Toast.LENGTH_LONG).show();
                        }
                    }
                })
                .negativeText(context.getResources().getString(R.string.cancel))
                .show();
    }

    private static void applyEvie(final Context context, final String launcherPackage, final String launcherName) {
        new MaterialDialog.Builder(context)
                .typeface("Font-Medium.ttf", "Font-Regular.ttf")
                .title(launcherName)
                .content(context.getResources().getString(R.string.apply_manual,
                        launcherName,
                        context.getResources().getString(R.string.app_name)) + "\n\n" +
                        context.getResources().getString(R.string.apply_manual_evie,
                                context.getResources().getString(R.string.app_name)))
                .positiveText(context.getResources().getString(R.string.ok))
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        try {
                            final Intent intent = new Intent(Intent.ACTION_MAIN);
                            intent.setComponent(new ComponentName(launcherPackage,
                                    "com.voxel.launcher3.Launcher"));
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_NO_HISTORY);
                            context.startActivity(intent);

                        } catch (ActivityNotFoundException | NullPointerException e) {
                            openGooglePlay(context, launcherPackage, launcherName);
                        } catch (SecurityException | IllegalArgumentException e) {
                            Toast.makeText(context, String.format(context.getResources().getString(
                                    R.string.apply_launch_failed), launcherName),
                                    Toast.LENGTH_LONG).show();
                        }
                    }
                }).negativeText(context.getResources().getString(R.string.cancel))
                .show();
    }

    private static void openGooglePlay(final Context context, final String packageName, final String launcherName) {
        new MaterialDialog.Builder(context)
                .typeface("Font-Medium.ttf", "Font-Regular.ttf")
                .title(launcherName)
                .content(String.format(context.getString(R.string.apply_launcher_not_installed), launcherName))
                .positiveText(context.getString(R.string.install))
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        try {
                            Intent store = new Intent(Intent.ACTION_VIEW, Uri.parse(
                                    "https://play.google.com/store/apps/details?id=" + launcherName));
                            context.startActivity(store);
                        } catch (ActivityNotFoundException e) {
                            Toast.makeText(context, context.getResources().getString(
                                    R.string.no_browser), Toast.LENGTH_LONG).show();
                        }
                    }
                })
                .negativeText(R.string.cancel)
                .show();
    }
}
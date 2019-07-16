package dev.ukanth.iconmgr.util;


import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.stericson.roottools.RootTools;

import java.util.HashMap;

import dev.ukanth.iconmgr.App;
import dev.ukanth.iconmgr.Prefs;
import dev.ukanth.iconmgr.R;
import dev.ukanth.iconmgr.dao.DaoSession;
import dev.ukanth.iconmgr.dao.IPObj;
import dev.ukanth.iconmgr.dao.IPObjDao;

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

    public static final int UNKNOWN = -1;
    public static final int ACTION = 1;
    public static final int ADW = 2;
    public static final int APEX = 3;
    public static final int ATOM = 4;
    public static final int AVIATE = 5;
    public static final int CMTHEME = 6;
    public static final int GO = 7;
    // public static final int HOLO = 8;
    //public static final int HOLOHD = 9;
    //public static final int LGHOME = 10;
    private static final int LAWNCHAIR = 10;
    //public static final int LGHOME3 = 11;
    public static final int LUCID = 12;
    // public static final int MINI = 13;
    public static final int NEXT = 14;
    public static final int NOVA = 15;
    public static final int SMART = 16;
    public static final int SOLO = 17;
    public static final int ZENUI = 18;
    public static final int NOUGAT = 19;
    public static final int M = 20;
    public static final int ZERO = 21;
    public static final int V = 22;
    public static final int ABC = 23;
    public static final int EVIE = 24;
    public static final int ARROW = 25;
    public static final int FLICK = 26;
    public static final int PIXEL = 27;


    public static final int POSIDON = 28;
    public static final int TOTAL = 29;




    public static int getLauncherId(String packageName) {
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
           /* case "com.mobint.hololauncher":
                return HOLO;
            case "com.mobint.hololauncher.hd":
                return HOLOHD;
            case "com.lge.launcher2":
               return LGHOME;
            case "com.lge.launcher3":
               return LGHOME3;*/

            case "ch.deletescape.lawnchair":
            case "ch.deletescape.lawnchair.plah":
            case "ch.deletescape.lawnchair.ci":
                return LAWNCHAIR;

            case "com.powerpoint45.launcher":
                return LUCID;
            /*case "com.jiubang.go.mini.launcher":
                return MINI;*/
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
            case "com.microsoft.launcher":
                return ARROW;
            case "com.universallauncher.universallauncher":
                return FLICK;
            case "com.google.android.apps.nexuslauncher":
                return PIXEL;
            case "posidon.launcher":
                return POSIDON;
            case "com.ss.launcher2":
                return TOTAL;
            default:
                return UNKNOWN;
        }
    }

    public static String getLauncherPackage(Context context) {
        PackageManager localPackageManager = context.getPackageManager();
        Intent intent = new Intent("android.intent.action.MAIN");
        intent.addCategory("android.intent.category.HOME");
        String packageName = localPackageManager.resolveActivity(intent,
                PackageManager.MATCH_DEFAULT_ONLY).activityInfo.packageName;
        return packageName;
    }

    public static String getLauncherName(Context context, String pkgName) {
        PackageManager pm = context.getPackageManager();
        ApplicationInfo ai;
        try {
            ai = pm.getApplicationInfo(pkgName, 0);
        } catch (final PackageManager.NameNotFoundException e) {
            ai = null;
        }
        return (String) (ai != null ? pm.getApplicationLabel(ai) : "Unknown");
    }

    public static int getLauncherId(Context context) {
        return getLauncherId(getLauncherPackage(context));
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
            /*case HOLO:
                applyManual(context, launcherPackage, launcherName, "com.mobint.hololauncher.SettingsActivity");
                break;
            case HOLOHD:
                applyManual(context, launcherPackage, launcherName, "com.mobint.hololauncher.SettingsActivity");
                break;*/
            case LUCID:
                try {
                    final Intent lucid = new Intent("com.powerpoint45.action.APPLY_THEME", null);
                    lucid.putExtra("icontheme", launcherPackage);
                    context.startActivity(lucid);

                } catch (ActivityNotFoundException | NullPointerException e) {
                    openGooglePlay(context, launcherPackage, launcherName);
                }
                break;
           /* case MINI:
                applyManual(context, launcherPackage, launcherName,
                        "com.jiubang.go.mini.launcher.setting.MiniLauncherSettingActivity");
                break;*/
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
                } catch (ActivityNotFoundException | NullPointerException e) {
                    openGooglePlay(context, launcherPackage, launcherName);
                }
                break;
            case ARROW:
                try {
                    HashMap<String, String> data = new HashMap<>();
                    data.put("cur_iconpack_package", launcherPackage);
                    data.put("cur_iconpack_name", getLabel(launcherPackage, context));
                    if (RootTools.isRootAvailable() && Prefs.useRoot()) {
                        Util.changeSharedPreferences(context, "com.microsoft.launcher", data, "GadernSalad.xml", true);
                    } else {
                        Toast.makeText(context, context.getString(R.string.onlysupportedroot), Toast.LENGTH_SHORT).show();
                    }
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
            case LAWNCHAIR:
                try {
                    final Intent lawnchair = context.getPackageManager().getLaunchIntentForPackage(
                            "ch.deletescape.lawnchair.plah");
                    final Intent lawnchairAction = new Intent("ch.deletescape.lawnchair.APPLY_ICONS");
                    lawnchairAction.putExtra("packageName", context.getPackageName());
                    lawnchairAction.addCategory(Intent.CATEGORY_DEFAULT);
                    lawnchair.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.sendBroadcast(lawnchairAction);
                    context.startActivity(lawnchair);
                    ((AppCompatActivity) context).finish();
                } catch (ActivityNotFoundException | NullPointerException e) {
                    e.printStackTrace();
                    //openGooglePlay(context, "ch.deletescape.lawnchair.plah", launcherName);
                }
                break;
            case TOTAL:
                try {

                    final Intent total = context.getPackageManager().getLaunchIntentForPackage(
                            "com.ss.launcher2");

                    final Intent totalIntent = new Intent("com.ss.launcher2.ACTION_APPLY_ICONPACK");
                    totalIntent.putExtra("com.ss.iconpack.PickIconActivity.extra.ICON_PACK", launcherPackage);
                    totalIntent.putExtra("com.ss.iconpack.PickIconActivity.extra.ICON", launcherPackage);
                    context.sendBroadcast(totalIntent);
                    total.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(total);
                    ((AppCompatActivity) context).finish();
                } catch (ActivityNotFoundException | NullPointerException e) {
                    e.printStackTrace();
                    //openGooglePlay(context, "ch.deletescape.lawnchair.plah", launcherName);
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
                try {
                    HashMap<String, String> data = new HashMap<>();
                    data.put("package_name", launcherPackage);
                    data.put("label", getLabel(launcherPackage, context));
                    if (RootTools.isRootAvailable() && Prefs.useRoot()) {
                        Util.changeSharedPreferences(context, "is.shortcut", data, "com.voxel.simplesearchlauncher.iconpack.IconPackManager.pref.xml", true);
                    } else {
                        Toast.makeText(context, context.getString(R.string.onlysupportedroot), Toast.LENGTH_SHORT).show();
                    }
                } catch (ActivityNotFoundException | NullPointerException e) {
                    openGooglePlay(context, launcherPackage, launcherName);
                }
                break;
            case FLICK:
                //Todo: fix direct apply for flick launcher
                try {
                    final Intent flick = context.getPackageManager().getLaunchIntentForPackage(
                            "com.universallauncher.universallauncher");
                    final Intent flickAction = new Intent("com.android.launcher3.FLICK_ICON_PACK_APPLIER");
                    flickAction.putExtra("com.android.launcher3.extra.ICON_THEME_PACKAGE", context.getPackageName());
                    flick.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.sendBroadcast(flickAction);
                    context.startActivity(flick);
                    ((AppCompatActivity) context).finish();
                } catch (ActivityNotFoundException | NullPointerException e) {
                    openGooglePlay(context, launcherPackage, launcherName);
                }
                break;
            case PIXEL:
                try {
                    final Intent pixelAction = new Intent("com.android.launcher3.FLICK_ICON_PACK_APPLIER");
                    pixelAction.putExtra("com.android.launcher3.extra.ICON_THEME_PACKAGE", context.getPackageName());
                    context.sendBroadcast(pixelAction);
                    ((AppCompatActivity) context).finish();
                } catch (ActivityNotFoundException | NullPointerException e) {
                    openGooglePlay(context, launcherPackage, launcherName);
                }
                break;

            case POSIDON:
                try {
                    final  Intent i = new Intent(Intent.ACTION_MAIN);
                    i.setComponent(new ComponentName("posidon.launcher", "posidon.launcher.applyicons"));
                    i.putExtra("iconpack", launcherPackage);
                    i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(i);
                    ((AppCompatActivity) context).finish();
                } catch (ActivityNotFoundException | NullPointerException e) {
                    openGooglePlay(context, launcherPackage, launcherName);
                }
                break;
            default:
                Toast.makeText(context, String.format(context.getString(R.string.notsupported), launcherName), Toast.LENGTH_LONG).show();
        }
    }

    private static String getLabel(String launcherPackage, Context context) {
        App app = ((App) context.getApplicationContext());
        DaoSession daoSession = app.getDaoSession();
        IPObjDao ipObjDao = daoSession.getIPObjDao();
        IPObj pkgObj = ipObjDao.queryBuilder().where(IPObjDao.Properties.IconPkg.eq(launcherPackage)).unique();
        return pkgObj.getIconName();
    }

    private static void applyManual(final Context context, final String launcherPackage, final String launcherName, final String activity) {
        new MaterialDialog.Builder(context)
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

    /*private static void applyEvie(final Context context, final String launcherPackage, final String launcherName) {
        try {
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
        } catch(Exception e) {
               // Toast.makeText(context,R.string.unexceptec)
        }
    }*/

    private static void openGooglePlay(final Context context, final String packageName, final String launcherName) {
        new MaterialDialog.Builder(context)
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
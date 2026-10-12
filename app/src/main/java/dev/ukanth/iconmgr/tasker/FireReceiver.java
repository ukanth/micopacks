/*
 * Copyright 2012 two forty four a.m. LLC <http://www.twofortyfouram.com>
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * <http://www.apache.org/licenses/LICENSE-2.0>
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 */

package dev.ukanth.iconmgr.tasker;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import com.stericson.roottools.RootTools;

import java.util.HashMap;

import dev.ukanth.iconmgr.App;
import dev.ukanth.iconmgr.Prefs;
import dev.ukanth.iconmgr.R;
import dev.ukanth.iconmgr.data.ApplyHistory;
import dev.ukanth.iconmgr.dao.IPObj;
import dev.ukanth.iconmgr.util.LauncherHelper;
import dev.ukanth.iconmgr.util.Util;


/**
 * This is the "fire" BroadcastReceiver for a Locale Plug-in setting.
 */
public final class FireReceiver extends BroadcastReceiver {
    public static final String TAG = "ITM";

    /**
     * @param context {@inheritDoc}.
     * @param intent  the incoming {@link com.twofortyfouram.locale.Intent#ACTION_FIRE_SETTING} Intent. This
     *                should contain the {@link com.twofortyfouram.locale.Intent#EXTRA_BUNDLE} that was saved by
     *                {@link } and later broadcast by Locale.
     */
    @Override
    public void onReceive(final Context context, final Intent intent) {
        /*
         * Always be sure to be strict on input parameters! A malicious third-party app could always send an
         * empty or otherwise malformed Intent. And since Locale applies settings in the background, the
         * plug-in definitely shouldn't crash in the background.
         */

        /*
         * Locale guarantees that the Intent action will be ACTION_FIRE_SETTING
         */
        if (!com.twofortyfouram.locale.Intent.ACTION_FIRE_SETTING.equals(intent.getAction())) {
            return;
        }

        /*
         * A hack to prevent a private serializable classloader attack
         */
        BundleScrubber.scrub(intent);
        BundleScrubber.scrub(intent.getBundleExtra(com.twofortyfouram.locale.Intent.EXTRA_BUNDLE));
        final Bundle bundle = intent.getBundleExtra(com.twofortyfouram.locale.Intent.EXTRA_BUNDLE);

        if (!PluginBundleManager.isBundleValid(bundle)) {
            return;
        }
        final String index = bundle.getString(PluginBundleManager.BUNDLE_EXTRA_STRING_MESSAGE);
        if (index == null || !index.contains(":")) {
            return;
        }
        final String[] msg = index.split(":");
        if (msg.length < 2) {
            return;
        }

        // Room refuses main-thread queries, so look up the random pack in the background
        // and come back to the main thread to apply (toasts, startActivity).
        final PendingResult result = goAsync();
        final Handler main = new Handler(Looper.getMainLooper());
        App.getInstance().getDbExecutor().execute(() -> {
            String iconName = msg[0];
            String iconPackage = msg[1];
            boolean random = iconName.equals("rand") && iconPackage.equals("rand");
            if (random) {
                IPObj ipObj = Util.getRandomInstalledIconPack(App.getInstance().getIPObjDao());
                if (ipObj != null) {
                    iconName = ipObj.getIconName();
                    iconPackage = ipObj.getIconPkg();
                }
            }
            final String finalIconName = iconName;
            final String finalIconPackage = iconPackage;
            main.post(() -> {
                try {
                    if (random && !"rand".equals(finalIconPackage)) {
                        Toast.makeText(context, context.getString(R.string.selected_pack) + finalIconName, Toast.LENGTH_LONG).show();
                    }
                    applyPack(context, finalIconName, finalIconPackage);
                } finally {
                    result.finish();
                }
            });
        });
    }

    private static void applyPack(Context context, String iconName, String iconPackage) {
        String launcherPack = LauncherHelper.getLauncherPackage(context);
        if (launcherPack == null || launcherPack.equals("android")) {
            Toast.makeText(context, R.string.nodefault, Toast.LENGTH_LONG).show();
            return;
        }
        //String launcherName = LauncherHelper.getLauncherName(context, launcherPack);
        switch (LauncherHelper.getLauncherId(launcherPack)) {
            case LauncherHelper.NOVA:
                if (RootTools.isRootAvailable() && Prefs.useRoot()) {
                    ApplyHistory.record(iconPackage, launcherPack);
                    Util.changeSharedPreferences(context, "com.teslacoilsw.launcher", "theme_icon_pack", iconName + ":GO:" + iconPackage, "com.teslacoilsw.launcher" + "_preferences.xml");
                    Util.restartLauncher(context, "com.teslacoilsw.launcher");
                } else {
                    LauncherHelper.apply(context, iconPackage, launcherPack);
                }
                break;
            case LauncherHelper.SOLO:
            case LauncherHelper.ZERO:
            case LauncherHelper.V:
            case LauncherHelper.ABC:
            case LauncherHelper.NEXT:
            case LauncherHelper.GO:
                LauncherHelper.apply(context, iconPackage, launcherPack);
                break;
            case LauncherHelper.MICROSOFT:
                HashMap<String, String> data = new HashMap<>();
                data.put("cur_iconpack_package", iconPackage);
                data.put("cur_iconpack_name", iconName);
                if (RootTools.isRootAvailable() && Prefs.useRoot()) {
                    ApplyHistory.record(iconPackage, launcherPack);
                    Util.changeSharedPreferences(context, "com.microsoft.launcher", data, "GadernSalad.xml", true);
                } else {
                    Toast.makeText(context, context.getString(R.string.onlysupportedroot), Toast.LENGTH_SHORT).show();
                }
                break;
            case LauncherHelper.EVIE:
                data = new HashMap<>();
                data.put("package_name", iconPackage);
                data.put("label", iconName);
                if (RootTools.isRootAvailable() && Prefs.useRoot()) {
                    ApplyHistory.record(iconPackage, launcherPack);
                    Util.changeSharedPreferences(context, "is.shortcut", data, "com.voxel.simplesearchlauncher.iconpack.IconPackManager.pref.xml", true);
                } else {
                    Toast.makeText(context, context.getString(R.string.onlysupportedroot), Toast.LENGTH_SHORT).show();
                }
                break;
            default:
                LauncherHelper.apply(context, iconPackage, launcherPack);
        }
    }
}
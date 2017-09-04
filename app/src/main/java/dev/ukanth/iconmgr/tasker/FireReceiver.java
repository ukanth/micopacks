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
import android.os.Message;
import android.widget.Toast;

import com.stericson.roottools.RootTools;

import dev.ukanth.iconmgr.R;
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

        final Handler toaster = new Handler() {
            public void handleMessage(Message msg) {
                if (msg.arg1 != 0) Toast.makeText(context, msg.arg1, Toast.LENGTH_SHORT).show();
            }
        };
        /*
         * Final verification of the plug-in Bundle before firing the setting.
         */
        if (PluginBundleManager.isBundleValid(bundle)) {
            String index = bundle.getString(PluginBundleManager.BUNDLE_EXTRA_STRING_MESSAGE);
            String iconName = null, iconPackage = null;

            if (index.contains(":")) {
                String[] msg = index.split(":");
                iconName = msg[0];
                iconPackage = msg[1];
                String launcherPack = LauncherHelper.getLauncherPackage(context);
                if (launcherPack == null || launcherPack.equals("android")) {
                    Toast.makeText(context, R.string.nodefault, Toast.LENGTH_LONG).show();
                    return;
                }

                String launcherName = LauncherHelper.getLauncherName(context, launcherPack);
                switch (LauncherHelper.getLauncherId(launcherPack)) {
                    case LauncherHelper.NOVA:
                        if (RootTools.isRootAvailable()) {
                            Util.changeSharedPreferences(context, "com.teslacoilsw.launcher", iconName + ":GO:" + iconPackage);
                            Util.restartLauncher(context, "com.teslacoilsw.launcher");
                        } else {
                            Toast.makeText(context, R.string.rootrequired, Toast.LENGTH_LONG).show();
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
                    default:
                        Toast.makeText(context, String.format(context.getString(R.string.notsupported), launcherName), Toast.LENGTH_LONG).show();
                }
            }
        }
    }
}
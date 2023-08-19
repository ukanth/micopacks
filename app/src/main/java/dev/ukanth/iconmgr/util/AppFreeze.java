package dev.ukanth.iconmgr.util;

import android.util.Log;

import com.topjohnwu.superuser.Shell;

public class AppFreeze {
    private static final String FREEZE_COMMAND = "pm disable %s";
    private static final String UNFREEZE_COMMAND = "pm enable %s";

    public static boolean freezeApp(String packageName) {
        String command = String.format(FREEZE_COMMAND, packageName);
        return executeCommand(command);
    }

    public static boolean unfreezeApp(String packageName) {
        String command = String.format(UNFREEZE_COMMAND, packageName);
        return executeCommand(command);
    }

    private static boolean executeCommand(String command) {
        try {
            Shell.Result result;
            result = Shell.cmd(command).exec();
            return result.isSuccess();
        } catch (Exception e) {
            Log.e("micopacks", e.getMessage());
        }
        return false;
    }
}

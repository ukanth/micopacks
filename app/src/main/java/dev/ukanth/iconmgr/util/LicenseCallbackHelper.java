package dev.ukanth.iconmgr.util;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.danimahardhika.android.helpers.license.LicenseCallback;
import com.danimahardhika.android.helpers.license.LicenseHelper;

import dev.ukanth.iconmgr.Prefs;
import dev.ukanth.iconmgr.R;

/**
 * Created by ukanth on 9/9/17.
 */

public class LicenseCallbackHelper implements LicenseCallback {

    private final Context mContext;
    private final MaterialDialog mDialog;

    public LicenseCallbackHelper(@NonNull Context context) {
        mContext = context;

        MaterialDialog.Builder builder = new MaterialDialog.Builder(mContext);
        builder.content(R.string.license_checking)
                .progress(true, 0);

        mDialog = builder.build();
        mDialog.setCancelable(false);
        mDialog.setCanceledOnTouchOutside(false);
    }

    @Override
    public void onLicenseCheckStart() {
        try {
            if(!mDialog.isCancelled()) {
                mDialog.show();
            }
        } catch (Exception e){

        }
    }

    @Override
    public void onLicenseCheckFinished(LicenseHelper.Status status) {
        mDialog.dismiss();
        if (status == LicenseHelper.Status.RETRY) {
            showRetryDialog();
            return;
        }

        showLicenseDialog(status);
    }

    private void showLicenseDialog(final LicenseHelper.Status status) {
        int message = status == LicenseHelper.Status.SUCCESS ?
                R.string.license_check_success : R.string.license_check_failed;
        new MaterialDialog.Builder(mContext)
                .title(R.string.license_check)
                .content(message)
                .positiveText(R.string.close)
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        onLicenseChecked(status);
                        dialog.dismiss();
                    }
                })
                .cancelable(false)
                .canceledOnTouchOutside(false)
                .show();
    }

    private void showRetryDialog() {
        new MaterialDialog.Builder(mContext)
                .title(R.string.license_check)
                .content(R.string.license_check_retry)
                .positiveText(R.string.close)
                .cancelable(false)
                .canceledOnTouchOutside(false)
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        ((AppCompatActivity) mContext).finish();
                    }
                })
                .show();
    }

    private void onLicenseChecked(LicenseHelper.Status status) {

        Prefs.setFirstRun(false);
        if (status == LicenseHelper.Status.SUCCESS) {
            Prefs.setLicensed(true);
        } else if (status == LicenseHelper.Status.FAILED) {
            Prefs.setLicensed(false);
            ((AppCompatActivity) mContext).finish();
        }
    }
}
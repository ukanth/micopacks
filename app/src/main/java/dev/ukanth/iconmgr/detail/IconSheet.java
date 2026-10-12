package dev.ukanth.iconmgr.detail;

import android.app.Activity;
import android.graphics.Bitmap;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;

import java.io.ByteArrayOutputStream;

import dev.ukanth.iconmgr.App;
import dev.ukanth.iconmgr.R;
import dev.ukanth.iconmgr.dao.FavDao;
import dev.ukanth.iconmgr.dao.Favorite;
import dev.ukanth.iconmgr.util.IconSaver;

/**
 * Bottom sheet for one icon: large preview, save to gallery, favourite. Shared by the pack detail
 * screen and icon search.
 */
public final class IconSheet {

    private IconSheet() {
    }

    /**
     * @param drawable pack drawable name, or null for an app the pack does not cover (no actions)
     */
    public static void show(Activity activity, Bitmap bitmap, String title, String subtitle,
                            String packPackage, String packName, String drawable) {
        BottomSheetDialog sheet = new BottomSheetDialog(activity);
        View view = activity.getLayoutInflater().inflate(R.layout.sheet_icon, null);
        ((ImageView) view.findViewById(R.id.sheet_icon)).setImageBitmap(bitmap);
        ((TextView) view.findViewById(R.id.sheet_title)).setText(title);
        ((TextView) view.findViewById(R.id.sheet_subtitle)).setText(subtitle);

        View actions = view.findViewById(R.id.sheet_actions);
        if (drawable == null || bitmap == null) {
            actions.setVisibility(View.GONE);
        } else {
            MaterialButton save = view.findViewById(R.id.sheet_save);
            MaterialButton favorite = view.findViewById(R.id.sheet_favorite);
            FavDao favDao = App.getInstance().getFavDao();

            save.setOnClickListener(v -> App.getInstance().getDbExecutor().execute(() -> {
                String location = IconSaver.save(activity.getApplicationContext(), bitmap, packName, drawable);
                activity.runOnUiThread(() -> Toast.makeText(activity,
                        location != null ? activity.getString(R.string.icon_saved, location)
                                : activity.getString(R.string.icon_save_failed, drawable),
                        Toast.LENGTH_SHORT).show());
            }));

            App.getInstance().getDbExecutor().execute(() -> {
                boolean isFavorite = favDao.isFavorite(packPackage, drawable, packName);
                activity.runOnUiThread(() -> showFavorite(favorite, isFavorite));
            });
            favorite.setOnClickListener(v -> App.getInstance().getDbExecutor().execute(() -> {
                boolean nowFavorite = !favDao.isFavorite(packPackage, drawable, packName);
                if (nowFavorite) {
                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
                    favDao.insertFavorite(new Favorite(packPackage, packName, drawable, stream.toByteArray()));
                } else {
                    favDao.deleteFavorite(packPackage, drawable, packName);
                }
                activity.runOnUiThread(() -> showFavorite(favorite, nowFavorite));
            }));
        }
        sheet.setContentView(view);
        sheet.show();
    }

    private static void showFavorite(MaterialButton button, boolean favorite) {
        button.setText(favorite ? R.string.icon_favorited : R.string.icon_favorite);
        button.setIconResource(favorite ? R.drawable.fav_filled : R.drawable.fav_border);
    }
}

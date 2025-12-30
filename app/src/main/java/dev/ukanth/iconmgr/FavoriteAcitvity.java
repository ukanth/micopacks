package dev.ukanth.iconmgr;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.ViewGroup;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.afollestad.materialdialogs.MaterialDialog;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import dev.ukanth.iconmgr.dao.FavDao;
import dev.ukanth.iconmgr.dao.Favorite;

public class FavoriteAcitvity extends AppCompatActivity {
    FavDao favDao = App.getInstance().getFavDao();
    private Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (Prefs.isDarkTheme()) {
            setTheme(R.style.AppTheme_Dark);
        } else {
            setTheme(R.style.AppTheme_Light);
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.favorite);
        mContext = FavoriteAcitvity.this;
        setupActionBar();

        LinearLayout iconPackContainer = findViewById(R.id.icon_pack_container);

        // Load data on background thread
        new Thread(() -> {
            List<Favorite> allFavorites = favDao.getAllFavorites();
            
            // Group favorites by icon name
            Map<String, List<Favorite>> groupedFavorites = new HashMap<>();
            for (Favorite fav : allFavorites) {
                String iconName = fav.getIconName();
                if (!groupedFavorites.containsKey(iconName)) {
                    groupedFavorites.put(iconName, new java.util.ArrayList<>());
                }
                groupedFavorites.get(iconName).add(fav);
            }

            // Update UI on main thread
            runOnUiThread(() -> {
                for (Map.Entry<String, List<Favorite>> entry : groupedFavorites.entrySet()) {
                    String iconName = entry.getKey();
                    List<Favorite> favorites = entry.getValue();
                    
                    // Create a TextView for the current icon name
                    TextView textView = new TextView(this);
                    textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
                    textView.setTypeface(null, Typeface.BOLD);
                    textView.setText(iconName);
                    textView.setPadding(20, 30, 20, 10);
                    iconPackContainer.addView(textView);
                    
                    // Create a GridLayout for the associated icon images
                    GridLayout iconGridLayout = new GridLayout(this);
                    iconGridLayout.setColumnCount(4);
                    iconGridLayout.setLayoutParams(new ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT
                    ));
                    
                    // Display each favorite icon
                    for (Favorite favorite : favorites) {
                        byte[] iconImageData = favorite.getIconImageData();
                        Bitmap bitmap = BitmapFactory.decodeByteArray(iconImageData, 0, iconImageData.length);

                        ImageView image = new ImageView(getApplicationContext());
                        int sizePx = (int) TypedValue.applyDimension(
                                TypedValue.COMPLEX_UNIT_DIP, 64,
                                getResources().getDisplayMetrics()
                        );
                        GridLayout.LayoutParams params = new GridLayout.LayoutParams();
                        params.width = sizePx;
                        params.height = sizePx;
                        params.setMargins(15, 15, 15, 15);
                        image.setLayoutParams(params);
                        image.setScaleType(ImageView.ScaleType.FIT_CENTER);
                        image.setImageDrawable(new BitmapDrawable(getResources(), bitmap));
                        
                        // Set click listener to show dialog with options
                        image.setOnClickListener(v -> showIconOptionsDialog(favorite, bitmap, iconName));

                        iconGridLayout.addView(image);
                    }
                    
                    iconPackContainer.addView(iconGridLayout);
                }
            });
        }).start();
    }

    private void showIconOptionsDialog(Favorite favorite, Bitmap bitmap, String iconName) {
        String packageName = favorite.getIconPkg();
        String iconTitle = favorite.getIcontitle();
        
        new MaterialDialog.Builder(this)
                .title(iconName)
                .content("Icon Pack: " + iconTitle)
                .positiveText("Remove")
                .negativeText("Download")
                .neutralText("Cancel")
                .onPositive((dialog, which) -> {
                    // Remove from favorites
                    new Thread(() -> {
                        favDao.deleteFavorite(packageName, iconTitle, iconName);
                        runOnUiThread(() -> {
                            Toast.makeText(this, "Removed from favorites", Toast.LENGTH_SHORT).show();
                            // Refresh the activity
                            recreate();
                        });
                    }).start();
                })
                .onNegative((dialog, which) -> {
                    // Download icon
                    new Thread(() -> {
                        try {
                            File picturesDir = new File(android.os.Environment.getExternalStoragePublicDirectory(
                                    android.os.Environment.DIRECTORY_PICTURES), "IconManager");
                            if (!picturesDir.exists()) {
                                picturesDir.mkdirs();
                            }
                            
                            String fileName = iconTitle + "_" + iconName + ".png";
                            File file = new File(picturesDir, fileName);
                            
                            FileOutputStream fos = new FileOutputStream(file);
                            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
                            fos.close();
                            
                            runOnUiThread(() -> {
                                Toast.makeText(this, "Saved to Pictures/IconManager/" + fileName, Toast.LENGTH_LONG).show();
                            });
                        } catch (Exception e) {
                            runOnUiThread(() -> {
                                Toast.makeText(this, "Failed to save: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            });
                        }
                    }).start();
                })
                .show();
    }

    private void setupActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }
}

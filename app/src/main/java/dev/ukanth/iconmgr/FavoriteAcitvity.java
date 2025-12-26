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

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import dev.ukanth.iconmgr.dao.FavDao;

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
            List<String> iconName = favDao.geticonName();

            // Initialize a map to store the grouped icon images
            Map<String, List<byte[]>> groupedIconImages = new HashMap<>();
            // Group the icon images by iconName
            for (String iconname : iconName) {
                List<byte[]> iconImages = favDao.getIconImageData(iconname);
                groupedIconImages.put(iconname, iconImages);
            }

            // Update UI on main thread
            runOnUiThread(() -> {
                for (String icon : iconName) {
                    // Create a TextView for the current icon name
                    TextView textView = new TextView(this);
                    textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
                    textView.setTypeface(null, Typeface.BOLD);
                    textView.setText(icon);
                    // Add the TextView to the icon pack container
                    iconPackContainer.addView(textView);
                    // Create a GridLayout for the associated icon images
                    GridLayout iconGridLayout = new GridLayout(this);
                    iconGridLayout.setLayoutParams(new ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT
                    ));
                    // Get the list of icon images for the current iconName
                    List<byte[]> iconImages = groupedIconImages.get(icon);
                    // Display the icon images under the TextView
                    for (byte[] iconImageData : iconImages) {
                        Bitmap bitmap = BitmapFactory.decodeByteArray(iconImageData, 0, iconImageData.length);

                        ImageView image = new ImageView(getApplicationContext());
                        image.setPadding(15, 15, 15, 15);
                        image.setScaleType(ImageView.ScaleType.FIT_CENTER);
                        image.setImageDrawable(new BitmapDrawable(getResources(), bitmap));

                        // Add the ImageView to the GridLayout
                        iconGridLayout.addView(image);
                    }
                    // Add the GridLayout to the icon pack container
                    iconPackContainer.addView(iconGridLayout);
                }
            });
        }).start();
    }

    private void setupActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }
}

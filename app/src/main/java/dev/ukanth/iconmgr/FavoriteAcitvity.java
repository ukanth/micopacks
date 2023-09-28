package dev.ukanth.iconmgr;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import com.afollestad.materialdialogs.MaterialDialog;
import java.io.File;
import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import dev.ukanth.iconmgr.dao.FavDao;


public class FavoriteAcitvity  extends AppCompatActivity {
    FavDao favDao = App.getInstance().getFavDao();
    private Context mContext;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.favorite);
        mContext = FavoriteAcitvity.this;
        setupActionBar();
        LinearLayout iconPackContainer = findViewById(R.id.icon_pack_container);
        List<String> iconName = favDao.geticonName();


        if (Prefs.isDarkTheme()) {
            setTheme(R.style.AppTheme_Dark);
        } else {
            setTheme(R.style.AppTheme_Light);
        }

        // Initialize a map to store the grouped icon images
        Map<String, List<byte[]>> groupedIconImages = new HashMap<>();
        // Group the icon images by iconName
        for (String iconname : iconName) {
            List<byte[]> iconImages = favDao.getIconImageData(iconname);
            groupedIconImages.put(iconname, iconImages);
        }

        for (String icon : iconName) {
            // Create a TextView for the current icon name
            TextView textView = new TextView(this);
            textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);  // Text size
            textView.setText(icon);
            // Add the TextView to the icon pack container
            iconPackContainer.addView(textView);
            // Create a GridLayout for the associated icon images
            GridLayout iconGridLayout = new GridLayout(this);
            iconGridLayout.setLayoutParams(new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            ));
            // Set the number of columns for the GridLayout
            int numColumns = Prefs.getCol();    // same no of column as settings
            iconGridLayout.setColumnCount(numColumns);
            // Get the list of icon images for the current iconName
            List<byte[]> iconImages = groupedIconImages.get(icon);
            // Display the icon images under the TextView
            for (byte[] iconImageData : iconImages) {
                Bitmap bitmap = BitmapFactory.decodeByteArray(iconImageData, 0, iconImageData.length);

                String icontitle = favDao.getIcontitleForIconImageData(iconImageData);

                ImageView image = new ImageView(getApplicationContext());
                image.setPadding(15, 15, 15, 15);
                image.setScaleType(ImageView.ScaleType.FIT_CENTER);
                image.setImageDrawable(new BitmapDrawable(getResources(), bitmap));

                image.setOnClickListener(v -> {
                    View dialogView = LayoutInflater.from(mContext).inflate(R.layout.favorite_dialog, null);
                    ImageView saveImageView = dialogView.findViewById(R.id.saveImageView);
                    ImageView closeImageView = dialogView.findViewById(R.id.closeImageView);
                    ImageView removeImageView = dialogView.findViewById(R.id.removeImageView);
                    TextView titletextView = dialogView.findViewById(R.id.icon_title);

                    MaterialDialog dialog = new MaterialDialog.Builder(mContext)
                            .customView(dialogView, false)
                            .show();

                    titletextView.setText("Action for"+" "+ icontitle );

                    saveImageView.setOnClickListener(view -> {
                        File mediaDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
                        if (mediaDir == null) {
                            Log.e("MICO", "External storage not available");
                            return;
                        }
                        File myDir = new File(mediaDir, "micopacks/general/");
                        if (!myDir.exists() && !myDir.mkdirs()) {
                            Log.e("MICO", "Failed to create directory: " + myDir.getAbsolutePath());
                            return;
                        }
                        String fname =  icontitle + ".png";
                        File file = new File(myDir, fname);
                        if (file.exists()) {
                            boolean deleted = file.delete();
                            if (deleted) {
                                Toast.makeText(getApplicationContext(), "Image deleted successfully", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(getApplicationContext(), "Failed to delete image", Toast.LENGTH_SHORT).show();
                            }
                            return;
                        }
                                try {
                                    FileOutputStream out = new FileOutputStream(file);
                                    bitmap.compress(Bitmap.CompressFormat.PNG, 85, out);
                                    out.flush();
                                    out.close();
                                    Toast.makeText(getApplicationContext(), "Icon saved successfully: " + file.getAbsolutePath(), Toast.LENGTH_SHORT).show();
                                } catch (Exception e) {
                                    Log.e("MICO", e.getMessage(), e);
                                }
                });

                    removeImageView.setOnClickListener(view -> {
                        favDao.deleteIcon(iconImageData);
                        Toast.makeText(mContext, "Removed from favorites: " , Toast.LENGTH_SHORT).show();

                    });

                    closeImageView.setOnClickListener(view -> {
                        dialog.dismiss();
                    });

                });
                // Add the ImageView to the GridLayout
                iconGridLayout.addView(image);
            }
            // Add the GridLayout to the icon pack container
            iconPackContainer.addView(iconGridLayout);

        }
    }
    private void setupActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }
    }

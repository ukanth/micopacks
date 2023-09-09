package dev.ukanth.iconmgr;

import android.app.ActionBar;
import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;

import java.util.List;

import dev.ukanth.iconmgr.dao.FavDao;


public class FavoriteAcitvity extends Activity {

    private GridLayout gridLayout;

    private LinearLayout.LayoutParams params;

    FavDao favDao = App.getInstance().getFavDao();

    @Override
    protected void onCreate(Bundle savedInstanceState) {

       if (Prefs.isDarkTheme()) {
            setTheme(R.style.AppTheme_Dark);
        } else {
            setTheme(R.style.AppTheme_Light);
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.favorite);

        gridLayout = (GridLayout) findViewById(R.id.favorite);
        int colNumber = Prefs.getCol();
        gridLayout.setColumnCount(colNumber);  // Same number of columns in preview and Favorites
        DisplayMetrics metrics = getResources().getDisplayMetrics();
        int screenWidth = metrics.widthPixels;
        params = new LinearLayout.LayoutParams(screenWidth / colNumber, screenWidth / colNumber);

        List<byte[]> iconImageDataList = favDao.getIconImageData();

        for (byte[] iconImageData : iconImageDataList) {
            Bitmap bitmap = BitmapFactory.decodeByteArray(iconImageData, 0, iconImageData.length);

            ImageView image = new ImageView(getApplicationContext());
            image.setLayoutParams(params);
            image.setPadding(15, 15, 15, 15);
            image.setScaleType(ImageView.ScaleType.FIT_CENTER);
            image.setImageDrawable(new BitmapDrawable(getResources(), bitmap));
            gridLayout.addView(image);
        }


    }


}

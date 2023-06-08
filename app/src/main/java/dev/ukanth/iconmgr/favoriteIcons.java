package dev.ukanth.iconmgr;

import android.content.Intent;

import androidx.appcompat.app.AppCompatActivity;

import java.util.List;

import dev.ukanth.iconmgr.dao.IPObj;

public class favoriteIcons extends AppCompatActivity {
    public void onCreate() {
        setContentView(R.layout.favorite);

        Intent intent = getIntent();
        List<IPObj> favoriteIcons = intent.getParcelableArrayListExtra("favoriteIcons");
    }
}

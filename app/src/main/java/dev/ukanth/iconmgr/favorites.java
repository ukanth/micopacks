package dev.ukanth.iconmgr;

import android.content.Context;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import dev.ukanth.iconmgr.dao.IPObj;

public class favorites extends AppCompatActivity {
    private RecyclerView recyclerView;

    private Context ctx;

    public void onCreate() {
        setContentView(R.layout.favorite);
        recyclerView = findViewById(R.id.recyclerview);
        IconAdapter adapter = null;
        List<IPObj> favoriteIconPackList = adapter.getFavoriteIconPackList();
        adapter = new IconAdapter(ctx, favoriteIconPackList);
        adapter.setData(favoriteIconPackList);
        recyclerView.setAdapter(adapter);

    }



}

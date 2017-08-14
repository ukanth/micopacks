package dev.ukanth.iconmgr;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;

import java.util.ArrayList;
import java.util.List;


public class DetailsActivity extends AppCompatActivity {

    private StaggeredGridLayoutManager staggeredGridLayoutManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (Prefs.isDarkTheme(getApplicationContext())) {
            setTheme(R.style.AppTheme_Dark);
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);

        staggeredGridLayoutManager = new StaggeredGridLayoutManager(2,
                StaggeredGridLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(staggeredGridLayoutManager);

        List<ItemObject> sList = getListItemData();

        DetailsRecyclerViewAdapter rcAdapter = new DetailsRecyclerViewAdapter(
                DetailsActivity.this, sList);
        recyclerView.setAdapter(rcAdapter);
    }

    private List<ItemObject> getListItemData() {
        List<ItemObject> listViewItems = new ArrayList<ItemObject>();
        return listViewItems;
    }
}

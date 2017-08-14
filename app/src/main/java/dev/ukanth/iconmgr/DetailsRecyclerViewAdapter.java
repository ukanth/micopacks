package dev.ukanth.iconmgr;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

/**
 * Created by ukanth on 6/8/17.
 */

public class DetailsRecyclerViewAdapter extends RecyclerView.Adapter<SampleViewHolders> {
    private List<ItemObject> itemList;
    private Context context;

    public DetailsRecyclerViewAdapter(Context context,
                                      List<ItemObject> itemList) {
        this.itemList = itemList;
        this.context = context;
    }

    @Override
    public SampleViewHolders onCreateViewHolder(ViewGroup parent, int viewType) {
        View layoutView = LayoutInflater.from(parent.getContext()).inflate(
                R.layout.details_card, null);
        SampleViewHolders rcv = new SampleViewHolders(layoutView);
        return rcv;
    }

    @Override
    public void onBindViewHolder(SampleViewHolders holder, int position) {
        holder.statsText.setText(itemList.get(position).get_stat());
        holder.statsTitle.setText(itemList.get(position).get_statsHeader());
    }

    @Override
    public int getItemCount() {
        return this.itemList.size();
    }
}

class SampleViewHolders extends RecyclerView.ViewHolder implements
        View.OnClickListener {
    public TextView statsText;
    public TextView statsTitle;

    public SampleViewHolders(View itemView) {
        super(itemView);
        itemView.setOnClickListener(this);
        statsText = (TextView) itemView.findViewById(R.id.statsText);
        statsTitle = (TextView) itemView.findViewById(R.id.statsTitle);
    }

    @Override
    public void onClick(View view) {
        Toast.makeText(view.getContext(),
                "Clicked Position = " + getPosition(), Toast.LENGTH_SHORT)
                .show();
    }
}
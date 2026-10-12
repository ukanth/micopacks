package dev.ukanth.iconmgr.home;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import dev.ukanth.iconmgr.R;

/**
 * Single header row above the pack list: the one-time "swipe / long-press" hint and the
 * "no packs match" message.
 */
public class HomeHeaderAdapter extends RecyclerView.Adapter<HomeHeaderAdapter.Holder> {

    public interface Listener {
        void onHintDismissed();
    }

    private final Listener listener;
    private CharSequence hint;
    private boolean filterEmpty;

    public HomeHeaderAdapter(Listener listener) {
        this.listener = listener;
    }

    /** Text of the gestures hint; null hides it. */
    public void setHint(CharSequence hint) {
        this.hint = hint;
        notifyItemChanged(0);
    }

    public void setFilterEmpty(boolean empty) {
        if (filterEmpty != empty) {
            filterEmpty = empty;
            notifyItemChanged(0);
        }
    }

    static final class Holder extends RecyclerView.ViewHolder {
        final View hintCard;
        final TextView hintText;
        final TextView filterEmpty;

        Holder(View view) {
            super(view);
            hintCard = view.findViewById(R.id.hint_card);
            hintText = view.findViewById(R.id.hint_text);
            filterEmpty = view.findViewById(R.id.filter_empty);
        }
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Holder holder = new Holder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_home_header, parent, false));
        holder.itemView.findViewById(R.id.hint_close).setOnClickListener(v -> listener.onHintDismissed());
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        holder.hintCard.setVisibility(hint != null ? View.VISIBLE : View.GONE);
        holder.hintText.setText(hint);
        holder.filterEmpty.setVisibility(filterEmpty ? View.VISIBLE : View.GONE);
    }

    @Override
    public int getItemCount() {
        return 1;
    }
}

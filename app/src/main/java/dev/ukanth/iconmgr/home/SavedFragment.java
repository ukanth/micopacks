package dev.ukanth.iconmgr.home;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import dev.ukanth.iconmgr.App;
import dev.ukanth.iconmgr.Prefs;
import dev.ukanth.iconmgr.R;
import dev.ukanth.iconmgr.dao.Favorite;
import dev.ukanth.iconmgr.detail.IconSheet;

/**
 * Saved tab: favourite icons grouped by pack. Updates live when an icon is (un)favourited.
 */
public class SavedFragment extends Fragment {

    private static final ExecutorService decoder = Executors.newSingleThreadExecutor();

    private SavedAdapter adapter;
    private TextView empty;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        empty = view.findViewById(R.id.list_empty);
        empty.setText(R.string.saved_empty);
        RecyclerView list = view.findViewById(R.id.list);
        int columns = Math.max(3, Prefs.getCol());
        GridLayoutManager layout = new GridLayoutManager(requireContext(), columns);
        adapter = new SavedAdapter();
        layout.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                return adapter.isHeader(position) ? columns : 1;
            }
        });
        list.setLayoutManager(layout);
        list.setPadding(8, list.getPaddingTop(), 8, list.getPaddingBottom());
        list.setAdapter(adapter);

        App.getInstance().getFavDao().observeAll().observe(getViewLifecycleOwner(), favorites -> {
            adapter.setFavorites(favorites);
            empty.setVisibility(favorites == null || favorites.isEmpty() ? View.VISIBLE : View.GONE);
        });
    }

    private final class SavedAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        private static final int HEADER = 0;
        private static final int ICON = 1;
        /** String = pack header, Favorite = icon */
        private final List<Object> rows = new ArrayList<>();
        private final Handler main = new Handler(Looper.getMainLooper());

        void setFavorites(List<Favorite> favorites) {
            rows.clear();
            String currentPack = null;
            if (favorites != null) {
                for (Favorite favorite : favorites) {
                    if (!favorite.getIconName().equals(currentPack)) {
                        currentPack = favorite.getIconName();
                        rows.add(currentPack);
                    }
                    rows.add(favorite);
                }
            }
            notifyDataSetChanged();
        }

        boolean isHeader(int position) {
            return rows.get(position) instanceof String;
        }

        @Override
        public int getItemViewType(int position) {
            return isHeader(position) ? HEADER : ICON;
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            int layout = viewType == HEADER ? R.layout.item_section_header : R.layout.item_icon_grid;
            return new RecyclerView.ViewHolder(inflater.inflate(layout, parent, false)) {
            };
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            Object row = rows.get(position);
            if (row instanceof String) {
                ((TextView) holder.itemView).setText((String) row);
                return;
            }
            Favorite favorite = (Favorite) row;
            ImageView icon = holder.itemView.findViewById(R.id.icon);
            ((TextView) holder.itemView.findViewById(R.id.label)).setText(favorite.getIcontitle());
            icon.setImageDrawable(null);
            icon.setTag(favorite.getId());
            byte[] data = favorite.getIconImageData();
            decoder.execute(() -> {
                if (!Integer.valueOf(favorite.getId()).equals(icon.getTag())) return;
                Bitmap bitmap = data == null ? null : BitmapFactory.decodeByteArray(data, 0, data.length);
                main.post(() -> {
                    if (Integer.valueOf(favorite.getId()).equals(icon.getTag())) icon.setImageBitmap(bitmap);
                });
            });
            holder.itemView.setOnClickListener(v -> {
                Bitmap bitmap = data == null ? null : BitmapFactory.decodeByteArray(data, 0, data.length);
                IconSheet.show(requireActivity(), bitmap, favorite.getIcontitle(), favorite.getIconName(),
                        favorite.getIconPkg(), favorite.getIconName(), favorite.getIcontitle());
            });
        }

        @Override
        public int getItemCount() {
            return rows.size();
        }
    }
}

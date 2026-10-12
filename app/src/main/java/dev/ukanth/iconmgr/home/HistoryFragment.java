package dev.ukanth.iconmgr.home;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import dev.ukanth.iconmgr.App;
import dev.ukanth.iconmgr.R;
import dev.ukanth.iconmgr.dao.ApplyEvent;
import dev.ukanth.iconmgr.dao.History;
import dev.ukanth.iconmgr.detail.PackDetailActivity;
import dev.ukanth.iconmgr.util.LauncherHelper;
import dev.ukanth.iconmgr.util.TimeText;

/**
 * History tab: packs applied from Micopacks (newest first) and packs that were uninstalled.
 */
public class HistoryFragment extends Fragment {

    private final HistoryAdapter adapter = new HistoryAdapter();
    private List<ApplyEvent> applies = new ArrayList<>();
    private List<History> uninstalled = new ArrayList<>();
    private TextView empty;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        empty = view.findViewById(R.id.list_empty);
        empty.setText(R.string.history_empty);
        RecyclerView list = view.findViewById(R.id.list);
        list.setLayoutManager(new LinearLayoutManager(requireContext()));
        list.setAdapter(adapter);

        LiveData<List<ApplyEvent>> appliesData = App.getInstance().getHistoryDao().observeApplies(50);
        appliesData.observe(getViewLifecycleOwner(), events -> {
            applies = events != null ? events : new ArrayList<>();
            rebuild();
        });
        App.getInstance().getHistoryDao().observeUninstalled().observe(getViewLifecycleOwner(), rows -> {
            uninstalled = rows != null ? rows : new ArrayList<>();
            rebuild();
        });
    }

    private void rebuild() {
        List<Object> rows = new ArrayList<>();
        if (!applies.isEmpty()) {
            rows.add(getString(R.string.history_applied));
            rows.addAll(applies);
        }
        if (!uninstalled.isEmpty()) {
            rows.add(getString(R.string.history_uninstalled));
            rows.addAll(uninstalled);
        }
        adapter.setRows(rows);
        empty.setVisibility(rows.isEmpty() ? View.VISIBLE : View.GONE);
    }

    private static boolean isInstalled(Context context, String pkg) {
        try {
            context.getPackageManager().getPackageInfo(pkg, 0);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    private void openPlayStore(String pkg) {
        try {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + pkg)));
        } catch (android.content.ActivityNotFoundException e) {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + pkg)));
        }
    }

    private final class HistoryAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        private static final int HEADER = 0;
        private static final int ROW = 1;
        private List<Object> rows = new ArrayList<>();

        void setRows(List<Object> rows) {
            this.rows = rows;
            notifyDataSetChanged();
        }

        @Override
        public int getItemViewType(int position) {
            return rows.get(position) instanceof String ? HEADER : ROW;
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            int layout = viewType == HEADER ? R.layout.item_section_header : R.layout.item_history_row;
            return new RecyclerView.ViewHolder(LayoutInflater.from(parent.getContext()).inflate(layout, parent, false)) {
            };
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            Object row = rows.get(position);
            Context ctx = holder.itemView.getContext();
            if (row instanceof String) {
                ((TextView) holder.itemView).setText((String) row);
                return;
            }
            ImageView icon = holder.itemView.findViewById(R.id.row_icon);
            TextView title = holder.itemView.findViewById(R.id.row_title);
            TextView detail = holder.itemView.findViewById(R.id.row_detail);
            final String pkg;
            if (row instanceof ApplyEvent) {
                ApplyEvent event = (ApplyEvent) row;
                pkg = event.packPkg;
                title.setText(event.packName);
                String launcher = event.launcherPkg != null ? LauncherHelper.getLauncherName(ctx, event.launcherPkg) : "";
                detail.setText(ctx.getString(R.string.last_applied_detail, launcher,
                        TimeText.relative(ctx, event.time)));
            } else {
                History history = (History) row;
                pkg = history.getIconPkg();
                title.setText(history.getIconName());
                detail.setText(ctx.getString(R.string.history_uninstalled_detail,
                        TimeText.relative(ctx, history.getUninstallTime())));
            }
            boolean installed = isInstalled(ctx, pkg);
            try {
                icon.setImageDrawable(installed ? ctx.getPackageManager().getApplicationIcon(pkg)
                        : ctx.getDrawable(R.drawable.iconpack));
            } catch (PackageManager.NameNotFoundException e) {
                icon.setImageDrawable(null);
            }
            // the generic pack glyph is white; tint it for the current theme
            icon.setImageTintList(installed ? null : android.content.res.ColorStateList.valueOf(
                    com.google.android.material.color.MaterialColors.getColor(icon,
                            com.google.android.material.R.attr.colorOnSurfaceVariant)));
            icon.setAlpha(installed ? 1f : 0.6f);
            holder.itemView.setOnClickListener(v -> {
                if (isInstalled(ctx, pkg)) {
                    startActivity(PackDetailActivity.intent(ctx, pkg));
                } else {
                    openPlayStore(pkg);
                }
            });
        }

        @Override
        public int getItemCount() {
            return rows.size();
        }
    }
}

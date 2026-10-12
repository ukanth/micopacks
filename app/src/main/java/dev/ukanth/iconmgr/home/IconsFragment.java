package dev.ukanth.iconmgr.home;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import dev.ukanth.iconmgr.App;
import dev.ukanth.iconmgr.IconPackUtil;
import dev.ukanth.iconmgr.Prefs;
import dev.ukanth.iconmgr.R;
import dev.ukanth.iconmgr.detail.IconGridAdapter;
import dev.ukanth.iconmgr.detail.IconItem;
import dev.ukanth.iconmgr.detail.IconSheet;

/**
 * Icons tab: one search box over every pack's icons, results in the shared lazy grid.
 */
public class IconsFragment extends Fragment {

    private static final long DEBOUNCE_MS = 250;

    private IconsViewModel viewModel;
    private IconGridAdapter adapter;
    private TextView empty;
    private View progress;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private Runnable pendingSearch;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_icons, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        viewModel = new ViewModelProvider(this).get(IconsViewModel.class);
        empty = view.findViewById(R.id.icons_empty);
        progress = view.findViewById(R.id.icons_progress);
        RecyclerView grid = view.findViewById(R.id.icons_grid);
        grid.setLayoutManager(new GridLayoutManager(requireContext(), Math.max(3, Prefs.getCol())));
        adapter = new IconGridAdapter(null, false, this::onIconClick);
        grid.setAdapter(adapter);

        EditText query = view.findViewById(R.id.icons_query);
        query.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (pendingSearch != null) handler.removeCallbacks(pendingSearch);
                final String text = s.toString();
                pendingSearch = () -> viewModel.search(text);
                handler.postDelayed(pendingSearch, DEBOUNCE_MS);
            }
        });

        viewModel.isLoading().observe(getViewLifecycleOwner(), loading ->
                progress.setVisibility(Boolean.TRUE.equals(loading) ? View.VISIBLE : View.GONE));
        viewModel.getResults().observe(getViewLifecycleOwner(), results -> {
            adapter.submitList(results.items);
            showEmpty(results.query, results.items);
        });
        if (viewModel.getResults().getValue() == null) {
            showEmpty("", null);
        }
    }

    private void showEmpty(String query, List<IconItem> items) {
        if (items != null && !items.isEmpty()) {
            empty.setVisibility(View.GONE);
            return;
        }
        empty.setVisibility(View.VISIBLE);
        if (query == null || query.isEmpty()) {
            int count = viewModel.packCount();
            if (count == 0) {
                // catalogues not loaded yet; count from the stored packs
                App.getInstance().getDbExecutor().execute(() -> {
                    int n = App.getInstance().getIPObjDao().getAll().size();
                    if (isAdded()) requireActivity().runOnUiThread(() -> empty.setText(getString(R.string.icons_empty, n)));
                });
            } else {
                empty.setText(getString(R.string.icons_empty, count));
            }
        } else {
            empty.setText(getString(R.string.icons_no_results, query));
        }
    }

    private void onIconClick(IconItem item, Bitmap thumbnail) {
        String pack = item.packPackage;
        String packName = viewModel.packName(pack);
        App.getInstance().getBackgroundExecutor().execute(() -> {
            Bitmap full = new IconPackUtil().loadIcon(pack, item.drawable);
            if (!isAdded()) return;
            requireActivity().runOnUiThread(() -> IconSheet.show(requireActivity(), full != null ? full : thumbnail,
                    item.drawable, packName, pack, packName, item.drawable));
        });
    }

    @Override
    public void onDestroyView() {
        if (pendingSearch != null) handler.removeCallbacks(pendingSearch);
        super.onDestroyView();
    }
}

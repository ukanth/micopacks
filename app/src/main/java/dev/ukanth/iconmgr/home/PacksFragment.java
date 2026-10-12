package dev.ukanth.iconmgr.home;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.core.view.MenuProvider;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.ConcatAdapter;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import dev.ukanth.iconmgr.App;
import dev.ukanth.iconmgr.MainActivity;
import dev.ukanth.iconmgr.MainViewModel;
import dev.ukanth.iconmgr.Prefs;
import dev.ukanth.iconmgr.R;
import dev.ukanth.iconmgr.dao.ApplyEvent;
import dev.ukanth.iconmgr.dao.IPObj;
import dev.ukanth.iconmgr.data.PackListFilter;
import dev.ukanth.iconmgr.detail.PackDetailActivity;
import dev.ukanth.iconmgr.util.LauncherHelper;
import dev.ukanth.iconmgr.util.PackageComparator;
import dev.ukanth.iconmgr.util.ShortcutHelper;
import dev.ukanth.iconmgr.util.Util;

/**
 * Packs tab: a clean list of installed icon packs. Tap opens a pack, long-press shows every
 * action, and swiping right/left runs the actions chosen in Settings. The subtitle says which
 * pack is in use; applies and favourite changes offer Undo.
 */
public class PacksFragment extends Fragment implements PackRowAdapter.Listener, HomeHeaderAdapter.Listener {

    /** An apply older than this when the list sees it (e.g. made from a widget long ago) gets no Undo prompt. */
    private static final long APPLY_SNACKBAR_WINDOW_MS = 2 * 60 * 1000L;

    private MainViewModel viewModel;
    private PackRowAdapter adapter;
    private HomeHeaderAdapter header;
    private PackSwipeCallback swipeCallback;
    private RecyclerView recyclerView;
    private TextView emptyView;
    private SwipeRefreshLayout swipe;
    private boolean menuAdded;
    /** Time of the latest apply already seen, so only new applies show the "Applied" message. */
    private long seenApplyTime = -1;

    private final MenuProvider menuProvider = new MenuProvider() {
        @Override
        public void onCreateMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
            inflater.inflate(R.menu.menu_packs, menu);
            MenuItem sort = sortMenuItem(menu, viewModel.getSortKey());
            if (sort != null) sort.setChecked(true);
            MenuItem show = menu.findItem(scopeMenuId(viewModel.getScope()));
            if (show != null) show.setChecked(true);

            MenuItem searchItem = menu.findItem(R.id.action_search);
            if (searchItem != null) {
                SearchView searchView = (SearchView) searchItem.getActionView();
                searchView.setQueryHint(getString(R.string.action_search));
                searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                    @Override
                    public boolean onQueryTextSubmit(String query) {
                        return false;
                    }

                    @Override
                    public boolean onQueryTextChange(String query) {
                        viewModel.setQuery(query);
                        return true;
                    }
                });
            }
        }

        @Override
        public boolean onMenuItemSelected(@NonNull MenuItem item) {
            int id = item.getItemId();
            if (id == R.id.action_random) {
                shuffle();
                return true;
            } else if (id == R.id.force) {
                // recomputes every pack's stats in the background; favourites are kept
                viewModel.refresh(true);
                return true;
            } else if (id == R.id.share) {
                ((MainActivity) requireActivity()).shareListScreenshot(recyclerView);
                return true;
            } else if (id == R.id.show_all) {
                return showScope(item, PackListFilter.Scope.ALL);
            } else if (id == R.id.show_favorites) {
                return showScope(item, PackListFilter.Scope.FAVORITES);
            } else if (id == R.id.show_recent) {
                return showScope(item, PackListFilter.Scope.RECENT);
            } else if (id == R.id.sort_alpha) {
                return sortBy(item, PackageComparator.BY_NAME);
            } else if (id == R.id.sort_lastupdate) {
                return sortBy(item, PackageComparator.BY_LAST_UPDATE);
            } else if (id == R.id.sort_count) {
                return sortBy(item, PackageComparator.BY_ICON_COUNT);
            } else if (id == R.id.sort_size) {
                return sortBy(item, PackageComparator.BY_SIZE);
            } else if (id == R.id.sort_percent) {
                return sortBy(item, PackageComparator.BY_COVERAGE);
            } else if (id == R.id.author_name) {
                return sortBy(item, PackageComparator.BY_AUTHOR);
            }
            return false;
        }
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_packs, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        viewModel = new ViewModelProvider(requireActivity()).get(MainViewModel.class);
        recyclerView = view.findViewById(R.id.recycler_view);
        emptyView = view.findViewById(R.id.empty_view);
        swipe = view.findViewById(R.id.swipe_container);

        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new PackRowAdapter(this);
        header = new HomeHeaderAdapter(this);
        recyclerView.setAdapter(new ConcatAdapter(header, adapter));
        swipeCallback = new PackSwipeCallback(adapter, this,
                PackAction.fromKey(Prefs.swipeRight()), PackAction.fromKey(Prefs.swipeLeft()));
        new ItemTouchHelper(swipeCallback).attachToRecyclerView(recyclerView);
        swipe.setOnRefreshListener(() -> viewModel.refresh(false));

        viewModel.getVisiblePacks().observe(getViewLifecycleOwner(), packs -> {
            adapter.submitList(packs);
            updateEmptyState();
        });
        viewModel.getAllPacks().observe(getViewLifecycleOwner(), packs -> {
            updateSubtitle();
            updateEmptyState();
            App.getInstance().getDbExecutor().execute(() -> ShortcutHelper.publish(requireContext().getApplicationContext(), packs));
        });
        viewModel.isSyncing().observe(getViewLifecycleOwner(), syncing -> {
            swipe.setRefreshing(Boolean.TRUE.equals(syncing));
            updateEmptyState();
        });
        viewModel.getInstalledCount().observe(getViewLifecycleOwner(), count -> adapter.setInstalled(count == null ? 0 : count));
        viewModel.getLastApplied().observe(getViewLifecycleOwner(), this::onLastApplied);

        if (!isHidden()) {
            onShown();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        // Settings may have changed the swipe actions or "favourites first"
        swipeCallback.setActions(PackAction.fromKey(Prefs.swipeRight()), PackAction.fromKey(Prefs.swipeLeft()));
        viewModel.reloadPreferences();
        updateHint();
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (hidden) {
            if (menuAdded) {
                requireActivity().removeMenuProvider(menuProvider);
                menuAdded = false;
            }
        } else {
            onShown();
        }
    }

    private void onShown() {
        if (!menuAdded) {
            requireActivity().addMenuProvider(menuProvider, getViewLifecycleOwner());
            menuAdded = true;
        }
        updateSubtitle();
    }

    private void onLastApplied(ApplyEvent event) {
        adapter.setAppliedPkg(event == null ? null : event.packPkg);
        updateSubtitle();
        long time = event == null ? 0 : event.time;
        boolean fresh = seenApplyTime >= 0 && time > seenApplyTime
                && System.currentTimeMillis() - time < APPLY_SNACKBAR_WINDOW_MS;
        seenApplyTime = Math.max(seenApplyTime, time);
        if (fresh && !isHidden()) {
            Snackbar bar = snackbar(getString(R.string.applied_pack, event.packName));
            if (viewModel.findUndoTarget() != null) {
                bar.setAction(R.string.undo, v -> undoApply());
            }
            bar.show();
        }
    }

    /** "Delta is on Nova Launcher", or the pack count when nothing has been applied yet. */
    private void updateSubtitle() {
        if (isHidden() || !(getActivity() instanceof AppCompatActivity)) {
            return;
        }
        ActionBar bar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        if (bar == null) {
            return;
        }
        bar.setTitle(R.string.packs_title);
        ApplyEvent last = viewModel.getLastApplied().getValue();
        if (last != null && last.launcherPkg != null) {
            bar.setSubtitle(getString(R.string.packs_on_launcher, last.packName,
                    LauncherHelper.getLauncherName(requireContext(), last.launcherPkg)));
            return;
        }
        List<IPObj> packs = viewModel.getAllPacks().getValue();
        int count = packs == null ? 0 : packs.size();
        bar.setSubtitle(count > 0 ? getResources().getQuantityString(R.plurals.icon_pack_count, count, count) : null);
    }

    private void updateEmptyState() {
        List<IPObj> all = viewModel.getAllPacks().getValue();
        List<IPObj> shown = viewModel.getVisiblePacks().getValue();
        boolean syncing = Boolean.TRUE.equals(viewModel.isSyncing().getValue());
        boolean noPacksAtAll = all != null && all.isEmpty();
        // while the first sync is still finding packs, show the refresh indicator rather than "none found"
        emptyView.setVisibility(noPacksAtAll && !syncing ? View.VISIBLE : View.GONE);
        recyclerView.setVisibility(noPacksAtAll ? View.GONE : View.VISIBLE);
        header.setFilterEmpty(!noPacksAtAll && shown != null && shown.isEmpty());
    }

    /** One-time hint built from the current swipe settings; gone once dismissed or a gesture was used. */
    private void updateHint() {
        if (Prefs.swipeHintShown()) {
            header.setHint(null);
            return;
        }
        List<String> parts = new ArrayList<>();
        addSwipeHint(parts, R.string.swipe_right_word, PackAction.fromKey(Prefs.swipeRight()));
        addSwipeHint(parts, R.string.swipe_left_word, PackAction.fromKey(Prefs.swipeLeft()));
        parts.add(getString(R.string.hint_long_press));
        header.setHint(String.join(" · ", parts));
    }

    private void addSwipeHint(List<String> parts, @StringRes int direction, PackAction action) {
        int verb;
        switch (action) {
            case APPLY:
                verb = R.string.hint_verb_apply;
                break;
            case FAVORITE:
                verb = R.string.hint_verb_favorite;
                break;
            case OPEN:
                verb = R.string.hint_verb_open;
                break;
            case PLAY_STORE:
                verb = R.string.hint_verb_playstore;
                break;
            case UNINSTALL:
                verb = R.string.hint_verb_uninstall;
                break;
            default:
                return;
        }
        parts.add(getString(R.string.hint_swipe, getString(direction), getString(verb)));
    }

    private void gestureLearned() {
        if (!Prefs.swipeHintShown()) {
            Prefs.setSwipeHintShown();
            header.setHint(null);
        }
    }

    private boolean showScope(MenuItem item, PackListFilter.Scope scope) {
        item.setChecked(true);
        viewModel.setScope(scope);
        recyclerView.scrollToPosition(0);
        return true;
    }

    private boolean sortBy(MenuItem item, String sortKey) {
        item.setChecked(true);
        viewModel.setSortKey(sortKey);
        recyclerView.scrollToPosition(0);
        return true;
    }

    private static int scopeMenuId(PackListFilter.Scope scope) {
        switch (scope) {
            case FAVORITES:
                return R.id.show_favorites;
            case RECENT:
                return R.id.show_recent;
            default:
                return R.id.show_all;
        }
    }

    private static MenuItem sortMenuItem(Menu menu, String sortKey) {
        switch (sortKey) {
            case PackageComparator.BY_LAST_UPDATE:
                return menu.findItem(R.id.sort_lastupdate);
            case PackageComparator.BY_ICON_COUNT:
                return menu.findItem(R.id.sort_count);
            case PackageComparator.BY_SIZE:
                return menu.findItem(R.id.sort_size);
            case PackageComparator.BY_COVERAGE:
                return menu.findItem(R.id.sort_percent);
            case PackageComparator.BY_AUTHOR:
                return menu.findItem(R.id.author_name);
            default:
                return menu.findItem(R.id.sort_alpha);
        }
    }

    // --- PackRowAdapter.Listener (rows, swipes, actions sheet, accessibility actions)

    @Override
    public void onOpen(IPObj pack) {
        startActivity(PackDetailActivity.intent(requireContext(), pack.getIconPkg()));
    }

    @Override
    public void onShowActions(IPObj pack) {
        gestureLearned();
        PackActionsSheet.show(requireActivity(), pack, adapter.metaFor(requireContext(), pack), this);
    }

    @Override
    public void onAction(IPObj pack, PackAction action) {
        gestureLearned();
        Context ctx = requireContext();
        switch (action) {
            case APPLY:
                if (adapter.isApplied(pack)) {
                    snackbar(getString(R.string.already_in_use, pack.getIconName())).show();
                } else {
                    // the "Applied" message with Undo follows from the new apply event
                    Util.determineApply(requireActivity(), pack);
                }
                break;
            case FAVORITE:
                boolean favorite = !pack.isFavorite();
                viewModel.setFavorite(pack, favorite);
                snackbar(getString(favorite ? R.string.favorite_added : R.string.favorite_removed, pack.getIconName()))
                        .setAction(R.string.undo, v -> viewModel.setFavorite(pack, !favorite))
                        .show();
                break;
            case OPEN:
                Intent launch = ctx.getPackageManager().getLaunchIntentForPackage(pack.getIconPkg());
                if (launch != null) {
                    startActivity(launch);
                }
                break;
            case PLAY_STORE:
                openPlayStore(pack.getIconPkg());
                break;
            case UNINSTALL:
                // the system asks for confirmation; the list updates from the package broadcast
                startActivity(new Intent(Intent.ACTION_DELETE, Uri.fromParts("package", pack.getIconPkg(), null)));
                break;
            default:
                break;
        }
    }

    private void openPlayStore(String pkg) {
        try {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + pkg)));
        } catch (ActivityNotFoundException e) {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + pkg)));
        }
    }

    // --- HomeHeaderAdapter.Listener

    @Override
    public void onHintDismissed() {
        gestureLearned();
    }

    /** Apply a random pack, never the one that is already applied (when there is a choice). */
    private void shuffle() {
        List<IPObj> packs = viewModel.getAllPacks().getValue();
        if (packs == null || packs.isEmpty()) {
            snackbar(getString(R.string.unable_iconpack)).show();
            return;
        }
        ApplyEvent last = viewModel.getLastApplied().getValue();
        List<IPObj> candidates = new ArrayList<>();
        for (IPObj pack : packs) {
            if (last == null || !pack.getIconPkg().equals(last.packPkg)) {
                candidates.add(pack);
            }
        }
        if (candidates.isEmpty()) {
            candidates = packs;
        }
        Util.determineApply(requireActivity(), candidates.get(new Random().nextInt(candidates.size())));
    }

    private void undoApply() {
        IPObj previous = viewModel.findUndoTarget();
        if (previous == null) {
            snackbar(getString(R.string.nothing_to_undo)).show();
            return;
        }
        Util.determineApply(requireActivity(), previous);
    }

    /** Snackbar shown above the bottom navigation. */
    private Snackbar snackbar(CharSequence text) {
        Snackbar bar = Snackbar.make(requireView(), text, Snackbar.LENGTH_LONG);
        View nav = requireActivity().findViewById(R.id.bottom_nav);
        if (nav != null) {
            bar.setAnchorView(nav);
        }
        return bar;
    }
}

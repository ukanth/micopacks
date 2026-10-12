package dev.ukanth.iconmgr;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.ViewModel;

import java.util.List;

import dev.ukanth.iconmgr.dao.ApplyEvent;
import dev.ukanth.iconmgr.dao.IPObj;
import dev.ukanth.iconmgr.data.PackListFilter;
import dev.ukanth.iconmgr.data.PackRepository;

/**
 * Home state: the stored packs, filtered by search query and chip, sorted by the chosen key,
 * plus the "Last applied" card. Shared by the host activity and the Packs tab.
 */
public class MainViewModel extends ViewModel {

    private final PackRepository repository = PackRepository.get();
    private final LiveData<List<IPObj>> allPacks = repository.observePacks();
    private final LiveData<List<ApplyEvent>> recentApplies = App.getInstance().getHistoryDao().observeApplies(20);
    private final MediatorLiveData<List<IPObj>> visiblePacks = new MediatorLiveData<>();
    private final MediatorLiveData<ApplyEvent> lastApplied = new MediatorLiveData<>();

    private String query = "";
    private String sortKey = Prefs.sortBy();
    private boolean favoritesFirst = Prefs.useFavorite();
    private PackListFilter.Scope scope = PackListFilter.Scope.ALL;

    public MainViewModel() {
        visiblePacks.addSource(allPacks, packs -> recompute());
        lastApplied.addSource(allPacks, packs -> recomputeLastApplied());
        lastApplied.addSource(recentApplies, applies -> recomputeLastApplied());
    }

    /** Packs to show, already filtered and sorted. */
    public LiveData<List<IPObj>> getVisiblePacks() {
        return visiblePacks;
    }

    /** Every stored pack, regardless of the search query. */
    public LiveData<List<IPObj>> getAllPacks() {
        return allPacks;
    }

    /** Most recent apply whose pack is still installed; null if none. */
    public LiveData<ApplyEvent> getLastApplied() {
        return lastApplied;
    }

    public LiveData<Boolean> isSyncing() {
        return repository.isSyncing();
    }

    public LiveData<Integer> getInstalledCount() {
        return repository.getInstalledCount();
    }

    public void refresh(boolean force) {
        repository.sync(force);
    }

    public void setQuery(String query) {
        this.query = query == null ? "" : query;
        recompute();
    }

    public void setSortKey(String sortKey) {
        this.sortKey = sortKey;
        Prefs.sortBy(sortKey);
        recompute();
    }

    public String getSortKey() {
        return sortKey;
    }

    public void setScope(PackListFilter.Scope scope) {
        this.scope = scope;
        recompute();
    }

    public PackListFilter.Scope getScope() {
        return scope;
    }

    /** Re-read display preferences (after returning from Settings). */
    public void reloadPreferences() {
        favoritesFirst = Prefs.useFavorite();
        recompute();
    }

    public void setFavorite(IPObj pack, boolean favorite) {
        repository.setFavorite(pack.getIconPkg(), favorite);
    }

    /**
     * Pack to go back to with Undo: the most recent apply of a different pack that is still
     * installed, or null.
     */
    public IPObj findUndoTarget() {
        ApplyEvent current = lastApplied.getValue();
        List<ApplyEvent> applies = recentApplies.getValue();
        if (current == null || applies == null) {
            return null;
        }
        for (ApplyEvent event : applies) {
            if (!event.packPkg.equals(current.packPkg)) {
                IPObj pack = findPack(event.packPkg);
                if (pack != null) {
                    return pack;
                }
            }
        }
        return null;
    }

    public IPObj findPack(String pkg) {
        List<IPObj> packs = allPacks.getValue();
        if (packs != null) {
            for (IPObj pack : packs) {
                if (pack.getIconPkg().equals(pkg)) {
                    return pack;
                }
            }
        }
        return null;
    }

    private void recompute() {
        List<IPObj> packs = allPacks.getValue();
        if (packs == null) {
            // the DB hasn't answered yet; leave the list unset so no empty state flashes
            return;
        }
        visiblePacks.setValue(PackListFilter.apply(packs, query, sortKey, favoritesFirst, scope, System.currentTimeMillis()));
    }

    private void recomputeLastApplied() {
        List<ApplyEvent> applies = recentApplies.getValue();
        ApplyEvent latest = applies == null || applies.isEmpty() ? null : applies.get(0);
        lastApplied.setValue(latest != null && findPack(latest.packPkg) != null ? latest : null);
    }
}

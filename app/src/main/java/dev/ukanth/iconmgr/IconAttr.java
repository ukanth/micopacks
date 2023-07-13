package dev.ukanth.iconmgr;

import com.google.gson.GsonBuilder;

/**
 * Created by ukanth on 12/9/17.
 */

public class IconAttr {

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public int getMissed() {
        return missed;
    }

    public void setMissed(int missed) {
        this.missed = missed;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    public long getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(long lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    public boolean getPurchased() {
        return purchased;
    }

    public void isPurchased(boolean purchased) {
        this.purchased = purchased;
    }

    public boolean isFavorite() {
        return favorite;
    }

    public void setFavorite(boolean favorite) {
        this.favorite = favorite;
    }

    private long size = 0;
    private int missed = 0;
    private long lastUpdated = 0;
    private boolean deleted = false;
    private boolean purchased = false;
    private boolean favorite = false;



    @Override
    public String toString() {
        return new GsonBuilder().create().toJson(this, IconAttr.class);
    }
}

package dev.ukanth.iconmgr;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;

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



    /**
     * Parses the JSON stored in IPObj.additional; never returns null so callers can
     * update individual fields without dropping the rest (e.g. favorite).
     */
    public static IconAttr fromJson(String json) {
        if (json != null) {
            try {
                IconAttr attr = new Gson().fromJson(json, IconAttr.class);
                if (attr != null) {
                    return attr;
                }
            } catch (JsonSyntaxException e) {
                // fall through to defaults
            }
        }
        return new IconAttr();
    }

    @Override
    public String toString() {
        return new GsonBuilder().create().toJson(this, IconAttr.class);
    }
}

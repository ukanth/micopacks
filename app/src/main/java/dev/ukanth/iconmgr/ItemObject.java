package dev.ukanth.iconmgr;

/**
 * Created by ukanth on 6/8/17.
 */

class ItemObject {
    private String _stat;
    private String _statsHeader;

    public ItemObject(String stat, String header) {
        this._stat = stat;
        this._statsHeader = header;
    }

    public String get_stat() {
        return _stat;
    }

    public void set_stat(String _stat) {
        this._stat = _stat;
    }

    public String get_statsHeader() {
        return _statsHeader;
    }

    public void set_statsHeader(String _statsHeader) {
        this._statsHeader = _statsHeader;
    }
}

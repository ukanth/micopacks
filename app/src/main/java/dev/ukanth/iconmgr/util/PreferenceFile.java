package dev.ukanth.iconmgr.util;

import android.text.TextUtils;

import org.xmlpull.v1.XmlPullParserException;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class PreferenceFile {

    private boolean isValidPreferenceFile = true;
    private Map<String, Object> mPreferences;
    private List<Entry<String, Object>> mList;

    public static PreferenceSortType preferenceSortType = PreferenceSortType.TYPE_AND_ALPHANUMERIC;


    private PreferenceFile() {
        super();
        mPreferences = new HashMap<String, Object>();
    }

    @SuppressWarnings("unchecked")
    public static PreferenceFile fromXml(String xml) {
        PreferenceFile preferenceFile = new PreferenceFile();

        // Check for empty files
        if (TextUtils.isEmpty(xml) || xml.trim().isEmpty()) {
            return preferenceFile;
        }

        try {
            InputStream in = new ByteArrayInputStream(xml.getBytes());
            Map<String, Object> map = XmlUtils.readMapXml(in);
            in.close();

            if (map != null) {
                preferenceFile.setPreferences(map);
                return preferenceFile;
            }
        } catch (XmlPullParserException ignored) {
        } catch (IOException ignored) {
        }

        preferenceFile.isValidPreferenceFile = false;
        return preferenceFile;
    }

    private void setPreferences(Map<String, Object> map) {
        mPreferences = map;
        mList = new ArrayList<Entry<String, Object>>(mPreferences.entrySet());
        updateSort();
    }

    public String toXml() {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            XmlUtils.writeMapXml(mPreferences, out);
        } catch (XmlPullParserException ignored) {
        } catch (IOException ignored) {
        }
        return out.toString();
    }

    public List<Entry<String, Object>> getList() {
        if (mList == null) {
            mList = new ArrayList<Entry<String, Object>>();
        }
        return mList;
    }

    public void setList(List<Entry<String, Object>> mList) {
        this.mList = mList;
        this.mPreferences = new HashMap<String, Object>();
        for (Entry<String, Object> entry : mList) {
            mPreferences.put(entry.getKey(), entry.getValue());
        }

        updateSort();
    }

    public void updateValue(String key, Object value) {
        for (Entry<String, Object> entry : mList) {
            if (entry.getKey().equals(key)) {
                entry.setValue(value);
                break;
            }
        }
        mPreferences.put(key, value);
        updateSort();
    }

    public void removeValue(String key) {
        mPreferences.remove(key);
        for (Entry<String, Object> entry : mList) {
            if (entry.getKey().equals(key)) {
                mList.remove(entry);
                break;
            }
        }
    }

    private void createAndAddValue(String key, Object value) {
        mList.add(0, new AbstractMap.SimpleEntry<String, Object>(key, value));
        mPreferences.put(key, value);
        updateSort();
    }

    public void add(String previousKey, String newKey, Object value, boolean editMode) {
        if (TextUtils.isEmpty(newKey)) {
            return;
        }

        if (!editMode) {
            if (mPreferences.containsKey(newKey)) {
                updateValue(newKey, value);
            } else {
                createAndAddValue(newKey, value);
            }
        } else {
            if (newKey.equals(previousKey)) {
                updateValue(newKey, value);
            } else {
                removeValue(previousKey);

                if (mPreferences.containsKey(newKey)) {
                    updateValue(newKey, value);
                } else {
                    createAndAddValue(newKey, value);
                }
            }
        }
    }

    public boolean isValid() {
        try {
            XmlUtils.readMapXml(new ByteArrayInputStream(toXml().getBytes()));
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    public boolean isValidPreferenceFile() {
        return isValidPreferenceFile;
    }

    public void updateSort() {
        Collections.sort(getList(), new PreferenceComparator(preferenceSortType));
    }

    class PreferenceComparator implements Comparator<Entry<String, Object>> {

        private PreferenceSortType mType = PreferenceSortType.ALPHANUMERIC;

        public PreferenceComparator(PreferenceSortType type) {
            mType = type;
        }

        @Override
        public int compare(Entry<String, Object> lhs, Entry<String, Object> rhs) {
            if (mType == PreferenceSortType.TYPE_AND_ALPHANUMERIC) {
                String l = lhs == null ? "" : (lhs.getValue() == null ? "" : lhs.getValue().getClass().getName());
                String r = rhs == null ? "" : (rhs.getValue() == null ? "" : rhs.getValue().getClass().getName());
                int res = l.compareToIgnoreCase(r);
                if (res != 0) {
                    return res;
                }
            }
            return (lhs == null ? "" : lhs.getKey()).compareToIgnoreCase((rhs == null ? "" : rhs.getKey()));
        }
    }

    enum PreferenceSortType {
        ALPHANUMERIC, TYPE_AND_ALPHANUMERIC
    }
}
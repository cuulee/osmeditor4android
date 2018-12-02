package de.blau.android.presets;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import de.blau.android.osm.OsmElement.ElementType;
import de.blau.android.presets.Preset.PresetItem;
import de.blau.android.util.collections.MRUList;

/**
 * Container for most recently used tags
 * 
 * @author simon
 *
 */
public class MRUTags {
    private static final String                   DEBUG_TAG  = "MRUTags";
    Map<PresetItem, Map<String, MRUList<String>>> valueStore = new HashMap<>();
    Map<ElementType, MRUList<String>>             keyStore   = new HashMap<>();
    final PresetItem                              dummyItem;

    /**
     * Construct a new container for most recently used tags
     */
    public MRUTags() {
        dummyItem = new Preset().new PresetItem(null, "dummy", null, null);
        dummyItem.setAppliesToNode();
        dummyItem.setAppliesToWay();
        dummyItem.setAppliesToClosedway();
        dummyItem.setAppliesToRelation();
        dummyItem.setAppliesToArea();
    }

    /**
     * Add a key value tupel to the MRU list
     * 
     * @param item the PresetITem this applies to
     * @param key the key
     * @param value the value
     */
    public void put(@NonNull PresetItem item, @NonNull String key, @NonNull String value) {
        Map<String, MRUList<String>> keyMap = valueStore.get(item);
        if (keyMap == null) {
            keyMap = new HashMap<>();
            valueStore.put(item, keyMap);
        }
        MRUList<String> mru = keyMap.get(key);
        if (mru == null) {
            mru = new MRUList<>(10);
            keyMap.put(key, mru);
        }
        mru.push(value);
        putKey(item, key);
    }

    /**
     * Add a key value tupel to the MRU list
     * 
     * This is for tags for which there is no corresponding PresetItem
     * 
     * @param key the key
     * @param value the value
     */
    public void put(@NonNull String key, @NonNull String value) {
        put(dummyItem, key, value);
    }

    /**
     * Store a key according to applicable ElementType
     * 
     * @param item the PresetItem
     * @param key the key
     */
    private void putKey(@NonNull PresetItem item, @NonNull String key) {
        for (ElementType elementType : item.appliesTo()) {
            MRUList<String> mru = keyStore.get(elementType);
            if (mru == null) {
                mru = new MRUList<>(20);
                keyStore.put(elementType, mru);
            }
            mru.push(key);
        }
    }

    /**
     * Get MRU keys for a specific ElementType
     * 
     * @param elementType the ElementType if null all keys will be returned
     * @return a List of keys
     */
    @NonNull
    public List<String> getKeys(@Nullable ElementType elementType) {
        if (elementType != null) {
            List<String> mru = keyStore.get(elementType);
            return mru != null ? mru : new ArrayList<>();
        } else {
            Set<String> result = new HashSet<>();
            for (ElementType et : ElementType.values()) {
                List<String> mru = keyStore.get(et);
                if (mru != null) {
                    result.addAll(mru);
                }
            }
            return new ArrayList<String>(result);
        }
    }

    /**
     * Get MRU values for a specific PresetITem and key
     * 
     * @param item the PresetItem
     * @param key the key
     * @return a List of values or null if none found
     */
    @Nullable
    public List<String> getValues(@NonNull PresetItem item, @NonNull String key) {
        Map<String, MRUList<String>> keyMap = valueStore.get(item);
        if (keyMap != null) {
            MRUList<String> mru = keyMap.get(key);
            if (mru != null) {
                for (String m : mru) {
                    Log.d(DEBUG_TAG, "Value " + m);
                }
                return mru;
            }
        }
        return null;
    }

    /**
     * Get MRU values for a specific PresetITem and key
     * 
     * This is for tags for which there is no corresponding PresetItem
     * 
     * @param key the key
     * @return a List of values or null if none found
     */
    @Nullable
    public List<String> getValues(@NonNull String key) {
        return getValues(dummyItem, key);
    }
}

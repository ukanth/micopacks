package dev.ukanth.iconmgr.iconpack;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Everything Micopacks needs from an icon pack's appfilter.xml, parsed once.
 * Immutable; build it with {@link AppFilterParser}.
 */
public final class AppFilter {

    public static final AppFilter EMPTY = new AppFilter(Collections.emptyMap(), Collections.emptySet(),
            Collections.emptyList(), null, null, 1.0f);

    /** "pkg/fully.qualified.Activity" -> drawable name */
    private final Map<String, String> componentToDrawable;
    /** dynamic calendar icon prefixes (&lt;calendar prefix="..."/&gt;) */
    private final Set<String> calendarPrefixes;
    private final List<String> backNames;
    private final String maskName;
    private final String uponName;
    private final float scale;

    AppFilter(Map<String, String> componentToDrawable, Set<String> calendarPrefixes, List<String> backNames,
              String maskName, String uponName, float scale) {
        this.componentToDrawable = Collections.unmodifiableMap(new HashMap<>(componentToDrawable));
        this.calendarPrefixes = Collections.unmodifiableSet(new HashSet<>(calendarPrefixes));
        this.backNames = Collections.unmodifiableList(backNames);
        this.maskName = maskName;
        this.uponName = uponName;
        this.scale = scale;
    }

    public Map<String, String> getComponentToDrawable() {
        return componentToDrawable;
    }

    public String drawableFor(String component) {
        return componentToDrawable.get(component);
    }

    public boolean covers(String component) {
        return componentToDrawable.containsKey(component);
    }

    /** Distinct drawables referenced by items plus calendar prefixes (the old "appfilter count"). */
    public int distinctIconCount() {
        Set<String> names = new HashSet<>(componentToDrawable.values());
        names.addAll(calendarPrefixes);
        return names.size();
    }

    public Set<String> getCalendarPrefixes() {
        return calendarPrefixes;
    }

    public List<String> getBackNames() {
        return backNames;
    }

    public String getMaskName() {
        return maskName;
    }

    public String getUponName() {
        return uponName;
    }

    public float getScale() {
        return scale;
    }

    public boolean hasMaskingResources() {
        return !backNames.isEmpty() || maskName != null || uponName != null;
    }

    /**
     * Number of installed launcher components this pack has no icon for.
     *
     * @param installedComponents "pkg/fully.qualified.Activity" for every launcher activity
     */
    public int countMissing(Collection<String> installedComponents) {
        int missing = 0;
        for (String component : installedComponents) {
            if (!componentToDrawable.containsKey(component)) {
                missing++;
            }
        }
        return missing;
    }
}

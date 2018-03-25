package de.taubsi.aviary.util;

import de.taubsi.aviary.model.FullLampConfiguration;

import java.util.Comparator;

public class FullLampConfigurationTimeComparator implements Comparator<FullLampConfiguration> {
    @Override
    public int compare(FullLampConfiguration o1, FullLampConfiguration o2) {
        return Integer.compare(o1.getTime(), o2.getTime());
    }
}

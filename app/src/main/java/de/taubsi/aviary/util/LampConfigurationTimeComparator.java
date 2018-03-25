package de.taubsi.aviary.util;

import de.taubsi.aviary.model.FullLampConfiguration;
import de.taubsi.aviary.model.LampConfiguration;

import java.util.Comparator;

public class LampConfigurationTimeComparator implements Comparator<LampConfiguration> {
    @Override
    public int compare(LampConfiguration o1, LampConfiguration o2) {
        return Integer.compare(o1.getTime(), o2.getTime());
    }
}

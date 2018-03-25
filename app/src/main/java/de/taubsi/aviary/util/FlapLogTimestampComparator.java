package de.taubsi.aviary.util;

import de.taubsi.aviary.model.FlapLog;
import de.taubsi.aviary.model.HeaterLog;

import java.util.Comparator;

public class FlapLogTimestampComparator implements Comparator<FlapLog> {
    @Override
    public int compare(FlapLog o1, FlapLog o2) {
        return o1.getTimestamp().compareTo(o2.getTimestamp());
    }
}

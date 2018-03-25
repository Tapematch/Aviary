package de.taubsi.aviary.util;

import de.taubsi.aviary.model.HeaterLog;

import java.util.Comparator;

public class HeaterLogTimestampComparator implements Comparator<HeaterLog> {
    @Override
    public int compare(HeaterLog o1, HeaterLog o2) {
        return o1.getTimestamp().compareTo(o2.getTimestamp());
    }
}

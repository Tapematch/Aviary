package de.taubsi.aviary.model;

import java.sql.Timestamp;
import java.util.Calendar;

public class HeaterLog {
    private Timestamp timestamp;
    private boolean on;
    private boolean auto;
    private long id;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }

    public boolean isOn() {
        return on;
    }

    public void setOn(boolean on) {
        this.on = on;
    }

    public boolean isAuto() {
        return auto;
    }

    public void setAuto(boolean auto) {
        this.auto = auto;
    }

    public boolean isValid() {
        return timestamp != null;
    }
}

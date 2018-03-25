package de.taubsi.aviary.model;

import java.sql.Timestamp;
import java.util.Calendar;

public class FlapLog {
    private Timestamp timestamp;
    private boolean open;
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

    public boolean isOpen() {
        return open;
    }

    public void setOpen(boolean open) {
        this.open = open;
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


package de.taubsi.aviary.model;

public class LampConfiguration {
    private int id;
    private int number;
    private int time;
    private int value;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public int getTime() {
        return time;
    }

    public void setTime(int time) {
        this.time = time;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        LampConfiguration that = (LampConfiguration) o;

        if (id != that.id) return false;
        if (number != that.number) return false;
        if (time != that.time) return false;
        if (value != that.value) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = id;
        result = 31 * result + number;
        result = 31 * result + time;
        result = 31 * result + value;
        return result;
    }
}

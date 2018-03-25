package de.taubsi.aviary.model;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.Calendar;

public class Log implements Serializable {
    private Timestamp timestamp;
    private float tempOut;
    private float tempIn;
    private float humidOut;
    private float humidIn;
    private int lamp1;
    private int lamp2;
    private int lamp3;
    private int lamp4;
    private int lamp5;
    private int lamp6;
    private int lux;
    private int luxDimPercent;
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

    public float getTempOut() {
        return tempOut;
    }

    public void setTempOut(float tempOut) {
        this.tempOut = tempOut;
    }

    public float getTempIn() {
        return tempIn;
    }

    public void setTempIn(float tempIn) {
        this.tempIn = tempIn;
    }

    public float getHumidOut() {
        return humidOut;
    }

    public void setHumidOut(float humidOut) {
        this.humidOut = humidOut;
    }

    public float getHumidIn() {
        return humidIn;
    }

    public void setHumidIn(float humidIn) {
        this.humidIn = humidIn;
    }

    public int getLamp1() {
        return lamp1;
    }

    public void setLamp1(int lamp1) {
        this.lamp1 = lamp1;
    }

    public int getLamp2() {
        return lamp2;
    }

    public void setLamp2(int lamp2) {
        this.lamp2 = lamp2;
    }

    public int getLamp3() {
        return lamp3;
    }

    public void setLamp3(int lamp3) {
        this.lamp3 = lamp3;
    }

    public int getLamp4() {
        return lamp4;
    }

    public void setLamp4(int lamp4) {
        this.lamp4 = lamp4;
    }

    public int getLamp5() {
        return lamp5;
    }

    public void setLamp5(int lamp5) {
        this.lamp5 = lamp5;
    }

    public int getLamp6() {
        return lamp6;
    }

    public void setLamp6(int lamp6) {
        this.lamp6 = lamp6;
    }

    public int getLux() {
        return lux;
    }

    public void setLux(int lux) {
        this.lux = lux;
    }

    public int getLuxDimPercent() {
        return luxDimPercent;
    }

    public void setLuxDimPercent(int luxDimPercent) {
        this.luxDimPercent = luxDimPercent;
    }

    @Override
    public String toString() {
        return "timestamp - " + timestamp + " | tempOut - " + tempOut + " | tempIn - " + tempIn + " | humidOut - " + humidOut + " | humidIn - " + humidIn + " | lamp1 - " + lamp1 + " | lamp2 - " + lamp2 + " | lamp3 - " + lamp3 + " | lamp4 - " + lamp4 + " | lamp5 - " + lamp5 + " | lamp6 - " + lamp6 + " | lux - " + lux + " | luxDimPercent - " + luxDimPercent;
    }

    public boolean isValid() {
        return timestamp != null;
    }
}

package de.taubsi.aviary.model;

import android.content.Context;
import de.taubsi.aviary.R;
import de.taubsi.aviary.util.TimeUtil;

public class Configuration {
    private int id;
    private String name;
    private float value;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public float getValue() {
        return value;
    }

    public void setValue(float value) {
        this.value = value;
    }

    public String getConfigurationTitle(Context context){
        switch (getName()){
            case "hysteresis":
                return context.getString(R.string.hysteresis_title);
            case "low_temp_alarm":
                return context.getString(R.string.low_temp_alarm_title);
            case "lux_dim":
                return context.getString(R.string.lux_dim_title);
            case "lux_dim_percent":
                return context.getString(R.string.lux_dim_percent_title);
            case "temp_open_flap":
                return context.getString(R.string.temp_open_flap_title);
            case "temp_target":
                return context.getString(R.string.temp_target_title);
            case "time_open_flap":
                return context.getString(R.string.time_open_flap_title);
            case "time_end_open_flap":
                return context.getString(R.string.time_end_open_flap_title);
        }
        return "Unbekannte Konfiguration";
    }

    public String getConfigurationDescription(Context context){
        switch (getName()){
            case "hysteresis":
                return String.format(context.getString(R.string.hysteresis_description), getValue());
            case "low_temp_alarm":
                return String.format(context.getString(R.string.low_temp_alarm_description), getValue());
            case "lux_dim":
                return String.format(context.getString(R.string.lux_dim_description), getValue());
            case "lux_dim_percent":
                return String.format(context.getString(R.string.lux_dim_percent_description), getValue());
            case "temp_open_flap":
                return String.format(context.getString(R.string.temp_open_flap_description), getValue());
            case "temp_target":
                return String.format(context.getString(R.string.temp_target_description), getValue());
            case "time_open_flap":
                int time = (int) getValue();
                return String.format(context.getString(R.string.time_open_flap_description), TimeUtil.getHours(time), TimeUtil.getMinutes(time));
            case "time_end_open_flap":
                int timeEnd = (int) getValue();
                return String.format(context.getString(R.string.time_end_open_flap_description), TimeUtil.getHours(timeEnd), TimeUtil.getMinutes(timeEnd));
        }
        return "";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Configuration that = (Configuration) o;

        if (Float.compare(that.value, value) != 0) return false;
        if (name != null ? !name.equals(that.name) : that.name != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (value != +0.0f ? Float.floatToIntBits(value) : 0);
        return result;
    }

    public boolean isValid() {
        return name != null;
    }
}

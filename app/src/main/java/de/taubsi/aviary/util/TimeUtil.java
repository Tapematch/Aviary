package de.taubsi.aviary.util;

public class TimeUtil {
    public static int getHours(int time){
        return time / 60;
    }

    public static int getMinutes(int time){
        return time % 60;
    }

    public static int getTime(int hours, int minutes){
        return hours * 60 + minutes;
    }
}

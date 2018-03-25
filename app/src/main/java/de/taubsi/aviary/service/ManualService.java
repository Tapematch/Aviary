package de.taubsi.aviary.service;

import android.content.Context;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.taubsi.aviary.model.Log;

import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.Scanner;


public class ManualService {
    HttpService httpService;

    public ManualService(Context context) throws IOException {
        this.httpService = new HttpService(context);
    }

    public String toggle(boolean on) throws IOException {

        String onOff = on ? "on" : "off";
        String url = "/manual/" + onOff;
        InputStream resultIS = httpService.get(url);
        String result = new Scanner(resultIS, "utf-8").useDelimiter("\\Z").next();
        if(Boolean.parseBoolean(result))
            return null;
        else
            return result;
    }

    public String setLamp(int number, int value) throws IOException {

        String url = "/manual/set/lamp" + number + "?value=" + value;
        InputStream resultIS = httpService.get(url);
        String result = new Scanner(resultIS, "utf-8").useDelimiter("\\Z").next();
        if(Boolean.parseBoolean(result))
            return null;
        else
            return result;
    }

    public String setHeater(boolean on) throws IOException {

        String url = "/manual/set/heater?value=" + on;
        InputStream resultIS = httpService.get(url);
        String result = new Scanner(resultIS, "utf-8").useDelimiter("\\Z").next();
        if(Boolean.parseBoolean(result))
            return null;
        else
            return result;
    }

    public String setFlap(boolean open) throws IOException {

        String url = "/manual/set/flap?value=" + open;
        InputStream resultIS = httpService.get(url);
        String result = new Scanner(resultIS, "utf-8").useDelimiter("\\Z").next();
        if(Boolean.parseBoolean(result))
            return null;
        else
            return result;
    }

}

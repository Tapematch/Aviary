package de.taubsi.aviary.service;

import android.content.Context;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.taubsi.aviary.model.HeaterLog;
import de.taubsi.aviary.model.Log;

import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class HeaterLogService {
    HttpService httpService;

    public HeaterLogService(Context context) throws IOException {
        this.httpService = new HttpService(context);
    }

    public HeaterLog getLatestHeaterLog() throws IOException {
        String url = "/heaterlog/get/latest";
        InputStream result = httpService.get(url);

        ObjectMapper mapper = new ObjectMapper();
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S", Locale.GERMAN);
        mapper.setDateFormat(df);

        return mapper.readValue(result, new TypeReference<HeaterLog>() {
        });
    }

    public List<HeaterLog> listHeaterLog(int minutes, int hours, int days) throws IOException {
        String url = "/heaterlog/get/list?days=" + days + "&hours=" + hours + "&minutes=" + minutes;
        InputStream result = httpService.get(url);

        ObjectMapper mapper = new ObjectMapper();
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S", Locale.GERMAN);
        mapper.setDateFormat(df);

        return mapper.readValue(result, new TypeReference<List<HeaterLog>>() {
        });
    }
}

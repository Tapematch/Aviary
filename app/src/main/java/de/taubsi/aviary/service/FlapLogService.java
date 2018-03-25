package de.taubsi.aviary.service;

import android.content.Context;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.taubsi.aviary.model.FlapLog;
import de.taubsi.aviary.model.HeaterLog;

import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class FlapLogService {

    HttpService httpService;


    public FlapLogService(Context context) throws IOException {
        this.httpService = new HttpService(context);
    }

    public FlapLog getLatestFlapLog() throws IOException {
        String url = "/flaplog/get/latest";
        InputStream result = httpService.get(url);

        ObjectMapper mapper = new ObjectMapper();
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S", Locale.GERMAN);
        mapper.setDateFormat(df);

        return mapper.readValue(result, new TypeReference<FlapLog>() {
        });
    }

    public List<FlapLog> listFlapLog(int minutes, int hours, int days) throws IOException {
        String url = "/flaplog/get/list?days=" + days + "&hours=" + hours + "&minutes=" + minutes;
        InputStream result = httpService.get(url);

        ObjectMapper mapper = new ObjectMapper();
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S", Locale.GERMAN);
        mapper.setDateFormat(df);

        return mapper.readValue(result, new TypeReference<List<FlapLog>>() {
        });
    }
}

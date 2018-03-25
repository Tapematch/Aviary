package de.taubsi.aviary.service;

import android.content.Context;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.taubsi.aviary.model.FullLampConfiguration;
import de.taubsi.aviary.model.LampConfiguration;

import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class LampConfigurationService {
    HttpService httpService;

    public LampConfigurationService(Context context) throws IOException {
        this.httpService = new HttpService(context);
    }

    public List<LampConfiguration> getLampConfigurations(int number) throws IOException {
        String url = "/lampconfiguration/get/list?number=" + number;
        InputStream result = httpService.get(url);

        ObjectMapper mapper = new ObjectMapper();
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S", Locale.GERMAN);
        mapper.setDateFormat(df);

        return mapper.readValue(result, new TypeReference<List<LampConfiguration>>() {});
    }

    public List<FullLampConfiguration> getFullLampConfigurations() throws IOException {
        String url = "/lampconfiguration/get/full/list";
        InputStream result = httpService.get(url);

        ObjectMapper mapper = new ObjectMapper();
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S", Locale.GERMAN);
        mapper.setDateFormat(df);

        return mapper.readValue(result, new TypeReference<List<FullLampConfiguration>>() {});
    }

    public String deleteLampConfig(int id) throws IOException {
        String url = "/lampconfiguration/delete?id=" + id;
        return httpService.delete(url);
    }

    public String saveLampConfiguration(LampConfiguration lampConfiguration) throws IOException {
        String url = "/lampconfiguration/post";

        ObjectMapper mapper = new ObjectMapper();
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S", Locale.GERMAN);
        mapper.setDateFormat(df);
        String json = mapper.writeValueAsString(lampConfiguration);

        return httpService.post(url, json);
    }
}

package de.taubsi.aviary.service;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.taubsi.aviary.SettingsActivity;

import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class ConfigurationService {

    HttpService httpService;

    public ConfigurationService(Context context) throws IOException {
        this.httpService = new HttpService(context);
    }

    public List<de.taubsi.aviary.model.Configuration> getConfigurations() throws IOException {
        String url = "/configuration/get/list";
        InputStream result = httpService.get(url);

        ObjectMapper mapper = new ObjectMapper();
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S", Locale.GERMAN);
        mapper.setDateFormat(df);

        return mapper.readValue(result, new TypeReference<List<de.taubsi.aviary.model.Configuration>>() {});
    }

    public String updateConfiguration(de.taubsi.aviary.model.Configuration configuration) throws IOException {
        String url = "/configuration/post";

        ObjectMapper mapper = new ObjectMapper();
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S", Locale.GERMAN);
        mapper.setDateFormat(df);
        String json = mapper.writeValueAsString(configuration);

        return httpService.post(url, json);
    }
}

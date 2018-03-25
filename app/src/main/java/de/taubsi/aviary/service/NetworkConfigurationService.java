package de.taubsi.aviary.service;

import android.content.Context;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class NetworkConfigurationService {

    HttpService httpService;

    public NetworkConfigurationService(Context context) throws IOException {
        this.httpService = new HttpService(context);
    }

    public de.taubsi.aviary.model.NetworkConfiguration getConfiguration(String name) throws IOException {
        String url = "/networkconfiguration/get?name=" + name;
        InputStream result = httpService.get(url);

        ObjectMapper mapper = new ObjectMapper();
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S", Locale.GERMAN);
        mapper.setDateFormat(df);

        return mapper.readValue(result, new TypeReference<de.taubsi.aviary.model.NetworkConfiguration>() {});
    }
}

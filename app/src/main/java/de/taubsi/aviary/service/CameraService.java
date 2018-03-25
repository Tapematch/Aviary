package de.taubsi.aviary.service;

import android.content.Context;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.taubsi.aviary.model.FlapLog;

import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class CameraService {

    HttpService httpService;

    public CameraService(Context context) throws IOException {
        this.httpService = new HttpService(context);
    }

    public void controlCamera(String action) throws IOException {
        String url = "/camera/" + action;
        httpService.post(url, "");
    }

}

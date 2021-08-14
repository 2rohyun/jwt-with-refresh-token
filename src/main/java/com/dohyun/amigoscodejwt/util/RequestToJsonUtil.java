package com.dohyun.amigoscodejwt.util;

import org.json.JSONObject;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.io.BufferedReader;

public class RequestToJsonUtil {

    public static JSONObject readJsonFromRequestBody(HttpServletRequest request){
        StringBuilder json = new StringBuilder();
        String line = null;
        try {
            BufferedReader reader = request.getReader();
            while((line = reader.readLine()) != null) {
                json.append(line);
            }

        }catch(Exception e) {
            System.out.println("Error reading JSON string: " + e.toString());
        }
        return new JSONObject(json.toString());
    }
}

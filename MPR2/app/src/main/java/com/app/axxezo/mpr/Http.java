package com.app.axxezo.mpr;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by nmartin on 24-01-17.
 */

public class Http {

    public static String Get(String url) throws Exception {
        URL obj = new URL(url);
        HttpURLConnection conn = (HttpURLConnection) obj.openConnection();
        conn.setRequestMethod("GET");

        //Running request
        int respCode = conn.getResponseCode(); // TODO Handle response error codes

        //Reading response
        BufferedReader br = new BufferedReader(new InputStreamReader((conn.getInputStream())));
        StringBuilder sb = new StringBuilder();
        String output;
        while ((output = br.readLine()) != null)
            sb.append(output);
        return sb.toString();
    }

    public static String Post(String url, String postData, String contentType) throws Exception {
        URL obj = new URL(url);
        HttpURLConnection conn = (HttpURLConnection) obj.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", contentType);

        //Writing POST data
        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(conn.getOutputStream()));
        bw.write(postData);
        bw.flush();
        bw.close();

        //Running request
        int respCode = conn.getResponseCode(); // TODO Handle response error codes

        //Reading response
        BufferedReader br = new BufferedReader(new InputStreamReader((conn.getInputStream())));
        StringBuilder sb = new StringBuilder();
        String output;
        while ((output = br.readLine()) != null)
            sb.append(output);
        return sb.toString();
    }
}

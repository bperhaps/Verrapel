package com.example.user.myapplication;

import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;

import okhttp3.*;


public class Sender extends Thread{
    private HashMap<String, String> params = null;
    private HashMap<String, String> result = null;

    public void setParams(HashMap<String, String> params) {
        this.params = params;
    }

    public HashMap<String, String> getResult() {
        return result;
    }

    @Override
    public void run(){


        try {
            String body = getPostDataString(params);


            try (Socket client = new Socket()) {
                //클라이언트 초기화
                InetSocketAddress ipep = new InetSocketAddress("localhost", 3000);
                //접속
                client.connect(ipep);

                //send,reciever 스트림 받아오기
                //자동 close
                try (OutputStream sender = client.getOutputStream();
                     InputStream receiver = client.getInputStream();) {
                    String header = "POST /login HTTP/1.1\r\n" +
                            "Host: localhost:3000\r\n" +
                            "Content-Length: " + body.length() + "\r\n" +
                            "Content-Type: application/x-www-form-urlencoded\r\n" +
                            "\r\n" +
                             body;
                    sender.write(header.getBytes(), 0, header.length());

                    //서버로부터 데이터 받기
                    //11byte
                    BufferedReader rd = new BufferedReader(new InputStreamReader(receiver, "UTF-8"));
                    String line = "";
                    result = new HashMap<String, String>();
                    result.put("islogin", "true");
                    while (true) {
                        line = rd.readLine();
                        if (line.equals(""))
                            break;
                        if (line.contains("set-cookie")) {
                            String cookie = line.substring( line.indexOf("set-cookie: ")+12, line.length() );
                            result.put("cookie", cookie);
                        }
                        if (line.contains("/login"))
                            result.put("islogin", "false");
                    }
                    client.close();
                }
            } catch (Throwable e) {
                e.printStackTrace();
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    private String getPostDataString(HashMap<String, String> params) throws UnsupportedEncodingException {
        StringBuilder result = new StringBuilder();
        boolean first = true;
        for (Map.Entry<String, String> entry : params.entrySet()) {
            if (first)
                first = false;
            else
                result.append("&");

            result.append(URLEncoder.encode(entry.getKey(), "UTF-8"));
            result.append("=");
            result.append(URLEncoder.encode(entry.getValue(), "UTF-8"));
        }

        return result.toString();
    }
}

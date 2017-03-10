package com.whpe.nfcdemo.common;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class HttpUtils {

    private static String seesionId="";
    private static String tokenkey="";

    public static String sendHttpRequest(String apiUrl, Map<String, String> params) {
        String result = "";
        DataOutputStream out = null;
        InputStreamReader in = null;
        try {
            URL url = new URL(apiUrl);

            HttpURLConnection httpConn = (HttpURLConnection) url.openConnection();
            httpConn.setRequestProperty("X-Requested-With", "XMLHttpRequest");
            httpConn.setRequestProperty("Client-Type", "Android");
            if(tokenkey != null && !"".equals(tokenkey)){
                httpConn.addRequestProperty("Cookie", "TOKENKEY=" + tokenkey);
            }
            if(seesionId != null && !"".equals(seesionId)){
                httpConn.addRequestProperty("Cookie", "JSESSIONID=" + seesionId);
            }
            httpConn.setDoOutput(true);
            httpConn.setDoInput(true);
            httpConn.connect();
            if(params != null && params.size() > 0){
                //DataOutputStream流
                out = new DataOutputStream(httpConn.getOutputStream());
                //要上传的参数
                String content = buildRequestBody(params);
                //将要上传的内容写入流中
                out.writeBytes(content);
                //刷新、关闭
                out.flush();
            }
            in = new InputStreamReader(httpConn.getInputStream());
            BufferedReader buffer = new BufferedReader(in);

            Map<String, List<String>> headerFields = httpConn.getHeaderFields();
            Set<Map.Entry<String, List<String>>> entrySet = headerFields.entrySet();
            for(Map.Entry<String, List<String>> entry : entrySet){
                if("Set-Cookie".equals(entry.getKey())){
                    List<String> cookieValues = entry.getValue();
                    for(String cookieValue : cookieValues){
                        if(cookieValue.startsWith("JSESSIONID")){
                            seesionId = cookieValue.substring(11,cookieValue.indexOf(";"));
                        }
                        if(cookieValue.startsWith("TOKENKEY")){
                            tokenkey = cookieValue.substring(9,cookieValue.indexOf(";"));
                        }
                    }
                    break;
                }
            }

            result = buffer.readLine();
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            try {
                if(out != null){out.close();}
                if(in != null){in.close();}
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return result;
    }

    public static String sendHttpRequest(String apiUrl) {
        return sendHttpRequest(apiUrl, null);
    }

    private static String buildRequestBody(Map<String, String> params) throws UnsupportedEncodingException {
        String requestBody = "";
        Set<Map.Entry<String, String>> entrySet = params.entrySet();
        for(Map.Entry<String, String> entry : entrySet){
            if(entry.getKey() != null && entry.getValue() != null){
                requestBody += (entry.getKey() + "=" + URLEncoder.encode(entry.getValue(), "UTF-8") + "&");
            }
        }
        return requestBody.substring(0,requestBody.length()-1);
    }

    public static String getSeesionId() {
        return seesionId;
    }

    public static void setSeesionId(String seesionId) {
        HttpUtils.seesionId = seesionId;
    }

    public static String getTokenkey() {
        return tokenkey;
    }

    public static void setTokenkey(String tokenkey) {
        HttpUtils.tokenkey = tokenkey;
    }
}

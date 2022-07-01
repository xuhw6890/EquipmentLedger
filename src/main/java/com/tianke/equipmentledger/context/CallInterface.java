package com.tianke.equipmentledger.context;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.CharArrayBuffer;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public class CallInterface {

    //post
    public static String postJsonTest(String url, JSONArray jsonArray) {
        String result = null;
        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpPost post = new HttpPost(url);
        CloseableHttpResponse response = null;
        try {
            post.setHeader("Content-Type", "application/json;charset=utf-8");
            JSONObject param = new JSONObject();
            param.put("sessionID", jsonArray);//添加参数
            //json参数自己添加
            post.setEntity(new StringEntity(param.toString(), "UTF-8"));
            response = httpClient.execute(post);
            if (response != null && response.getStatusLine().getStatusCode() == 200) {
                HttpEntity entity = response.getEntity();
                result = entityToString(entity);
            }
            return result;
        } catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            try {
                httpClient.close();
                if (response != null) {
                    response.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public static String entityToString(HttpEntity entity) throws IOException {
        String result = null;
        if (entity != null) {
            long length = entity.getContentLength();
            if (length != -1 && length < 2048) {
                result = EntityUtils.toString(entity, "UTF-8");
            } else {
                try (InputStreamReader reader1 = new InputStreamReader(entity.getContent(), StandardCharsets.UTF_8)) {
                    CharArrayBuffer buffer = new CharArrayBuffer(2048);
                    char[] tmp = new char[1024];
                    int l;
                    while ((l = reader1.read(tmp)) != -1) {
                        buffer.append(tmp, 0, l);
                    }
                    result = buffer.toString();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return result;
    }

}

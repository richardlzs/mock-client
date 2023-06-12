package com.micerlabs.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.databind.util.JSONPObject;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;

@Service
public class ImgSendService {

    @Value("${destination_url}")
    private String destinationUrl;

    public void sengImage(InputStream is, String fileName) {
        try {
            CloseableHttpClient httpClient = HttpClients.createDefault();
            HttpPost httpPost = new HttpPost(destinationUrl);
            MultipartEntityBuilder builder = MultipartEntityBuilder.create();
            // 绑定文件参数，传入文件流和ContentType
            builder.addBinaryBody("file",is, ContentType.MULTIPART_FORM_DATA,fileName);
            HttpEntity entity = builder.build();
            httpPost.setEntity(entity);
            //执行提交
            HttpResponse response = httpClient.execute(httpPost);
            HttpEntity responseEntity = response.getEntity();
            if (responseEntity != null) {
                String result = EntityUtils.toString(responseEntity, StandardCharsets.UTF_8);
                JSONObject output = JSON.parseObject(result);
                System.out.println(output.get("status"));
            }
            if (is != null) {
                is.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}

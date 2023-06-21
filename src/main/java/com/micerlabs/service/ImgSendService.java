package com.micerlabs.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.databind.util.JSONPObject;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.GZIPOutputStream;

@Service
public class ImgSendService {

    @Value("${destination_url}")
    private String destinationUrl;

    @Value("${login_url}")
    private String loginUrl;
    @Value("${lowavl_username}")
    private String username;
    @Value("${lowavl_password}")
    private String passwd;

    CloseableHttpClient httpClient = HttpClients.createDefault();
    boolean loggedIn = false;

    public void login() {
        try {
            HttpPost httpPost = new HttpPost(loginUrl);
            List<BasicNameValuePair> params = new ArrayList<>();
            params.add(new BasicNameValuePair("uname", username));
            params.add(new BasicNameValuePair("passwd", passwd));
            httpPost.setEntity(new UrlEncodedFormEntity(params));
            HttpResponse response = httpClient.execute(httpPost);
            HttpEntity responseEntity = response.getEntity();
            System.out.println(responseEntity);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void sengImage(InputStream is, String fileName,String hash) {
        try {
            if (!loggedIn) {
                login();
                loggedIn = true;
            }
            HttpPost httpPost = new HttpPost(destinationUrl);
            MultipartEntityBuilder builder = MultipartEntityBuilder.create();
            // 绑定文件参数，传入文件流和ContentType
            builder.addBinaryBody("file",is, ContentType.MULTIPART_FORM_DATA,fileName);
            HttpEntity entity = builder.build();
            httpPost.setEntity(entity);

            httpPost.setHeader("hash",hash);
            //执行提交
            HttpResponse response = httpClient.execute(httpPost);
            HttpEntity responseEntity = response.getEntity();
            if(response.getStatusLine().getStatusCode()!=200)
            {
                System.err.println(response.getStatusLine().getStatusCode());
                return;
            }
            if (responseEntity != null) {
                String result = EntityUtils.toString(responseEntity, StandardCharsets.UTF_8);
                JSONObject output = JSON.parseObject(result);
                System.out.println(output.get("status"));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

    }

    /**
     * get_hash
     * @param inputStream 输入流
     * @return String:SHA-1(formax:0x) or null
     */
    public String get_hash_SHA1(InputStream inputStream)
    {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            DigestInputStream dis = new DigestInputStream(inputStream, md);

            byte[] buffer = new byte[8192]; // 缓冲区大小
            while (dis.read(buffer) != -1) {
                // 读取文件内容以更新哈希值
            }
            dis.close();

            byte[] hashBytes = md.digest();
            StringBuilder sb = new StringBuilder();
            for (byte b : hashBytes) {
                sb.append(String.format("%02x", b)); // 转换为十六进制表示
            }
            String hash = sb.toString();

            System.out.println("SHA-1 Hash value: " + hash);
            return hash;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    //压缩后写入temp文件防止图片过大（我不知道有没有必要？）
     public static Path compressFile(InputStream inputStream) throws IOException {
        Path compressedFilePath = null;
        FileOutputStream fos = null;
        GZIPOutputStream gzipOS = null;

        try {
            // 创建临时文件
            compressedFilePath = Files.createTempFile("compressed", ".gz");

            fos = new FileOutputStream(compressedFilePath.toFile());
            gzipOS = new GZIPOutputStream(fos);
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                gzipOS.write(buffer, 0, bytesRead);
            }

          //  System.out.println("文件压缩成功！");

        }catch (IOException e)
        {
            e.printStackTrace();
            return null;
        }
        finally {
            // 关闭资源
            if (gzipOS != null) {
                try {
                    gzipOS.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return compressedFilePath;
    }
}

package com.micerlabs.service;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

@Service
public class ImgGenService {

    @Resource
    private ImgSendService imgSendService;

    @Scheduled(cron = "0/5 * * * * *")
    public void imageGenerate() throws IOException {
        // 当前时间戳
        Date date = new Date();
        // 生成图片
        BufferedImage bufferedImage = new BufferedImage(300, 50, BufferedImage.TYPE_INT_RGB);
        Graphics paint = bufferedImage.getGraphics();
        paint.setColor(Color.WHITE);
        paint.fillRect(0, 0, 300, 50);
        paint.setColor(Color.blue);
        paint.drawString(getImgContent(date), 5, 20);
        imgSendService.sengImage(bufferedImageToInputStream(bufferedImage), date.getTime() + ".jpg");
    }

    /**
     * 返回图片内容：当前时间戳 - 格式化
     * @return eg: 2022/05/10 20:45
     */
    public String getImgContent(Date now) {
        SimpleDateFormat sf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        return sf.format(now);
    }

    /**
     * 将BufferedImage转换为InputStream以实现网络传递
     * @param image BufferedImage
     * @return InputStream
     */
    public InputStream bufferedImageToInputStream(BufferedImage image) {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        try {
            ImageIO.write(image, "jpg", os);
            InputStream input = new ByteArrayInputStream(os.toByteArray());
            return input;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}

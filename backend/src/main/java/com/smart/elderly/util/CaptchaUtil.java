package com.smart.elderly.util;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.Random;

public class CaptchaUtil {

    private static final int WIDTH = 120;
    private static final int HEIGHT = 40;
    private static final int CODE_COUNT = 4;
    private static final int LINE_COUNT = 20;
    private static final String CODE_CHARS = "23456789abcdefghjkmnpqrstuvwxyzABCDEFGHJKMNPQRSTUVWXYZ";

    private static final Random RANDOM = new Random();

    public static String generateCode() {
        StringBuilder code = new StringBuilder();
        for (int i = 0; i < CODE_COUNT; i++) {
            code.append(CODE_CHARS.charAt(RANDOM.nextInt(CODE_CHARS.length())));
        }
        return code.toString();
    }

    public static String generateCaptchaBase64(String code) {
        BufferedImage image = generateCaptchaImage(code);
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            ImageIO.write(image, "png", baos);
            return "data:image/png;base64," + Base64.getEncoder().encodeToString(baos.toByteArray());
        } catch (IOException e) {
            throw new RuntimeException("生成验证码图片失败", e);
        }
    }

    private static BufferedImage generateCaptchaImage(String code) {
        BufferedImage image = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = image.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g2.setColor(getRandomColor(200, 250));
        g2.fillRect(0, 0, WIDTH, HEIGHT);

        g2.setFont(new Font("Arial", Font.BOLD, 28));

        for (int i = 0; i < LINE_COUNT; i++) {
            int x1 = RANDOM.nextInt(WIDTH);
            int y1 = RANDOM.nextInt(HEIGHT);
            int x2 = RANDOM.nextInt(WIDTH);
            int y2 = RANDOM.nextInt(HEIGHT);
            g2.setColor(getRandomColor(160, 200));
            g2.drawLine(x1, y1, x2, y2);
        }

        for (int i = 0; i < code.length(); i++) {
            g2.setColor(getRandomColor(20, 130));
            int x = 25 + i * 22;
            int y = 28;
            g2.translate(x, y);
            double theta = RANDOM.nextDouble() * 0.4 - 0.2;
            g2.rotate(theta);
            g2.drawString(String.valueOf(code.charAt(i)), 0, 0);
            g2.rotate(-theta);
            g2.translate(-x, -y);
        }

        g2.dispose();
        return image;
    }

    private static Color getRandomColor(int fc, int bc) {
        if (fc > 255) fc = 255;
        if (bc > 255) bc = 255;
        int r = fc + RANDOM.nextInt(bc - fc);
        int g = fc + RANDOM.nextInt(bc - fc);
        int b = fc + RANDOM.nextInt(bc - fc);
        return new Color(r, g, b);
    }
}

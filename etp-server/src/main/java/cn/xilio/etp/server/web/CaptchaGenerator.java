package cn.xilio.etp.server.web;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Random;

/**
 * 验证码生成工具类
 * 生成120×40像素的验证码图片
 * @author liuxin
 */
public class CaptchaGenerator {
    private static final int WIDTH = 120;
    private static final int HEIGHT = 40;
    private static final int CODE_LENGTH = 4;

    /**
     *字符集（排除易混淆字符）
     */
    private static final String CHAR_SET = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghjkmnpqrstuvwxyz23456789";

    private final Random random = new Random();
    /**
     * 生成验证码图片和验证码值
     */
    public CaptchaResult generateCaptcha() {
        // 1. 生成随机验证码
        String code = generateRandomCode();

        // 2. 创建图片对象
        BufferedImage image = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();

        // 3. 设置抗锯齿
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // 4. 绘制背景
        drawBackground(g);

        // 5. 绘制干扰线
        drawInterferenceLines(g, 5);

        // 6. 绘制验证码字符
        drawCode(g, code);

        // 7. 添加噪点
        addNoise(image, 30);

        g.dispose();

        return new CaptchaResult(code, image);
    }

    /**
     * 生成随机验证码字符串
     */
    private String generateRandomCode() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < CODE_LENGTH; i++) {
            int index = random.nextInt(CHAR_SET.length());
            sb.append(CHAR_SET.charAt(index));
        }
        return sb.toString();
    }

    /**
     * 绘制背景
     */
    private void drawBackground(Graphics2D g) {
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, WIDTH, HEIGHT);
    }

    /**
     * 绘制干扰线
     */
    private void drawInterferenceLines(Graphics2D g, int lineCount) {
        for (int i = 0; i < lineCount; i++) {
            int x1 = random.nextInt(WIDTH);
            int y1 = random.nextInt(HEIGHT);
            int x2 = random.nextInt(WIDTH);
            int y2 = random.nextInt(HEIGHT);

            g.setColor(getRandomColor(100, 200));
            g.setStroke(new BasicStroke(1.5f));
            g.drawLine(x1, y1, x2, y2);
        }
    }

    /**
     * 绘制验证码字符
     */
    private void drawCode(Graphics2D g, String code) {
        // 设置字体
        Font font = new Font("Arial", Font.BOLD, 24);
        g.setFont(font);

        for (int i = 0; i < code.length(); i++) {
            // 设置字符颜色
            g.setColor(getRandomColor(0, 150));

            // 计算字符位置（居中显示）
            int x = i * (WIDTH / code.length()) + 10;
            int y = HEIGHT / 2 + 8;

            // 添加随机旋转（-15°到15°）
            double angle = Math.toRadians(random.nextInt(30) - 15);
            g.rotate(angle, x, y);

            // 绘制字符
            g.drawString(String.valueOf(code.charAt(i)), x, y);

            // 恢复旋转
            g.rotate(-angle, x, y);
        }
    }

    /**
     * 添加噪点
     */
    private void addNoise(BufferedImage image, int noiseCount) {
        for (int i = 0; i < noiseCount; i++) {
            int x = random.nextInt(WIDTH);
            int y = random.nextInt(HEIGHT);
            image.setRGB(x, y, getRandomColor(150, 255).getRGB());
        }
    }

    /**
     * 生成随机颜色
     */
    private Color getRandomColor(int min, int max) {
        if (min > max) {
            int temp = min;
            min = max;
            max = temp;
        }

        int r = min + random.nextInt(max - min);
        int g = min + random.nextInt(max - min);
        int b = min + random.nextInt(max - min);

        return new Color(r, g, b);
    }

    /**
     * 将图片转换为字节数组
     */
    public static byte[] toByteArray(BufferedImage image) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(image, "JPEG", baos);
        return baos.toByteArray();
    }

    /**
     * 验证码结果封装类
     */
    public record CaptchaResult(String code, BufferedImage image) {
    }
}

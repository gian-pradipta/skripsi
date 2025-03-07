package org.example;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;
import java.io.File;
import java.io.InputStream;

public class Main {

    public static void main(String[] args) {
        Preprocessor p = new Preprocessor();
        System.out.println("Hello, world!");
        int gkernel_size = 5;
        float[] kernel = p.generateGaussianKernel(gkernel_size, gkernel_size/6f);
        float[] l_kernel = {
                0,   0,  -1,   0,   0,
                0,  -1,  -2,  -1,   0,
                -1,  -2,  16,  -2,  -1,
                0,  -1, -2,  -1,   0,
                0,   0,  -1,  0,   0
        };
        float[] l_kernel2 = {
                1, 1, 1,
                1, -8, 1,
                1, 1, 1
        };
        try {
            BufferedImage imgToPixels = new BufferedImage(436, 618, BufferedImage.TYPE_BYTE_GRAY);
            InputStream imageStream = Preprocessor.class.getResourceAsStream("/burik.jpg");
            BufferedImage img = ImageIO.read(imageStream);
            int[][] pixels = p.fromImgToPixels(img);
            imgToPixels = p.fromPixelsToImg(pixels);
            img = p.fromPixelsToImg(pixels);

            ConvolveOp op = new ConvolveOp(new Kernel(3, 3, l_kernel2), ConvolveOp.EDGE_NO_OP, null);
            imgToPixels = op.filter(img, imgToPixels);
            pixels = p.fromImgToPixels(imgToPixels);
            double variance = p.calculateVariance(pixels);
            System.out.println(variance);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}

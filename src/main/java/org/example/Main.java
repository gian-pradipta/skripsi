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
            InputStream imageStream = Preprocessor.class.getResourceAsStream("/brightened_image.jpg");
            BufferedImage img = ImageIO.read(imageStream);
            int[][] pixels = p.fromImgToPixels(img);

//            pixels = p.convolve(pixels, p.generateGaussianKernel(3, 3/6f), 3);

            if (p.isFoggy(pixels)) System.out.println("Gambar Berkabut!");
            if (p.isBlurry(pixels)) System.out.println("Gambar Blurr!");

            pixels = p.medianFilter(pixels, 5);
            imgToPixels = p.fromPixelsToImg(pixels);
            System.out.printf("kk");
            ImageIO.write(imgToPixels, "jpg" ,new File("out23.jpg"));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}

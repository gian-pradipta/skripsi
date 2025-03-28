package org.example

import java.awt.image.BufferedImage
import java.util.*

class Preprocessor {
    private fun gaussian(x: Int, y: Int, sigma: Double): Double {
        require(!(sigma <= 0)) { "Sigma must be positive." }
        val squaredDistance = (x * x + y * y).toDouble()
        val denominator = 2 * sigma * sigma
        val exponent = -squaredDistance / denominator
        return 1 / (2 * Math.PI * sigma * sigma) * Math.exp(exponent)
    }

    private fun calculateVariance(image: Array<IntArray>): Double {
        val rows = image.size
        val cols = image[0].size
        var sum: Long = 0
        for (i in 0 until rows) {
            for (j in 0 until cols) {
                sum += image[i][j].toLong()
            }
        }
        val mean = sum.toDouble() / (rows * cols)
        var varianceSum = 0.0
        for (i in 0 until rows) {
            for (j in 0 until cols) {
                varianceSum += Math.pow(image[i][j] - mean, 2.0)
            }
        }
        return varianceSum / (rows * cols)
    }

    fun histogramEqualization(image: Array<IntArray>, width: Int, height: Int): Array<IntArray> {
        val histogram = IntArray(256)
        val cdf = IntArray(256)
        val equalizedImage = Array(width) { IntArray(height) }
        for (x in 0 until width) {
            for (y in 0 until height) {
                histogram[image[x][y]]++
            }
        }
        cdf[0] = histogram[0]
        for (i in 1..255) {
            cdf[i] = cdf[i - 1] + histogram[i]
        }
        val cdfMin = cdf[0]
        val totalPixels = width * height
        val equalizationMap = IntArray(256)
        for (i in 0..255) {
            equalizationMap[i] = Math.round((cdf[i] - cdfMin) / (totalPixels - cdfMin).toFloat() * 255)
            if (equalizationMap[i] < 0) equalizationMap[i] = 0
            if (equalizationMap[i] > 255) equalizationMap[i] = 255
        }
        for (x in 0 until width) {
            for (y in 0 until height) {
                equalizedImage[x][y] = equalizationMap[image[x][y]]
            }
        }
        return equalizedImage
    }

    fun generateGaussianKernel(size: Int, sigma: Float): FloatArray {
        val arr = Array(size * size) { FloatArray(2) }
        var x = 0f
        var y = 0f
        val offset = size / 2
        for (i in 0 until size * size) {
            arr[i] = floatArrayOf(x - offset, y - offset)
            y++
            if (y >= size) {
                y = 0f
                x++
            }
        }
        val doubleResult = Arrays.stream(arr)
                .mapToDouble { n: FloatArray -> gaussian(n[0].toInt(), offset, sigma.toDouble()).toFloat().toDouble() }
                .toArray()
        val sum = Arrays.stream(doubleResult).sum()
        for (i in doubleResult.indices) {
            doubleResult[i] /= sum
        }
        val result = FloatArray(doubleResult.size)
        for (i in result.indices) {
            result[i] = doubleResult[i].toFloat()
        }
        return result
    }

    fun convolve(pixels: Array<IntArray>, kernel: FloatArray, ksize: Int): Array<IntArray?> {
        val width = pixels.size
        val height = pixels[0].size
        val out = arrayOfNulls<IntArray>(pixels.size)
        for (i in pixels.indices) {
            out[i] = Arrays.copyOf(pixels[i], pixels[i].size)
        }
        val half_k = ksize / 2
        for (i in half_k until width - half_k) {
            for (j in half_k until height - half_k) {
                var sum = 0f
                for (ki in kernel.indices) {
                    val kx = ki % ksize
                    val ky = ki / ksize
                    val offset_x = kx - half_k
                    val offset_y = ky - half_k
                    sum += kernel[ki] * pixels[i + offset_y][j + offset_x]
                }
                val temp = Math.min(255, Math.max(0, Math.abs(sum.toInt())))
                out[i]!![j] = temp
            }
        }
        return out
    }

    fun medianFilter(pixels: Array<IntArray>, ksize: Int) : Array<IntArray>{
        val width = pixels.size
        val height = pixels[0].size
        val out = arrayOfNulls<IntArray>(pixels.size)
        for (i in pixels.indices) {
            out[i] = Arrays.copyOf(pixels[i], pixels[i].size)
        }
        val kernel = FloatArray(ksize * ksize)
        val half_k = ksize / 2
        for (i in half_k until width - half_k) {
            for (j in half_k until height - half_k) {
                for (ki in kernel.indices) {
                    val kx = ki % ksize
                    val ky = ki / ksize
                    val offset_x = kx - half_k
                    val offset_y = ky - half_k
                    kernel[ki] =  pixels[i + offset_y][j + offset_x].toFloat()
                }
                kernel.sort()
                out[i]!![j] = kernel[ksize*ksize / 2].toInt();
            }
        }
        return out.requireNoNulls();
    }

    fun isFoggy(pixels: Array<IntArray>) : Boolean {
        val sobelX : FloatArray = arrayOf(
            -1f, 0f, 1f,
            -2f, 0f, 2f,
            -1f, 0f, 1f
        ).toFloatArray()

        val sobelY : FloatArray = arrayOf(
            -1f, -2f, -1f,
            0f,  0f,  0f,
            1f,  2f,  1f
        ).toFloatArray()
        val blurredPixels = convolve(pixels, generateGaussianKernel(3, 3/6f), 3);
        val eqPixels = histogramEqualization(blurredPixels.requireNoNulls(), 224, 224);

        val gx = convolve(eqPixels, sobelX, 3)
        val gy = convolve(eqPixels, sobelY, 3)

        val rows = pixels.size
        val cols = pixels[0].size
        var sumMagnitude = 0.0
        var count = 0

        for (i in 0 until rows) {
            for (j in 0 until cols) {
                val gradientX = gx[i]?.get(j)?.toDouble()
                val gradientY = gy[i]?.get(j)?.toDouble()
                if (gradientY == null || gradientX == null) return  false;
                val magnitude = Math.sqrt(gradientX * gradientX + gradientY * gradientY)
                sumMagnitude += magnitude
                count++
            }
        }

        val contrastScore = sumMagnitude / count
        println(contrastScore);
        return contrastScore < 35.0
    }

    fun  isBlurry(pixels: Array<IntArray>): Boolean {
        val threshold = 550;
        val gaussianKernel = generateGaussianKernel(3, 3/6f);
        val l_kernel2 : FloatArray = arrayOf(
                    1f, 1f, 1f,
                    1f, -8f, 1f,
                    1f, 1f, 1f
        ).toFloatArray();
        var newPixels : Array<IntArray?>;
        val eqPixels = histogramEqualization(pixels, 224, 224);
        newPixels = convolve(eqPixels, gaussianKernel, 3);
        if (newPixels == null) return false;

        newPixels = convolve(newPixels.requireNoNulls(), l_kernel2, 3);
        if (newPixels == null) return false;
        val variance = calculateVariance(newPixels.requireNoNulls());
        println(variance);
        return variance < threshold;
    }

    fun fromImgToPixels(img: BufferedImage): Array<IntArray> {
        val width = img.width
        val height = img.height
        val output = Array(height) { IntArray(width) }
        for (y in 0 until height) {
            for (x in 0 until width) {
                val rgb = img.getRGB(x, y)
                val r = rgb shr 16 and 0xFF
                val g = rgb shr 8 and 0xFF
                val b = rgb and 0xFF
                val gray = (0.299 * r + 0.587 * g + 0.114 * b).toInt()
                output[y][x] = gray
            }
        }
        return output
    }

    fun fromPixelsToImg(pixels: Array<IntArray>): BufferedImage {
        val height = pixels.size
        val width = pixels[0].size
        val img = BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY)
        for (y in 0 until height) {
            for (x in 0 until width) {
                val gray = pixels[y][x] // Grayscale value (0-255)
                val rgb = gray shl 16 or (gray shl 8) or gray // R=G=B=gray
                img.setRGB(x, y, rgb)
            }
        }
        return img
    }
}

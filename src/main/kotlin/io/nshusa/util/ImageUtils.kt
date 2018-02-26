package io.nshusa.util

import java.awt.image.FilteredImageSource
import java.awt.image.RGBImageFilter
import java.awt.image.BufferedImage
import java.awt.Color
import java.awt.Image
import java.awt.Toolkit


object ImageUtils {

    fun imageToBufferedImage(image: Image): BufferedImage {
        val bufferedImage = BufferedImage(image.getWidth(null), image.getHeight(null), BufferedImage.TYPE_INT_ARGB)
        val g2 = bufferedImage.createGraphics()
        g2.drawImage(image, 0, 0, null)
        g2.dispose()
        return bufferedImage
    }

    fun makeColorTransparent(im: BufferedImage, color: Color): Image {
        val filter = object : RGBImageFilter() {
            var markerRGB = color.getRGB() or -0x1000000

            override fun filterRGB(x: Int, y: Int, rgb: Int): Int {
                return if (rgb or -0x1000000 == markerRGB) {
                    0x00FFFFFF and rgb
                } else {
                    rgb
                }
            }
        }

        val ip = FilteredImageSource(im.source, filter)
        return Toolkit.getDefaultToolkit().createImage(ip)
    }

    fun fxColorToAwtColor(color: javafx.scene.paint.Color) : Color {
        return Color(color.red.toFloat(), color.green.toFloat(), color.blue.toFloat(), color.opacity.toFloat())
    }

}
package io.nshusa.util

import javafx.embed.swing.SwingFXUtils
import javafx.scene.image.Image
import java.awt.image.BufferedImage
import java.io.File

object SpritePackerUtils {

    fun isImage(file: File) : Boolean {
        return file.name.endsWith(".png") || file.name.endsWith(".jpg")
    }

    fun toFXImage(bimage: BufferedImage) : Image {
        return SwingFXUtils.toFXImage(bimage, null);
    }

}
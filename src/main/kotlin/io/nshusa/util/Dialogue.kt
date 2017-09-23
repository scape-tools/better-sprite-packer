package io.nshusa.util

import javafx.scene.control.Alert
import java.awt.Desktop
import javafx.scene.control.ButtonType
import javafx.scene.control.ButtonBar.ButtonData
import java.io.File

object Dialogue {

    fun openDirectory(headerText: String, dir: File) {
        val alert = Alert(Alert.AlertType.CONFIRMATION)
        alert.title = "Information"
        alert.headerText = headerText
        alert.contentText = "Choose an option."

        val choiceOne = ButtonType("Yes.")
        val close = ButtonType("No", ButtonData.CANCEL_CLOSE)

        alert.buttonTypes.setAll(choiceOne, close)

        val result = alert.showAndWait()

        if (result.isPresent) {

            val type = result.get()

            if (type == choiceOne) {
                try {
                    Desktop.getDesktop().open(dir)
                } catch (ex: Exception) {
                    ex.printStackTrace()
                }

            }

        }
    }

}
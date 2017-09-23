@file:JvmName("App")

package io.nshusa

import io.nshusa.util.BSPUtils
import javafx.application.Application
import javafx.application.Platform
import javafx.fxml.FXMLLoader
import javafx.scene.Parent
import javafx.scene.Scene
import javafx.scene.control.Alert
import javafx.scene.control.ButtonType
import javafx.scene.image.Image
import javafx.stage.Stage
import javafx.stage.StageStyle
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.URL
import java.util.*
import java.util.concurrent.CountDownLatch
import java.util.concurrent.FutureTask

class App : Application() {

    val properties = Properties()

    override fun init() {
        try {
            properties.load(App::class.java.getResourceAsStream("/settings.properties"))

            val latch = CountDownLatch(1)

            val future = FutureTask<Boolean> {

                BufferedReader(InputStreamReader(URL(properties.getProperty("version_link")).openStream())).use {

                    val version = it.readLine().trim()

                    if (properties.getProperty("version") != version) {

                        val alert = Alert(Alert.AlertType.CONFIRMATION)
                        alert.title = "Update"
                        alert.headerText = "Update $version available."
                        alert.contentText = "Would you like to update to version $version? "

                        val result = alert.showAndWait()

                        if (result.isPresent) {
                            if (result.get() == ButtonType.OK) {
                                BSPUtils.launchURL(properties.getProperty("release_link"))
                                Platform.exit()
                            }
                        }

                    }

                    latch.countDown()

                }

                true
            }

            Platform.runLater(future)

            try {
                latch.await()
            } catch (e: InterruptedException) {
                e.printStackTrace()
                Platform.exit()
            }

        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }

    override fun start(stage: Stage?) {
        mainStage = stage!!
        val root : Parent? = FXMLLoader.load(App::class.java.getResource("/Main.fxml"))
        stage.title = "Better Sprite Packer"
        val scene = Scene(root)
        scene.stylesheets.add(App::class.java.getResource("/style.css").toExternalForm())
        stage.scene = scene
        stage.icons?.add(Image(App::class.java.getResourceAsStream("/icons/icon.png")))
        stage.centerOnScreen()
        stage.sizeToScene()
        stage.initStyle(StageStyle.UNDECORATED)
        stage.show()
    }

    companion object {

        lateinit var mainStage : Stage

        @JvmStatic
        fun main(args : Array<String>) {
            launch(App::class.java)
        }
    }

}
package io.nshusa.controller

import javafx.application.Platform
import javafx.fxml.FXML
import javafx.fxml.Initializable
import javafx.scene.control.Label
import javafx.scene.input.MouseEvent
import java.net.URL
import java.util.*

class Controller : Initializable {

    var offsetX = 0.0

    var offsetY = 0.0

    override fun initialize(location: URL?, resource: ResourceBundle?) {

    }

    @FXML
    fun importImages() {

    }

    @FXML
    fun handleMouseDragged(event: MouseEvent) {
        var stage = App.mainStage

        stage.x = event.screenX - offsetX
        stage.y = event.screenY - offsetY
    }

    @FXML
    fun handleMousePressed(event: MouseEvent) {
        offsetX = event.sceneX
        offsetY = event.sceneY
    }

    @FXML
    fun minimizeProgram() {
        App.mainStage.isIconified = true
    }

    @FXML
    fun closeProgram() {
        Platform.exit()
    }

}
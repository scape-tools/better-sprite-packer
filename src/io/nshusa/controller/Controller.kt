package io.nshusa.controller

import javafx.application.Platform
import javafx.fxml.FXML
import javafx.fxml.Initializable
import javafx.scene.control.Label
import java.net.URL
import java.util.*

class Controller : Initializable {

    override fun initialize(location: URL?, resource: ResourceBundle?) {

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
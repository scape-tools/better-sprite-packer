package io.nshusa.controller

import javafx.fxml.FXML
import javafx.fxml.Initializable
import javafx.scene.control.Label
import java.net.URL
import java.util.*

class Controller : Initializable {

    @FXML
    var label : Label = Label()

    override fun initialize(location: URL?, resource: ResourceBundle?) {

    }

    @FXML
    fun sayHello() {
        if (label.text.isEmpty()) {
            label.text = "Hello world!"
        } else {
            label.text = ""
        }
    }

}
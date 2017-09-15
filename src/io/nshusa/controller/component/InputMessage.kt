package io.nshusa.controller.component

import javafx.scene.control.TextInputDialog

class InputMessage(headerText: String) : TextInputDialog() {

    init {
        this.headerText = headerText
    }

}

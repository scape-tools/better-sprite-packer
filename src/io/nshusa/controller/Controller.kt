package io.nshusa.controller

import io.nshusa.controller.component.InputMessage
import javafx.application.Platform
import javafx.fxml.FXML
import javafx.fxml.Initializable
import javafx.scene.control.ColorPicker
import javafx.scene.control.TreeItem
import javafx.scene.control.TreeView
import javafx.scene.input.MouseEvent
import javafx.scene.paint.Color
import javafx.stage.FileChooser
import java.net.URL
import java.nio.file.Paths
import java.util.*

class Controller : Initializable {

    var offsetX = 0.0

    var offsetY = 0.0

    @FXML
    lateinit var treeView: TreeView<TreeNode>

    @FXML
    lateinit var colorPicker: ColorPicker

    val userHome = Paths.get(System.getProperty("user.home"))

    override fun initialize(location: URL?, resource: ResourceBundle?) {
        treeView.root = TreeItem(TreeNode("root"))
        colorPicker.value = Color.MAGENTA
    }

    @FXML
    fun createArchive() {
        val msg = InputMessage("Enter the name of the archive to create.")

        val result = msg.showAndWait()

        if (!result.isPresent) {
            return
        }

        treeView.root.children.add(TreeItem(TreeNode(result.get())))
    }

    @FXML
    fun importImages() {
        val fc = FileChooser()
        fc.initialDirectory = userHome.toFile()
        fc.extensionFilters.addAll(FileChooser.ExtensionFilter("All Images", "*.*"),
                FileChooser.ExtensionFilter("JPG", "*.jpg"),
                FileChooser.ExtensionFilter("PNG", "*.png"))
        val files = fc.showOpenMultipleDialog(App.mainStage)

        val selected = treeView.selectionModel.selectedIndex

        if (selected == -1) {
            return
        }


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
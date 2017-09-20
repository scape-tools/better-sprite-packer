package io.nshusa.controller

import io.nshusa.TreeNode
import io.nshusa.component.InputMessage
import io.nshusa.util.SpritePackerUtils
import javafx.application.Platform
import javafx.fxml.FXML
import javafx.fxml.Initializable
import javafx.scene.control.ColorPicker
import javafx.scene.control.TreeItem
import javafx.scene.control.TreeView
import javafx.scene.image.Image
import javafx.scene.image.ImageView
import javafx.scene.input.MouseEvent
import javafx.scene.paint.Color
import javafx.stage.FileChooser
import java.net.URL
import java.nio.file.Paths
import java.util.*
import javax.imageio.ImageIO

class Controller : Initializable {

    var offsetX = 0.0

    var offsetY = 0.0

    @FXML
    lateinit var treeView: TreeView<TreeNode>

    @FXML
    lateinit var colorPicker: ColorPicker

    lateinit var placeholderIcon: Image

    val userHome = Paths.get(System.getProperty("user.home"))

    override fun initialize(location: URL?, resource: ResourceBundle?) {
        treeView.root = TreeItem(TreeNode("root"))
        colorPicker.value = Color.MAGENTA

       try {
           placeholderIcon = Image("icons/placeholder.png")
       } catch (ex: Exception) {
            println("Failed to load icons.")
       }

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

        val selectedItem = treeView.selectionModel.selectedItem

        if (selectedItem.parent.parent != null) {
            println("detected non directory")
            return
        }

        for (file in files) {

            if (!SpritePackerUtils.isImage(file)) {
                continue
            }

            val bimage = ImageIO.read(file)

            val prefix = SpritePackerUtils.getFilePrefix(file)

            var spriteId = -1

            try {
                spriteId = Integer.parseInt(prefix)
            } catch (ex: Exception) {

            }

            for (i in 0 until spriteId) {

                val size = selectedItem.children.size

                if (size < spriteId) {
                    val placeholder = ImageView(placeholderIcon)

                    placeholder.fitWidth = 32.0
                    placeholder.fitHeight = 32.0
                    placeholder.isPreserveRatio = true

                    selectedItem.children.add(TreeItem<TreeNode>(TreeNode(size.toString()), placeholder))
                }

            }

            if (spriteId != -1) {
                selectedItem.children.add(TreeItem<TreeNode>(TreeNode(spriteId.toString()), ImageView(SpritePackerUtils.toFXImage(bimage))))
            }

        }

        println(selectedItem.children.size)

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
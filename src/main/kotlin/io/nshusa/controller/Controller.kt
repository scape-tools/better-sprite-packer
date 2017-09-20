package io.nshusa.controller

import io.nshusa.TreeNode
import io.nshusa.component.InputMessage
import io.nshusa.util.SpritePackerUtils
import javafx.application.Platform
import javafx.fxml.FXML
import javafx.fxml.Initializable
import javafx.scene.control.*
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
        treeView.root = TreeItem(TreeNode("Archives"))
        treeView.root.isExpanded = true
        colorPicker.value = Color.MAGENTA

        treeView.selectionModel.selectedItemProperty().addListener {obs, oldSelecttion, newSelection ->

            if (newSelection.parent == null) { // root
                val contextMenu = ContextMenu()

                val createMI = MenuItem("Create")
                createMI.setOnAction { it -> createArchive() }

                val exportMI = MenuItem("Export")

                contextMenu.items.addAll(arrayOf(createMI, exportMI))

                treeView.contextMenu = contextMenu
            } else if (newSelection.parent.parent == null) { // archives
                val contextMenu = ContextMenu()

                val importMI = MenuItem("Import")
                importMI.setOnAction { it -> importImages() }

                val exportMI = MenuItem("Export")

                val removeMI = MenuItem("Remove")
                removeMI.setOnAction { it -> removeNode() }

                contextMenu.items.addAll(arrayOf(importMI, exportMI, removeMI))

                treeView.contextMenu = contextMenu
            } else { // images
                val contextMenu = ContextMenu()

                val importMI = MenuItem("Import")
                importMI.setOnAction { it -> importImages() }

                val exportMI = MenuItem("Export")

                val removeMI = MenuItem("Remove")
                removeMI.setOnAction { it -> removeNode() }

                val replaceMI = MenuItem("Replace")

                contextMenu.items.addAll(arrayOf(importMI, exportMI, removeMI, replaceMI))

                treeView.contextMenu = contextMenu
            }
        }

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

        val selectedIndex = treeView.selectionModel.selectedIndex

        val selectedItem = treeView.selectionModel.selectedItem

        if (selectedIndex == -1) {
            return
        }

        if (selectedItem.parent == null) {
            return
        }

        if (selectedItem.parent.parent != null) {
            println("detected non directory")
            return
        }

        val fc = FileChooser()
        fc.initialDirectory = userHome.toFile()
        fc.extensionFilters.addAll(FileChooser.ExtensionFilter("All Images", "*.*"),
                FileChooser.ExtensionFilter("JPG", "*.jpg"),
                FileChooser.ExtensionFilter("PNG", "*.png"))

        val files = fc.showOpenMultipleDialog(App.mainStage) ?: return

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
                if (selectedItem.children.size > spriteId) {
                    selectedItem.children.removeAt(spriteId)

                    val imageView = ImageView(SpritePackerUtils.toFXImage(bimage))

                    if (bimage.width > 256) {
                        imageView.fitWidth = 256.0
                    }

                    if (bimage.height > 256) {
                        imageView.fitHeight = 256.0
                    }

                    imageView.isPreserveRatio = true

                    selectedItem.children.add(spriteId, TreeItem<TreeNode>(TreeNode(spriteId.toString()), imageView))
                } else {

                    val imageView = ImageView(SpritePackerUtils.toFXImage(bimage))

                    if (bimage.width > 128) {
                        imageView.fitWidth = 128.0
                    }

                    if (bimage.height > 128) {
                        imageView.fitHeight = 128.0
                    }

                    imageView.isPreserveRatio = true

                    selectedItem.children.add(TreeItem<TreeNode>(TreeNode(spriteId.toString()), imageView))
                }
            }

        }

    }

    @FXML
    fun removeNode() {
        val selectedIndex = treeView.selectionModel.selectedIndex

        if (selectedIndex == -1) {
            return
        }

        val selectedItem = treeView.selectionModel.selectedItem

        val selectedParent = treeView.selectionModel.selectedItem.parent

        if (selectedParent.parent != null) {

            val indexToRemove = selectedParent.children.indexOf(selectedItem)

            // remove
            if (indexToRemove == selectedParent.children.size - 1) {
                selectedParent.children.removeAt(indexToRemove)
            } else { // set graphic
                val node = selectedParent.children.get(indexToRemove)

                val placeholder = ImageView(placeholderIcon)

                placeholder.fitWidth = 32.0
                placeholder.fitHeight = 32.0
                placeholder.isPreserveRatio = true

                node.graphic = placeholder
            }

        } else {
            selectedParent.children.remove(selectedItem)
        }

    }

    @FXML
    fun clearProgram() {
        treeView.root.children.clear()
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
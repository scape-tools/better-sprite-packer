package io.nshusa.controller

import io.nshusa.Sprite
import io.nshusa.bsp.extra.SpritePackerUtils
import io.nshusa.util.Dialogue
import io.nshusa.util.BSPUtils
import javafx.application.Platform
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import javafx.collections.transformation.FilteredList
import javafx.embed.swing.SwingFXUtils
import javafx.fxml.FXML
import javafx.fxml.Initializable
import javafx.scene.control.*
import javafx.scene.image.Image
import javafx.scene.image.ImageView
import javafx.scene.input.MouseEvent
import javafx.scene.paint.Color
import javafx.stage.DirectoryChooser
import javafx.stage.FileChooser
import java.io.ByteArrayInputStream
import java.io.File
import java.io.IOException
import java.net.URL
import java.nio.file.Paths
import java.util.*
import java.nio.file.Files
import javax.imageio.ImageIO

class Controller : Initializable {

    var offsetX = 0.0

    var offsetY = 0.0

    @FXML
    lateinit var listView: ListView<Sprite>

    @FXML
    lateinit var colorPicker: ColorPicker

    @FXML
    lateinit var imageView: ImageView

    @FXML
    lateinit var searchTf: TextField

    lateinit var placeholderIcon: Image

    val userHome = Paths.get(System.getProperty("user.home"))

    private val elements: ObservableList<Sprite> = FXCollections.observableArrayList()

    private lateinit var filteredSprites: FilteredList<Sprite>

    private lateinit var newImage : Image

    override fun initialize(location: URL?, resource: ResourceBundle?) {
        colorPicker.value = Color.MAGENTA
        listView.selectionModel.selectionMode = SelectionMode.MULTIPLE

        try {
            placeholderIcon = Image("icons/placeholder.png")
        } catch (ex: Exception) {
            println("Failed to load icons.")
        }

        filteredSprites = FilteredList(elements, { _ -> true })

        searchTf.textProperty().addListener({ _, _, newValue -> filteredSprites.setPredicate({

            if (newValue.isEmpty()) {
                true
            } else {
                Integer.toString(it.id) == newValue
            }

        })})

        listView.items = this.filteredSprites

        listView.setCellFactory({ _ ->
            object : ListCell<Sprite>() {
                private val imageView = ImageView()

                override fun updateItem(sprite: Sprite?, empty: Boolean) {
                    super.updateItem(sprite, empty)

                    if (empty) {
                        text = ""
                        graphic = null
                    } else {

                        try {
                            if (sprite?.data?.isEmpty()!!) {
                                imageView.image = placeholderIcon
                                imageView.fitWidth = 32.0
                                imageView.fitHeight = 32.0
                                text = sprite?.id.toString()
                                graphic = imageView
                                return
                            }

                            val image = ImageIO.read(ByteArrayInputStream(sprite.data))
                            imageView.fitWidth = (if (image.width > 128) 128.0 else image.width.toDouble())
                            imageView.fitHeight = (if (image.height > 128) 128.0 else image.height.toDouble())
                            imageView.isPreserveRatio = true
                            newImage = SwingFXUtils.toFXImage(image, null)
                            imageView.image = newImage
                            text = sprite.id.toString()
                            graphic = imageView
                        } catch (e: IOException) {
                            e.printStackTrace()
                        }
                    }
                }
            }
        })

        listView.selectionModel.selectedItemProperty().addListener({_, _, newValue ->

            if (newValue != null && !newValue.data?.isEmpty()!!) {
                imageView.image = newValue.toImage()
            }

        })

    }

    @FXML
    fun importImage() {
        val chooser = FileChooser()
        chooser.initialDirectory = userHome.toFile()
        chooser.extensionFilters.add(FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.gif"))
        val selectedFile = chooser.showOpenDialog(App.mainStage) ?: return

        if (!SpritePackerUtils.isValidImage(selectedFile)) {
            Dialogue.showWarning(String.format("${selectedFile.name} is not a valid image.")).showAndWait()
            return
        }

        try {
            val id = Integer.parseInt(BSPUtils.getFilePrefix(selectedFile))

            val data = Files.readAllBytes(selectedFile.toPath())

            if (id < elements.size) {
                elements[id].data = data
            } else {

                for (i in elements.size until id) {
                    elements.add(Sprite(i, ByteArray(0)))
                }

                elements.add(Sprite(id, data))
            }

            listView.refresh()

        } catch (ex: Exception) {
            Dialogue.showWarning("Images should be named like 0.png, 1.png, 2.png could not read id for ${selectedFile.name}.").showAndWait()
        }

    }

    @FXML
    fun importImages() {
        val chooser = DirectoryChooser()
        chooser.initialDirectory = userHome.toFile()
        val selectedDirectory = chooser.showDialog(App.mainStage) ?: return

        val files = selectedDirectory.listFiles()

        for (file in files) {
            if (!SpritePackerUtils.isValidImage(file)) {
                Dialogue.showWarning(String.format("${file.name} is not a valid image.")).showAndWait()
                return
            }
        }

        BSPUtils.sortFiles(files)

        for (file in files) {
            val data = Files.readAllBytes(file.toPath())

            val id = Integer.parseInt(BSPUtils.getFilePrefix(file))

            elements.add(Sprite(id, data))
        }
    }

    @FXML
    fun exportImage() {

        if (elements.isEmpty()) {
            Dialogue.showInfo("There isn't anything to export silly!").showAndWait()
            return
        }

        val selectedItems = listView.selectionModel.selectedItems

        for (selectedItem in selectedItems) {
            if (selectedItem?.data?.isEmpty()!!) {
                Dialogue.showInfo("You can't export a placeholder silly!").showAndWait()
                return
            }
        }

        val chooser = DirectoryChooser()
        chooser.initialDirectory = userHome.toFile()
        val selectedDirectory = chooser.showDialog(App.mainStage) ?: return

        for (selectedItem in selectedItems) {
            ImageIO.write(selectedItem.toBufferdImage(), "png", File(selectedDirectory, "${selectedItem.id}.png"))
        }

    }

    @FXML
    fun exportImages() {

        if (elements.isEmpty()) {
            Dialogue.showInfo("There isn't anything to export silly!").showAndWait()
            return
        }

        val chooser = DirectoryChooser()
        chooser.initialDirectory = userHome.toFile()
        val selectedDirectory = chooser.showDialog(App.mainStage) ?: return

        val output = File(selectedDirectory, "bsp_output")

        if (!output.exists()) {
            output.mkdir()
        }

        for (sprite in filteredSprites) {
            ImageIO.write(sprite.toBufferdImage(), "png", File(output, "$sprite.png"))
        }

        Dialogue.openDirectory("Would you like to view the exported sprites?", output)
    }

    @FXML
    fun removeSprite() {
        val selectedItems = listView.selectionModel.selectedItems

        if (selectedItems.isEmpty()) {
            Dialogue.showInfo("You have not selected anything to remove!").showAndWait()
            return
        }

        for (selectedItem in selectedItems) {

            if (selectedItem.id == filteredSprites.size - 1) {
                elements.removeAt(selectedItem.id)

                val start = selectedItem.id - 1

                for (i in start downTo 0) {
                    // truncates placeholders
                    if (elements[i].data?.isEmpty()!!) {
                        elements.removeAt(i)
                    } else {
                        break
                    }
                }
            } else {
                selectedItem.data = ByteArray(0)
            }

            listView.refresh()

        }
    }

    @FXML
    fun clearProgram() {

    }

    @FXML
    fun handleMouseDragged(event: MouseEvent) {
        val stage = App.mainStage

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
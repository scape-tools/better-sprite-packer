package io.nshusa.controller

import io.nshusa.Sprite
import io.nshusa.util.SpritePackerUtils
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
import java.io.ByteArrayInputStream
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

        try {
            placeholderIcon = Image("icons/placeholder.png")
        } catch (ex: Exception) {
            println("Failed to load icons.")
        }

        filteredSprites = FilteredList(elements, { _ -> true })

        searchTf.textProperty().addListener({ _, _, newValue -> filteredSprites.setPredicate({ it -> Integer.toString(it.id) == newValue }) })

        listView.items = this.filteredSprites

        listView.setCellFactory({ _ ->
            object : ListCell<Sprite>() {
                private val imageView = ImageView()

                override fun updateItem(sprite: Sprite?, empty: Boolean) {
                    super.updateItem(sprite, empty)
                    if (empty) {
                        text = null
                        graphic = null
                    } else {
                        try {
                            if (sprite?.data == null) {
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

    }

    @FXML
    fun importImages() {
        val chooser = DirectoryChooser()
        chooser.initialDirectory = userHome.toFile()
        val selectedDirectory = chooser.showDialog(App.mainStage) ?: return

        val files = selectedDirectory.listFiles()

        SpritePackerUtils.sortFiles(files)

        for (file in files) {
            val data = Files.readAllBytes(file.toPath())

            val id = Integer.parseInt(SpritePackerUtils.getFilePrefix(file))

            elements.add(Sprite(id, data))
        }
    }

    @FXML
    fun exportImages() {

    }

    @FXML
    fun removeSprite() {

    }

    @FXML
    fun clearProgram() {

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
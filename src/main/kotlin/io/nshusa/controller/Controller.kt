package io.nshusa.controller

import io.nshusa.SpriteNode
import javafx.application.Platform
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import javafx.collections.transformation.FilteredList
import javafx.fxml.FXML
import javafx.fxml.Initializable
import javafx.scene.control.*
import javafx.scene.image.Image
import javafx.scene.image.ImageView
import javafx.scene.input.MouseEvent
import javafx.scene.paint.Color
import java.net.URL
import java.nio.file.Paths
import java.util.*

class Controller : Initializable {

    var offsetX = 0.0

    var offsetY = 0.0

    @FXML
    lateinit var listView: ListView<SpriteNode>

    @FXML
    lateinit var colorPicker: ColorPicker

    @FXML
    lateinit var imageView: ImageView

    @FXML
    lateinit var searchTf: TextField

    lateinit var placeholderIcon: Image

    val userHome = Paths.get(System.getProperty("user.home"))

    private val elements: ObservableList<SpriteNode> = FXCollections.observableArrayList()

    private var filteredSprites: FilteredList<SpriteNode> = FilteredList(elements)

    override fun initialize(location: URL?, resource: ResourceBundle?) {
        colorPicker.value = Color.MAGENTA

        try {
            placeholderIcon = Image("icons/placeholder.png")
        } catch (ex: Exception) {
            println("Failed to load icons.")
        }
        searchTf.textProperty().addListener({ _, _, newValue -> filteredSprites.setPredicate({ it -> Integer.toString(it.id).contains(newValue) }) })

        listView.items = this.filteredSprites

        listView.setCellFactory({ _ ->
            object : ListCell<SpriteNode>() {
                private val listIconView = ImageView()
                override fun updateItem(value: SpriteNode, empty: Boolean) {
                    super.updateItem(value, empty)
                    if (empty) {
                        graphic = null
                        text = ""
                    } else {

                    }
                }
            }
        })
    }

    @FXML
    fun importImages() {

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
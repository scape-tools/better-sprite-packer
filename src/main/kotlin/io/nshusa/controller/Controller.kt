package io.nshusa.controller

import io.nshusa.App
import io.nshusa.component.Sprite
import io.nshusa.bsp.extra.SpritePackerUtils
import io.nshusa.util.Dialogue
import io.nshusa.util.BSPUtils
import javafx.application.Platform
import javafx.beans.value.ChangeListener
import javafx.beans.value.ObservableValue
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import javafx.collections.transformation.FilteredList
import javafx.embed.swing.SwingFXUtils
import javafx.fxml.FXML
import javafx.fxml.Initializable
import javafx.scene.control.*
import javafx.scene.image.Image
import javafx.scene.image.ImageView
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyEvent
import javafx.scene.input.MouseEvent
import javafx.scene.paint.Color
import javafx.stage.DirectoryChooser
import javafx.stage.FileChooser
import org.apache.commons.imaging.Imaging
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

    @FXML
    lateinit var idTf: TextField

    @FXML
    lateinit var offsetXTf: TextField

    @FXML
    lateinit var offsetYTf: TextField

    @FXML
    lateinit var imageSizeTf: TextField

    @FXML
    lateinit var colorsTf: TextField

    @FXML
    lateinit var formatNameTf: TextField

    @FXML
    lateinit var fileSizeTf: TextField

    @FXML
    lateinit var colorTypeTf: TextField

    @FXML
    lateinit var bitsPerPixelTf: TextField

    @FXML
    lateinit var transparentTf: TextField

    @FXML
    lateinit var compATf: TextField

    lateinit var placeholderIcon: Image

    val userHome = Paths.get(System.getProperty("user.home"))

    private val elements: ObservableList<Sprite> = FXCollections.observableArrayList()

    private lateinit var filteredSprites: FilteredList<Sprite>

    private lateinit var newImage: Image

    override fun initialize(location: URL?, resource: ResourceBundle?) {
        colorPicker.value = Color.MAGENTA
        listView.selectionModel.selectionMode = SelectionMode.MULTIPLE

        try {
            placeholderIcon = Image("icons/placeholder.png")
        } catch (ex: Exception) {
            println("Failed to load icons.")
        }

        filteredSprites = FilteredList(elements, { _ -> true })

        searchTf.textProperty().addListener({ _, _, newValue ->
            filteredSprites.setPredicate({

                if (newValue.isEmpty()) {
                    true
                } else {
                    Integer.toString(it.id) == newValue
                }

            })
        })

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

        listView.selectionModel.selectedItemProperty().addListener({ _, _, newValue ->

            if (newValue != null && !newValue.data?.isEmpty()!!) {
                imageView.image = newValue.toImage()

                val info = Imaging.getImageInfo(newValue.data)

                idTf.text = newValue.id.toString()
                imageSizeTf.text = "${info.width} x ${info.height}"

                formatNameTf.text = info.format.name
                fileSizeTf.text = BSPUtils.readableFileSize(newValue.data?.size?.toLong()!!)
                colorTypeTf.text = info.colorType.toString()
                bitsPerPixelTf.text = info.bitsPerPixel.toString()
                transparentTf.text = info.isTransparent.toString()
                compATf.text = info.compressionAlgorithm

                val bimage = ImageIO.read(ByteArrayInputStream(newValue.data))

                val set = mutableSetOf<Int>()

                for (x in 0 until bimage.width) {
                    for (y in 0 until bimage.height) {
                        set.add(bimage.getRGB(x, y))
                    }
                }

                colorsTf.text = set.size.toString()

            } else {
                idTf.text = ""
                imageSizeTf.text = ""
                formatNameTf.text = ""
                fileSizeTf.text = ""
                colorTypeTf.text = ""
                bitsPerPixelTf.text = ""
                transparentTf.text = ""
                compATf.text = ""
                colorsTf.text = ""
            }

        })

        offsetXTf.textProperty().addListener({ _, _, newValue ->
            if (!newValue.matches("\\d*".toRegex()) || newValue.length > 3) {
                offsetXTf.text = offsetXTf.text.substring(0, offsetXTf.length - 1)
            }
            if (offsetXTf.text.length > 3) {
                offsetXTf.text = offsetXTf.text.substring(0, 3)
            }
        })

        offsetYTf.textProperty().addListener({ _, _, newValue ->
            if (!newValue.matches("\\d*".toRegex()) || newValue.length > 3) {
                offsetYTf.text = offsetYTf.text.substring(0, offsetYTf.length - 1)
            }
            if (offsetYTf.text.length > 3) {
                offsetYTf.text = offsetYTf.text.substring(0, 3)
            }
        })

    }

    @FXML
    fun importImage() {
        val chooser = FileChooser()
        chooser.initialDirectory = userHome.toFile()
        chooser.extensionFilters.add(FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.gif"))
        val selectedFiles = chooser.showOpenMultipleDialog(App.mainStage) ?: return

        val ids = arrayOfNulls<Int>(selectedFiles.size)
        val datas = arrayOfNulls<ByteArray>(selectedFiles.size)

        for (i in 0 until selectedFiles.size) {

            val selectedFile = selectedFiles[i]

            if (!SpritePackerUtils.isValidImage(selectedFile)) {
                Dialogue.showWarning(String.format("${selectedFile.name} is not a valid image.")).showAndWait()
                return
            }

            try {
                val id = Integer.parseInt(BSPUtils.getFilePrefix(selectedFile))

                val data = Files.readAllBytes(selectedFile.toPath())

                for (sprite in elements) {
                    if (Arrays.equals(sprite.data, datas[i])) {
                        Dialogue.showWarning(String.format("Detected a duplicate image at index=${sprite.id} and $id")).showAndWait()
                        return
                    }
                }

                ids[i] = id
                datas[i] = data
            } catch (ex: Exception) {
                Dialogue.showWarning("Images should be named like 0.png, 1.png, 2.png could not read id for ${selectedFile.name}.").showAndWait()
                return
            }

        }

        for (i in 0 until selectedFiles.size) {
            val id = ids[i] ?: continue

            val data = datas[i] ?: continue

            val info = Imaging.getImageInfo(data)

            if (id < elements.size) {
                elements[id].data = data
            } else {

                for (j in elements.size until id) {
                    elements.add(Sprite(j, ByteArray(0), info.formatName.toLowerCase()))
                }

                elements.add(Sprite(id, data, info.formatName.toLowerCase()))
            }

            listView.refresh()

        }

    }

    @FXML
    fun importImages() {

        val chooser = DirectoryChooser()
        chooser.initialDirectory = userHome.toFile()
        val selectedDirectory = chooser.showDialog(App.mainStage) ?: return

        val files = selectedDirectory.listFiles()

        BSPUtils.sortFiles(files)

        val ids = arrayOfNulls<Int>(files.size)
        val datas = arrayOfNulls<ByteArray>(files.size)

        for (i in 0 until files.size) {
            val selectedFile = files[i]

            if (!SpritePackerUtils.isValidImage(selectedFile)) {
                Dialogue.showWarning(String.format("${selectedFile.name} is not a valid image.")).showAndWait()
                return
            }

            try {
                val id = Integer.parseInt(BSPUtils.getFilePrefix(selectedFile))

                val data = Files.readAllBytes(selectedFile.toPath())

                for (sprite in elements) {
                    if (Arrays.equals(sprite.data, datas[i])) {
                        Dialogue.showWarning(String.format("Detected a duplicate image at index=${sprite.id} and $id")).showAndWait()
                        return
                    }
                }

                ids[i] = id
                datas[i] = data
            } catch (ex: Exception) {
                Dialogue.showWarning("Images should be named like 0.png, 1.png, 2.png could not read id for ${selectedFile.name}.").showAndWait()
                return
            }

        }

        clearProgram()

        for (i in 0 until files.size) {
            val id = ids[i] ?: continue
            val data = datas[i] ?: continue

            val info = Imaging.getImageInfo(data)

            if (id < files.size) {
                elements.add(Sprite(id, data, info.formatName))
            } else {
                for (j in elements.size until id) {
                    elements.add(Sprite(j, ByteArray(0), info.formatName))
                }

                elements.add(Sprite(id, data, info.formatName))
            }
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
            ImageIO.write(selectedItem.toBufferdImage(), selectedItem.format, File(selectedDirectory, "${selectedItem.id}.${selectedItem.format}"))
        }

        Dialogue.openDirectory("Would you like to view these images?", selectedDirectory)

    }

    @FXML
    fun replaceImage() {
        val selectedItem = listView.selectionModel.selectedItem ?: return

        val chooser = FileChooser()
        chooser.initialDirectory = userHome.toFile()
        chooser.extensionFilters.add(FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.gif"))
        val selectedFile = chooser.showOpenDialog(App.mainStage) ?: return

        if (!SpritePackerUtils.isValidImage(selectedFile)) {
            Dialogue.showWarning(String.format("${selectedFile.name} is not a valid image.")).showAndWait()
            return
        }

        val fileData = Files.readAllBytes(selectedFile.toPath())

        for (sprite in elements) {
            if (Arrays.equals(sprite.data, fileData)) {
                Dialogue.showWarning(String.format("Detected a duplicate image at index=${sprite.id} and ${selectedItem.id}")).showAndWait()
                return
            }
        }

        selectedItem.data = fileData
        listView.refresh()
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
            ImageIO.write(sprite.toBufferdImage(), sprite.format, File(output, "$sprite.${sprite.format}"))
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
        imageView.image = null
        elements.clear()
        filteredSprites.clear()

        idTf.text = ""
        imageSizeTf.text = ""

        formatNameTf.text = ""
        fileSizeTf.text = ""
        colorTypeTf.text = ""
        bitsPerPixelTf.text = ""
        transparentTf.text = ""
        compATf.text = ""
    }

    @FXML
    fun handleKeyEventPressed(event: KeyEvent) {

        val selectedItem = listView.selectionModel.selectedItem ?: return

        var flag = false

        if (event.code == KeyCode.ENTER) {
            if (!offsetXTf.text.isEmpty()) {
                try {
                    selectedItem.drawOffsetX = Integer.parseInt(offsetXTf.text)
                    flag = true
                } catch (ex: Exception) {

                }
            }

            if (!offsetYTf.text.isEmpty()) {
                try {
                    selectedItem.drawOffsetY = Integer.parseInt(offsetYTf.text)
                    flag = true
                } catch (ex: Exception) {

                }
            }

            if (flag) {
                Dialogue.showInfo("Updated!").showAndWait()
            }

        }
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
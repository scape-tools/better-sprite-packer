package io.nshusa.controller

import io.nshusa.App
import io.nshusa.component.Sprite
import io.nshusa.util.Dialogue
import io.nshusa.util.BSPUtils
import javafx.application.Platform
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import javafx.collections.transformation.FilteredList
import javafx.concurrent.Task
import javafx.embed.swing.SwingFXUtils
import javafx.fxml.FXML
import javafx.fxml.Initializable
import javafx.scene.control.*
import javafx.scene.image.Image
import javafx.scene.image.ImageView
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyEvent
import javafx.scene.input.MouseEvent
import javafx.scene.text.Text
import javafx.stage.DirectoryChooser
import javafx.stage.FileChooser
import org.apache.commons.imaging.Imaging
import java.io.*
import java.net.URL
import java.nio.ByteBuffer
import java.nio.channels.FileChannel
import java.nio.file.Paths
import java.util.*
import java.nio.file.Files
import java.nio.file.StandardOpenOption
import javax.imageio.ImageIO
import kotlin.experimental.and

class Controller : Initializable {

    var offsetX = 0.0

    var offsetY = 0.0

    @FXML
    lateinit var listView: ListView<Sprite>

    @FXML
    lateinit var imageView: ImageView

    @FXML
    lateinit var searchTf: TextField

    @FXML
    lateinit var offsetXTf: TextField

    @FXML
    lateinit var offsetYTf: TextField

    @FXML
    lateinit var idL: Label

    @FXML
    lateinit var imageSizeL: Label

    @FXML
    lateinit var colorsL: Label

    @FXML
    lateinit var fileSizeL: Label

    @FXML
    lateinit var formatNameL: Label

    @FXML
    lateinit var transL: Label

    @FXML
    lateinit var compAL: Label

    @FXML
    lateinit var bppL: Label

    lateinit var placeholderIcon: Image

    val userHome = Paths.get(System.getProperty("user.home"))

    private val observableList: ObservableList<Sprite> = FXCollections.observableArrayList()

    private lateinit var filteredList: FilteredList<Sprite>

    private lateinit var displayedImage: Image

    override fun initialize(location: URL?, resource: ResourceBundle?) {
        listView.selectionModel.selectionMode = SelectionMode.MULTIPLE

        try {
            placeholderIcon = Image("icons/placeholder.png")
        } catch (ex: Exception) {
            println("Failed to load icons.")
        }

        filteredList = FilteredList(observableList, { _ -> true })

        searchTf.textProperty().addListener({ _, _, newValue ->
            filteredList.setPredicate({

                if (newValue.isEmpty()) {
                    true
                } else {
                    Integer.toString(it.id) == newValue
                }

            })
        })

        listView.items = this.filteredList

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
                                text = sprite.id.toString()
                                graphic = imageView
                                return
                            }

                            val image = ImageIO.read(ByteArrayInputStream(sprite.data))
                            imageView.fitWidth = (if (image.width > 128) 128.0 else image.width.toDouble())
                            imageView.fitHeight = (if (image.height > 128) 128.0 else image.height.toDouble())
                            imageView.isPreserveRatio = true
                            displayedImage = SwingFXUtils.toFXImage(image, null)
                            imageView.image = displayedImage
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

                idL.text = newValue.id.toString()
                offsetXTf.text = newValue.drawOffsetX.toString()
                offsetYTf.text = newValue.drawOffsetY.toString()
                imageSizeL.text = "${info.width} x ${info.height}"
                formatNameL.text = info.format.name
                fileSizeL.text = BSPUtils.readableFileSize(newValue.data?.size?.toLong()!!)
                 bppL.text = info.bitsPerPixel.toString()
                transL.text = (if (info.isTransparent) "Yes" else "No")
                compAL.text = info.compressionAlgorithm

                val bimage = ImageIO.read(ByteArrayInputStream(newValue.data))

                val set = mutableSetOf<Int>()

                for (x in 0 until bimage.width) {
                    for (y in 0 until bimage.height) {
                        set.add(bimage.getRGB(x, y))
                    }
                }

                colorsL.text = set.size.toString()

            } else {
                resetLabels()
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

        val task: Task<Boolean> = object : Task<Boolean>() {

            override fun call(): Boolean {
                val ids = arrayOfNulls<Int>(selectedFiles.size)
                val datas = arrayOfNulls<ByteArray>(selectedFiles.size)

                for (i in 0 until selectedFiles.size) {

                    val selectedFile = selectedFiles[i]

                    if (!BSPUtils.isValidImage(selectedFile)) {
                        Platform.runLater({ Dialogue.showWarning(String.format("${selectedFile.name} is not a valid image.")).showAndWait() })
                        return false
                    }

                    try {
                        val id = Integer.parseInt(BSPUtils.getFilePrefix(selectedFile))

                        val data = Files.readAllBytes(selectedFile.toPath())

                        for (sprite in observableList) {
                            if (Arrays.equals(sprite.data, datas[i])) {
                                Platform.runLater({ Dialogue.showWarning(String.format("Detected a duplicate image at index=${sprite.id} and $id")).showAndWait() })
                                return false
                            }
                        }

                        ids[i] = id
                        datas[i] = data
                    } catch (ex: Exception) {
                        Platform.runLater({ Dialogue.showWarning("Images should be named like 0.png, 1.png, 2.png could not read id for ${selectedFile.name}.").showAndWait() })
                        return false
                    }

                }

                for (i in 0 until selectedFiles.size) {
                    val id = ids[i] ?: continue

                    val data = datas[i] ?: continue

                    val info = Imaging.getImageInfo(data)

                    if (id < observableList.size) {
                        Platform.runLater({ observableList[id].data = data })
                    } else {

                        for (j in observableList.size until id) {
                            Platform.runLater({ observableList.add(Sprite(j, ByteArray(0), info.format.name.toLowerCase())) })
                        }
                        Platform.runLater({ observableList.add(Sprite(id, data, info.format.name.toLowerCase())) })
                    }

                }
                return true
            }
        }

        Thread(task).start()

    }

    @FXML
    fun importImages() {

        val chooser = DirectoryChooser()
        chooser.initialDirectory = userHome.toFile()
        val selectedDirectory = chooser.showDialog(App.mainStage) ?: return

        val files = selectedDirectory.listFiles()

        for (file in files) {

            if (file.isDirectory) {
                Dialogue.showWarning("${file.name} cannot be a directory.").showAndWait()
                return
            }

            if (!BSPUtils.isValidImage(file)) {
                Dialogue.showWarning("${file.name} is not a valid image.").showAndWait()
                return
            }

        }

        BSPUtils.sortFiles(files)

        val task: Task<Boolean> = object : Task<Boolean>() {

            override fun call(): Boolean {
                val ids = arrayOfNulls<Int>(files.size)
                val datas = arrayOfNulls<ByteArray>(files.size)

                for (i in 0 until files.size) {
                    val selectedFile = files[i]

                    if (!BSPUtils.isValidImage(selectedFile)) {
                        Platform.runLater({ Dialogue.showWarning(String.format("${selectedFile.name} is not a valid image.")).showAndWait() })
                        return false
                    }

                    try {
                        val id = Integer.parseInt(BSPUtils.getFilePrefix(selectedFile))

                        val data = Files.readAllBytes(selectedFile.toPath())

                        for (sprite in observableList) {
                            if (Arrays.equals(sprite.data, datas[i])) {
                                Platform.runLater({ Dialogue.showWarning(String.format("Detected a duplicate image at index=${sprite.id} and $id")).showAndWait() })
                                return false
                            }
                        }

                        ids[i] = id
                        datas[i] = data
                    } catch (ex: Exception) {
                        Platform.runLater({ Dialogue.showWarning("Images should be named like 0.png, 1.png, 2.png could not read id for ${selectedFile.name}.").showAndWait() })
                        return false
                    }

                }

                Platform.runLater({ clearProgram() })

                for (i in 0 until files.size) {
                    val id = ids[i] ?: continue
                    val data = datas[i] ?: continue

                    val info = Imaging.getImageInfo(data)

                    if (id < files.size) {
                        Platform.runLater({ observableList.add(Sprite(id, data, info.format.name)) })
                    } else {
                        for (j in observableList.size until id) {
                            Platform.runLater({ observableList.add(Sprite(j, ByteArray(0), info.format.name)) })
                        }
                        Platform.runLater({ observableList.add(Sprite(id, data, info.format.name)) })
                    }
                }
                return false
            }
        }

        Thread(task).start()

    }

    @FXML
    fun exportImage() {

        if (observableList.isEmpty()) {
            Dialogue.showWarning("There isn't anything to export silly!").showAndWait()
            return
        }

        val selectedItems = listView.selectionModel.selectedItems

        for (selectedItem in selectedItems) {
            if (selectedItem?.data?.isEmpty()!!) {
                Dialogue.showWarning("You can't export a placeholder silly!").showAndWait()
                return
            }
        }

        val chooser = DirectoryChooser()
        chooser.initialDirectory = userHome.toFile()
        val selectedDirectory = chooser.showDialog(App.mainStage) ?: return

        val task: Task<Boolean> = object : Task<Boolean>() {

            override fun call(): Boolean {
                for (selectedItem in selectedItems) {

                    println("${selectedItem.id}.${selectedItem.format}")
                    ImageIO.write(selectedItem.toBufferdImage(), selectedItem.format, File(selectedDirectory, "${selectedItem.id}.${selectedItem.format}"))
                }

                Platform.runLater({ Dialogue.openDirectory("Would you like to view these images?", selectedDirectory) })
                return true
            }
        }

        Thread(task).start()
    }

    @FXML
    fun replaceImage() {
        val selectedItem = listView.selectionModel.selectedItem ?: return

        val chooser = FileChooser()
        chooser.initialDirectory = userHome.toFile()
        chooser.extensionFilters.add(FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.gif"))
        val selectedFile = chooser.showOpenDialog(App.mainStage) ?: return

        if (!BSPUtils.isValidImage(selectedFile)) {
            Dialogue.showWarning(String.format("${selectedFile.name} is not a valid image.")).showAndWait()
            return
        }

        val task: Task<Boolean> = object : Task<Boolean>() {

            override fun call(): Boolean {
                val fileData = Files.readAllBytes(selectedFile.toPath())

                for (sprite in observableList) {
                    if (Arrays.equals(sprite.data, fileData)) {
                        Platform.runLater({ Dialogue.showWarning(String.format("Detected a duplicate image at index=${sprite.id} and ${selectedItem.id}")).showAndWait() })
                        return false
                    }
                }

                selectedItem.data = fileData
                Platform.runLater({ listView.refresh() })
                return true
            }
        }

        Thread(task).start()
    }

    @FXML
    fun exportImages() {

        if (observableList.isEmpty()) {
            Dialogue.showInfo("There isn't anything to export silly!").showAndWait()
            return
        }

        val chooser = DirectoryChooser()
        chooser.initialDirectory = userHome.toFile()
        val selectedDirectory = chooser.showDialog(App.mainStage) ?: return

        val task: Task<Boolean> = object : Task<Boolean>() {

            override fun call(): Boolean {
                for (sprite in filteredList) {
                    ImageIO.write(sprite.toBufferdImage(), sprite.format, File(selectedDirectory, "$sprite.${sprite.format}"))
                }

                Platform.runLater({ Dialogue.openDirectory("Would you like to view the exported sprites?", selectedDirectory) })
                return true
            }
        }

        Thread(task).start()
    }

    @FXML
    fun removeImage() {
        val selectedItems = listView.selectionModel.selectedItems

        if (selectedItems.isEmpty()) {
            Dialogue.showInfo("You have not selected anything to remove!").showAndWait()
            return
        }

        val task: Task<Boolean> = object : Task<Boolean>() {

            override fun call(): Boolean {
                for (selectedItem in selectedItems) {

                    if (selectedItem.id == filteredList.size - 1) {
                        Platform.runLater({ observableList.removeAt(selectedItem.id) })

                        val start = selectedItem.id - 1

                        for (i in start downTo 0) {
                            // truncates placeholders
                            if (observableList[i].data?.isEmpty()!!) {
                                Platform.runLater({ observableList.removeAt(i) })
                            } else {
                                break
                            }
                        }
                    } else {
                        selectedItem.data = ByteArray(0)
                    }

                    Platform.runLater({ listView.refresh() })

                }
                return true
            }
        }

        Thread(task).start()

    }

    @FXML
    fun importBinary() {

        val chooser = FileChooser()
        chooser.initialDirectory = userHome.toFile()
        chooser.extensionFilters.add(FileChooser.ExtensionFilter("main_file_sprites.dat", "*.dat"))
        val selectedFile = chooser.showOpenDialog(App.mainStage) ?: return

        if (selectedFile.length() < 3) {
            return
        }

        val prefix = BSPUtils.getFilePrefix(selectedFile)

        val metaFile = File(selectedFile.parent, "$prefix.idx")

        if (!metaFile.exists()) {
            Dialogue.showWarning("Could not locate corresponding idx file=${metaFile.name}").showAndWait()
            return
        }

        val task: Task<Boolean> = object : Task<Boolean>() {

            override fun call(): Boolean {
                FileChannel.open(selectedFile.toPath(), StandardOpenOption.READ).use { dat ->
                    val signature = ByteBuffer.allocate(3)

                    dat.read(signature)

                    if (signature[0].toChar() != 'b' && signature[1].toChar() != 's' && signature[2].toChar() != 'p') {
                        Platform.runLater({ Dialogue.showWarning("Detected invalid file format.").showAndWait() })
                        return false
                    }

                }

                val dataBuf = ByteBuffer.wrap(Files.readAllBytes(selectedFile.toPath()))
                dataBuf.position(3)

                val metaBuf = ByteBuffer.wrap(Files.readAllBytes(metaFile.toPath()))

                val entries = metaBuf.capacity() / 10

                for (i in 0 until entries) {
                    try {
                        val dataOffset = ((metaBuf.get().toInt() and 0xFF) shl 16) + ((metaBuf.get().toInt() and 0xFF) shl 8) + (metaBuf.get().toInt() and 0xFF)
                        val length = ((metaBuf.get().toInt() and 0xFF) shl 16) + ((metaBuf.get().toInt() and 0xFF) shl 8) + (metaBuf.get().toInt() and 0xFF)
                        val offsetX = (metaBuf.short and 0xFF).toInt()
                        val offsetY = (metaBuf.short and 0xFF).toInt()

                        dataBuf.position(dataOffset)

                        val imageData = ByteArray(length)

                        if (length == 0) {
                            Platform.runLater({ observableList.add(Sprite(i, imageData, "png")) })
                        } else {

                            dataBuf.get(imageData)

                            val info = Imaging.getImageInfo(imageData)

                            val sprite = Sprite(i, imageData, info.format.name)
                            sprite.drawOffsetX = offsetX
                            sprite.drawOffsetY = offsetY

                            Platform.runLater({ observableList.add(sprite) })
                        }

                    } catch (ex: Exception) {
                        ex.printStackTrace()
                        Platform.runLater({ Dialogue.showWarning("Detected corrupt file or invalid format.").showAndWait() })
                        return false
                    }

                }
                return true
            }
        }

        Thread(task).start()

    }

    private fun resetLabels() {
        idL.text = "-1"
        imageSizeL.text = "0 x 0"
        formatNameL.text = "None"
        fileSizeL.text = "0 b"
        bppL.text = "0"
        transL.text = "No"
        compAL.text = "None"
        colorsL.text = "0"
    }

    @FXML
    fun exportBinary() {
        if (observableList.isEmpty()) {
            Dialogue.showWarning("You can't export when you have nothing to export silly!").showAndWait()
            return
        }

        val chooser = DirectoryChooser()
        chooser.initialDirectory = userHome.toFile()
        val selectedDirectory = chooser.showDialog(App.mainStage) ?: return

        val task: Task<Boolean> = object : Task<Boolean>() {

            override fun call(): Boolean {
                var dataLength = 3

                observableList.forEach { dataLength += it.getLength() }

                val dataBuf = ByteBuffer.allocate(dataLength)
                val metaBuf = ByteBuffer.allocate(observableList.size * 10)

                val signature = ByteArray(3)
                signature[0] - 'b'.toByte()
                signature[1] = 's'.toByte()
                signature[2] = 'p'.toByte()

                dataBuf.put(signature)

                for (sprite in observableList) {
                    val dataOffset = dataBuf.position()

                    val length = sprite.getLength()

                    // data offset
                    metaBuf.put((dataOffset shr 16).toByte())
                    metaBuf.put((dataOffset shr 8).toByte())
                    metaBuf.put(dataOffset.toByte())

                    // length
                    metaBuf.put((length shr 16).toByte())
                    metaBuf.put((length shr 8).toByte())
                    metaBuf.put(length.toByte())

                    // offset x
                    metaBuf.putShort(sprite.drawOffsetX.toShort())

                    // offset y
                    metaBuf.putShort(sprite.drawOffsetY.toShort())

                    // image data
                    dataBuf.put(sprite.data)
                }

                FileOutputStream(File(selectedDirectory, "main_file_sprites.dat")).use {
                    it.write(dataBuf.array())
                }

                FileOutputStream(File(selectedDirectory, "main_file_sprites.idx")).use {
                    it.write(metaBuf.array())
                }

                Platform.runLater({ Dialogue.openDirectory("Would you like to view these files?", selectedDirectory) })
                return true
            }
        }

        Thread(task).start()

    }

    @FXML
    fun clearProgram() {
        imageView.image = null
        observableList.clear()
        filteredList.clear()
        resetLabels()
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
import javafx.application.Application
import javafx.fxml.FXMLLoader
import javafx.scene.Parent
import javafx.scene.Scene
import javafx.scene.image.Image
import javafx.stage.Stage
import javafx.stage.StageStyle

class App : Application() {

    override fun start(stage: Stage?) {
        App.mainStage = stage!!
        val root : Parent? = FXMLLoader.load(App.javaClass.getResource("/Main.fxml"))
        stage?.title = "Better Sprite Packer"
        val scene = Scene(root)
        scene.stylesheets.add(App.javaClass.getResource("/style.css").toExternalForm())
        stage?.scene = scene
        stage?.icons?.add(Image(javaClass.getResourceAsStream("/icons/icon.png")))
        stage?.centerOnScreen()
        stage?.sizeToScene()
        stage?.initStyle(StageStyle.UNDECORATED)
        stage?.show()
    }

    companion object {

        lateinit var mainStage : Stage

        @JvmStatic
        fun main(args : Array<String>) {
            launch(App::class.java)
        }
    }

}
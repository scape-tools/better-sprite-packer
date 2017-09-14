import javafx.application.Application
import javafx.fxml.FXMLLoader
import javafx.scene.Parent
import javafx.scene.Scene
import javafx.stage.Stage

class App : Application() {

    override fun start(stage: Stage?) {
        val root : Parent? = FXMLLoader.load(App.javaClass.getResource("/Main.fxml"))

        stage?.title = "Hello world!"
        stage?.scene = Scene(root)
        stage?.show()
    }

    companion object {
        @JvmStatic
        fun main(args : Array<String>) {
            launch(App::class.java)
        }
    }

}
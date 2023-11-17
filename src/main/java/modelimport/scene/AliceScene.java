package modelimport.scene;

import javafx.scene.Scene;
import javafx.stage.Stage;

import modelimport.HelloFX;

public class AliceScene {
    protected AliceScene self; // Allows inline functions to access "this"

    protected Stage stage;
    protected Scene scene;
    protected HelloFX app;

    public AliceScene(Stage stage, Scene scene, HelloFX app) {
        self = this;

        this.stage = stage;
        this.scene = scene;
        this.app = app;
    }

    public Scene getScene() {
        return scene;
    }

    public void stopTask() {
    }
}

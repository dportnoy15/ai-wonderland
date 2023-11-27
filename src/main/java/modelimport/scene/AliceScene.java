package modelimport.scene;

import javafx.scene.Scene;
import javafx.stage.Stage;

import modelimport.HelloFX;

public class AliceScene {
    protected Stage stage;
    protected Scene scene;
    protected HelloFX app;

    public AliceScene(Stage stage, Scene scene, HelloFX app) {
        this.stage = stage;
        this.scene = scene;
        this.app = app;
    }

    public Scene getScene() {
        return scene;
    }

    public void clearFields() {
    }

    public void stopTask() {
    }
}

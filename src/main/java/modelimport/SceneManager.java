package modelimport;

import java.util.ArrayList;

import javafx.stage.Stage;

import modelimport.scene.AliceScene;

public class SceneManager {
    private static Stage stage;
    private static SceneManager instance = null;

    private ArrayList<AliceScene> scenes = null;

    public static void setStage(Stage stage) {
        SceneManager.stage = stage;
    }

    public static SceneManager getInstance() {
        if (instance == null) {
            instance = new SceneManager();
        }
        
        return instance;
    }

    public void addScene(AliceScene scene) {
        if (scenes == null) {
            scenes = new ArrayList<AliceScene>();
        }

        scenes.add(scene);
    }

    public void setScene(int sceneIdx) {
        stage.setScene(scenes.get(sceneIdx).getScene());
    }
}

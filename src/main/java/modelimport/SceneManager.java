package modelimport;

import java.util.ArrayList;

import javafx.scene.Scene;
import javafx.stage.Stage;

public class SceneManager {
    private static Stage stage;
    private static SceneManager instance = null;

    private ArrayList<Scene> scenes = null;

    public static void setStage(Stage stage) {
        SceneManager.stage = stage;
    }

    public static SceneManager getInstance() {
        if (instance == null) {
            instance = new SceneManager();
        }
        
        return instance;
    }

    public void addScene(Scene scene) {
        if (scenes == null) {
            scenes = new ArrayList<Scene>();
        }

        scenes.add(scene);
    }

    public void setScene(int sceneIdx) {
        stage.setScene(scenes.get(sceneIdx));
    }
}

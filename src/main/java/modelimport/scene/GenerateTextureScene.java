package modelimport.scene;

import javafx.event.ActionEvent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import modelimport.HelloFX;
import modelimport.SceneManager;

public class GenerateTextureScene extends AliceScene {
    
    private static final int ART_STYLE_COUNT = 8;

    public GenerateTextureScene(Stage stage, Scene scene, HelloFX app) {
        super(stage, scene, app);
    }

    public void initLayout() {
    }

    public void registerButtonActions(Button[] artStyleButtons, Button btnNext) {
        for (int i = 0; i < ART_STYLE_COUNT; i++) {
            int finalI = i;
            artStyleButtons[i].setOnAction((ActionEvent actionEvent) -> {
                app.setArtStyle(finalI);
            });
        }

        btnNext.setOnAction((ActionEvent event) -> {
            SceneManager.getInstance().setActiveScene(2);
        });
    }
}

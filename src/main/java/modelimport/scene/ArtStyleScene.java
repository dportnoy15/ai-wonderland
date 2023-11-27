package modelimport.scene;

import java.util.ArrayList;
import java.util.List;

import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

import modelimport.HelloFX;
import modelimport.SceneManager;

public class ArtStyleScene extends AliceScene {
    
    private static final int ART_STYLE_COUNT = 8;

    Button[] artStyleButtons;
    private int curStyleSelection;

    public ArtStyleScene(Stage stage, Scene scene, HelloFX app) {
        super(stage, scene, app);
    }

    public void initLayout() {
        BorderPane layout = (BorderPane) getScene().getRoot();

        HBox topPane = new HBox();
        HBox bottomPane = new HBox();
        Pane leftPane = new FlowPane();
        Pane rightPane = new FlowPane();
        GridPane centerPane = new GridPane();

        layout.setTop(topPane);    // Title
        layout.setBottom(bottomPane); // Nav Buttons
        layout.setLeft(leftPane);
        layout.setRight(rightPane);
        layout.setCenter(centerPane); // Main Content

        topPane.setPrefHeight(100);
        bottomPane.setPrefHeight(100);
        leftPane.setPrefWidth(0);
        rightPane.setPrefWidth(0);

        Font uiFont = Font.font("Tahoma", FontWeight.NORMAL, 20);

        Label scenetitle = new Label("Select your art style");
        scenetitle.setFont(uiFont);
        topPane.setAlignment(Pos.CENTER);
        topPane.getChildren().add(scenetitle);

        /* Start footer definition */

        Button btnPrev = new Button("Back");
        Region region = new Region();
        Button btnNext = new Button("Next");

        HBox.setHgrow(region, Priority.ALWAYS);
        bottomPane.getChildren().addAll(btnPrev, region, btnNext);
        bottomPane.setAlignment(Pos.CENTER);
        bottomPane.setPadding(new Insets(0, 50, 0, 50));

        /* Start screen customization */

        GridPane grid = centerPane;
        grid.setAlignment(Pos.CENTER);
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(25, 25, 25, 25));

        artStyleButtons = new Button[ART_STYLE_COUNT];
        artStyleButtons[0] = new Button("Realistic");
        artStyleButtons[1] = new Button("Voxel");
        artStyleButtons[2] = new Button("2.5D Cartoon");
        artStyleButtons[3] = new Button("Japanese Anime");
        artStyleButtons[4] = new Button("Cartoon Line Art");
        artStyleButtons[5] = new Button("Realistic Hand-drawn");
        artStyleButtons[6] = new Button("2.5D Hand-drawn");
        artStyleButtons[7] = new Button("Oriental Comic Ink");

        for (int i = 0; i < ART_STYLE_COUNT; i++) {
            artStyleButtons[i].setMinSize(200, 50);
            grid.add(artStyleButtons[i], i % 2 * 5, 5 + i/2 * 3, 2, 2);
        }

        registerButtonActions(artStyleButtons, btnPrev, btnNext);

        curStyleSelection = 0;
        artStyleButtons[curStyleSelection].fire();
    }

    public void registerButtonActions(Button[] artStyleButtons, Button btnPrev, Button btnNext) {
        for (int i = 0; i < ART_STYLE_COUNT; i++) {
            int newStyleIndex = i;
            artStyleButtons[i].setOnAction((ActionEvent actionEvent) -> {
                System.out.println("Selecting art style " + newStyleIndex + " (" + getArtStyle() + "), was " + curStyleSelection);

                curStyleSelection = newStyleIndex;
            });
        }

        btnPrev.setOnAction((ActionEvent event) -> {
            SceneManager.getInstance().setActiveScene(1);
        });

        btnNext.setOnAction((ActionEvent event) -> {
            SceneManager.getInstance().setActiveScene(3);
        });
    }

    public void selectCurrentStyleButton() {
        artStyleButtons[curStyleSelection].requestFocus();
    }

    public String getArtStyle() {
        ArrayList<String> artStyleValues = new ArrayList<>(List.of(
            "realistic",
            "voxel",
            "fake-3d-cartoon",
            "japanese-anime",
            "cartoon-line-art",
            "realistic-hand-drawn",
            "fake-3d-hand-drawn",
            "oriental-comic-ink"
        ));

        return artStyleValues.get(curStyleSelection);
    }
}

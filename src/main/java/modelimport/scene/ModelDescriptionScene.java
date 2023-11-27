package modelimport.scene;

import javafx.concurrent.Task;
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
import modelimport.PromptIO;
import modelimport.SceneManager;

public class ModelDescriptionScene extends AliceScene {

    protected String message;

    protected Thread bgThread;
    protected Task<Void> pythonTask;

    private PromptIO promptReader;

    private TextField objectPromptInput;
    
    public ModelDescriptionScene(Stage stage, Scene scene, HelloFX app) {
        super(stage, scene, app);

        message = "";
        bgThread = null;
        pythonTask = null;

        promptReader = new PromptIO();
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

        Label scenetitle = new Label("Model Description");
        scenetitle.setFont(uiFont);
        topPane.setAlignment(Pos.CENTER);
        topPane.getChildren().add(scenetitle);

        /* Start footer definition */

        Button btnPrev = new Button("Back");
        Region region = new Region();
        Button btnNext = new Button("Next");

        btnPrev.setVisible(false);

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

        Label objectPromptDescription = new Label("Model Description:");
        grid.add(objectPromptDescription, 0, 1);

        objectPromptInput = new TextField();
        grid.add(objectPromptInput, 1, 1);

        Button btnRandomize = new Button("Create a random prompt");
        grid.add(btnRandomize, 2, 1);

        registerButtonActions(btnRandomize, btnNext);
    }

    public void registerButtonActions(Button btnRandomize, Button btnNext) {
        btnRandomize.setOnAction((ActionEvent actionEvent) -> {
            int i = (int) (Math.random() * promptReader.objectDataList.size());

            String objectDescription = promptReader.objectDataList.get(i).getObjectDescription();
            objectPromptInput.setText(objectDescription);
            app.setObjectDescription(objectDescription);
        });

        btnNext.setOnAction((ActionEvent event) -> {
            String objectPrompt = objectPromptInput.getText();
            app.setObjectDescription(objectPrompt);

            SceneManager.getInstance().setActiveScene(2);

            ((ArtStyleScene) SceneManager.getInstance().getScene(2)).selectCurrentStyleButton();
        });
    }

    public void clearFields() {
        objectPromptInput.clear();
    }

    public void stopTask() {
        if (pythonTask != null) {
            bgThread.interrupt();

            while (bgThread.isAlive()) {
                try {
                    Thread.sleep(50);
                } catch(InterruptedException e) {
                    System.out.println("NIGHTMARE");
                }
            }
        }
    }
}

package modelimport.scene;

import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

import modelimport.HelloFX;
import modelimport.PromptIO;
import modelimport.SceneManager;

public class ModelDescriptionScene extends AliceScene {
    private static final int WORD_LIMIT = 3;

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

        Button btnPrev = new Button("Cancel");
        Region region = new Region();
        Button btnNext = new Button("Next");

        HBox.setHgrow(region, Priority.ALWAYS);
        bottomPane.getChildren().addAll(btnPrev, region, btnNext);
        bottomPane.setAlignment(Pos.CENTER);
        bottomPane.setPadding(new Insets(0, 50, 0, 50));

        /* Start screen customization */

        GridPane grid = centerPane;
        grid.setAlignment(Pos.TOP_LEFT);
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(10, 100, 100, 100));

        Label objectPromptDescription = new Label("Item Description:");
        objectPromptDescription.setFont(Font.font(15));
        grid.add(objectPromptDescription, 0, 1);

        Label wordCountLabel = new Label();
        wordCountLabel.setFont(Font.font(15));
        wordCountLabel.setBackground(new Background(new javafx.scene.layout.BackgroundFill(
                                            Color.BLUEVIOLET, // Border color
                                            new CornerRadii(3),
                                    null))); // CornerRadii);
        grid.add(wordCountLabel, 1, 3);
        wordCountLabel.setPrefWidth(120);
        wordCountLabel.setText("Word limit:3/3");
        wordCountLabel.setStyle("-fx-text-fill: #FFFFFF;");

        objectPromptInput = new TextField();
        objectPromptInput.setPrefWidth(500);
        objectPromptInput.setPrefHeight(300);
        objectPromptInput.setAlignment(Pos.TOP_LEFT);
        objectPromptInput.setPromptText("eg. A treasure box");

        objectPromptInput.setBorder(new Border(new javafx.scene.layout.BorderStroke(
                Color.MEDIUMPURPLE, // Border color
                BorderStrokeStyle.SOLID, // Border style
                new CornerRadii(3), // CornerRadii
                new BorderWidths(2)))); // Border width);

        objectPromptInput.textProperty().addListener((observable, oldValue, newValue) -> {
            String[] words = newValue.trim().split("\\s+");
            // Limit the input to WORD_LIMIT words
            if (words.length > WORD_LIMIT) {
                objectPromptInput.setText(oldValue);
            } else
            // Update the word count label
                wordCountLabel.setText("Word limit:" + (WORD_LIMIT - words.length) + "/" + WORD_LIMIT);
        });
        grid.add(objectPromptInput, 0, 2);

        Button btnRandomize = new Button("Create a random prompt");
        //grid.add(btnRandomize, 2, 1);

        registerButtonActions(btnRandomize, btnPrev, btnNext);
    }

    public void registerButtonActions(Button btnRandomize, Button btnPrev, Button btnNext) {
        btnRandomize.setOnAction((ActionEvent actionEvent) -> {
            int i = (int) (Math.random() * promptReader.objectDataList.size());

            String objectDescription = promptReader.objectDataList.get(i).getObjectDescription();
            objectPromptInput.setText(objectDescription);
            app.setObjectDescription(objectDescription);
        });

        btnPrev.setOnAction((ActionEvent event) -> {
            SceneManager.getInstance().setActiveScene(0);
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

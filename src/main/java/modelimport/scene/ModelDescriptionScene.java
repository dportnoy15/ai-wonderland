package modelimport.scene;

import java.util.regex.Pattern;

import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

import modelimport.FilterIO;
import modelimport.HelloFX;
import modelimport.PromptIO;
import modelimport.SceneManager;
import modelimport.Utils;

public class ModelDescriptionScene extends AliceScene {
    private static final int WORD_LIMIT = 3;

    private FilterIO languageFilter;

    protected String message;

    protected Thread bgThread;
    protected Task<Void> pythonTask;

    private PromptIO promptReader;

    private TextField objectPromptInput;

    private Label improperWarning;
    private ImageView improper;
    private HBox improperMsg;
    
    public ModelDescriptionScene(Stage stage, Scene scene, HelloFX app) {
        super(stage, scene, app);

        message = "";
        bgThread = null;
        pythonTask = null;

        promptReader = new PromptIO();
        languageFilter = new FilterIO();
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
        ImageView backImg = new ImageView(new Image("file:src/main/pic/PreviousLocation.png"));
        backImg.setFitWidth(30);
        backImg.setFitHeight(30);

        Button btnPrev = new Button("", backImg);
        btnPrev.setMaxSize(40, 40);
        btnPrev.setBackground(Utils.getBackgroundColor(Color.TRANSPARENT, 3));
        Region region = new Region();
        Button btnNext = new Button("Add Details");
        btnNext.setStyle("-fx-font-size: 14;");
        btnNext.setBackground(Utils.getBackgroundColor(Color.BLUEVIOLET));
        btnNext.setMinSize(120, 30);
        btnNext.setTextFill(Color.WHITE);


        HBox.setHgrow(region, Priority.ALWAYS);

        bottomPane.getChildren().addAll(btnPrev, region, btnNext);
        bottomPane.setAlignment(Pos.CENTER);
        bottomPane.setPadding(new Insets(0, 50, 0, 50));

        /* Start screen customization */

        GridPane grid = centerPane;
        grid.setAlignment(Pos.TOP_LEFT);
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(10, 100, 0, 100));

        Label objectPromptDescription = new Label("Item Description:");
        objectPromptDescription.setFont(Font.font(15));
        grid.add(objectPromptDescription, 0, 1);

        Label wordCountLabel = new Label();
        wordCountLabel.setFont(Font.font(15));
        wordCountLabel.setBackground(Utils.getBackgroundColor(Color.BLUEVIOLET, 3));
        //grid.add(wordCountLabel, 1, 3);
        wordCountLabel.setPrefWidth(120);
        wordCountLabel.setText("Word limit: 0/3");
        wordCountLabel.setTextFill(Color.WHITE);
        wordCountLabel.setAlignment(Pos.CENTER);

        objectPromptInput = new TextField();
        objectPromptInput.setPrefWidth(570);
        objectPromptInput.setPrefHeight(300);
        objectPromptInput.setAlignment(Pos.TOP_LEFT);
        objectPromptInput.setPromptText("e.g., a treasure box");
        objectPromptInput.setStyle("-fx-font-size: 20;");

        objectPromptInput.setBorder(new Border(new BorderStroke(
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
                wordCountLabel.setText("Word limit: " + (words.length - (words[0].equals("")? 1 : 0)) + "/" + WORD_LIMIT);
        });

        objectPromptInput.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                btnNext.fire();
            }
        });
        //grid.add(objectPromptInput, 0, 2);

        StackPane stackPane = new StackPane();
        stackPane.getChildren().addAll(objectPromptInput, wordCountLabel);
        StackPane.setAlignment(wordCountLabel, Pos.BOTTOM_RIGHT);

        grid.add(stackPane, 0, 2);

        improper = new ImageView(new Image("file:src/main/pic/ImproperText.png"));
        improper.setFitWidth(60);
        improper.setFitHeight(40);

        improperWarning = new Label("Improper input");
        improperWarning.setFont(Font.font(25));
        improperWarning.setTextFill(Color.RED);
        improperMsg = new HBox();
        improperMsg.getChildren().addAll(improper, improperWarning);
        grid.add(improperMsg, 0, 3);
        improperMsg.setVisible(false);

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
            improperMsg.setVisible(false);
            SceneManager.getInstance().setActiveScene(0);
        });

        btnNext.setOnAction((ActionEvent event) -> {
            String objectPrompt = objectPromptInput.getText();
            boolean isFiltered = false;
            for (String word : languageFilter.wordList) {
                String replacedPrompt = objectPrompt.replaceAll(Pattern.compile("\\b" + word + "\\b", Pattern.CASE_INSENSITIVE).pattern(), "");
                if (!replacedPrompt.equals(objectPrompt)){
                    isFiltered = true;
                    objectPromptInput.setText(replacedPrompt);
                }
            }
            // Before going to the next page, detect if there exists improper words, if so, delete them and display a warning instead.
            if (isFiltered){
                improperMsg.setVisible(true);
                return;
            }
            improperMsg.setVisible(false);
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

package modelimport.scene;

import java.io.File;
import java.io.IOException;

import javafx.animation.KeyFrame;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;

import javafx.util.Duration;
import modelimport.AliceModel;
import modelimport.HelloFX;
import modelimport.PromptIO;
import modelimport.SceneManager;
import modelimport.Utils;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.animation.Timeline;

public class TextureDescriptionScene extends AliceScene {

    private static final int WORD_LIMIT = 10;

    protected String message;

    protected Thread bgThread;
    protected Task<Void> pythonTask;

    private PromptIO promptReader;

    private TextArea texturePromptInput;

    private Button btnModel, btnTexture;

    private ImageView randomizeGif;

    public TextureDescriptionScene(Stage stage, Scene scene, HelloFX app) {
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

        Label scenetitle = new Label("Texture Description");
        scenetitle.setFont(uiFont);
        topPane.setAlignment(Pos.CENTER);
        topPane.getChildren().add(scenetitle);

        /* Start footer definition */
        ImageView backImg = new ImageView(new Image("file:src/main/pic/PreviousLocation.png"));
        backImg.setFitWidth(30);
        backImg.setFitHeight(30);
        Button btnPrev = new Button("", backImg);
        btnPrev.setMaxHeight(40);
        btnPrev.setMaxWidth(40);
        btnPrev.setBackground(Utils.getBackgroundColor(Color.TRANSPARENT, 3));

        Region region = new Region();
        Button btnNext = new Button("Next");

        btnNext.setVisible(false);

        HBox.setHgrow(region, Priority.ALWAYS);
        bottomPane.setAlignment(Pos.CENTER);
        bottomPane.setPadding(new Insets(0, 50, 0, 50));

        /* Start screen customization */

        GridPane grid = centerPane;
        grid.setAlignment(Pos.TOP_LEFT);
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(10, 100, 0, 100));

        Label texturePromptDescription = new Label("Detailed Description:");
        texturePromptDescription.setFont(Font.font(15));
        grid.add(texturePromptDescription, 0, 1);

        texturePromptInput = new TextArea();

        texturePromptInput.setPrefWidth(500);
        texturePromptInput.setPrefHeight(300);
        texturePromptInput.setBorder(new Border(new BorderStroke(
                Color.TRANSPARENT, // Border color
                BorderStrokeStyle.SOLID, // Border style
                new CornerRadii(3), // CornerRadii
                new BorderWidths(0)))); // Border width);
        texturePromptInput.setStyle("-fx-font-size: 20;");
        //grid.add(texturePromptInput, 0, 2);

        Label wordCountLabel = new Label();
        wordCountLabel.setFont(Font.font(15));
        wordCountLabel.setBackground(Utils.getBackgroundColor(Color.BLUEVIOLET));
        //grid.add(wordCountLabel, 1, 3);
        wordCountLabel.setPrefWidth(140);
        wordCountLabel.setAlignment(Pos.CENTER);
        wordCountLabel.setText("Word limit: 0/10");
        wordCountLabel.setTextFill(Color.WHITE);
        texturePromptInput.textProperty().addListener((observable, oldValue, newValue) -> {
            String[] words = newValue.trim().split("\\s+");
            // Limit the input to WORD_LIMIT words
            if (words.length > WORD_LIMIT) {
                texturePromptInput.setText(oldValue);
            } else
                // Update the word count label
                wordCountLabel.setText("Word limit: " + (words.length - (words[0].equals("")? 1 : 0)) + "/" + WORD_LIMIT);
        });
        texturePromptInput.setPromptText("Describe the appearance of the 3D model as how it feels or looks. The model shape won’t be edited by this input.");
        texturePromptInput.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                btnTexture.fire();
            }
        });

        randomizeGif = new ImageView(new Image("file:src/main/pic/dice.png"));
        randomizeGif.setFitWidth(50);
        randomizeGif.setFitHeight(50);

        Button btnRandomize = new Button("", randomizeGif);
        btnRandomize.setBackground(Utils.getBackgroundColor(Color.BLUEVIOLET, 3));
        btnRandomize.setMaxHeight(50);
        btnRandomize.setMaxWidth(50);

        StackPane stackPane = new StackPane();
        stackPane.getChildren().addAll(texturePromptInput, wordCountLabel);
        StackPane.setAlignment(wordCountLabel, Pos.BOTTOM_RIGHT);

        HBox box = new HBox();
        box.getChildren().addAll(stackPane, btnRandomize);
        box.setBorder(new Border(new BorderStroke(
                Color.BLUEVIOLET, // Border color
                BorderStrokeStyle.SOLID, // Border style
                new CornerRadii(5), // CornerRadii
                new BorderWidths(2)))); // Border width);

        grid.add(box, 0, 2);

        btnModel = new Button("Generate");
        btnModel.setStyle("-fx-font-size: 15;");
        btnModel.setBackground(Utils.getBackgroundColor(Color.BLUEVIOLET));

        btnModel.setMinSize(120, 30);
        btnModel.setMaxSize(120, 30);
        btnModel.setTextFill(Color.WHITE);

        btnTexture = new Button("Regenerate Texture");
        btnTexture.setBackground(Utils.getBackgroundColor(Color.BLUEVIOLET));
        btnTexture.setTextFill(Color.WHITE);

        bottomPane.getChildren().addAll(btnPrev, region, btnModel, btnTexture);

        registerButtonActions(btnModel, btnTexture, btnRandomize, btnPrev);
    }

    private void playAndStopAnimation(Duration stopTime) {
        randomizeGif.setImage(new Image("file:src/main/pic/random_GIF.gif"));
        Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(1), event -> {
            // Load the new image
            randomizeGif.setImage(new Image("file:src/main/pic/dice.png"));
        }));
        timeline.play();
    }

    public void registerButtonActions(Button btnModel, Button btnTexture, Button btnRandomize, Button btnPrev) {
        btnModel.setOnAction((ActionEvent event) -> {
            message = "Generating 3D model ...";

            try {
                pythonTask = new Task<Void>() {
                    @Override
                    protected Void call() throws Exception {
                        System.out.println("Starting python process...");

                        String texturePrompt = texturePromptInput.getText();
                        app.setTextureDescription(texturePrompt);

                        app.logModelGeneration(app.getObjectDescription(), app.getTextureDescription());

                        try {
                            app.startTimer();

                            //int exitCode = Utils.invokeScript("python", new File("generate_model_shap-e.py").getAbsolutePath(), objectPrompt);
                            int exitCode = Utils.invokeScript("python", new File("generate_model_meshy.py").getAbsolutePath());

                            app.stopTimer();

                            if (exitCode == 0) {
                                message = "Model generated successfully";
                            } else {
                                message = "Error geenerating model: " + exitCode;
                                return null;
                            }

                            System.out.println("GLB model generation complete. Converting to DAE now...");

                            exitCode = Utils.invokeScript("blender", "--background", "--python", "model/format.py");

                            if (exitCode == 0) {
                                message = "Model converted successfully";
                            } else {
                                message = "Error converting model: " + exitCode;
                                return null;
                            }

                            System.out.println("Conversion to DAE finished. Showing model now...");

                            AliceModel model = AliceModel.createFromMeshy(app.getNewModelName(), app.getObjectUrl(), app.getThumbnailUrl());

                            app.copyModelFileToLibrary(model);

                            app.addModelToLibrary(model);
                            ((SelectModelScene) SceneManager.getInstance().getScene(0)).refreshModelLibrary();

                            Platform.runLater(() -> {
                                app.showProgressMinimized(false);

                                app.showModel("model.glb");

                                SceneManager.getInstance().setActiveScene(0);
                            });
                        } catch(InterruptedException e) {
                            System.out.println("Task canceled");
                        } catch(IOException e) {
                            System.out.println("Error generating model");
                            e.printStackTrace();

                            System.exit(0);
                        }

                        return null;
                    }
                };

                pythonTask.setOnSucceeded(ev -> {
                    System.out.println("COMPLETED PYTHON TASK");

                    // setStatusText(message);
                });

                app.showProgressMinimized(true);

                bgThread = new Thread(pythonTask);
                bgThread.setDaemon(true);
                bgThread.start();
            } catch(Exception e) {
                System.out.println("ERROR BERRPR");
                e.printStackTrace();
            }
        });

        btnTexture.setOnAction((ActionEvent event) -> {
            message = "Generating a new texture for the model ...";

            try {
                pythonTask = new Task<Void>() {
                    @Override
                    protected Void call() throws Exception {
                        System.out.println("Starting python process...");

                        String texturePrompt = texturePromptInput.getText();
                        app.setTextureDescription(texturePrompt);

                        app.logTextureGeneration(app.getObjectDescription(), app.getTextureDescription());

                        try {
                            app.startTimer();

                            int exitCode = Utils.invokeScript("python", new File("generate_texture_meshy.py").getAbsolutePath());

                            app.stopTimer();

                            if (exitCode == 0) {
                                message = "Texture generated successfully";
                            } else {
                                message = "Error geenerating texture: " + exitCode;
                                return null;
                            }

                            System.out.println("Texture generation complete. Converting to DAE now...");

                            exitCode = Utils.invokeScript("blender", "--background", "--python", "model/format.py");

                            if (exitCode == 0) {
                                message = "Model converted successfully";
                            } else {
                                message = "Error converting model: " + exitCode;
                                return null;
                            }

                            AliceModel model = AliceModel.createFromMeshy(app.getNewModelName(), app.getObjectUrl(), app.getThumbnailUrl());

                            app.copyModelFileToLibrary(model);

                            app.addModelToLibrary(model);
                            ((SelectModelScene) SceneManager.getInstance().getScene(0)).refreshModelLibrary();

                            Platform.runLater(() -> {
                                app.showProgressMinimized(false);

                                app.showModel("model.glb");

                                SceneManager.getInstance().setActiveScene(0);
                            });
                        } catch(InterruptedException e) {
                            System.out.println("Task canceled");
                        } catch(IOException e) {
                            System.out.println("Error generating texture");
                            e.printStackTrace();

                            System.exit(0);
                        }

                        return null;
                    }
                };

                pythonTask.setOnSucceeded(ev -> {
                    System.out.println("COMPLETED PYTHON TASK");

                    // setStatusText(message);
                });

                app.showProgressMinimized(true);

                bgThread = new Thread(pythonTask);
                bgThread.setDaemon(true);
                bgThread.start();
            } catch(Exception e) {
                System.out.println("ERROR GENERATING TEXTURE");
                e.printStackTrace();
            }
        });

        btnRandomize.setOnAction((ActionEvent actionEvent) -> {
            playAndStopAnimation(Duration.seconds(1));

            int i = (int) (Math.random() * promptReader.objectDataList.size());

            String textureDescription = promptReader.objectDataList.get(i).getTextureDescription();
            texturePromptInput.setText(textureDescription);
            app.setTextureDescription(textureDescription);
        });

        btnPrev.setOnAction((ActionEvent event) -> {
            SceneManager.getInstance().setActiveScene(2);

            ((ArtStyleScene) SceneManager.getInstance().getScene(2)).selectCurrentStyleButton();
        });
    }

    public void clearFields() {
        texturePromptInput.clear();
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

    public void generateNewModel(boolean genNew) {
        btnModel.setVisible(genNew);
        btnModel.setManaged(genNew);

        btnTexture.setVisible(!genNew);
        btnTexture.setManaged(!genNew);
    }
}

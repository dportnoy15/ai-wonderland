package modelimport.scene;

import java.io.File;
import java.io.IOException;

import javafx.application.Platform;
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

import modelimport.AliceModel;
import modelimport.HelloFX;
import modelimport.PromptIO;
import modelimport.SceneManager;
import modelimport.Utils;

public class TextureDescriptionScene extends AliceScene {

    protected String message;

    protected Thread bgThread;
    protected Task<Void> pythonTask;

    private PromptIO promptReader;

    private TextField texturePromptInput;

    private Button btnModel, btnTexture;

    private boolean genNewModel; // will regenerate the texture if set to false
    
    public TextureDescriptionScene(Stage stage, Scene scene, HelloFX app) {
        super(stage, scene, app);

        message = "";
        bgThread = null;
        pythonTask = null;

        genNewModel = true;

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

        Button btnPrev = new Button("Back");
        Region region = new Region();
        Button btnNext = new Button("Next");

        btnNext.setVisible(false);

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

        Label texturePromptDescription = new Label("Texture Description:");
        grid.add(texturePromptDescription, 0, 1);

        texturePromptInput = new TextField();
        grid.add(texturePromptInput, 1, 1);

        Button btnRandomize = new Button("Create a random prompt");
        grid.add(btnRandomize, 2, 1);

        btnModel = new Button("Generate Model");
        btnTexture = new Button("Regenerate Texture");

        HBox hbBtn = new HBox(10);
        hbBtn.setAlignment(Pos.BOTTOM_LEFT);
        hbBtn.getChildren().add(btnModel);
        hbBtn.getChildren().add(btnTexture);
        grid.add(hbBtn, 1, 12, 5, 1);

        registerButtonActions(btnModel, btnTexture, btnRandomize, btnPrev);
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

                        //app.logModelPrompt(objectPrompt);

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

                            AliceModel model = AliceModel.createFromMeshy("some name", app.getObjectUrl(), app.getThumbnailUrl());

                            app.copyModelFileToLibrary(model);

                            app.addModelToLibrary(model);
                            ((SelectModelScene) SceneManager.getInstance().getScene(0)).refreshModelLibrary();

                            // enable regenerating textures for the current model
                            btnTexture.setDisable(false);

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

                        //app.logTexturePrompt(objectPrompt, texturePrompt);

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

                            AliceModel model = AliceModel.createFromMeshy("some name", app.getObjectUrl(), app.getThumbnailUrl());

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
        genNewModel = genNew;

        btnModel.setVisible(genNew);
        btnTexture.setVisible(!genNew);
    }
}
package modelimport;

import java.awt.Desktop;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.text.SimpleDateFormat;
import java.util.*;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.*;
import javafx.geometry.*;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.*;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import py4j.GatewayServer;

import modelimport.scene.SelectModelScene;
import modelimport.scene.ModelDescriptionScene;
import modelimport.scene.ArtStyleScene;
import modelimport.scene.TextureDescriptionScene;

public class HelloFX extends Application {

    private static final String MODEL_LIB_DIR = "model-lib";
    private static final String GEN_MODEL_DIR = "gen-model";

    public static HelloFX self;

    Stage stage;

    GatewayServer gatewayServer;

    private String objectDescription;
    private String textureDescription;
    private boolean cancelGeneration;

    private AliceModel activeModel;

    // UI Elements

    private Text status;
    private ProgressBar progress;

    private Text elapsedTime;

    private Timer waitTimer;
    private TimerTask waitTask;

    int elapsedSec;
    boolean isTimerRunning;

    private ArrayList<AliceModel> models;

    private String objectUrl;
    private String thumbnailUrl;

    private Stage progressStage;

    private boolean isShowingProgress;

    public static void main(String[] args) {
        launch();
    }

    @Override
    public void start(Stage stage) {
        HelloFX.self = this;

        this.stage = stage;

        models = new ArrayList<>();

        activeModel = null;

        objectUrl = "";
        thumbnailUrl = "";

        isShowingProgress = false;

        String javaVersion = System.getProperty("java.version");
        String javafxVersion = System.getProperty("javafx.version");

        System.out.println("Hello, JavaFX " + javafxVersion + ", running on Java " + javaVersion + ".");

        try {
            Utils.deleteDirectory(MODEL_LIB_DIR);
        } catch(IOException ioe) {
            System.out.println(MODEL_LIB_DIR + " not found");
        }

        try {
            Files.createDirectories(Paths.get(MODEL_LIB_DIR));
        } catch(IOException ioe) {
            System.out.println("Error: Unable to create " + MODEL_LIB_DIR);
            ioe.printStackTrace();
        }

        try {
            Files.createDirectories(Paths.get(GEN_MODEL_DIR));
        } catch(IOException ioe) {
            System.out.println("Error: Unable to create " + GEN_MODEL_DIR);
            ioe.printStackTrace();
        }

        SceneManager.setStage(stage);

        SelectModelScene sceneSelectModel = new SelectModelScene(stage, new Scene(new BorderPane(), 800, 600), this);
        sceneSelectModel.initLayout();
        SceneManager.getInstance().addScene(sceneSelectModel);

        ModelDescriptionScene sceneModelDescription = new ModelDescriptionScene(stage, new Scene(new BorderPane(), 800, 600), this);
        sceneModelDescription.initLayout();
        SceneManager.getInstance().addScene(sceneModelDescription);

        ArtStyleScene sceneArtStyle = new ArtStyleScene(stage, new Scene(new BorderPane(), 800, 600), this);
        sceneArtStyle.initLayout();
        SceneManager.getInstance().addScene(sceneArtStyle);

        TextureDescriptionScene sceneTextureDescription = new TextureDescriptionScene(stage, new Scene(new BorderPane(), 800, 600), this);
        sceneTextureDescription.initLayout();
        SceneManager.getInstance().addScene(sceneTextureDescription);

        stage.setTitle("AI Wonderland");

        setupProgressWindow();

        SceneManager.getInstance().setActiveScene(0);
        stage.show();

        gatewayServer = new GatewayServer(new HelloFX());
        gatewayServer.start();
        System.out.println("Gateway Server Started");
    }

    private void setupProgressWindow() {
        progressStage = new Stage();
        BorderPane layout = new BorderPane();
        Scene progressScene = new Scene(layout, 250, 100);
        progressScene.setFill(Color.color(0.2, 0.2, 0.6, 0.4));
        GridPane centerPane = new GridPane();
        centerPane.setAlignment(Pos.CENTER);
        centerPane.setHgap(5);
        centerPane.setVgap(5);
        centerPane.setPadding(new Insets(10, 10, 10, 10));
        layout.setCenter(centerPane);

        progress = new ProgressBar();
        progress.setProgress(0.0F);
        progress.setVisible(false);

        status = new Text("");
        elapsedTime = new Text("");

        progress.setStyle("-fx-accent: blue;");
        centerPane.add(progress, 0, 0);
        status.setFill(Color.WHITE);
        centerPane.add(status, 0, 1);
        elapsedTime.setFill(Color.WHITE);
        centerPane.add(elapsedTime, 0, 2);

        Button btnStopGen = new Button("Stop");
        centerPane.add(btnStopGen, 2, 2);

        layout.setStyle("-fx-background-color: transparent;");
        centerPane.setStyle("-fx-background-color: transparent;");

        progressStage.setScene(progressScene);
        progressStage.setTitle("Generating");
        progressStage.setAlwaysOnTop(true);
        progressStage.initStyle(StageStyle.TRANSPARENT);

        Screen screen = Screen.getPrimary();
        Rectangle2D bounds = screen.getVisualBounds();
        double desiredX = bounds.getMinX() + 10;
        double desiredY = bounds.getMaxY() - stage.getHeight() - 50;

        progressStage.setX(desiredX);
        progressStage.setY(desiredY);

        // This code prevents the user from opening the main window while a model is being generated.
        // If they try to unminimize the window, it just minimizes again!
        // This prevents them from trying to generate a new model while one is already being generated
        stage.iconifiedProperty().addListener((observable, oldValue, newValue) -> {
            if (isShowingProgress && !newValue) {
                Platform.runLater(() -> {
                    stage.setIconified(true);
                });
            }
        });

        btnStopGen.setOnAction((ActionEvent event) -> {
            showProgressMinimized(false);
            setCancelStatus(true);
        });
    }

    @Override
    public void stop() {
        System.out.println("Stopping application");

        stopTimer();

        SceneManager.getInstance().getScene(1).stopTask();

        gatewayServer.shutdown();

        Platform.exit();
    }

    public String getApiKey() {
        return Data.getApiKey();
    }

    public String getNewModelName() {
        return "Model " + (models.size() + 1);
    }

    public void copyModelFileToLibrary(AliceModel model) {
        String modelName = "gen-model_" + String.format("%03d", getModels().size() + 1);
        String modelDirName = MODEL_LIB_DIR + "/" + modelName;

        try {
            Files.createDirectories(Paths.get(modelDirName));

            File folder = new File(GEN_MODEL_DIR);
            File[] modelFiles = folder.listFiles();

            for (int i = 0; i < modelFiles.length; i++) {
                String modelFileName = modelFiles[i].getName();

                Files.copy(Paths.get(GEN_MODEL_DIR + "/" + modelFileName), Paths.get(modelDirName + "/" + modelFileName));
            }

            String modelFilePath = modelDirName + "/model.dae";

            model.setLocalPath(modelFilePath);
        } catch(IOException ioe) {
            System.out.println("Error copying model to libary folder");
            ioe.printStackTrace();
        }
    }

    public void addModelToLibrary(AliceModel model) {
        models.add(0, model);
    }

    public ArrayList<AliceModel> getModels() {
        return models;
    }

    public AliceModel getActiveModel() {
        return HelloFX.self.activeModel;
    }

    public void setActiveModel(AliceModel model) {
        HelloFX.self.activeModel = model;
    }

    public void showModel(String modelName) {
        System.out.println("Showing 3D model: " + modelName);

        try {
            Desktop.getDesktop().open(new File(GEN_MODEL_DIR + "/" + modelName));
        } catch(IOException ioe) {
            System.out.println("Error showing model");
            ioe.printStackTrace();
        }
    }

    public void showAliceImportTool() {
        if (HelloFX.self.activeModel == null) {
            return;
        }

        AliceModel model = HelloFX.self.activeModel;

        try {
            // ImportModel.exe -a "Some guy" -s 0.5 -n MyRoadBarrier -t Fish .\RoadBarrier.dae
            // Author (-a) Scale (-s) Name (-n) and Type (-t)
            System.out.println(Utils.invokeScript(new File("ImportModel/ImportModel.exe").getAbsolutePath(),
                                                  "-n", model.getName().replaceAll(" ", "_"),
                                                  new File(model.getLocalPath()).getAbsolutePath()));
        } catch(Exception e) {
            System.out.println("Could not open Alice model importer");
            e.printStackTrace();
        }
    }

    public void setProgress(String statusText, int percent) {
        Platform.runLater(() -> {
            HelloFX.self.status.setVisible(true);
            HelloFX.self.status.setText(statusText);

            HelloFX.self.progress.setVisible(true);
            HelloFX.self.progress.setProgress(percent / 100f);
        });
    }

    public TimerTask createWaitTask() {
        return new TimerTask() {
            @Override
            public void run() {
                String timeString = String.format("Waiting for %d sec ...", elapsedSec);
                System.out.println(timeString);

                Platform.runLater(() -> {
                    elapsedTime.setText(timeString);
                });

                elapsedSec++;
            }
        };
    }

    public void startTimer() {
        if (!isTimerRunning) {
            waitTimer = new Timer();
            waitTask = createWaitTask();

            try {
                waitTimer.scheduleAtFixedRate(waitTask, 0, 1000);
            } catch(Exception e) {
                e.printStackTrace();
            }

            elapsedSec = 0;
            isTimerRunning = true;
        }
    }

    public void stopTimer() {
        if (isTimerRunning) {
            waitTimer.cancel();
            waitTask.cancel();

            isTimerRunning = false;

            Platform.runLater(() -> {
                elapsedTime.setText("");
            });
        }
    }

    public String getArtStyle() {
        return ((ArtStyleScene) SceneManager.getInstance().getScene(2)).getArtStyle();
    }
    public boolean getCancelStatus() {
        return HelloFX.self.cancelGeneration;
    }

    public void setCancelStatus(boolean b) {
        HelloFX.self.cancelGeneration = b;
    }

    public String getObjectUrl() {
        return HelloFX.self.objectUrl;
    }

    public void setObjectUrl(String objectUrl) {
        HelloFX.self.objectUrl = objectUrl;
    }

    public String getThumbnailUrl() {
        return HelloFX.self.thumbnailUrl;
    }

    public void setThumbnailUrl(String thumbnailUrl) {
        HelloFX.self.thumbnailUrl = thumbnailUrl;
    }

    public String getObjectDescription() {
        return HelloFX.self.objectDescription;
    }

    public void setObjectDescription(String text) {
        HelloFX.self.objectDescription = text;
    }

    public String getTextureDescription() {
        return HelloFX.self.textureDescription;
    }

    public void setTextureDescription(String text) {
        HelloFX.self.textureDescription = text;
    }

    public void showProgressMinimized(boolean showProgress) {
        if (showProgress) {
            stage.setAlwaysOnTop(true);
            progressStage.show();
            progress.setProgress(0.0F);
            stage.setIconified(true);
        } else {
            stage.setIconified(false);
            progressStage.hide();
            stage.setAlwaysOnTop(false);
        }

        isShowingProgress = showProgress;
    }

    public void logModelGeneration(String objectPrompt, String texturePrompt) {
        Path path = Paths.get("playtestLog.txt");

        try (BufferedWriter writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8, StandardOpenOption.APPEND, StandardOpenOption.CREATE)) {
            Date curTime = new Date();

            String day = new SimpleDateFormat("MM/dd/yyyy").format(curTime);
            String time = new SimpleDateFormat("HH:mm").format(curTime);

            String header = System.getProperty("line.separator") +  "Generating model on " + day + " at " + time + System.getProperty("line.separator") + System.getProperty("line.separator");
            String objectPromptStr = "Object prompt: " + objectPrompt + System.getProperty("line.separator");
            String texturePromptStr = "Texture prompt: " + texturePrompt + System.getProperty("line.separator");

            writer.write(header);
            writer.write(objectPromptStr);
            writer.write(texturePromptStr);
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    public void logTextureGeneration(String objectPrompt, String texturePrompt) {
        Path path = Paths.get("playtestLog.txt");

        try (BufferedWriter writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8, StandardOpenOption.APPEND, StandardOpenOption.CREATE)) {
            Date curTime = new Date();

            String day = new SimpleDateFormat("MM/dd/yyyy").format(curTime);
            String time = new SimpleDateFormat("HH:mm").format(curTime);

            String header = System.getProperty("line.separator") +  "Generating texture on " + day + " at " + time + System.getProperty("line.separator") + System.getProperty("line.separator");
            String objectPromptStr = "Object prompt: " + objectPrompt + System.getProperty("line.separator");
            String texturePromptStr = "Texture prompt: " + texturePrompt + System.getProperty("line.separator");

            writer.write(header);
            writer.write(objectPromptStr);
            writer.write(texturePromptStr);
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

}
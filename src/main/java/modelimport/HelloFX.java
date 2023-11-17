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
import javafx.stage.FileChooser;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import py4j.GatewayServer;

import com.jcraft.jsch.*;

import modelimport.scene.SelectModelScene;
import modelimport.scene.GenerateModelScene;
import modelimport.scene.GenerateTextureScene;

public class HelloFX extends Application {

    static HelloFX self;

    Stage stage;

    Process curProcess;

    GatewayServer gatewayServer;

    // UI Elements

    private TextField objectPromptInput;
    private TextField texturePromptInput;
    private TextField negativePromptInput;

    private Text status;
    private Text progressStatus;
    private Text elapsedTime;

    private ProgressBar progress;

    private Button modelBtn;
    private Button textureBtn;
    private Button uploadBtn;
    private Button randomizeBtn;

    private Button prevBtn;
    private Button nextBtn;

    private Button cancelGeneration;

    private Timer waitTimer;
    private TimerTask waitTask;

    int elapsedSec;
    boolean isTimerRunning;

    private ArrayList<AliceModel> models;

    private String objectUrl;

    // Temp solution
    private ChoiceBox<String> styleSelectBox;

    private static PromptIO promptReader;

    private Button[] artStyleButtons;
    private int curStyleSelection;

    private int curModel;

    private HBox libraryPane;

    private static final int ART_STYLE_COUNT = 8;

    private Stage progressStage;

    public static void main(String[] args) {
        launch();
    }

    @Override
    public void start(Stage stage) {
        HelloFX.self = this;

        promptReader = new PromptIO();

        this.stage = stage;

        models = new ArrayList<>();

        curProcess = null;

        objectUrl = "";

        String javaVersion = System.getProperty("java.version");
        String javafxVersion = System.getProperty("javafx.version");

        System.out.println("Hello, JavaFX " + javafxVersion + ", running on Java " + javaVersion + ".");

        SceneManager.setStage(stage);

        SelectModelScene sceneSelectModel = new SelectModelScene(stage, new Scene(new BorderPane(), 800, 600), this);
        GenerateModelScene sceneGenerateModel = new GenerateModelScene(stage, new Scene(new BorderPane(), 800, 600), this);
        GenerateTextureScene sceneGenerateTexture = new GenerateTextureScene(stage, new Scene(new BorderPane(), 800, 600), this);

        initLayout_SelectModelScene(sceneSelectModel);
        initLayout_GenerateModelScene(sceneGenerateModel);
        initLayout_GenerateTextureScene(sceneGenerateTexture);

        SceneManager.getInstance().addScene(sceneSelectModel);
        SceneManager.getInstance().addScene(sceneGenerateModel);
        SceneManager.getInstance().addScene(sceneGenerateTexture);

        isTimerRunning = false;
        initTimer();

        stage.setTitle("AI Wonderland");

        SetupSecondStage();

        addButtonActions();

        SceneManager.getInstance().setActiveScene(0);
        stage.show();

        gatewayServer = new GatewayServer(new HelloFX());
        gatewayServer.start();
        System.out.println("Gateway Server Started");

        curModel = -1;
    }

    private void SetupSecondStage() {
        progressStage = new Stage();
        BorderPane layout = new BorderPane();
        Scene progressScene = new Scene(layout, 250, 100);
        progressScene.setFill(Color.color(0.6, 0.6, 0.6, 0.2));
        GridPane centerPane = new GridPane();
        centerPane.setAlignment(Pos.CENTER);
        centerPane.setHgap(5);
        centerPane.setVgap(5);
        centerPane.setPadding(new Insets(10,10,10,10));
        layout.setCenter(centerPane);

        centerPane.add(progress, 0, 0);
        centerPane.add(status, 0, 1);
        centerPane.add(elapsedTime, 0, 2);

        cancelGeneration = new Button("Back");
        centerPane.add(cancelGeneration, 1, 2);
        layout.setStyle("-fx-background-color: transparent;");
        centerPane.setStyle("-fx-background-color: transparent;");

        progressStage.setScene(progressScene);
        progressStage.setTitle("Generating");
        progressStage.setAlwaysOnTop(true);
        progressStage.initStyle(StageStyle.TRANSPARENT);;

        Screen screen = Screen.getPrimary();
        Rectangle2D bounds = screen.getVisualBounds();
        double desiredX = bounds.getMinX() + 10;
        double desiredY = bounds.getMaxY() - stage.getHeight() - 50;

        progressStage.setX(desiredX);
        progressStage.setY(desiredY);
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

    private void initLayout_SelectModelScene(SelectModelScene scene) {
        BorderPane layout = (BorderPane) scene.getScene().getRoot();

        HBox topPane = new HBox();
        HBox bottomPane = new HBox(20);
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

        Label scenetitle = new Label("Select a Model");
        scenetitle.setFont(uiFont);
        topPane.setAlignment(Pos.CENTER);
        topPane.getChildren().add(scenetitle);

        /* Start screen customization */

        GridPane grid = centerPane;
        grid.setAlignment(Pos.CENTER);
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(25, 25, 25, 25));

        Button btnGenerateModel = new Button("Generate New Model");
        btnGenerateModel.setMinSize(150, 100);

        Button btnUploadModel = new Button("Upload Local Model");
        btnUploadModel.setMinSize(150, 100);

        HBox hbBtn = new HBox(200);
        hbBtn.setAlignment(Pos.TOP_CENTER);
        hbBtn.getChildren().add(btnGenerateModel);
        hbBtn.getChildren().add(btnUploadModel);
        grid.add(hbBtn, 1, 0, 5, 1);

        /* Start footer definition */
        bottomPane.setAlignment(Pos.CENTER);
        bottomPane.setPadding(new Insets(0, 50, 0, 50));
        bottomPane.setMinSize(600, 100);

        libraryPane = bottomPane;

        refreshModelLibrary();

        scene.registerButtonActions(btnGenerateModel, btnUploadModel);
    }

    private void initLayout_GenerateModelScene(GenerateModelScene scene) {
        BorderPane layout = (BorderPane) scene.getScene().getRoot();

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

        Label scenetitle = new Label("Welcome");
        scenetitle.setFont(uiFont);
        topPane.setAlignment(Pos.CENTER);
        topPane.getChildren().add(scenetitle);

        /* Start footer definition */

        Button prevBtn = new Button("Prev");
        prevBtn.setVisible(false);
        Region region = new Region();
        nextBtn = new Button("Next");

        HBox.setHgrow(region, Priority.ALWAYS);
        bottomPane.getChildren().addAll(prevBtn, region, nextBtn);
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

        randomizeBtn = new Button("Create a random prompt");
        grid.add(randomizeBtn, 2, 1);

        Label texturePromptDescription = new Label("Texture Description:");
        grid.add(texturePromptDescription, 0, 2);

        // Test for art style add
        //setupArtStyleBox();
        //grid.add(styleSelectBox, 0, 3);

        /*
        Label negativePromptDescription = new Label("Negative Prompt:");
        grid.add(negativePromptDescription, 0, 3);

        negativePromptInput = new TextField();
        grid.add(negativePromptInput, 1, 3);
        */

        texturePromptInput = new TextField();
        grid.add(texturePromptInput, 1, 2);

        status = new Text("");
        grid.add(status, 1, 5);

        progressStatus = new Text("");
        grid.add(progressStatus, 0, 7);

        Label progressDescription = new Label("Generation Progress:");
        grid.add(progressDescription, 0, 8);

        progress = new ProgressBar();
        progress.setProgress(0.0F);
        progress.setVisible(false);
        grid.add(progress, 1, 8);

        elapsedTime = new Text("");
        grid.add(elapsedTime, 0, 10);

        modelBtn = new Button("Generate Model");
        textureBtn = new Button("Regenerate Texture");
        textureBtn.setDisable(true);
        uploadBtn = new Button("Upload Model");

        HBox hbBtn = new HBox(10);
        hbBtn.setAlignment(Pos.BOTTOM_LEFT);
        hbBtn.getChildren().add(modelBtn);
        hbBtn.getChildren().add(textureBtn);
        hbBtn.getChildren().add(uploadBtn);
        grid.add(hbBtn, 1, 12, 5, 1);

        scene.registerButtonActions(modelBtn, textureBtn);
    }

    private void initLayout_GenerateTextureScene(GenerateTextureScene scene) {
        BorderPane layout = (BorderPane) scene.getScene().getRoot();

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

        prevBtn = new Button("Prev");
        Region region = new Region();
        Button nextBtn = new Button("Next");
        nextBtn.setVisible(false);

        HBox.setHgrow(region, Priority.ALWAYS);
        bottomPane.getChildren().addAll(prevBtn, region, nextBtn);
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
        artStyleButtons[0].setDisable(true);
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
    }

    public void addModelToLibrary(AliceModel model) {
        models.add(model);
    }

    public void refreshModelLibrary() {
        libraryPane.getChildren().clear();
        libraryPane.getChildren().addAll(generateModelLibraryButtons());
    }

    public void showModel(String modelName) {
        System.out.println("Showing 3D model: " + modelName);

        try {
            Desktop.getDesktop().open(new File("gen-model/" + modelName));
        } catch(IOException ioe) {
            System.out.println("Error showing model");
            ioe.printStackTrace();
        }
    }

    public void setProgress(String status, int percent) {
        Platform.runLater(() -> {
            System.out.println("Status set to " + status + ", percent set to " + percent + " from Python!!!");

            HelloFX.self.progressStatus.setVisible(true);
            HelloFX.self.progressStatus.setText(status);

            HelloFX.self.progress.setVisible(true);
            HelloFX.self.progress.setProgress(percent / 100f);
        });
    }

    public void resetProgress() {
        Platform.runLater(() -> {
            progressStatus.setVisible(false);
            progressStatus.setText("");

            progress.setVisible(false);
            progress.setProgress(0f);
        });
    }

    public void setStatusText(String text) {
        System.out.println(text);
        status.setText(text);
    }

    public void initTimer() {
        elapsedSec = 0;
        isTimerRunning = false;

        waitTimer = new Timer();

        waitTask = new TimerTask() {
            @Override
            public void run() {
                String timeString = String.format("Waiting for %d sec ...", elapsedSec);
                Platform.runLater(() -> elapsedTime.setText(timeString));

                System.out.println(timeString);

                elapsedSec++;
            }
        };
    }

    public void startTimer() {
        if (!isTimerRunning) {
            elapsedSec = 0;
            waitTimer.scheduleAtFixedRate(waitTask, 0, 1000);
            isTimerRunning = true;
        }
    }

    public void stopTimer() {
        if (isTimerRunning) {
            waitTask.cancel();
            Platform.runLater(() -> elapsedTime.setText(""));
            isTimerRunning = false;
        }
        waitTimer.cancel();
    }

    // this is useful for creating layouts and making different panes different colors, e.g. somePane.setBackground(getBackgroundColor(Color.RED))
    private Background getBackgroundColor(Color c) {
        BackgroundFill backgroundFill = new BackgroundFill(c, new CornerRadii(10), new Insets(10) );
        return new Background(backgroundFill);
    }

    private void setupArtStyleBox() {
        styleSelectBox = new ChoiceBox<>();
        styleSelectBox.getItems().addAll("Realistic", "Voxel", "2.5D Cartoon", "Japanese Anime", "Cartoon Line Art", "Realistic Hand-drawn", "2.5D Hand-drawn", "Oriental Comic Ink");
        styleSelectBox.setValue("Realistic");
    }

    public ArrayList<Button> generateModelLibraryButtons() {
        ArrayList<Button> modelButtons = new ArrayList<>();

        for (int i = 0; i<models.size(); i++) {
            Button btn = new Button("Model " + (i+1));
            btn.setMinSize(100, 100);

            int modelIdx = i;

            btn.setOnAction((ActionEvent event) -> {
                if (curModel != -1) {
                    modelButtons.get(curModel).setDisable(false);
                }

                if (curModel == modelIdx) {
                    curModel = -1;
                } else {
                    curModel = modelIdx;
                    modelButtons.get(curModel).setDisable(true);
                }
            });

            modelButtons.add(btn);
        }

        return modelButtons;
    }

    public String getArtStyle() {
        Map<Integer, String> artStyleValues = Map.of(
            0, "realistic",
            1, "voxel",
            2, "fake-3d-cartoon",
            3, "japanese-anime",
            4, "cartoon-line-art",
            5, "realistic-hand-drawn",
            6, "fake-3d-hand-drawn",
            7, "oriental-comic-ink"
        );

        return artStyleValues.get(curStyleSelection);
    }

    public void setArtStyle(String style) {
        this.styleSelectBox.setValue(style);
    }

    public String getObjectUrl() {
        return HelloFX.self.objectUrl;
    }

    public void setObjectUrl(String objectUrl) {
        HelloFX.self.objectUrl = objectUrl;
    }

    public String getObjectDescription() {
        return HelloFX.self.objectPromptInput.getText();
    }

    public String getTextureDescription() {
        return HelloFX.self.texturePromptInput.getText();
    }

    public void showProgressMinimized(boolean showProgress) {
        if (showProgress) {
            progressStage.show();
            stage.setIconified(true);
        } else {
            stage.setIconified(false);
            progressStage.hide();
        }
    }

    public void logModelPrompt(String objectPrompt) {
        Path path = Paths.get("playtestLog.txt");

        try (BufferedWriter writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8, StandardOpenOption.APPEND, StandardOpenOption.CREATE)) {
            Date curTime = new Date();

            String day = new SimpleDateFormat("MM/dd/yyyy").format(curTime);
            String time = new SimpleDateFormat("HH:mm").format(curTime);

            String header = System.getProperty("line.separator") +  "Playtest started on " + day + " at " + time + System.getProperty("line.separator") + System.getProperty("line.separator");
            String label = "Generating new object..." + System.getProperty("line.separator");
            String objectPromptStr = "Prompt: " + objectPrompt + System.getProperty("line.separator");

            writer.write(header);
            writer.write(label);
            writer.write(objectPromptStr);
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    public void logTexturePrompt(String objectPrompt, String texturePrompt) {
        Path path = Paths.get("playtestLog.txt");

        try (BufferedWriter writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8, StandardOpenOption.APPEND, StandardOpenOption.CREATE)) {
            Date curTime = new Date();

            String day = new SimpleDateFormat("MM/dd/yyyy").format(curTime);
            String time = new SimpleDateFormat("HH:mm").format(curTime);

            String header = System.getProperty("line.separator") +  "Playtest started on " + day + " at " + time + System.getProperty("line.separator") + System.getProperty("line.separator");
            String label = "Generating texture for existing object..." + System.getProperty("line.separator");
            String objectPromptStr = "Object prompt: " + objectPrompt + System.getProperty("line.separator");
            String texturePromptStr = "Texture prompt: " + texturePrompt + System.getProperty("line.separator");

            writer.write(header);
            writer.write(label);
            writer.write(objectPromptStr);
            writer.write(texturePromptStr);
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    private void addButtonActions() {
        cancelGeneration.setOnAction((ActionEvent event) -> {
            showProgressMinimized(false);

            // TODO: Add api call to cancel
         });

        uploadBtn.setOnAction((ActionEvent event) -> {
            System.out.println("Creating an awesome FTP connection");

            FileChooser filePicker = new FileChooser();

            File file = filePicker.showOpenDialog(self.stage);

            if (file != null) {
                System.out.println(file.getAbsolutePath());

                FileUploader uploader = new FileUploader("app.etc.cmu.edu", 15219);

                try {
                    uploader.connect(Data.getUsername(), Data.getPassword());

                    System.out.println("Connection established, uploading file...");

                    uploader.uploadFile(file.getAbsolutePath(), "/srv/www/html/ai-wonderland/");

                    String webUrl = "http://app.etc.cmu.edu/ai-wonderland/" + file.getName();

                    System.out.println(webUrl);

                    setObjectUrl(webUrl);
                    textureBtn.setDisable(false);
                } catch (JSchException | SftpException ex) {
                    System.out.println("ERROR UPLOADING FILER");
                    ex.printStackTrace();
                }

                uploader.disconnect();
            }

            System.out.println("DONE");
        });

        prevBtn.setOnAction((ActionEvent event) -> {
            SceneManager.getInstance().setActiveScene(1);
        });

        nextBtn.setOnAction((ActionEvent event) -> {
            SceneManager.getInstance().setActiveScene(2);
        });

        randomizeBtn.setOnAction((ActionEvent actionEvent) -> {
            int i = (int) (Math.random() * promptReader.objectDataList.size());
            objectPromptInput.setText(promptReader.objectDataList.get(i).getObjectDescription());
            texturePromptInput.setText(promptReader.objectDataList.get(i).getTextureDescription());
            //negativePromptInput.setText(promptReader.objectDataList.get(i).getNegativePrompt());
        });

        for (int i = 0; i < ART_STYLE_COUNT; i++) {
            int finalI = i;
            artStyleButtons[i].setOnAction((ActionEvent actionEvent) -> {
                artStyleButtons[curStyleSelection].setDisable(false);
                curStyleSelection = finalI;
                artStyleButtons[finalI].setDisable(true);
            });
        }
    }

}
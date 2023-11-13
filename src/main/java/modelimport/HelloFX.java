package modelimport;

import com.jcraft.jsch.*;

import py4j.GatewayServer;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.*;
import javafx.geometry.*;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.awt.Desktop;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.text.SimpleDateFormat;
import java.util.*;

public class HelloFX extends Application {

    static HelloFX self;

    Stage stage;

    private Scene sceneOne;
    private Scene sceneTwo;

    String message;

    Process curProcess;
    ProcessHandle processHandle;

    GatewayServer gatewayServer;

    Thread bgThread;
    Task<Void> pythonTask;

    // UI Elements

    private TextField objectPromptInput;
    private TextField texturePromptInput;
    private TextField negativePromptInput;

    private Text status;
    private Text progressStatus;
    private Text elapsedTime;

    private ProgressBar progress;

    private Button randomizeBtn;

    private Button modelBtn;
    private Button textureBtn;
    private Button uploadBtn;
    private Button prevBtn;
    private Button nextBtn;

    private Timer waitTimer;
    private TimerTask waitTask;

    int elapsedSec;
    boolean isTimerRunning;

    private String objectUrl;

    // Temp solution
    private ChoiceBox<String> styleSelectBox;

    private static PromptIO promptReader;

    public static void main(String[] args) {
        promptReader = new PromptIO();
        launch();
    }

    @Override
    public void start(Stage stage) {
        HelloFX.self = this;

        this.stage = stage;

        curProcess = null;
        processHandle = null;

        bgThread = null;
        pythonTask = null;

        objectUrl = "";

        String javaVersion = System.getProperty("java.version");
        String javafxVersion = System.getProperty("javafx.version");

        System.out.println("Hello, JavaFX " + javafxVersion + ", running on Java " + javaVersion + ".");

        initSceneOne();
        initSceneTwo();

        isTimerRunning = false;
        initTimer();

        stage.setTitle("AI Wonderland");

        addButtonActions();

        stage.setScene(sceneOne);
        stage.show();

        gatewayServer = new GatewayServer(new HelloFX());
        gatewayServer.start();
        System.out.println("Gateway Server Started");
    }

    @Override
    public void stop() {
        System.out.println("Stopping application");

        stopTimer();

        gatewayServer.shutdown();

        if (pythonTask != null) {
            // TODO: Find a better way to force the thread to stop
            bgThread.stop();

            while (bgThread.isAlive()) {
                try {
                    Thread.sleep(50);
                } catch(InterruptedException e) {
                    System.out.println("NIGHTMARE");
                }
            }
        }

        Platform.exit();
    }

    private void initSceneOne() {
        GridPane grid = new GridPane();
        grid.setAlignment(Pos.CENTER);
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(25, 25, 25, 25));

        sceneOne = new Scene(grid, 800, 600);

        Font uiFont = Font.font("Tahoma", FontWeight.NORMAL, 20);

        Text scenetitle = new Text("Welcome");
        scenetitle.setFont(uiFont);
        grid.add(scenetitle, 0, 0, 5, 1);

        Label objectPromptDescription = new Label("Model Description:");
        grid.add(objectPromptDescription, 0, 1);

        randomizeBtn = new Button("Create a random prompt");
        grid.add(randomizeBtn, 2, 1);

        objectPromptInput = new TextField();
        grid.add(objectPromptInput, 1, 1);

        Label texturePromptDescription = new Label("Texture Description:");
        grid.add(texturePromptDescription, 0, 2);

        // Test for art style add
        setupArtStyleBox();
        grid.add(styleSelectBox, 0, 3);

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

        Button prevBtn = new Button("Prev");
        prevBtn.setVisible(false);;
        grid.add(prevBtn, 0, 25, 2, 1);

        nextBtn = new Button("Next");
        grid.add(nextBtn, 10, 25, 2, 1);
    }

    private void initSceneTwo() {
        GridPane grid = new GridPane();
        grid.setAlignment(Pos.BOTTOM_CENTER);
        grid.setHgap(20);
        grid.setVgap(20);
        grid.setPadding(new Insets(25, 25, 25, 25));

        sceneTwo = new Scene(grid, 800, 600);

        Font uiFont = Font.font("Tahoma", FontWeight.NORMAL, 20);

        Text scenetitle = new Text("Scene Two");
        scenetitle.setFont(uiFont);
        grid.add(scenetitle, 0, 0, 5, 1);

        prevBtn = new Button("Prev");
        grid.add(prevBtn, 0, 25, 2, 1);

        Button nextBtn = new Button("Next");
        nextBtn.setVisible(false);;
        grid.add(nextBtn, 10, 25, 2, 1);
    }

    private void initTimer() {
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

    private void setupArtStyleBox(){
        styleSelectBox = new ChoiceBox<>();
        styleSelectBox.getItems().addAll("Realistic", "Voxel", "2.5D Cartoon", "Japanese Anime", "Cartoon Line Art", "Realistic Hand-drawn", "2.5D Hand-drawn", "Oriental Comic Ink");
        styleSelectBox.setValue("Realistic");
    }

    public String getApiKey() {
        return "msy_U6ZnHB25nPUMWH8840PSylxRKIJrw2gEydQM";
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

    public String getArtStyle() {
        String artStyleSelection = HelloFX.self.styleSelectBox.getValue();

        Map<String, String> artStyleValues = Map.of(
            "Realistic", "realistic",
            "Voxel", "voxel",
            "2.5D Cartoon", "fake-3d-cartoon",
            "Japanese Anime", "japanese-anime",
            "Cartoon Line Art", "cartoon-line-art",
            "Realistic Hand-drawn", "realistic-hand-drawn",
            "2.5D Hand-drawn", "fake-3d-hand-drawn",
            "Oriental Comic Ink", "oriental-comic-ink"
        );

        return artStyleValues.get(artStyleSelection);
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

    private void logPrompt(String objectPrompt) {
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

    private void logTexturePrompt(String objectPrompt, String texturePrompt) {
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

        modelBtn.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent event) {
                message = "Generating 3D model ... (may take about a minute)";

                status.setText(message);

                try {
                    pythonTask = new Task<Void>() {
                        @Override
                        protected Void call() throws Exception {
                            System.out.println("Starting python process...");

                            String objectPrompt = objectPromptInput.getText();
                            logPrompt(objectPrompt);

                            try {
                                startTimer();

                                //int exitCode = invokeScript("python", new File("generate_model_shap-e.py").getAbsolutePath(), objectPrompt);
                                int exitCode = invokeScript("python", new File("generate_model_meshy.py").getAbsolutePath(), objectPrompt);

                                stopTimer();

                                if (exitCode == 0) {
                                    message = "Model generated successfully";
                                } else {
                                    message = "Error geenerating model: " + exitCode;
                                }

                                System.out.println("GLB model generation finished. Converting to DAE now...");

                                exitCode = invokeScript("blender", "--background", "--python", "model/format.py");

                                if (exitCode == 0) {
                                    message = "Model converted successfully";
                                } else {
                                    message = "Error  model: " + exitCode;
                                }

                                System.out.println("Conversion to DAE finished. Showing model now...");

                                showModel("model.glb");

                                // enable regenerating textures for the current model
                                textureBtn.setDisable(false);

                                resetProgress();
                            } catch(InterruptedException | IOException e) {
                                System.out.println("Error generating model");
                                e.printStackTrace();

                                System.exit(0);
                            }

                            return null;
                        }
                    };

                    pythonTask.setOnSucceeded(ev -> {
                        System.out.println(message);
                        status.setText(message);
                    });

                    bgThread = new Thread(pythonTask);
                    bgThread.setDaemon(true);
                    bgThread.start();
                } catch(Exception e) {
                    System.out.println("ERROR BERRPR");
                    e.printStackTrace();
                }
            }
        });

        textureBtn.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent event) {
                message = "Generating a new texture for the model ... (may take about a minute)";

                status.setText(message);

                try {
                    pythonTask = new Task<Void>() {
                        @Override
                        protected Void call() throws Exception {
                            System.out.println("Starting python process...");

                            String objectPrompt = getObjectDescription();
                            String texturePrompt = getTextureDescription();

                            System.out.println(texturePrompt);

                            logTexturePrompt(objectPrompt, texturePrompt);

                            try {
                                startTimer();

                                int exitCode = invokeScript("python", new File("generate_texture_meshy.py").getAbsolutePath(), texturePrompt);

                                stopTimer();

                                if (exitCode == 0) {
                                    message = "Texture generated successfully";
                                } else {
                                    message = "Error geenerating texture: " + exitCode;
                                }

                                System.out.println("Texture generation complete");

                                showModel("model.glb");
                            } catch(InterruptedException | IOException e) {
                                System.out.println("Error generating model");
                                e.printStackTrace();

                                System.exit(0);
                            }

                            return null;
                        }
                    };

                    pythonTask.setOnSucceeded(ev -> {
                        System.out.println(message);
                        status.setText(message);
                    });

                    bgThread = new Thread(pythonTask);
                    bgThread.setDaemon(true);
                    bgThread.start();
                } catch(Exception e) {
                    System.out.println("ERROR GENERATING TEXTURE");
                    e.printStackTrace();
                }
            }
        });

        uploadBtn.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent event) {
                System.out.println("Creating an awesome FTP connection");

                FileChooser filePicker = new FileChooser();

                File file = filePicker.showOpenDialog(self.stage);
 
                if (file != null) {
                    System.out.println(file.getAbsolutePath());

                    FileUploader uploader = new FileUploader("app.etc.cmu.edu", 15219);

                    try {
                        uploader.connect("username", "password");

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
            }
        });

        prevBtn.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                stage.setScene(sceneOne);
            }
        });

        nextBtn.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                stage.setScene(sceneTwo);
            }
        });

        randomizeBtn.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                int i = (int) (Math.random() * promptReader.objectDataList.size());
                objectPromptInput.setText(promptReader.objectDataList.get(i).getObjectDescription());
                texturePromptInput.setText(promptReader.objectDataList.get(i).getTextureDescription());
                //negativePromptInput.setText(promptReader.objectDataList.get(i).getNegativePrompt());
            }
        });
    }

    private int invokeScript(String... cliArgs) throws InterruptedException, IOException {
        System.out.println("CLI command: " + String.join(" ", cliArgs));

        Process process = new ProcessBuilder(cliArgs).inheritIO().start();

        processHandle = process.toHandle();

        int exitCode = process.waitFor();

        System.out.println("Process finished running");

        processHandle.destroyForcibly();

        return exitCode;
    }

    private void showModel(String modelName) {
        System.out.println("Showing 3D model: " + modelName);

        try {
            Desktop.getDesktop().open(new File("gen-model/" + modelName));
        } catch(IOException ioe) {
            System.out.println("Error showing model");
            ioe.printStackTrace();
        }
    }

    private void resetProgress() {
        Platform.runLater(() -> {
            progressStatus.setVisible(false);
            progressStatus.setText("");

            progress.setVisible(false);
            progress.setProgress(0f);
        });
    }

    private void startTimer() {
        if (!isTimerRunning) {
            elapsedSec = 0;
            waitTimer.scheduleAtFixedRate(waitTask, 0, 1000);
            isTimerRunning = true;
        }
    }

    private void stopTimer() {
        if (isTimerRunning) {
            waitTask.cancel();
            Platform.runLater(() -> elapsedTime.setText(""));
            isTimerRunning = false;
        }
        waitTimer.cancel();
    }

}
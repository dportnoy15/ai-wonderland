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
import java.util.Date;
import java.util.concurrent.atomic.AtomicReference;

public class HelloFX extends Application {

    static HelloFX self;

    Stage stage;
    Scene scene;

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

    private Label status;
    private Label progressStatus;

    private ProgressBar progress;

    private Button modelBtn;
    private Button textureBtn;
    private Button newBtn;

    private String objectUrl;


    // Temp solution
    private ChoiceBox<String> styleSelectBox;

    public static void main(String[] args) {
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

        Label l = new Label("Hello, JavaFX " + javafxVersion + ", running on Java " + javaVersion + ".");

        GridPane grid = new GridPane();
        grid.setAlignment(Pos.CENTER);
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(25, 25, 25, 25));

        Scene scene = new Scene(grid, 640, 480);
        
        Text scenetitle = new Text("Welcome");
        scenetitle.setFont(Font.font("Tahoma", FontWeight.NORMAL, 20));
        grid.add(scenetitle, 0, 0, 2, 1);

        Label objectPromptDescription = new Label("Model Description:");
        grid.add(objectPromptDescription, 0, 1);

        objectPromptInput = new TextField();
        grid.add(objectPromptInput, 1, 1);

        Label texturePromptDescription = new Label("Texture Description:");
        grid.add(texturePromptDescription, 0, 2);

        texturePromptInput = new TextField();
        grid.add(texturePromptInput, 1, 2);

        // Test for art style add
        SetupArtStyleBox();
        grid.add(styleSelectBox, 0, 3);

        /*
        Label negativePromptDescription = new Label("Negative Prompt:");
        grid.add(negativePromptDescription, 0, 3);

        negativePromptInput = new TextField();
        grid.add(negativePromptInput, 1, 3);
        */

        status = new Label("");
        grid.add(status, 1, 5);

        progressStatus = new Label("");
        grid.add(progressStatus, 0, 7);
        progressStatus.setVisible(false);

        Label progressDescription = new Label("Generation Progress:");
        grid.add(progressDescription, 0, 8);

        progress = new ProgressBar();
        progress.setProgress(0.0F);
        progress.setVisible(false);

        grid.add(progress, 1, 8);

        modelBtn = new Button("Generate Model");
        textureBtn = new Button("Regenerate Texture");
        newBtn = new Button("Upload Model");

        textureBtn.setDisable(true);

        addButtonActions();

        HBox hbBtn = new HBox(10);
        hbBtn.setAlignment(Pos.BOTTOM_LEFT);
        hbBtn.getChildren().add(modelBtn);
        hbBtn.getChildren().add(textureBtn);
        hbBtn.getChildren().add(newBtn);
        grid.add(hbBtn, 1, 12);

        stage.setTitle("AI Wonderland");
        stage.setScene(scene);

        stage.show();

        gatewayServer = new GatewayServer(new HelloFX());
        gatewayServer.start();
        System.out.println("Gateway Server Started");
    }

    private void SetupArtStyleBox(){
        styleSelectBox = new ChoiceBox<>();
        styleSelectBox.getItems().addAll("Realistic", "Voxel", "2.5D Cartoon", "Japanese Anime", "Cartoon Line Art", "Realistic Hand-drawn", "2.5D Hand-drawn", "Oriental Comic Ink");
        styleSelectBox.setValue("Realistic");
    }

    @Override
    public void stop() {
        gatewayServer.shutdown();

        if (pythonTask != null) {
            bgThread.stop();

            while (bgThread.isAlive()) {
                try {
                    Thread.sleep(50);
                } catch(InterruptedException e) {
                    System.out.println("NIGHTMARE");
                }
            }
        }
    }

    public void setProgress(String status, int percent) {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                System.out.println("Status set to " + status + ", percent set to " + percent + " from Python!!!");

                HelloFX.self.progressStatus.setVisible(true);
                HelloFX.self.progressStatus.setText(status);

                HelloFX.self.progress.setVisible(true);
                HelloFX.self.progress.setProgress(percent / 100f);
            }
        });
    }

    public String getArtStyle() {
        AtomicReference<String> returnVal = new AtomicReference<>();;
        Platform.runLater(() -> {
            System.out.println(styleSelectBox.getValue());
            returnVal.set(styleSelectBox.getValue());
        });
        return returnVal.get();
    }

    public void setArtStyle(String style) {this.styleSelectBox.setValue(style);}

    public String getObjectUrl() {
        return objectUrl;
    }

    public void setObjectUrl(String objectUrl) {
        this.objectUrl = objectUrl;
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

        modelBtn.setOnAction(new EventHandler() {

            @Override
            public void handle(Event event) {
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
                                //int exitCode = invokeScript("python", new File("generate_model_shap-e.py").getAbsolutePath(), objectPrompt);
                                int exitCode = invokeScript("python", new File("generate_model_meshy.py").getAbsolutePath(), objectPrompt);

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

        textureBtn.setOnAction(new EventHandler() {

            @Override
            public void handle(Event event) {
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
                                int exitCode = invokeScript("python", new File("generate_texture_meshy.py").getAbsolutePath(), texturePrompt);

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

        newBtn.setOnAction(new EventHandler() {

            @Override
            public void handle(Event event) {
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
                    } catch (JSchException | SftpException ex) {
                        System.out.println("ERROR UPLOADING FILER");
                        ex.printStackTrace();
                    }

                    uploader.disconnect();
                }

                System.out.println("DONE");
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
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                progressStatus.setVisible(false);
                progressStatus.setText("");

                progress.setVisible(false);
                progress.setProgress(0f);
            }
        });
    }

}
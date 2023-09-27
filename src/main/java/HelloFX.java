import javafx.application.Application;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.*;
import javafx.geometry.*;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.*;
import javafx.stage.Stage;

import java.awt.Desktop;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class HelloFX extends Application {

    Scene scene;
    Label status;

    String message;

    Process curProcess;
    ProcessHandle processHandle;

    Thread bgThread;
    Task<Void> pythonTask;

    public static void main(String[] args) {
        launch();
    }

    @Override
    public void start(Stage stage) {
        curProcess = null;
        processHandle = null;

        bgThread = null;
        pythonTask = null;

        String javaVersion = System.getProperty("java.version");
        String javafxVersion = System.getProperty("javafx.version");

        message = "Generating 3D model... (may take about a minute)";

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

        Label description = new Label("Model Description:");
        grid.add(description, 0, 1);

        TextField descriptionInput = new TextField();
        grid.add(descriptionInput, 1, 1);

        status = new Label("");
        grid.add(status, 1, 5);

        /*
        Label pw = new Label("Password:");
        grid.add(pw, 0, 2);

        PasswordField pwBox = new PasswordField();
        grid.add(pwBox, 1, 2);
        */

        Button btn = new Button("Generate Model");
        btn.setOnAction(new EventHandler() {
        
            @Override
            public void handle(Event event) {
                status.setText(message);

                try {
                    pythonTask = new Task<Void>() {
                        @Override
                        protected Void call() throws Exception {
                            System.out.println("Starting python process...");

                            String description = descriptionInput.getText();

                            logPrompt(description);

                            try {
                                int exitCode = invokeScript("python", new File("generate-model.py").getAbsolutePath(), description);

                                if (exitCode == 0) {
                                    message = "Model generated successfully";
                                } else {
                                    message = "Error geenerating model: " + exitCode;
                                }

                                System.out.println("3D model generation finished. Showing model now...");

                                showModel("3d_model.glb");
                            } catch(Exception e) {
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

        HBox hbBtn = new HBox(10);
        hbBtn.setAlignment(Pos.BOTTOM_LEFT);
        hbBtn.getChildren().add(btn);
        grid.add(hbBtn, 1, 12);

        stage.setTitle("AI Wonderland");
        stage.setScene(scene);

        stage.show();
    }

    @Override
    public void stop() {
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

    private void logPrompt(String userPrompt) {
        Path path = Paths.get("playtestLog.txt");

        try (BufferedWriter writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8, StandardOpenOption.APPEND, StandardOpenOption.CREATE)) {
            Date curTime = new Date();

            String day = new SimpleDateFormat("MM/dd/yyyy").format(curTime);
            String time = new SimpleDateFormat("HH:mm").format(curTime);

            String header = System.getProperty("line.separator") +  "Playtest started on " + day + " at " + time + System.getProperty("line.separator") + System.getProperty("line.separator");
            String prompt = "Prompt: " + userPrompt + System.getProperty("line.separator");;

            writer.write(header);
            writer.write(prompt);
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    private int invokeScript(String... cliArgs) throws Exception {
        System.out.println("CLI command: " + cliArgs);

        Process process = new ProcessBuilder().inheritIO().command(cliArgs).start();

        processHandle = process.toHandle();

        int exitCode = process.waitFor();

        System.out.println("Process finished running");

        //process.destroy();
        processHandle.destroyForcibly();

        return exitCode;
    }

    private void showModel(String modelName) {
        System.out.println("Showing 3D model: " + modelName);

        try {
            Desktop.getDesktop().open(new File(modelName));
        } catch(IOException ioe) {
            System.out.println("Error showing model");
            ioe.printStackTrace();
        }
    }

}
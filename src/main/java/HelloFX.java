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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class HelloFX extends Application {

    Scene scene;
    Label status;

    String message;

    public static void main(String[] args) {
        launch();
    }

    @Override
    public void start(Stage stage) {
        String javaVersion = System.getProperty("java.version");
        String javafxVersion = System.getProperty("javafx.version");

        Label l = new Label("Hello, JavaFX " + javafxVersion + ", running on Java " + javaVersion + ".");

        /**********************************************/

        GridPane grid = new GridPane();
        grid.setAlignment(Pos.CENTER);
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(25, 25, 25, 25));

        Scene scene = new Scene(grid, 640, 480);

        /**********************************************/
        
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
                status.setText("Generating 3D model... (may take about a minute)");

                // hacky way to force UI referesh
                //scene.getWindow().setWidth(scene.getWidth() + 0.001);

                 Task<Void> aiWorker = new Task<Void>() {
                    @Override
                    protected Void call() throws Exception {
                        logPrompt(descriptionInput.getText());

                        try {
                            invokePythonToGenerateModel(descriptionInput.getText());       
                        } catch(Exception e) {
                            e.printStackTrace();
                        }
                        return null;
                    }
                };

                aiWorker.setOnSucceeded(ev -> {
                    System.out.println(message);
                    status.setText(message);
                });

                new Thread(aiWorker).start();
            }
        });

        HBox hbBtn = new HBox(10);
        hbBtn.setAlignment(Pos.BOTTOM_LEFT);
        hbBtn.getChildren().add(btn);
        grid.add(hbBtn, 1, 12);

        /**********************************************/

        stage.setTitle("AI Wonderland");
        stage.setScene(scene);

        stage.show();
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

    private void invokePythonToGenerateModel(String description) throws Exception {
        System.out.println("Genereating a 3D model based on the following description:");
        System.out.println(description);

        ProcessBuilder processBuilder = new ProcessBuilder("python", new File("generate-model.py").getAbsolutePath(), description);
        processBuilder.redirectErrorStream(true);

        Process process = processBuilder.start();

        int exitCode = process.waitFor();

        System.out.println("Process finished running");

        BufferedReader bri = new BufferedReader(new InputStreamReader(process.getInputStream()));
        BufferedReader bre = new BufferedReader(new InputStreamReader(process.getErrorStream()));

        String line;

        /*
        while ((line = bri.readLine()) != null) {
            System.out.println("Output: " + line);
        }
        */
        bri.close();

        System.out.println();

        while ((line = bre.readLine()) != null) {
            System.out.println(" Error: " + line);
        }
        bre.close();

        System.out.println();

        if (exitCode == 0) {
            message = "Model generated successfully";
        } else {
            message = "Error geenerating model: " + exitCode;
        }

        process.destroy();
    }

}
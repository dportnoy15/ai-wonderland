package modelimport.scene;

import java.io.File;
import java.io.IOException;

import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.stage.Stage;

import modelimport.AliceModel;
import modelimport.HelloFX;
import modelimport.Utils;

public class GenerateModelScene extends AliceScene {

    protected String message;

    protected Thread bgThread;
    protected Task<Void> pythonTask;
    
    public GenerateModelScene(Stage stage, Scene scene, HelloFX app) {
        super(stage, scene, app);

        message = "";
        bgThread = null;
        pythonTask = null;

        app.initTimer();
    }

    public void registerButtonActions(Button btnModel, Button btnTexture) {
        btnModel.setOnAction((ActionEvent event) -> {
            message = "Generating 3D model ...";

            app.setStatusText(message);

            try {
                pythonTask = new Task<Void>() {
                    @Override
                    protected Void call() throws Exception {
                        System.out.println("Starting python process...");

                        String objectPrompt = app.getObjectDescription();
                        app.logModelPrompt(objectPrompt);

                        try {
                            app.startTimer();

                            //int exitCode = Utils.invokeScript("python", new File("generate_model_shap-e.py").getAbsolutePath(), objectPrompt);
                            int exitCode = Utils.invokeScript("python", new File("generate_model_meshy.py").getAbsolutePath(), objectPrompt);

                            app.stopTimer();

                            if (exitCode == 0) {
                                message = "Model generated successfully";
                            } else {
                                message = "Error geenerating model: " + exitCode;
                            }

                            System.out.println("GLB model generation finished. Converting to DAE now...");

                            exitCode = Utils.invokeScript("blender", "--background", "--python", "model/format.py");

                            if (exitCode == 0) {
                                message = "Model converted successfully";
                            } else {
                                message = "Error  model: " + exitCode;
                            }

                            System.out.println("Conversion to DAE finished. Showing model now...");

                            // the object url should have been set from the Python script
                            app.addModelToLibrary(new AliceModel("some name", app.getObjectUrl()));

                            app.showModel("model.glb");

                            // enable regenerating textures for the current model
                            btnTexture.setDisable(false);

                            app.resetProgress();
                        } catch(InterruptedException | IOException e) {
                            System.out.println("Error generating model");
                            e.printStackTrace();

                            System.exit(0);
                        }

                        return null;
                    }
                };

                pythonTask.setOnSucceeded(ev -> {
                    app.setStatusText(message);
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

            app.setStatusText(message);

            try {
                pythonTask = new Task<Void>() {
                    @Override
                    protected Void call() throws Exception {
                        System.out.println("Starting python process...");

                        String objectPrompt = app.getObjectDescription();
                        String texturePrompt = app.getTextureDescription();

                        System.out.println(texturePrompt);

                        app.logTexturePrompt(objectPrompt, texturePrompt);

                        try {
                            app.startTimer();

                            int exitCode = Utils.invokeScript("python", new File("generate_texture_meshy.py").getAbsolutePath(), texturePrompt);

                            app.stopTimer();

                            if (exitCode == 0) {
                                message = "Texture generated successfully";
                            } else {
                                message = "Error geenerating texture: " + exitCode;
                            }

                            System.out.println("Texture generation complete");

                            app.showModel("model.glb");

                            // TODO: Show progress for texture generation as well
                        } catch(InterruptedException | IOException e) {
                            System.out.println("Error generating model");
                            e.printStackTrace();

                            System.exit(0);
                        }

                        return null;
                    }
                };

                pythonTask.setOnSucceeded(ev -> {
                    app.setStatusText(message);
                });

                bgThread = new Thread(pythonTask);
                bgThread.setDaemon(true);
                bgThread.start();
            } catch(Exception e) {
                System.out.println("ERROR GENERATING TEXTURE");
                e.printStackTrace();
            }
        });
    }

    public void stopTask() {
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
    }
}

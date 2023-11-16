package modelimport.scene;

import java.io.*;

import javafx.event.*;
import javafx.scene.control.*;
import javafx.scene.Scene;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import com.jcraft.jsch.*;

import modelimport.AliceModel;
import modelimport.FileUploader;
import modelimport.HelloFX;
import modelimport.SceneManager;

public class SelectModelScene extends AliceScene {

    public SelectModelScene(Stage stage, Scene scene, HelloFX app) {
        super(stage, scene, app);
    }

    public void registerButtonActions(Button btnGenerateModel, Button btnUploadModel) {
        btnGenerateModel.setOnAction((ActionEvent event) -> {
            System.out.println("Generating a model...");

            SceneManager.getInstance().setScene(1);
        });

        btnUploadModel.setOnAction((ActionEvent event) -> {
            System.out.println("Uploading a model...");

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

                    AliceModel model = new AliceModel("some name", webUrl);

                    app.addModelToLibrary(model);
                    app.refreshModelLibrary();

                    //setObjectUrl(webUrl);
                    //textureBtn.setDisable(false); // outdated
                } catch (JSchException | SftpException ex) {
                    System.out.println("ERROR UPLOADING FILE");
                    ex.printStackTrace();
                }

                uploader.disconnect();
            }

            System.out.println("DONE");
        });
    }
}
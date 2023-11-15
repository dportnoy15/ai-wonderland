package modelimport.scene;

import javafx.event.*;
import javafx.scene.control.*;

import modelimport.SceneManager;

public class SelectModelScene {
    public static void registerButtonActions(Button btnGenerateModel, Button btnUploadModel) {
        btnGenerateModel.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent event) {
                System.out.println("Generating a model...");

                SceneManager.getInstance().setScene(1);
            }
        });

        btnUploadModel.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent event) {
                System.out.println("Uploading a model...");

                /*
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
                */
            }
        });
    }
}
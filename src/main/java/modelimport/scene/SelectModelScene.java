package modelimport.scene;

import java.io.*;
import java.util.ArrayList;

import javafx.event.*;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.Scene;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import com.jcraft.jsch.*;

import modelimport.AliceModel;
import modelimport.FileUploader;
import modelimport.HelloFX;
import modelimport.SceneManager;
import modelimport.Utils;

public class SelectModelScene extends AliceScene {

    private HBox libraryPane;

    private int curModel;

    public SelectModelScene(Stage stage, Scene scene, HelloFX app) {
        super(stage, scene, app);

        libraryPane = null;
        curModel = -1;
    }

    public void initLayout() {
        BorderPane layout = (BorderPane) getScene().getRoot();

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

        registerButtonActions(btnGenerateModel, btnUploadModel);
    }

    public void registerButtonActions(Button btnGenerateModel, Button btnUploadModel) {
        btnGenerateModel.setOnAction((ActionEvent event) -> {
            System.out.println("Generating a model...");

            SceneManager.getInstance().setActiveScene(2);
        });

        btnUploadModel.setOnAction((ActionEvent event) -> {
            System.out.println("Uploading a model...");

            FileChooser filePicker = new FileChooser();

            File file = filePicker.showOpenDialog(stage);

            if (file != null) {
                System.out.println(file.getAbsolutePath());

                FileUploader uploader = new FileUploader("app.etc.cmu.edu", 15219);

                try {
                    uploader.connect("username", "password");

                    System.out.println("Connection established, uploading file...");

                    uploader.uploadFile(file.getAbsolutePath(), "/srv/www/html/ai-wonderland/");

                    String webUrl = "http://app.etc.cmu.edu/ai-wonderland/" + file.getName();

                    System.out.println(webUrl);

                    AliceModel model = new AliceModel("some name", webUrl, "");

                    app.addModelToLibrary(model);
                    refreshModelLibrary();

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

    public void refreshModelLibrary() {
        libraryPane.getChildren().clear();
        libraryPane.getChildren().addAll(generateModelLibraryButtons());
    }

    public ArrayList<Button> generateModelLibraryButtons() {
        ArrayList<Button> modelButtons = new ArrayList<>();

        for (int i = 0; i<app.getModels().size(); i++) {
            AliceModel model = app.getModels().get(i);

            Button btn = new Button("Model " + (i+1));
            btn.setMinSize(100, 100);
            btn.setMaxSize(100, 100);

            String imageUrl = model.getThumbnailUrl();
            ImageView background = new ImageView(new Image(imageUrl));

            background.fitWidthProperty().bind(btn.widthProperty());
            background.fitHeightProperty().bind(btn.heightProperty());
            background.setPreserveRatio(true);

            btn.setGraphic(background);

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

                showAliceImportTool(model);
            });

            modelButtons.add(btn);
        }

        return modelButtons;
    }

    public void showAliceImportTool(AliceModel model) {
        try {
            System.out.println(Utils.invokeScript(new File("ImportModel/ImportModel.exe").getAbsolutePath(), new File(model.getLocalPath()).getAbsolutePath()));
        } catch(Exception e) {
            System.out.println("Could not open Alice model importer");
            e.printStackTrace();
        }
    }
}
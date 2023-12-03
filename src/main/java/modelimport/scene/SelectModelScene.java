package modelimport.scene;

import java.io.*;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.*;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.Scene;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import javafx.util.Duration;
import modelimport.AliceModel;
import modelimport.Data;
import modelimport.FileUploader;
import modelimport.HelloFX;
import modelimport.SceneManager;
import modelimport.Utils;

public class SelectModelScene extends AliceScene {

    private HBox libraryPane;
    private ListView<AliceModel> libraryView;
    private ImageView leftArrow, rightArrow;

    private Button btnImport, btnTexture;

    private int curModel;

    private ImageView randomizeGif;
    private ImageView uploadGif;

    public SelectModelScene(Stage stage, Scene scene, HelloFX app) {
        super(stage, scene, app);

        libraryPane = null;
        libraryView = null;
        leftArrow = null;
        rightArrow = null;

        curModel = 0;
    }

    public void initLayout() {
        BorderPane layout = (BorderPane) getScene().getRoot();

        HBox topPane = new HBox();
        VBox bottomPane = new VBox();
        Pane leftPane = new FlowPane();
        Pane rightPane = new FlowPane();
        GridPane centerPane = new GridPane();

        layout.setTop(topPane);    // Title
        layout.setBottom(bottomPane); // Nav Buttons
        layout.setLeft(leftPane);
        layout.setRight(rightPane);
        layout.setCenter(centerPane); // Main Content

        topPane.setPrefHeight(100);
        bottomPane.setPrefHeight(200);
        leftPane.setPrefWidth(0);
        rightPane.setPrefWidth(0);

        Font uiFont = Font.font("Tahoma", FontWeight.NORMAL, 20);

        Label scenetitle = new Label("AI Model Assistant");
        scenetitle.setFont(uiFont);
        topPane.setAlignment(Pos.CENTER);
        topPane.getChildren().add(scenetitle);

        /* Start screen customization */

        GridPane grid = centerPane;
        grid.setAlignment(Pos.CENTER);
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(25, 25, 25, 25));

        randomizeGif = new ImageView(new Image("file:src/main/pic/Generate_3D_Model_GIF3.gif"));
        //randomizeGif = new ImageView(new Image("file:src/main/pic/Generate.png"));
        randomizeGif.setFitWidth(130);
        randomizeGif.setFitHeight(130);
        Button btnGenerateModel = new Button();
        btnGenerateModel.setBackground(new Background(new javafx.scene.layout.BackgroundFill(
                Color.TRANSPARENT, // Border color
                new CornerRadii(3),
                null)));
        btnGenerateModel.setMinSize(200, 150);

        Label buttonText = new Label("Generate New Model");
        buttonText.setFont(Font.font(15));
        buttonText.setMinSize(150, 20);
        buttonText.setStyle("-fx-text-fill: #FFFFFF;");

        Background blkBg = new Background(new javafx.scene.layout.BackgroundFill(
                Color.BLACK, // Border color
                new CornerRadii(10),
                null));

        StackPane genModelStack = new StackPane();
        genModelStack.setBackground(blkBg);
        genModelStack.getChildren().addAll(randomizeGif, buttonText, btnGenerateModel);
        genModelStack.setAlignment(randomizeGif, Pos.TOP_CENTER);
        genModelStack.setAlignment(buttonText, Pos.BOTTOM_CENTER);
        genModelStack.setMinSize(200, 150);

        //uploadGif = new ImageView(new Image("file:src/main/pic/Upload.png"));
        uploadGif = new ImageView(new Image("file:src/main/pic/Upload_Model_GIF2.gif"));
        uploadGif.setFitWidth(130);
        uploadGif.setFitHeight(130);
        Button btnUploadModel = new Button();
        btnUploadModel.setBackground(new Background(new javafx.scene.layout.BackgroundFill(
                Color.TRANSPARENT, // Border color
                new CornerRadii(3),
                null)));
        btnUploadModel.setMinSize(200, 150);

        Label uploadText = new Label("Upload Local Model");
        uploadText.setFont(Font.font(15));
        uploadText.setMinSize(150, 20);
        uploadText.setStyle("-fx-text-fill: #FFFFFF;");

        StackPane uploadStack = new StackPane();
        uploadStack.setBackground(blkBg);
        uploadStack.getChildren().addAll(uploadGif, uploadText, btnUploadModel);
        uploadStack.setAlignment(uploadGif, Pos.TOP_CENTER);
        uploadStack.setAlignment(uploadText, Pos.BOTTOM_CENTER);
        uploadStack.setMinSize(200, 150);

        HBox hbBtn = new HBox(200);
        hbBtn.setAlignment(Pos.TOP_CENTER);
        hbBtn.getChildren().add(genModelStack);
        hbBtn.getChildren().add(uploadStack);
        grid.add(hbBtn, 1, 0, 5, 1);

        /* Start footer definition */
        bottomPane.setAlignment(Pos.CENTER);

        libraryPane = new HBox(20);
        HBox buttons = new HBox(20);

        libraryPane.setAlignment(Pos.CENTER);
        libraryPane.setPadding(new Insets(0, 15, 0, 15));
        libraryPane.setMinSize(600, 100);

        libraryView = new ListView<>();
        libraryView.setOrientation(Orientation.HORIZONTAL);
        HBox.setHgrow(libraryView, Priority.ALWAYS);

        libraryView.setCellFactory(param -> new ListCell<AliceModel>() {

            private VBox itemView = new VBox();
            private ImageView imageView = new ImageView();
            private TextField nameInput = new TextField();

            @Override
            public void updateItem(AliceModel model, boolean empty) {
                super.updateItem(model, empty);

                if (empty) {
                    setGraphic(null);
                } else {

                    if (model.isLocalModel()) {
                        // this is intended to point at a non-existent file
                        imageView.setImage(new Image("file:placeholder.txt"));
                    } else {
                        imageView.setImage(new Image(model.getThumbnailUrl()));
                    }

                    imageView.setFitHeight(100);
                    imageView.setFitWidth(100);

                    nameInput = new TextField();
                    nameInput.setPrefWidth(80);
                    nameInput.setPrefHeight(20);
                    nameInput.setAlignment(Pos.BASELINE_CENTER);
                    nameInput.setText(model.getName());

                    itemView = new VBox();
                    itemView.setPrefHeight(120);
                    itemView.setPrefWidth(100);
                    itemView.getChildren().addAll(imageView, nameInput);

                    nameInput.setOnAction(new EventHandler<ActionEvent>() {
                        @Override public void handle(ActionEvent e) {
                            model.setName(nameInput.getText());
                        }
                    });

                    setGraphic(itemView);
                }
            }
        });

        libraryView.setOnMouseClicked(new EventHandler<MouseEvent>() {

            @Override
            public void handle(MouseEvent event) {
                AliceModel selectedModel = libraryView.getSelectionModel().getSelectedItem();

                if (selectedModel == null) {
                    btnImport.setVisible(false);
                    btnTexture.setVisible(false);

                    return;
                }

                btnImport.setVisible(true);
                btnTexture.setVisible(true);

                app.setActiveModel(selectedModel);
            }
        });

        leftArrow = new ImageView(new Image("file:left-arrow.png", 50, 50, true, true));
        leftArrow.setOnMouseClicked((MouseEvent e) -> {
            if (curModel > 0) {
                curModel--;
            }

            libraryView.scrollTo(curModel);
        });

        rightArrow = new ImageView(new Image("file:right-arrow.png", 50, 50, true, true));
        rightArrow.setOnMouseClicked((MouseEvent e) -> {
            if (curModel < (app.getModels().size() - 1)) {
                curModel++;
            }

            libraryView.scrollTo(curModel);
        });

        libraryPane.getChildren().addAll(leftArrow, libraryView, rightArrow);

        buttons.setAlignment(Pos.CENTER);
        buttons.setPadding(new Insets(20, 50, 0, 50));

        btnImport = new Button("Import into Alice");
        btnTexture = new Button("Regenerate Texture");

        btnImport.setVisible(false);
        btnTexture.setVisible(false);

        buttons.getChildren().addAll(btnImport, btnTexture);

        bottomPane.getChildren().addAll(libraryPane, buttons);

        refreshModelLibrary();

        registerButtonActions(btnGenerateModel, btnUploadModel, btnImport, btnTexture);
    }

    private void playAndStopAnimation(int button, Duration stopTime) {
        Timeline timeline;

        if (button == 1) {
            randomizeGif.setImage(new Image("file:src/main/pic/Generate_3D_Model_GIF3.gif"));
            timeline = new Timeline(new KeyFrame(Duration.seconds(1), event -> {
                // Load the new image
                randomizeGif.setImage(new Image("file:src/main/pic/Generate.png"));
            }));
        } else {
            uploadGif.setImage(new Image("file:src/main/pic/Upload_Model_GIF2.gif"));
            timeline = new Timeline(new KeyFrame(Duration.seconds(1), event -> {
                // Load the new image
                uploadGif.setImage(new Image("file:src/main/pic/Upload.png"));
            }));
        }
        timeline.play();
    }

    public void registerButtonActions(Button btnGenerateModel, Button btnUploadModel, Button btnImport, Button btnTexture) {
        btnGenerateModel.setOnAction((ActionEvent event) -> {
            System.out.println("Generating a model...");

            SceneManager.getInstance().getScene(1).clearFields();
            SceneManager.getInstance().getScene(3).clearFields();
            ((ArtStyleScene) SceneManager.getInstance().getScene(2)).generateNewModel(true);
            ((TextureDescriptionScene) SceneManager.getInstance().getScene(3)).generateNewModel(true);
            SceneManager.getInstance().setActiveScene(1);
        });

        btnUploadModel.setOnAction((ActionEvent event) -> {
            System.out.println("Uploading a model...");

            FileChooser filePicker = new FileChooser();

            // limiting to these formats guarantees that the models can be used with Meshy
            FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("3D Model Files", "*.fbx", "*.obj", "*.stl", "*.gltf", "*.glb");
            filePicker.getExtensionFilters().add(extFilter);

            File file = filePicker.showOpenDialog(stage);

            if (file != null) {
                System.out.println(file.getAbsolutePath());

                FileUploader uploader = new FileUploader("app.etc.cmu.edu", 15219);

                try {
                    uploader.connect(Data.getUsername(), Data.getPassword());

                    System.out.println("Connection established, uploading file...");

                    uploader.uploadFile(file.getAbsolutePath(), "/srv/www/html/ai-wonderland/");

                    String webUrl = "http://app.etc.cmu.edu/ai-wonderland/" + file.getName();

                    System.out.println(webUrl);

                    int exitCode = Utils.invokeScript("blender", "--background", "--python", "model/format.py", "--", Utils.getFileExtension(file), file.getAbsolutePath());

                    if (exitCode == 0) {
                        //message = "Model converted successfully";
                    } else {
                        //message = "Error converting model: " + exitCode;
                        return;
                    }

                    AliceModel model = AliceModel.createFromLocalFile(app.getNewModelName(), webUrl);

                    app.copyModelFileToLibrary(model);

                    app.addModelToLibrary(model);
                    refreshModelLibrary();
                } catch (Exception ex) {
                    System.out.println("ERROR UPLOADING FILE");
                    ex.printStackTrace();
                }

                uploader.disconnect();
            }

            System.out.println("DONE");
        });

        btnImport.setOnAction((ActionEvent event) -> {
            app.showAliceImportTool();
        });

        btnTexture.setOnAction((ActionEvent event) -> {
            System.out.println("Regenerating the texture");

            SceneManager.getInstance().getScene(1).clearFields();
            SceneManager.getInstance().getScene(3).clearFields();
            ((ArtStyleScene) SceneManager.getInstance().getScene(2)).generateNewModel(false);
            ((TextureDescriptionScene) SceneManager.getInstance().getScene(3)).generateNewModel(false);
            SceneManager.getInstance().setActiveScene(1);
        });
    }

    public void refreshModelLibrary() {
        libraryView.getItems().clear();
        libraryView.getItems().addAll(app.getModels());
    }
}

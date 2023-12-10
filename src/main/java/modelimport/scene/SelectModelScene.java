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

    private static String DEFAULT_FONT_FAMILY = Font.getDefault().getFamily();

    private HBox libraryPane;
    private ListView<AliceModel> libraryView;
    private ImageView leftArrow, rightArrow;

    private VBox buttons;

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
        GridPane centerPane = new GridPane();

        layout.setTop(topPane);    // Title
        layout.setBottom(bottomPane); // Nav Buttons
        layout.setCenter(centerPane); // Main Content

        topPane.setPrefHeight(50);
        bottomPane.setPrefHeight(320);

        Font uiFont = Font.font("Tahoma", FontWeight.NORMAL, 20);
        topPane.setAlignment(Pos.CENTER);

        /* Start screen customization */

        GridPane grid = centerPane;
        grid.setAlignment(Pos.CENTER);
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(25, 25, 25, 25));

        randomizeGif = new ImageView(new Image("file:src/main/pic/Generate_3D_Model_GIF3.gif"));
        //randomizeGif = new ImageView(new Image("file:src/main/pic/Generate.png"));
        randomizeGif.setFitWidth(170);
        randomizeGif.setFitHeight(170);
        Button btnGenerateModel = new Button();
        btnGenerateModel.setBackground(Utils.getBackgroundColor(Color.TRANSPARENT, 3));
        btnGenerateModel.setMinSize(220, 200);

        Label buttonText = new Label("Generate 3D Models");
        buttonText.setFont(Font.font(20));
        buttonText.setMinSize(220, 30);
        buttonText.setTextFill(Color.WHITE);
        buttonText.setAlignment(Pos.CENTER);

        StackPane genModelStack = new StackPane();
        genModelStack.setBackground(Utils.getBackgroundColor(Color.BLACK));
        genModelStack.getChildren().addAll(randomizeGif, buttonText, btnGenerateModel);
        StackPane.setAlignment(randomizeGif, Pos.TOP_CENTER);
        StackPane.setAlignment(buttonText, Pos.BOTTOM_CENTER);
        genModelStack.setMinSize(220, 200);

        //uploadGif = new ImageView(new Image("file:src/main/pic/Upload.png"));
        uploadGif = new ImageView(new Image("file:src/main/pic/Upload_Model_GIF2.gif"));
        uploadGif.setFitWidth(170);
        uploadGif.setFitHeight(170);
        Button btnUploadModel = new Button();
        btnUploadModel.setBackground(Utils.getBackgroundColor(Color.TRANSPARENT, 3));
        btnUploadModel.setMinSize(220, 200);

        Label uploadText = new Label("Import Local Models to Library");
        uploadText.setFont(Font.font(18));
        uploadText.setWrapText(true);
        uploadText.setMinSize(220, 60);
        uploadText.setMaxSize(220, 60);
        uploadText.setAlignment(Pos.CENTER);
        uploadText.setTextFill(Color.WHITE);
        uploadText.setPadding(new Insets(0, 20, 0, 20));

        Label supportTypeText = new Label("Accept .GLB, .FBX files");
        supportTypeText.setFont(Font.font(20));
        supportTypeText.setMinSize(220, 30);
        supportTypeText.setAlignment(Pos.CENTER);
        supportTypeText.setTextFill(Color.BLUEVIOLET);

        VBox v = new VBox();
        v.getChildren().addAll(uploadText);
        v.setMaxHeight(50);

        StackPane uploadStack = new StackPane();
        uploadStack.setBackground(Utils.getBackgroundColor(Color.BLACK));
        uploadStack.getChildren().addAll(uploadGif, v, btnUploadModel);
        StackPane.setAlignment(uploadGif, Pos.TOP_CENTER);
        StackPane.setAlignment(v, Pos.BOTTOM_CENTER);
        uploadStack.setMinSize(220, 200);

        HBox hbBtn = new HBox(200);
        hbBtn.setAlignment(Pos.TOP_CENTER);
        hbBtn.getChildren().add(genModelStack);
        hbBtn.getChildren().add(uploadStack);
        grid.add(hbBtn, 1, 0, 5, 1);

        /* Start footer definition */
        bottomPane.setAlignment(Pos.CENTER);

        Label libraryTitle = new Label("Model Library");

        libraryTitle.setAlignment(Pos.CENTER_LEFT);
        libraryTitle.setMinSize(600, 50);
        libraryTitle.setTextFill(Color.BLUEVIOLET);
        libraryTitle.setFont(Font.font(DEFAULT_FONT_FAMILY, FontWeight.BOLD, 18));

        libraryPane = new HBox(20);

        libraryPane.setAlignment(Pos.CENTER);
        libraryPane.setPadding(new Insets(0, 15, 0, 15));
        libraryPane.setMinSize(600, 100);

        libraryView = new ListView<>();
        libraryView.setOrientation(Orientation.HORIZONTAL);
        HBox.setHgrow(libraryView, Priority.ALWAYS);

        libraryView.setCellFactory(param -> {
            ListCell<AliceModel> cell = new ListCell<AliceModel>() {

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
            };

            cell.addEventFilter(MouseEvent.MOUSE_PRESSED, event -> {
                libraryView.requestFocus();

                int index = cell.getIndex();

                MultipleSelectionModel<AliceModel> libraryModel = libraryView.getSelectionModel();

                if (cell.isEmpty()) {
                    libraryModel.clearSelection(libraryModel.getSelectedIndex());
                } else {
                    if (libraryModel.getSelectedIndex() != index) {
                        libraryModel.select(index);
                    }
                }

                AliceModel selectedModel = libraryModel.getSelectedItem();

                buttons.setVisible(selectedModel != null);
                app.setActiveModel(selectedModel);
            });

            return cell;
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

        buttons = new VBox(20);

        buttons.setAlignment(Pos.CENTER);
        buttons.setMinHeight(120);
        buttons.setPadding(new Insets(0, 75, 0, 75));

        HBox importButtonBox = new HBox();

        Button btnImport = new Button("Import");
        btnImport.setMinWidth(100);
        btnImport.setBackground(Utils.getBackgroundColor(Color.BLUEVIOLET));
        btnImport.setTextFill(Color.WHITE);

        Label importButtonLabel = new Label("Import model into Alice Gallery");
        importButtonLabel.setTextFill(Color.BLUEVIOLET);
        importButtonLabel.setFont(Font.font(DEFAULT_FONT_FAMILY, FontWeight.NORMAL, 14));
        importButtonLabel.setPadding(new Insets(0, 20, 0, 20));
        HBox.setHgrow(importButtonLabel, Priority.ALWAYS);

        importButtonBox.getChildren().addAll(btnImport, importButtonLabel);

        HBox editButtonBox = new HBox();

        Button btnTexture = new Button("Edit");
        btnTexture.setMinWidth(100);
        btnTexture.setBackground(Utils.getBackgroundColor(Color.BLUEVIOLET));
        btnTexture.setTextFill(Color.WHITE);

        Label editButtonLabel = new Label("Adjust the appearance of the model");
        editButtonLabel.setTextFill(Color.BLUEVIOLET);
        editButtonLabel.setFont(Font.font(DEFAULT_FONT_FAMILY, FontWeight.NORMAL, 14));
        editButtonLabel.setPadding(new Insets(0, 20, 0, 20));
        HBox.setHgrow(editButtonLabel, Priority.ALWAYS);

        editButtonBox.getChildren().addAll(btnTexture, editButtonLabel);

        buttons.setVisible(false);

        buttons.getChildren().addAll(importButtonBox, editButtonBox);

        bottomPane.getChildren().addAll(libraryTitle, libraryPane, buttons);

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

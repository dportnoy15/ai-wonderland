package modelimport.scene;

import java.util.ArrayList;
import java.util.List;

import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Popup;
import javafx.stage.Stage;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import modelimport.HelloFX;
import modelimport.SceneManager;
import modelimport.Utils;

public class ArtStyleScene extends AliceScene {

    private static final String IMAGE_PATH = "src/main/pic";

    Button[] artStyleButtons;
    private int curStyleSelection;

    private GridPane grid;

    private Label scenetitle;

    public ArtStyleScene(Stage stage, Scene scene, HelloFX app) {
        super(stage, scene, app);
    }

    public void initLayout() {
        BorderPane layout = (BorderPane) getScene().getRoot();

        HBox bottomPane = new HBox();
        Pane leftPane = new FlowPane();
        Pane rightPane = new FlowPane();
        GridPane centerPane = new GridPane();


        layout.setBottom(bottomPane); // Nav Buttons
        layout.setLeft(leftPane);
        layout.setRight(rightPane);
        layout.setCenter(centerPane); // Main Content

        bottomPane.setPrefHeight(100);
        leftPane.setPrefWidth(0);
        rightPane.setPrefWidth(0);

        Font uiFont = Font.font("Tahoma", FontWeight.NORMAL, 14);

        scenetitle = new Label("Select your art style: Realistic");
        scenetitle.setFont(uiFont);
        centerPane.add(scenetitle, 0, 0);
        ColumnConstraints col = new ColumnConstraints();
        col.setPercentWidth(40);
        centerPane.getColumnConstraints().addAll(col, col);

        /* Start footer definition */

        ImageView backImg = new ImageView(new Image("file:src/main/pic/PreviousLocation.png"));
        backImg.setFitWidth(30);
        backImg.setFitHeight(30);
        Button btnPrev = new Button("", backImg);
        btnPrev.setMaxSize(40, 40);
        btnPrev.setBackground(Utils.getBackgroundColor(Color.TRANSPARENT, 3));
        Region region = new Region();

        Button btnNext = new Button("Describe Model");
        btnNext.setBackground(Utils.getBackgroundColor(Color.BLUEVIOLET));
        btnNext.setStyle("-fx-font-size: 14;");

        btnNext.setMinSize(120, 30);
        btnNext.setTextFill(Color.WHITE);


        HBox.setHgrow(region, Priority.ALWAYS);
        bottomPane.getChildren().addAll(btnPrev, region, btnNext);
        bottomPane.setAlignment(Pos.CENTER);
        bottomPane.setPadding(new Insets(0, 50, 0, 50));

        /* Start screen customization */

        grid = centerPane;
        grid.setAlignment(Pos.CENTER);
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(25, 25, 25, 25));

        artStyleButtons = new Button[] {
            new Button("Realistic"),
            new Button("2.5D Cartoon"),
            new Button("Japanese Anime"),
            new Button("Cartoon Line Art"),
            new Button("Realistic Hand-drawn"),
            new Button("2.5D Hand-drawn"),
            new Button("Oriental Comic Ink")
        };

        registerButtonActions(artStyleButtons, btnPrev, btnNext);

        curStyleSelection = 0;
        artStyleButtons[curStyleSelection].fire();
    }

    public void registerButtonActions(Button[] artStyleButtons, Button btnPrev, Button btnNext) {
        for (int i = 0; i < artStyleButtons.length; i++) {
            int newStyleIndex = i;
            artStyleButtons[i].setOnAction((ActionEvent actionEvent) -> {
                System.out.println("Selecting art style " + newStyleIndex + " (" + getArtStyle() + "), was " + curStyleSelection);
                artStyleButtons[curStyleSelection].setDisable(false);
                artStyleButtons[newStyleIndex].setDisable(true);

                scenetitle.setText("Select your art style: " + artStyleButtons[newStyleIndex].getText());
                curStyleSelection = newStyleIndex;
            });
        }

        btnPrev.setOnAction((ActionEvent event) -> {
            SceneManager.getInstance().setActiveScene(1);
        });

        btnNext.setOnAction((ActionEvent event) -> {
            SceneManager.getInstance().setActiveScene(3);
        });
    }

    public void selectCurrentStyleButton() {
        artStyleButtons[curStyleSelection].requestFocus();
    }

    public String getArtStyle() {
        ArrayList<String> artStyleValues = new ArrayList<>(List.of(
            "realistic",
            "fake-3d-cartoon",
            "japanese-anime",
            "cartoon-line-art",
            "realistic-hand-drawn",
            "fake-3d-hand-drawn",
            "oriental-comic-ink"
        ));

        return artStyleValues.get(curStyleSelection);
    }

    public void generateNewModel(boolean genNew) {
        generateArtStyleButtons(genNew);
    }

    private void generateArtStyleButtons(boolean genNewModel) {
        Node title = grid.getChildren().get(0);

        grid.getChildren().clear();

        grid.add(title, 0, 0);

        String filenamePrefix = genNewModel ? "model_style_" : "texture_style_";

        for (int i = 0; i < artStyleButtons.length; i++) {
            artStyleButtons[i].setMinSize(200, 50);

            artStyleButtons[i].setBackground(Utils.getBackgroundColor(Color.WHITE, 3));
            int finalI = i;
            artStyleButtons[i].setOnMousePressed(event -> artStyleButtons[finalI].setBackground(Utils.getBackgroundColor(Color.GRAY, 3)));
            artStyleButtons[i].setOnMouseReleased(event -> artStyleButtons[finalI].setBackground(Utils.getBackgroundColor(Color.WHITE, 3)));

            ImageView imageView = new ImageView(new Image("file:" + IMAGE_PATH + "/" + filenamePrefix + i + ".png"));
            imageView.setFitWidth(50);
            imageView.setFitHeight(50);
            HBox buttonWithImage = new HBox(imageView, artStyleButtons[i]);
            /*buttonWithImage.setBorder(new Border(new BorderStroke(
                                                Color.MEDIUMPURPLE, // Border color
                                                BorderStrokeStyle.SOLID, // Border style
                                                new CornerRadii(5), // CornerRadii
                                                new BorderWidths(2)))); // Border width);*/
            grid.add(buttonWithImage, i % 2, 5 + i / 2 * 3, 2, 2);

            Popup popup = new Popup();
            ImageView previewImageView = new ImageView(new Image("file:" + IMAGE_PATH + "/" + filenamePrefix + i + ".png"));
            previewImageView.setFitWidth(400);
            previewImageView.setFitHeight(400);
            Rectangle background = new Rectangle(400, 400);
            background.setFill(Color.WHITE);
            popup.getContent().addAll(background, previewImageView);

            artStyleButtons[i].setOnMouseEntered(event -> {
                // Show the Popup when the button is hovered
                popup.show(artStyleButtons[finalI], stage.getX() +stage.getWidth(), stage.getY() + 200);
            });

            artStyleButtons[i].setOnMouseExited(event -> {
                // Hide the Popup when the mouse is moved away from the button
                popup.hide();
            });
        }
    }
}

package sample;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.util.Callback;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

/**
 * Created by johnson on 12/11/14.
 */
public class UITestController implements Initializable{
    static Logger logger = LogManager.getLogger();
    static final int imageSize = 50;
    static final Map<String, ImageView> imageViewMap = new HashMap<String, ImageView>();

    @FXML private ListView chatList;

    @FXML private TextField input;

    ObservableList<String> data = FXCollections.observableArrayList(
            "lHello1",
            "lHello2",
            "lHello3");

    @Override @FXML
    public void initialize(URL location, ResourceBundle resources) {
        logger.debug(input.getText());
        chatList.setItems(data);
        chatList.setCellFactory(new Callback<ListView, ListCell>() {
            @Override
            public ListCell call(ListView param) {
                return new ColorRectCell();
            }
        });
        chatList.setEditable(false);
    }

    @FXML
    private void handleShoutAction(ActionEvent actionEvent) {
        data.add(input.getText());
        chatList.scrollTo(data.size());
    }

    public void send(String str) {
        logger.debug(str);
    }

    static class ColorRectCell extends ListCell<String> {
        @Override
        public void updateItem(String item, boolean empty) {
            this.setStyle("-fx-background-color: transparent");
            if (item != null) {
                HBox hBox = new HBox();
                Text text = new Text(item.substring(1));
                ImageView imageView = createImageView("file:///home/johnson/Pictures/head.jpeg");
                hBox.setStyle("-fx-background-color: transparent");
                if (item.startsWith("l")) {
                    hBox.setAlignment(Pos.CENTER_LEFT);
                    hBox.getChildren().addAll(imageView, text);
                    setGraphic(hBox);
                }
                else if (item.startsWith("r")) {
                    hBox.setAlignment(Pos.CENTER_RIGHT);
                    hBox.getChildren().addAll(text, imageView);
                    setGraphic(hBox);
                }
                else {
                    logger.error("expecting l/r leading error: " + item);
                }
            }
        }

        ImageView getImageView(String uri) {
            if (!imageViewMap.containsKey(uri)) {
                imageViewMap.put(uri, createImageView(uri));
            }
            return imageViewMap.get(uri);
        }

        ImageView createImageView(String uri) {
            ImageView imageView = new ImageView();
            imageView.setImage(new Image(uri));
            imageView.setFitHeight(imageSize);
            imageView.setFitWidth(imageSize);
            return imageView;
        }
    }

//    static class ChatItem extends
}

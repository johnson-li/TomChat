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
import javafx.scene.text.TextFlow;
import javafx.util.Callback;
import javafx.util.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.net.URL;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by johnson on 12/11/14.
 */
public class UITestController implements Initializable{
    static Logger logger = LogManager.getLogger();

    @FXML private TextFlow textFlow;

    @Override @FXML
    public void initialize(URL location, ResourceBundle resources) {
        String text = "papapa[1]qwer[21]1234[23][4]papapapapapa[1]qwer[21]1234[23][4]papapapapapa[1]qwer[21]1234[23][4]papapapapapa[1]qwer[21]1234[23][4]papapapapapa[1]qwer[21]1234[23][4]papapa";
        Pattern pattern = Pattern.compile("\\[(\\d+)\\]");
        Matcher matcher = pattern.matcher(text);
        Queue<Pair<Integer, Integer>> queue = new LinkedList<Pair<Integer, Integer>>();
        while (matcher.find()) {
            queue.add(new Pair<Integer, Integer>(matcher.start(), matcher.end()));
        }
        int last = 0;
        textFlow.setMaxWidth(100);
        for (Pair<Integer, Integer> pair: queue) {
            int start = pair.getKey();
            int end = pair.getValue();
            if (start > last) {
                Text text1 = new Text(text.substring(last, start));
                textFlow.getChildren().add(text1);
            }
            last = end;
            String sub = text.substring(start, end);
            ImageView imageView = new ImageView();
            imageView.setImage(new Image(getClass().getResource("../resources/motions/" + sub.substring(1, sub.length() - 1) + ".gif").toString()));
            textFlow.getChildren().add(imageView);
        }
        if (last < text.length()) {
            Text text1 = new Text(text.substring(last));
            textFlow.getChildren().add(text1);
        }
    }
}

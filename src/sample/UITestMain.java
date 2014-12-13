package sample;

import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.stage.Stage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;

public class UITestMain extends Application {
    static Logger logger = LogManager.getLogger();

    @Override
    public void start(Stage primaryStage) throws Exception{
//        Parent root = FXMLLoader.load(getClass().getResource("../resources/find_network.fxml"));
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("../resources/ui_test.fxml"));
        Parent root = fxmlLoader.load();
        primaryStage.setTitle("Hello World");
        final Scene scene = new Scene(root);
        scene.setOnDragOver(new EventHandler<DragEvent>() {
            @Override
            public void handle(DragEvent event) {
                if (event.getGestureSource() != scene && event.getDragboard().hasFiles()) {
                    event.acceptTransferModes(TransferMode.COPY);
                }
                event.consume();
            }
        });
        scene.setOnDragDropped(new EventHandler<DragEvent>() {
            @Override
            public void handle(DragEvent event) {
                Dragboard dragboard = event.getDragboard();
                if (dragboard.hasFiles()) {
                    for (File file : dragboard.getFiles()) {
                        logger.debug(file);
                    }
                }
                event.setDropCompleted(true);
                event.consume();
            }
        });
        primaryStage.setScene(scene);
        primaryStage.show();
    }


    public static void main(String[] args) {
        launch(args);
    }


}

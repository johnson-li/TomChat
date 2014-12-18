package sample;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Control;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class LoginController {
    static Logger logger = LogManager.getLogger();
    @FXML private Text actionTarget;

    @FXML private TextField entryIP, identification;

    @FXML private Button enter, setting;

    @FXML
    protected void handleSubmitButtonAction(ActionEvent event) {
        actionTarget.setText("Sign in button pressed");
        String ip = entryIP.getText();
        if (!Utils.checkIP(ip)) {
            showWarning("Invalid IP address!");
            return;
        }
        if (!MyPeer.initPeer(identification.getText(), ip)) {
            showWarning("Connecting to p2p network failed");
            return;
        }
        logger.debug(MyPeer.getPeerAddress());
        closeWindow(enter);
        startChat();
    }

    @FXML
    protected void handleInputFinishedAction(ActionEvent actionEvent) {
//        actionTarget.setText("Sign in button pressed");
//        String ip = entryIP.getText();
//        if (!Utils.checkIP(ip)) {
//            showWarning("Invalid IP address!");
//            return;
//        }
//        if (!MyPeer.initPeer(identification.getText(), ip)) {
//            showWarning("Connecting to p2p network failed");
//            return;
//        }
//        logger.debug(MyPeer.getPeerAddress());
//        closeWindow(enter);
//        startChat();
    }

    @FXML
    protected void handleSettingButtonAction(ActionEvent event) {
        startSetting();
    }

    public static void showWarning(String str) {
        try {
            Stage dialogStage = new Stage();
            dialogStage.setResizable(false);
            Text text = new Text(str);
            StackPane stackPane = new StackPane();
            stackPane.getChildren().add(text);
            dialogStage.setTitle("Waring");
            Scene scene = new Scene(stackPane, 250, 70);
            dialogStage.setScene(scene);
            dialogStage.show();
        }
        catch (Exception e) {
            logger.catching(e);
        }
    }

    protected void closeWindow(Control control) {
        Stage stage = (Stage)control.getScene().getWindow();
        stage.close();
    }

    protected void startChat() {
        Stage stage = new Stage();
        stage.setResizable(false);
        Parent root;
        try {
            root = FXMLLoader.load(getClass().getResource("/chat.fxml"));
        }
        catch (Exception e) {
            logger.catching(e);
            return;
        }
        stage.setTitle("Chat");
        stage.setScene(new Scene(root));
        stage.show();
    }

    protected void startSetting() {
        Stage stage = new Stage();
        stage.setResizable(false);
        Parent root;
        try {
            root = FXMLLoader.load(getClass().getResource("/setting.fxml"));
        }
        catch (Exception e) {
            logger.catching(e);
            return;
        }
        stage.setTitle("Chat");
        stage.setScene(new Scene(root));
        stage.show();

    }
}

package sample;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import net.tomp2p.peers.Number160;

/**
 * Created by johnson on 12/7/14.
 */
public class FindPeerController {

    @FXML
    private Button findButton;

    @FXML
    private TextField peerIdTestField;

    @FXML
    private void handleFindButtonAction(ActionEvent event) {
        String peerID = peerIdTestField.getText();
        if (MyPeer.checkOnLine(Number160.createHash(peerID))) {
            closeSelf();
            ChatController.refreshNeighbor();
        }
        else {
            LoginController.showWarning("find server failed");
        }
    }

    void closeSelf() {
        Stage stage = (Stage)findButton.getScene().getWindow();
        stage.close();
    }
}

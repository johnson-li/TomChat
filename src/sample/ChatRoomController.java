package sample;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import net.tomp2p.dht.FuturePut;
import net.tomp2p.peers.Number160;
import net.tomp2p.storage.Data;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Created by johnson on 12/10/14.
 */
public class ChatRoomController implements Initializable{
    static Logger logger = LogManager.getLogger();

    @FXML private TextField input;

    @FXML private Button shout;

    String peerName;

    @Override @FXML
    public void initialize(URL location, ResourceBundle resources) {

    }

    public void init(String peerName) {
        this.peerName = peerName;
    }

    @FXML
    private void handleShoutAction(ActionEvent actionEvent) {
        FuturePut futurePut = MyPeer.clientPeerDHT.add(Number160.createHash(peerName)).data(new Data(input.getText().getBytes())).start();
        futurePut.awaitUninterruptibly();
        logger.debug(futurePut.isSuccess());
    }
}

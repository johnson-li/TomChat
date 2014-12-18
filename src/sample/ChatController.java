package sample;

import com.sun.javafx.scene.control.skin.LabeledText;
import com.sun.javafx.scene.control.skin.ListViewSkin;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.stage.Stage;
import net.tomp2p.dht.FutureGet;
import net.tomp2p.futures.BaseFutureAdapter;
import net.tomp2p.peers.Number160;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.net.URL;
import java.nio.file.Paths;
import java.util.*;

/**
 * Created by johnson on 11/28/14.
 */
public class ChatController implements Initializable{
    static Logger logger = LogManager.getLogger();
    static ObservableList<String> neighborAddressObservableList = FXCollections.observableArrayList();

    @FXML private ListView neighborAddress;

    @FXML private Button addPeer, refresh;

    @Override @FXML
    public void initialize(URL location, ResourceBundle resources) {
        neighborAddress.setItems(neighborAddressObservableList);
        refreshNeighbor();
        initNeighborClickListener();
    }

    @FXML
    private void handleListViewAction(ActionEvent event) {

    }

    @FXML
    private void handleAddPeerButtonAction(ActionEvent event) {
        startFindPeerDialog();
    }

    @FXML
    private void handleRefreshButtonAction(ActionEvent event) {
        refreshNeighbor();
        MyPeer.showMap();
    }

    public static void refreshNeighbor() {
        neighborAddressObservableList.clear();
        for (Map.Entry<Number160, String> entry: MyPeer.neighborPeers.entrySet()) {
            Number160 peerID = entry.getKey();
            String identification = entry.getValue();
            neighborAddressObservableList.add(identification);
        }
    }

    void startFindPeerDialog() {
        Stage stage = new Stage();
        stage.setResizable(false);
        Parent root;
        try {
            root = FXMLLoader.load(getClass().getResource("/find_peer.fxml"));
        }
        catch (Exception e) {
            logger.catching(e);
            return;
        }
        stage.setTitle("Chat");
        stage.setScene(new Scene(root));
        stage.show();
    }

    void initNeighborClickListener() {
        neighborAddress.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                if (event.getClickCount() == 2) {
                    String peerName = (String)neighborAddress.getSelectionModel().getSelectedItem();
                    int location = peerName.indexOf("(");
                    if (location > 0) {
                        peerName = peerName.substring(0, location);
                    }
                    startChatRoom(peerName);
                }
            }
        });
    }

    void startChatRoom(String peerName) {
        Stage stage = new Stage();
        stage.setResizable(false);
        Parent root;
        ChatRoomController chatRoomController;
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/chat_room.fxml"));
            root = loader.load();
            chatRoomController = loader.getController();
            chatRoomController.init(peerName);
        }
        catch (Exception e) {
            logger.catching(e);
            return;
        }
        stage.setTitle(peerName);
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
        initFileDropListener(scene, chatRoomController);
    }

    void initFileDropListener(final Scene scene, final ChatRoomController chatRoomController) {
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
                        chatRoomController.sendFile(Paths.get(file.getAbsolutePath()));
                    }
                }
                event.setDropCompleted(true);
                event.consume();
            }
        });
    }
}

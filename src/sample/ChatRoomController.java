package sample;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.util.Callback;
import javafx.util.Pair;
import net.tomp2p.dht.FutureSend;
import net.tomp2p.peers.Number160;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.URL;
import java.util.LinkedList;
import java.util.Queue;
import java.util.ResourceBundle;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by johnson on 12/10/14.
 */
public class ChatRoomController implements Initializable{
    static Logger logger = LogManager.getLogger();
    static final int imageSize = 50;

    @FXML private TextField input;

    @FXML private Button shout;

    @FXML private ListView chatList;

    ObservableList<String> data = FXCollections.observableArrayList("lHello");

    String peerName;

    @Override @FXML
    public void initialize(URL location, ResourceBundle resources) {
        chatList.setItems(data);
        chatList.setCellFactory(new Callback<ListView, ListCell>() {
            @Override
            public ListCell call(ListView param) {
                return new ColorRectCell();
            }
        });
    }

    public void init(final String peerName) {
        this.peerName = peerName;
        MyPeer.registerMessageReceiver(new MessageReceiver() {
            @Override
            public boolean checkMine(Number160 peerID) {
                return peerID.equals(Number160.createHash(peerName));
            }

            @Override
            public void onReceived(ByteBuf request) {
                MessageType messageType = MessageType.values()[request.readInt()];
                switch (messageType) {
                    case MESSAGE:
                        byte[] bytes = new byte[request.readableBytes()];
                        request.readBytes(bytes);
                        String message = new String(bytes);
                        logger.debug("received message: " + message);
                        addRemoteMessage(message);
                        break;
                    case PICTURE:
                        break;
                }
            }
        });
    }

    @FXML
    private void handleShoutAction(ActionEvent actionEvent) {
        addLocalMessage(input.getText());
        sendMessage(input.getText(), peerName);
    }

    boolean sendMessage(String message, String peerName) {
        ByteBuf byteBuf = Unpooled.buffer();
        byteBuf.writeInt(MessageType.MESSAGE.ordinal());
        byteBuf.writeBytes(message.getBytes());
//        FutureSend futureSend = MyPeer.clientPeerDHT.send(Number160.createHash(peerName)).object(message).start();
        FutureSend futureSend = MyPeer.clientPeerDHT.send(Number160.createHash(peerName)).object(Utils.encodeBase64(byteBuf)).start();
        futureSend.awaitUninterruptibly();
        byteBuf.release();
        for (Object object: futureSend.rawDirectData2().values()) {
            if (object.equals("OK")) {
                logger.info("message sent succeeded");
                return true;
            }
        }
        logger.warn("no peer replied");
        return false;
    }



    void addLocalMessage(String str) {
        data.add("r" + str);
    }

    void addRemoteMessage(String str) {
        data.add("l" + str);
    }

    static class ColorRectCell extends ListCell<String> {
        @Override
        public void updateItem(String item, boolean empty) {
            this.setStyle("-fx-background-color: transparent");
            if (item != null) {
                HBox hBox = new HBox();
                String message = item.substring(1);
                TextFlow textFlow = createTextFlow(message);
                ImageView imageView = createImageView("file:///home/johnson/Pictures/head.jpeg");
                hBox.setStyle("-fx-background-color: transparent");
                if (item.startsWith("l")) {
                    hBox.setAlignment(Pos.CENTER_LEFT);
                    hBox.getChildren().addAll(imageView, textFlow);
                    setGraphic(hBox);
                }
                else if (item.startsWith("r")) {
                    hBox.setAlignment(Pos.CENTER_RIGHT);
                    hBox.getChildren().addAll(textFlow, imageView);
                    setGraphic(hBox);
                }
                else {
                    logger.error("expecting l/r leading error: " + item);
                }
            }
        }

        TextFlow createTextFlow(String message) {
            TextFlow textFlow = new TextFlow();
            textFlow.setMaxWidth(300);
            textFlow.setPadding(new Insets(imageSize / 2, 10, 0, 10));
            Pattern pattern = Pattern.compile("\\[(\\d+)\\]");
            Matcher matcher = pattern.matcher(message);
            Queue<Pair<Integer, Integer>> queue = new LinkedList<Pair<Integer, Integer>>();
            while (matcher.find()) {
                queue.add(new Pair<Integer, Integer>(matcher.start(), matcher.end()));
            }
            int last = 0;
            for (Pair<Integer, Integer> pair: queue) {
                int start = pair.getKey();
                int end = pair.getValue();
                if (start > last) {
                    Text text1 = new Text(message.substring(last, start));
                    textFlow.getChildren().add(text1);
                }
                last = end;
                String sub = message.substring(start, end);
                ImageView imageView = new ImageView();
                imageView.setImage(new Image(getClass().getResource("../resources/motions/" + sub.substring(1, sub.length() - 1) + ".gif").toString()));
                textFlow.getChildren().add(imageView);
            }
            if (last < message.length()) {
                Text text1 = new Text(message.substring(last));
                textFlow.getChildren().add(text1);
            }
            return textFlow;
        }

        ImageView createImageView(String uri) {
            ImageView imageView = new ImageView();
            imageView.setImage(new Image(uri));
            imageView.setFitHeight(imageSize);
            imageView.setFitWidth(imageSize);
            return imageView;
        }
    }

}

package sample;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import javafx.application.Platform;
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
import javafx.stage.FileChooser;
import javafx.util.Callback;
import javafx.util.Pair;
import net.tomp2p.dht.FutureSend;
import net.tomp2p.peers.Number160;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.activation.MimetypesFileTypeMap;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by johnson on 12/10/14.
 */
public class ChatRoomController implements Initializable{
    static Logger logger = LogManager.getLogger();
    static final int headImageSize = 50;
    static final int imageSize = 200;
    static final String MESSAGE_LEFT = "l";
    static final String MESSAGE_RIGHT = "r";
    static final String PICTURE_LEFT = "L";
    static final String PICTURE_RIGHT = "R";

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
                        byte[] messageBytes = new byte[request.readableBytes()];
                        request.readBytes(messageBytes);
                        String message = new String(messageBytes);
                        logger.debug("received message: " + message);
                        addRemoteMessage(message);
                        break;
                    case PICTURE:
//                        byte[] pictureBytes = new byte[request.readableBytes()];
//                        request.readBytes(pictureBytes);
                        Random random = new Random(new Date().getTime());
                        String tmpFileName = String.valueOf(random.nextInt());
                        String filePath = Utils.CACHE_PATH + "/" + tmpFileName;
                        Path path = Paths.get(filePath);
                        try {
                            storeFile(request, path);
                            addRemotePicture(path);
                        }
                        catch (Exception e) {
                            e.printStackTrace();
                        }
                        break;
                    case SMALL_FILE:
                        int fileNameLength = request.readInt();
                        byte[] fileNameBytes = new byte[fileNameLength];
                        request.readBytes(fileNameBytes, 0, fileNameLength);
                        String fileName = new String(fileNameBytes);
                        FileChooser fileChooser = new FileChooser();
                        fileChooser.setTitle("Save file");
                        fileChooser.setInitialDirectory(new File(Utils.DOWNLOAD_PATH));
                        fileChooser.setInitialFileName(fileName);
                        File file = fileChooser.showSaveDialog(input.getContextMenu());
                        if (file == null) return;
                        Path smallFileDownloadPath = Paths.get(file.getAbsolutePath());
                        try {
                            storeFile(request, smallFileDownloadPath);
                            addRemoteFile(smallFileDownloadPath);
                        }
                        catch (Exception e) {
                            logger.catching(e);
                        }
                        //ToDo
                        break;
                    case LARGE_FILE:
                        //ToDo
                        break;
                }
            }
        });
    }

    void storeFile(ByteBuf byteBuf, Path path) throws IOException{
        logger.debug(byteBuf.readableBytes());
        FileChannel fileChannel = FileChannel.open(path, StandardOpenOption.CREATE_NEW, StandardOpenOption.APPEND);
        fileChannel.write(byteBuf.nioBuffer());
        fileChannel.close();
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
        return sendByteBuf(byteBuf);
    }

    void addLocalMessage(String str) {
        data.add(MESSAGE_RIGHT + str);
    }

    void addRemoteMessage(String str) {
        data.add(MESSAGE_LEFT + str);
    }

    void addLocalPicture(Path path) {
        data.add(PICTURE_RIGHT + path.toAbsolutePath());
    }

    void addRemotePicture(Path path) {
        data.add(PICTURE_LEFT + path.toAbsolutePath());
    }

    void addLocalFile(Path path) {

    }

    void addRemoteFile(Path path) {

    }

    void sendFile(Path path) {
        if (checkImageFile(path)) {
            sendImageFile(path);
        }
        else if (checkSmallFile(path)) {
            sendSmallFile(path);
        }
        else {
            //ToDo large file transfer is not supported yet
        }
    }

    boolean sendSmallFile(Path path) {
        String fileName = path.getFileName().toString();
        try {
            addLocalFile(path);
            FileChannel fileChannel = FileChannel.open(path);
            if (fileChannel.size() > Integer.MAX_VALUE) throw new Exception("Image file size too large");
            ByteBuffer byteBuffer = ByteBuffer.allocate((int)fileChannel.size());
            fileChannel.read(byteBuffer);
            byteBuffer.flip();
            fileChannel.close();
            ByteBuf byteBuf = Unpooled.buffer();
            byteBuf.writeInt(MessageType.SMALL_FILE.ordinal());
            byteBuf.writeInt(fileName.getBytes().length);
            byteBuf.writeBytes(fileName.getBytes());
            byteBuf.writeBytes(byteBuffer);
            return sendByteBuf(byteBuf);
        }
        catch (Exception e) {
            logger.catching(e);
        }
        return false;
    }

    boolean sendImageFile(Path path) {
        try {
            addLocalPicture(path);
            FileChannel fileChannel = FileChannel.open(path);
            if (fileChannel.size() > Integer.MAX_VALUE) throw new Exception("Image file size too large");
            ByteBuffer byteBuffer = ByteBuffer.allocate((int)fileChannel.size());
            fileChannel.read(byteBuffer);
            byteBuffer.flip();
            fileChannel.close();
            ByteBuf byteBuf = Unpooled.buffer();
            byteBuf.writeInt(MessageType.PICTURE.ordinal());
            byteBuf.writeBytes(byteBuffer);
            return sendByteBuf(byteBuf);
        }
        catch (Exception e) {
            logger.catching(e);
        }
        return false;
    }

    boolean sendByteBuf(ByteBuf byteBuf) {
        try {
            FutureSend futureSend = MyPeer.clientPeerDHT.send(Number160.createHash(peerName)).object(Utils.encodeBase64(byteBuf)).start();
            futureSend.awaitUninterruptibly();
            byteBuf.release();
            for (Object object: futureSend.rawDirectData2().values()) {
                logger.debug(object);
                if (object.equals("OK")) {
                    logger.info("message sent succeeded");
                    return true;
                }
                else if (object.equals("No receiver detected")) {
                    logger.warn("maybe you are not registered by target");
                }
            }
            logger.warn("no peer replied");
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    boolean checkImageFile(Path path) {
        String mimeType = new MimetypesFileTypeMap().getContentType(path.toFile());
        logger.debug(mimeType);
        return mimeType.contains("image");
    }

    boolean checkSmallFile(Path path) {
        //ToDo the small file size threshold is to be determined
        return true;
    }

    static class ColorRectCell extends ListCell<String> {
        @Override
        public void updateItem(final String item, boolean empty) {
            this.setStyle("-fx-background-color: transparent");
            if (item != null) {
                final String message = item.substring(1);
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        HBox hBox = new HBox();
                        ImageView imageView = createHeadImageView("file:///home/johnson/Pictures/head.jpeg");
                        hBox.setStyle("-fx-background-color: transparent");
                        if (item.startsWith(MESSAGE_LEFT)) {
                            hBox.setAlignment(Pos.TOP_LEFT);
                            TextFlow textFlow = createTextFlow(message);
                            hBox.getChildren().addAll(imageView, textFlow);
                            setGraphic(hBox);
                        }
                        else if (item.startsWith(MESSAGE_RIGHT)) {
                            hBox.setAlignment(Pos.TOP_RIGHT);
                            TextFlow textFlow = createTextFlow(message);
                            hBox.getChildren().addAll(textFlow, imageView);
                            setGraphic(hBox);
                        }
                        else if (item.startsWith(PICTURE_LEFT)) {
                            logger.debug(message);
                            hBox.setAlignment(Pos.TOP_LEFT);
                            Path path = Paths.get(message);
                            try {
                                ImageView receivedImageView = createImageView(path.toUri().toURL().toString());
                                hBox.getChildren().addAll(imageView, receivedImageView);
                                setGraphic(hBox);
                            }
                            catch (Exception e) {
                                logger.catching(e);
                            }
                        }
                        else if (item.startsWith(PICTURE_RIGHT)) {
                            logger.entry();
                            hBox.setAlignment(Pos.TOP_RIGHT);
                            Path path = Paths.get(message);
                            try {
                                ImageView imageView1 = createImageView(path.toUri().toURL().toString());
                                hBox.getChildren().addAll(imageView1, imageView);
                                setGraphic(hBox);
                            }
                            catch (Exception e) {
                                logger.catching(e);
                            }
                        }
                        else {
                            logger.error("message header error: " + item);
                        }
                    }
                });
            }
        }

        TextFlow createTextFlow(String message) {
            TextFlow textFlow = new TextFlow();
            textFlow.setMaxWidth(300);
            textFlow.setPadding(new Insets(headImageSize / 2, 10, 0, 10));
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
                Image image = new Image(getClass().getResource("/motions/" + sub.substring(1, sub.length() - 1) + ".gif").toString());
                if (!image.isError()) {
                    imageView.setImage(image);
                    textFlow.getChildren().add(imageView);
                }
            }
            if (last < message.length()) {
                Text text1 = new Text(message.substring(last));
                textFlow.getChildren().add(text1);
            }
            return textFlow;
        }

        ImageView createHeadImageView(String uri) {
            ImageView imageView = new ImageView();
            imageView.setImage(new Image(uri));
            imageView.setFitHeight(headImageSize);
            imageView.setFitWidth(headImageSize);
            return imageView;
        }

        ImageView createImageView(String uri) {
            ImageView imageView = new ImageView();
            Image image = new Image(uri);
            imageView.setImage(image);
            if (image.getHeight() > image.getWidth()) {
                if (image.getHeight() > imageSize) {
                    imageView.setFitHeight(imageSize);
                    imageView.setFitWidth(imageSize * image.getWidth() / image.getHeight());
                }
            }
            else {
                if (image.getWidth() > imageSize) {
                    imageView.setFitWidth(imageSize);
                    imageView.setFitHeight(imageSize * image.getHeight() / image.getWidth());
                }
            }

            return imageView;
        }
    }
}

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
import net.tomp2p.futures.BaseFuture;
import net.tomp2p.futures.BaseFutureListener;
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
    static Map<Path, Long> pathMap = new HashMap<Path, Long>();
    static Map<Long, Path> idMap = new HashMap<Long, Path>();
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
    Number160 peerId;

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
        this.peerId = Number160.createHash(peerName);
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
                        downloadPicture(request);
                        break;
                    case SMALL_FILE:
                        downloadSmallFile(request);
                        break;
                    case LARGE_FILE_HEAD:
                        initLargeFileDownloader(request);
                        break;
                    case LARGE_FILE_BODY:
                        downloadLargeFile(request);
                        break;
                    case LARGE_FILE_REQUEST:
                        responseToLargeFileRequest(request);
                        break;
                }
                request.release();
            }
        });
    }

    void storeFile(ByteBuf byteBuf, Path path) throws IOException{
        logger.debug(byteBuf.readableBytes());
        Files.deleteIfExists(path);
        FileChannel fileChannel = FileChannel.open(path, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
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
        byteBuf.writeBytes(peerId.toByteArray());
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
            sendLargeFileHeader(path);
        }
    }

    void responseToLargeFileRequest(ByteBuf request) {
        long id = request.readLong();
        if (idMap.containsKey(id)) {
            sendLargeFile(idMap.get(id));
        }
    }

    boolean sendLargeFileHeader(Path path) {
        String fileName = path.getFileName().toString();
        ByteBuf byteBuf = allocByteBuf(MessageType.LARGE_FILE_HEAD);
        Random random = new Random(new Date().getTime());
        long id = random.nextLong();
        pathMap.put(path, id);
        idMap.put(id, path);
        byteBuf.writeLong(id);
        byteBuf.writeBytes(fileName.getBytes());
        try {
            byteBuf.writeLong(Files.size(path));
            return sendByteBuf(byteBuf);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    boolean sendLargeFile(Path path) {
//        if (!sendLargeFileHeader(path)) return false;
        try {
            FileChannel fileChannel = FileChannel.open(path, StandardOpenOption.READ);
            ByteBuffer byteBuffer = ByteBuffer.allocate(1 << 20);
            long offset = 0;
            while (fileChannel.read(byteBuffer) > 0) {
                byteBuffer.flip();
                ByteBuf byteBuf = allocByteBuf(MessageType.LARGE_FILE_BODY);
                if (!pathMap.containsKey(path)) {
                    logger.warn("large file download not initiated");
                    return false;
                }
                byteBuf.writeLong(pathMap.get(path));
                byteBuf.writeLong(offset);
                logger.info("sending large file with offset: " + offset);
//                if (!sendByteBuf(byteBuf.writeBytes(byteBuffer))) break;
                sendByteBufNotawiate(byteBuf.writeBytes(byteBuffer));
                offset = fileChannel.position();
                byteBuffer.clear();
            }
        }
        catch (Exception e) {
            logger.catching(e);
        }
        return false;
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
            byteBuf.writeBytes(peerId.toByteArray());
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
            byteBuf.writeBytes(peerId.toByteArray());
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
            return true;
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    //await may contribute to error when transporting large file, and should use this function
    void sendByteBufNotawiate(final ByteBuf byteBuf) {
        try {
            FutureSend futureSend = MyPeer.clientPeerDHT.send(Number160.createHash(peerName)).object(Utils.encodeBase64(byteBuf)).start();
            futureSend.addListener(new BaseFutureListener<FutureSend>() {
                @Override
                public void operationComplete(FutureSend send) throws Exception {
                    byteBuf.release();
                    for (Object object: send.rawDirectData2().values()) {
                        logger.debug(object);
                        if (object.equals("OK")) {
                            logger.info("message sent succeeded");
                            return;
                        }
                        else if (object.equals("No receiver detected")) {
                            logger.warn("maybe you are not registered by target");
                        }
                    }
                    logger.warn("no peer replied");
                }

                @Override
                public void exceptionCaught(Throwable throwable) throws Exception {
                    logger.catching(throwable);
                }
            });
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    void initLargeFileDownloader(ByteBuf request) {
        final long id = request.readLong();
        byte[] bytes = new byte[request.readableBytes() - Long.BYTES];
        request.readBytes(bytes);
        final String fileName = new String(bytes);
        final long length = request.readLong();
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                FileChooser fileChooser = new FileChooser();
                fileChooser.setTitle("Save file");
                fileChooser.setInitialDirectory(new File(Utils.DOWNLOAD_PATH));
                fileChooser.setInitialFileName(fileName);
                File file = fileChooser.showSaveDialog(input.getContextMenu());
                if (file == null) return;
                Path largeFileDownloadPath = Paths.get(file.getAbsolutePath());
                LargeFileDownloader.startNewLargeFileDownloader(id, largeFileDownloadPath, length);
                ByteBuf byteBuf = allocByteBuf(MessageType.LARGE_FILE_REQUEST);
                byteBuf.writeLong(id);
                sendByteBuf(byteBuf);
            }
        });
//        Path path = Paths.get(Utils.DOWNLOAD_PATH, fileName);
//        LargeFileDownloader.startNewLargeFileDownloader(id, path, length);
    }

    boolean downloadLargeFile(ByteBuf request) {
        return LargeFileDownloader.receive(request);
    }

    void downloadSmallFile(ByteBuf request) {
        int fileNameLength = request.readInt();
        byte[] fileNameBytes = new byte[fileNameLength];
        request.readBytes(fileNameBytes, 0, fileNameLength);
        final String fileName = new String(fileNameBytes);
        final ByteBuf byteBuf = request.copy();
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                FileChooser fileChooser = new FileChooser();
                fileChooser.setTitle("Save file");
                fileChooser.setInitialDirectory(new File(Utils.DOWNLOAD_PATH));
                fileChooser.setInitialFileName(fileName);
                File file = fileChooser.showSaveDialog(input.getContextMenu());
                if (file == null) return;
                Path smallFileDownloadPath = Paths.get(file.getAbsolutePath());
                try {
                    storeFile(byteBuf, smallFileDownloadPath);
                    byteBuf.clear();
                    addRemoteFile(smallFileDownloadPath);
                }
                catch (Exception e) {
                    logger.catching(e);
                }
            }
        });
    }

    void downloadPicture(ByteBuf request) {
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
    }

    boolean checkImageFile(Path path) {
        String mimeType = new MimetypesFileTypeMap().getContentType(path.toFile());
        logger.debug(mimeType);
        return mimeType.contains("image");
    }

    boolean checkSmallFile(Path path) {
        //ToDo the small file size threshold is to be determined
        try {
            long size = FileChannel.open(path).size();
            return size < (1 << 20);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    ByteBuf allocByteBuf(MessageType messageType) {
        ByteBuf byteBuf = Unpooled.buffer();
        byteBuf.writeBytes(peerId.toByteArray());
        byteBuf.writeInt(messageType.ordinal());
        return byteBuf;
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

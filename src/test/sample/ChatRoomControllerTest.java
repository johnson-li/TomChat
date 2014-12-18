package sample;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.junit.Test;
import sample.ChatRoomController;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.Assert.*;

public class ChatRoomControllerTest {

    @Test
    public void testStoreFile() throws Exception {
        ChatRoomController chatRoomController = new ChatRoomController();
        Path path = Paths.get("/tmp/123456");
        ByteBuf byteBuf = Unpooled.buffer();
        byteBuf.writeBytes("hello\n".getBytes());
        chatRoomController.storeFile(byteBuf, path);
        assertTrue(Files.exists(path));
    }
}
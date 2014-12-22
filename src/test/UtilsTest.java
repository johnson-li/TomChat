import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.apache.commons.validator.routines.InetAddressValidator;
import org.junit.Test;
import sample.Utils;

import java.io.File;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.PosixFileAttributes;
import java.util.Map;
import java.util.Set;
import java.util.jar.Attributes;

import static org.junit.Assert.*;

public class UtilsTest {

    @Test
    public void testCheckIP() throws Exception {
        InetAddressValidator inetAddressValidator = new InetAddressValidator();
        assertFalse(inetAddressValidator.isValid("192.168.1.1"));
        assertFalse(inetAddressValidator.isValid("192.168.1.11111"));
    }

    @Test
    public void testCreateFile() throws Exception{
        long size = 12345;
        Path path = Paths.get("/tmp/123");
        Files.deleteIfExists(path);
        Files.createFile(path);
        ByteBuf byteBuf = Unpooled.buffer();
        byteBuf.writeChar('c');
        RandomAccessFile randomAccessFile = new RandomAccessFile(path.toFile(), "rw");
        randomAccessFile.setLength(size);
        assertEquals(size, Files.size(path));
    }
}
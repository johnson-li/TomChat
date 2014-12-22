package sample;

import io.netty.buffer.ByteBuf;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Date;
import java.util.HashMap;
import java.util.Random;

/**
 * Created by johnson on 12/22/14.
 */
public class LargeFileDownloader {
    static Logger logger = LogManager.getLogger();
    static HashMap<Long, LargeFileDownloader> DOWNLOADING_HASH_TABLE = new HashMap<Long, LargeFileDownloader>();

    private FileChannel fileChannel;
    private Path path;
    private long id;
    private long length;

    private LargeFileDownloader(long id, Path path, long length) {
        try {
            this.id = id;
            this.length = length;
            this.path = path;
            Files.deleteIfExists(path);
            Files.createFile(path);
            RandomAccessFile randomAccessFile = new RandomAccessFile(path.toFile(), "rw");
            randomAccessFile.setLength(length);
            fileChannel = FileChannel.open(path, StandardOpenOption.WRITE);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static boolean receive(ByteBuf byteBuf) {
        long id = byteBuf.readLong();
        if (DOWNLOADING_HASH_TABLE.containsKey(id)) DOWNLOADING_HASH_TABLE.get(id).writeIntoFile(byteBuf);
        else return false;
        return true;
    }

    public static boolean startNewLargeFileDownloader(long id, Path path, long length) {
        LargeFileDownloader largeFileDownloader = new LargeFileDownloader(id, path, length);
        DOWNLOADING_HASH_TABLE.put(largeFileDownloader.id, largeFileDownloader);
        return true;
    }

    void writeIntoFile(ByteBuf byteBuf) {
        long offset = byteBuf.readLong();
        try {
            this.length -= byteBuf.readableBytes();
            fileChannel.write(byteBuf.nioBuffer(), offset);
            if (this.length <= 0) completeDownloadFile();
        }
        catch (Exception e) {
            logger.catching(e);
        }
    }

    void completeDownloadFile() {
        logger.info("file download completed: " + path.toAbsolutePath());
        try {
            fileChannel.close();
            DOWNLOADING_HASH_TABLE.remove(this.id);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
}

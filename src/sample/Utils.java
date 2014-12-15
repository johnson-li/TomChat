package sample;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.apache.commons.validator.routines.InetAddressValidator;
import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by johnson on 11/27/14.
 */
public class Utils {
    public static final int TARGET_PORT = 4000;
    public static final int CLIENT_PORT = 4567;

    public static boolean checkIP(String ip) {
        InetAddressValidator inetAddressValidator = new InetAddressValidator();
        return inetAddressValidator.isValid(ip);
    }

    static String encodeBase64(byte[] bytes) {
        BASE64Encoder base64Encoder = new BASE64Encoder();
        return base64Encoder.encode(bytes);
    }

    static String encodeBase64(ByteBuf byteBuf) {
        byte[] bytes = new byte[byteBuf.readableBytes()];
        byteBuf.readBytes(bytes);
        return encodeBase64(bytes);
    }

    static byte[] decodeBase64Bytes(String str) throws IOException {
        BASE64Decoder base64Decoder = new BASE64Decoder();
        return base64Decoder.decodeBuffer(str);
    }

    static ByteBuf decodeBase64ByteBuf(String str) throws IOException{
        byte[] bytes = decodeBase64Bytes(str);
        ByteBuf byteBuf = Unpooled.buffer();
        byteBuf.writeBytes(bytes);
        return byteBuf;
    }
}

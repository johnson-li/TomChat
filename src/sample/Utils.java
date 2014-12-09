package sample;

import org.apache.commons.validator.routines.InetAddressValidator;

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
}

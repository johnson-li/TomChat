package test;

import org.apache.commons.validator.routines.InetAddressValidator;
import org.junit.Test;
import sample.Utils;

import static org.junit.Assert.*;

public class UtilsTest {

    

    @Test
    public void testCheckIP() throws Exception {
        InetAddressValidator inetAddressValidator = new InetAddressValidator();
        assertFalse(inetAddressValidator.isValid("192.168.1.1"));
        assertFalse(inetAddressValidator.isValid("192.168.1.11111"));
    }
}
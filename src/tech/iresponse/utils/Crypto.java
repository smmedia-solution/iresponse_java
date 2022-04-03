package tech.iresponse.utils;

import java.io.UnsupportedEncodingException;
import java.util.Base64;
import javax.xml.bind.DatatypeConverter;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.EncoderException;
import org.apache.commons.codec.net.QuotedPrintableCodec;

public class Crypto {

    public static String Base64Encode(String str) {
        return new String(Base64.getEncoder().encode(str.getBytes()));
    }

    public static String Base64Decode(String str) {
        return new String(Base64.getDecoder().decode(str));
    }

    public static String encodeToHex(String paramString) {
        try {
            char[] arrayOfChar = DatatypeConverter.printHexBinary(paramString.getBytes("UTF-8")).toCharArray();
            String str = "=";
            for (int b = 1; b <= arrayOfChar.length; ++b) {
                str = str + arrayOfChar[b - 1];
                if (b % 2 == 0){
                    str = str + "=";
                }
            }
            paramString = str.endsWith("=") ? str.substring(0, str.length() - 1) : str;
        } catch (UnsupportedEncodingException uns) {}
        return paramString;
    }

    public static String QuotaPrintEncode(String paramString) {
        try {
            return (new QuotedPrintableCodec()).encode(paramString);
        } catch (EncoderException encd) {
        return paramString;
        }
    }

    public static String QuotaPrintDecode(String paramString) {
        try {
            return (new QuotedPrintableCodec()).decode(paramString);
        } catch (DecoderException dcd) {
        return paramString;
        }
    }

    public static String BoucleBase64Decode(String str, int nb) {
        for (int i = 0; i < nb; i++){
            str = Base64Decode(str);
        }
        return str;
    }
}

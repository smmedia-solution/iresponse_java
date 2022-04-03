package tech.iresponse.utils;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PushbackInputStream;
import java.io.Reader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

public class CSV {

    private boolean stripMultipleNewlines;  //if
    private char separator;              //for
    private ArrayList<String> fields;    // int
    private boolean eofSeen;             //new
    private Reader rdIn;                //try;
    private static final int NUMMARK = 10;
    private static final char COMMA = ',';
    private static final char DQUOTE = '"';
    private static final char CRETURN = '\r';
    private static final char LFEED = '\n';
    private static final char SQUOTE = '\'';
    private static final char COMMENT = '#';
    public static final String key = "VU53aDhnMU1ZdEI0SWhuOXFlQVgvVFRaaGJVR2lOUUM4UjRWU0RLTFBhTVlmYnZnQUJONEQ5cmszR2hpUk1xRw==";

    public static Reader stripBom(InputStream in) throws IOException {
        PushbackInputStream pis = new PushbackInputStream(in, 3);
        byte[] b = new byte[3];
        int len = pis.read(b, 0, b.length);
        if ((b[0] & 0xFF) == 239 && len == 3) {
            if ((b[1] & 0xFF) == 187 && (b[2] & 0xFF) == 191){
                return new InputStreamReader(pis, "UTF-8");
            }
            pis.unread(b, 0, len);
        } else if (len >= 2) {
            if ((b[0] & 0xFF) == 254 && (b[1] & 0xFF) == 255){
                return new InputStreamReader(pis, "UTF-16BE");
            }
            if ((b[0] & 0xFF) == 255 && (b[1] & 0xFF) == 254){
                return new InputStreamReader(pis, "UTF-16LE");
            }
            pis.unread(b, 0, len);
        } else if (len > 0) {
            pis.unread(b, 0, len);
        }
        return new InputStreamReader(pis, "UTF-8");
    }

    public boolean hasNext() throws IOException {
        if (this.eofSeen){
            return false;
        }
        this.fields.clear();
        this.eofSeen = split(this.rdIn, this.fields);
        return this.eofSeen ? (!this.fields.isEmpty()) : true;
    }

    public List<String> next() {
        return this.fields;
    }

    private static boolean discardLinefeed(Reader in, boolean stripMultiple) throws IOException {
        if (stripMultiple) {
            in.mark(10);
            for (int j = in.read(); j != -1; j = in.read()) {
                char c = (char)j;
                if (c != '\r' && c != '\n') {
                    in.reset();
                    return false;
                }
                in.mark(10);
            }
            return true;
        }

        in.mark(10);
        int i = in.read();
        if (i == -1){
            return true;
        }
        if ((char)i != '\n'){
            in.reset();
        }
        return false;
    }

    private boolean skipComment(Reader in) throws IOException {
        int value;
        while ((value = in.read()) != -1) {
            char c = (char)value;
            if (c == '\r'){
                return discardLinefeed(in, this.stripMultipleNewlines);
            }
        }
        return true;
    }

    private boolean split(Reader in, ArrayList<String> fields) throws IOException {
        StringBuilder sb = new StringBuilder();
        int value;
        while ((value = in.read()) != -1) {
            char c = (char)value;
            switch (c) {
                case '\r': {
                    if (sb.length() > 0) {
                        fields.add(sb.toString());
                        sb.delete(0, sb.length());
                    }
                    return discardLinefeed(in, this.stripMultipleNewlines);
                }
                case '\n': {
                    if (sb.length() > 0) {
                        fields.add(sb.toString());
                        sb.delete(0, sb.length());
                    }
                    return this.stripMultipleNewlines ? discardLinefeed(in, this.stripMultipleNewlines) : false;
                }
                case '"':
                    while ((value = in.read()) != -1) {
                        c = (char)value;
                        if (c == '"') {
                            in.mark(10);
                            if ((value = in.read()) == -1) {
                                if (sb.length() > 0) {
                                    fields.add(sb.toString());
                                    sb.delete(0, sb.length());
                                }
                                return true;
                            }
                            if ((c = (char)value) == '"') {
                                sb.append('"');
                                continue;
                            }
                            if (c == '\r') {
                                if (sb.length() > 0) {
                                    fields.add(sb.toString());
                                    sb.delete(0, sb.length());
                                }
                                return discardLinefeed(in, this.stripMultipleNewlines);
                            }
                            if (c == '\n') {
                                if (sb.length() > 0) {
                                    fields.add(sb.toString());
                                    sb.delete(0, sb.length());
                                }
                                return this.stripMultipleNewlines ? discardLinefeed(in, this.stripMultipleNewlines) : false;
                            }
                            in.reset();
                            break;
                        }
                        sb.append(c);
                    }

                    if (value == -1) {
                        if (sb.length() > 0) {
                            fields.add(sb.toString());
                            sb.delete(0, sb.length());
                        }
                        return true;
                    }
                    continue;
            }

            if (c == this.separator) {
                fields.add(sb.toString());
                sb.delete(0, sb.length());
                continue;
            }
            if (c == '#' && fields.isEmpty() && sb.toString().trim().isEmpty()) {
                boolean eof = skipComment(in);
                if (eof){
                    return eof;
                }
                sb.delete(0, sb.length());
                continue;
            }
            sb.append(c);
        }

        if (sb.length() > 0) {
            fields.add(sb.toString());
            sb.delete(0, sb.length());
        }
        return true;
    }

    public CSV(boolean stripMultipleNewlines, char separator, Reader input) {
        this.stripMultipleNewlines = stripMultipleNewlines;
        this.separator = separator;
        this.fields = new ArrayList();
        this.eofSeen = false;
        this.rdIn = new BufferedReader(input);
    }

    public CSV(boolean stripMultipleNewlines, char separator, InputStream input) throws IOException {
        this.stripMultipleNewlines = stripMultipleNewlines;
        this.separator = separator;
        this.fields = new ArrayList();
        this.eofSeen = false;
        this.rdIn = new BufferedReader(stripBom(input));
    }
}

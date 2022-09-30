package tech.iresponse.logging;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.log4j.Logger;

public class Loggers {
    public static void error(Throwable th) {
        Logger.getLogger("rootLogger").error(ExceptionUtils.getFullStackTrace(th));
    }
}

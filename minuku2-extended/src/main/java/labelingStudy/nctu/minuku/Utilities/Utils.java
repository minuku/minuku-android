package labelingStudy.nctu.minuku.Utilities;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * Created by Lawrence on 2018/4/19.
 */

public class Utils {


    public static String getStackTrace(final Throwable throwable) {
        final StringWriter sw = new StringWriter();
        final PrintWriter pw = new PrintWriter(sw, true);
        throwable.printStackTrace(pw);
        return sw.getBuffer().toString();
    }

}

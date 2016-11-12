package top.guinguo.util;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by guin_guo on 2016/11/12.
 */
public class HttpUtils {

    public static String toFileText(File file) {
        if (file != null) {
            StringBuffer sb = new StringBuffer();
            BufferedReader br = null;
            try {
                br = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
                String line = br.readLine();
                while (line != null) {
                    sb.append(line).append(Constants.NEWLINE);
                    line = br.readLine();
                }
                return sb.toString();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return "";
    }
}

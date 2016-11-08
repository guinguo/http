package top.guinguo.util;

import javax.swing.*;

/**
 * Created by guin_guo on 2016/11/8.
 */
public class DialogUtil {
    /**
     * 弹窗,提示信息为msg
     * @param msg
     */
    public static void showMsg(String msg) {
        Object[] options = { "OK"};
        JOptionPane.showOptionDialog(null, msg, "Warning",
                JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE,
                null, options, options[0]);
    }
}

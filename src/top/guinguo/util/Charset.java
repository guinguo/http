package top.guinguo.util;

/**
 * Created by guin_guo on 2016/11/7.
 */
public enum Charset {
    UTF8("UTF-8");

    private String value;

    Charset(String value) {
        this.value = value;
    }
    public String getValue() {
        return value;
    }
}

package top.guinguo.http;

/**
 * Created by guin_guo on 2016/11/7.
 */
public class Header {
    private String key;
    private String value;

    public Header() {
    }

    public Header(String key, String value) {
        this.key = key;
        this.value = value;
    }
    public static Header parse(String key_value) {
        int index = key_value.indexOf(":");
        return new Header(
                key_value.substring(0, index).trim(),
                key_value.substring(index + 1).trim());
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}

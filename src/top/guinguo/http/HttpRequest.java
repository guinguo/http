package top.guinguo.http;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by guin_guo on 2016/11/7.
 */
public class HttpRequest {
    private URL url;
    private RequsetType requsetType;
    private List<Header> headers = new ArrayList<>();
    private Long contentLength;

    public HttpRequest(InputStream inputStream) {

    }

    public enum RequsetType {
        GET,
        POST,
        DELETE,
        PUT
    }

    public static class URL{
        private String route;
        private String path;
        private String queryString;
        private Map<String, String> params = new HashMap<>();

        public URL(String route) {
            String[] parts = route.split("\\?");
            this.route = route;
            if (parts.length > 1) {
                this.path = parts[0];
                this.queryString = parts[1];
            }
            if (queryString != null) {
                String[] kvs = queryString.split("&");
                for (int i=0;i<kvs.length;i++) {
                    String[] kv = kvs[i].split("=");
                    if (kv.length == 2) {
                        params.put(kv[0], kv[1]);
                    }
                }
            }
        }
    }
}

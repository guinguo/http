package top.guinguo.http;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by guin_guo on 2016/11/7.
 */
public class HttpRequest {
    private URL url;
    private RequsetType requestTpye;
    private Header formContentHeader;
    private List<Header> headers = new ArrayList<>();
    private Long contentLength;
    private Map<String, String> postParams = new HashMap<>();

    public HttpRequest(InputStream inputStream) throws IOException {
        InputStreamReader raw = new InputStreamReader(inputStream);
        BufferedReader reader = new BufferedReader(raw);
        String inputLine = reader.readLine();
        while (inputLine != null && !inputLine.isEmpty()) {
            if (requestTpye == null) {
                parseReqType(inputLine);
            } else {
                addHeader(inputLine);
            }
            inputLine = reader.readLine();
        }
        if (formContentHeader.getValue().trim().equals("application/x-www-form-urlencoded")) {

        }
    }

    private void addHeader(String inputLine) {
        Header header = Header.parse(inputLine);
        if (header.getKey().equals("Content-Length")) {
            this.contentLength = Long.valueOf(header.getValue());
        } else if (header.getKey().equals("Content-Type")) {
            this.formContentHeader = header;
        }
        this.headers.add(header);
    }

    private void parseReqType(String value) {
        String[] values = value.split(" ");
        requestTpye = RequsetType.valueOf(values[0]);
        url = new URL(values[1]);
    }

    public URL getUrl() {
        return url;
    }

    public RequsetType getRequestTpye() {
        return requestTpye;
    }

    public List<Header> getHeaders() {
        return headers;
    }

    public Long getContentLength() {
        return contentLength;
    }

    public Map<String, String> getPostParams() {
        return postParams;
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

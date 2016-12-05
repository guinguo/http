package top.guinguo.http;



import top.guinguo.util.Constants;

import java.io.*;
import java.util.*;

/**
 * Created by guin_guo on 2016/11/7.
 */
public class HttpRequest {
    private URL url;
    private RequsetType requestTpye;
    private Header formContentHeader;
    private Map<String,Header> headers = new HashMap<>();
    private Long contentLength;
    private Map<String, String> postParams = new HashMap<>();
    private StringBuffer fileContent = new StringBuffer();//not support chinese

    public HttpRequest(InputStream inputStream) throws IOException {
        InputStreamReader raw = new InputStreamReader(inputStream,"UTF-8");
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
        if (!this.getUrl().getRoute().startsWith("http")) {
            url = new URL(this.headers.get("Host").getValue()+this.getUrl().getRoute());
        }
        if (formContentHeader != null) {
            if (formContentHeader.getValue().trim().equals("application/x-www-form-urlencoded")) {
                //get query parameter
            } else {
                // file content
                inputLine = reader.readLine();
                String fileSeparator = "--" + formContentHeader.getValue().substring(formContentHeader.getValue().indexOf("=") + 1) + "--";
                while (inputLine != null && !fileSeparator.equals(inputLine)) {
                    fileContent.append(inputLine).append(top.guinguo.util.Constants.NEWLINE);
                    inputLine = reader.readLine();
                }
                fileContent.append(fileSeparator);
            }
        }
    }

    private void addHeader(String inputLine) {
        Header header = Header.parse(inputLine);
        if (header.getKey().equals("Content-Length")) {
            this.contentLength = Long.valueOf(header.getValue());
        } else if (header.getKey().equals("Content-Type")) {
            this.formContentHeader = header;
        }
        this.headers.put(header.getKey(),header);
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

    public Map<String, Header> getHeaders() {
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

        public String getRoute() {
            return route;
        }

        public String getPath() {
            return path;
        }

        public String getQueryString() {
            return queryString;
        }

        public Map<String, String> getParams() {
            return params;
        }
    }

    public StringBuffer getFileContent() {
        return fileContent;
    }

    public Header getFormContentHeader() {
        return formContentHeader;
    }
}

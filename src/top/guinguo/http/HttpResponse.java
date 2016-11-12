package top.guinguo.http;

import top.guinguo.util.Charset;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Date;

/**
 * Created by guin_guo on 2016/11/7.
 */
public class HttpResponse {
    private ResponseStatusCode code;

    private OutputStream outputStream;
    private Charset charset = Charset.UTF8;
    private ContentType contentType;
    private long contentLengthInBytes;
    private static final String CONN_CLOSE_HEADER_STRING =
            new Header("Connection", "close").toString();

    public HttpResponse(OutputStream outputStream) {
        this.outputStream = outputStream;
    }

    public void renderByType(String content, ContentType type) {
        this.contentType = type;
        this.renderText(content);
    }

    public void renderHTML(String html) {
        this.contentType = ContentType.HTML;
        this.renderText(html);
    }

    public void renderText(String text) {
        this.contentLengthInBytes = text.length();
        PrintStream pstream = writerHeaders();
        pstream.println(text);
        pstream.flush();
        pstream.close();
    }

    public PrintStream writerHeaders(){
        PrintStream pstream = new PrintStream(outputStream);
        pstream.println(code.getResponseHeader());
        pstream.println(getContentType());
        pstream.println(new Header("Content-Length", String.valueOf(contentLengthInBytes)));
        pstream.println(CONN_CLOSE_HEADER_STRING);
        pstream.println(Header.parse("Date: " + new Date()));
        pstream.println(Header.parse("Server: HttpServer/1.1"));
        pstream.println();
        pstream.flush();
        return pstream;
    }

    public enum ContentType {
        TEXT("text/plain"),
        HTML("text/html"),
        CSS("text/css"),
        PNG("image/png"),
        JS("text/javascript"),
        JSON("application/json");

        private String mimeType;

        private ContentType(String value) {
            this.mimeType = value;
        }

        public String getMimeType() {
            return mimeType;
        }
        
    }

    public enum ResponseStatusCode {
        OK(200, "OK"),
        BadRequest(400, "Bad Request"),
        NotFound(404, "Not Found"),
        InternalServerError(500, "Internal Server Error");

        private int code;
        private String phrase;

        ResponseStatusCode(int code, String phrase) {
            this.code = code;
            this.phrase = phrase;
        }

        public String getResponseHeader() {
            return "HTTP/1.1 " + code + " " + phrase;
        }

        public byte[] getResponseHeaderBytes() {
            return getResponseHeader().getBytes();
        }
    }

    public ResponseStatusCode getCode() {
        return code;
    }

    public void setCode(ResponseStatusCode code) {
        this.code = code;
    }

    public OutputStream getOutputStream() {
        return outputStream;
    }

    public void setOutputStream(OutputStream outputStream) {
        this.outputStream = outputStream;
    }

    public Charset getCharset() {
        return charset;
    }

    public void setCharset(Charset charset) {
        this.charset = charset;
    }

    public ContentType getContentType() {
        return contentType;
    }

    public void setContentType(ContentType contentType) {
        this.contentType = contentType;
    }

    public long getContentLengthInBytes() {
        return contentLengthInBytes;
    }

    public void setContentLengthInBytes(long contentLengthInBytes) {
        this.contentLengthInBytes = contentLengthInBytes;
    }

    public ContentType switchType(String extension) {
        if (extension != null) {
            if (extension.toLowerCase().endsWith(".txt")) {
                return ContentType.TEXT;
            } else if (extension.toLowerCase().endsWith(".css")) {
                return ContentType.CSS;
            } else if (extension.toLowerCase().endsWith(".js")) {
                return ContentType.JS;
            } else if (extension.toLowerCase().endsWith(".json")) {
                return ContentType.JSON;
            } else if (extension.toLowerCase().endsWith(".html")) {
                return ContentType.HTML;
            } else {
                return null;
            }
        }
        return null;
    }
}

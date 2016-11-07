package top.guinguo.http;

import top.guinguo.util.Charset;

import java.io.OutputStream;

/**
 * Created by guin_guo on 2016/11/7.
 */
public class HttpResponse {
    private ResponseStatusCode code;

    private OutputStream outputStream;
    private Charset charset = Charset.UTF8;
    private ContentType contentType;
    private long contentLengthInBytes;

    public HttpResponse(OutputStream outputStream) {
        this.outputStream = outputStream;
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
}

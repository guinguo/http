package top.guinguo.server;

import top.guinguo.http.Header;
import top.guinguo.http.HttpRequest;
import top.guinguo.http.HttpResponse;
import top.guinguo.util.Constants;
import top.guinguo.util.HttpUtils;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.*;

import static top.guinguo.client.Client.NEWLINE;

/**
 * Created by guin_guo on 2016/11/7.
 */
public class Server extends JFrame {
    private static int port = 80;
    private ServerSocket serverSocket;
    private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    private JTextArea jta = new JTextArea();

    public static void main(String[] args) {
        new Server();
    }
    public Server() {
        setLayout(new BorderLayout());
        jta.setBackground(Color.BLACK);
        jta.setForeground(Color.WHITE);
        jta.setEditable(false);
        add(new JScrollPane(jta), BorderLayout.CENTER);

        setTitle("HttpServer");
        setSize(800, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setVisible(true);
        setLocation(1000,100);
        init();

        try {
            serverSocket = new ServerSocket(port);
            log("HttpServer is started");


            while (true) {
                Socket socket = serverSocket.accept();
                log("收到请求，时间是" + sdf.format(new Date()));

                InetAddress inetAddress = socket.getInetAddress();
                log("客户主机名为" + inetAddress.getHostName());
                log("客户IP地址为" + inetAddress.getHostAddress());

                Thread task = new Thread(new HandleClient(socket,this));
                task.start();
            }
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
    }

    class HandleClient implements Runnable {
        private Socket socket;
        private Server server;

        public HandleClient(Socket socket, Server server) {
            this.socket = socket;
            this.server = server;
        }

        @Override
        public void run() {
            try {
                HttpRequest request = new HttpRequest(socket.getInputStream());
                HttpResponse response = new HttpResponse(socket.getOutputStream());
                this.server.handle(request, response);


            } catch (IOException e) {
                System.err.println(e.getMessage());
                jta.append(e.getMessage());
            } finally {
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                    jta.append(e.getMessage());
                }
            }

        }

    }

    public void init(){
        File publicDir = new File(Constants.PUBLICPATH);
        if (!publicDir.exists()) {
            publicDir.mkdir();
        }
    }
    public void handle(HttpRequest req, HttpResponse resp) {
        log(req.getRequestTpye() + " " + req.getUrl().getRoute());
        //distribute method
        if (req.getRequestTpye().equals(HttpRequest.RequsetType.GET)) {
            this.doGet(req, resp);
        } else if (req.getRequestTpye().equals(HttpRequest.RequsetType.POST)) {
            this.doPost(req, resp);
        } else if (req.getRequestTpye().equals(HttpRequest.RequsetType.PUT)) {
            this.doPut(req, resp);
        } else if (req.getRequestTpye().equals(HttpRequest.RequsetType.DELETE)) {
            this.doDelete(req, resp);
        } else {
            String errMsg1 = ("http.method_not_implemented");
            Object[] errArgs = new Object[]{req.getRequestTpye()};
            errMsg1 = MessageFormat.format(errMsg1, errArgs);
            resp.setCode(HttpResponse.ResponseStatusCode.InternalServerError);
            resp.renderByType(errMsg1, HttpResponse.ContentType.TEXT);
        }
    }

    private void doGet(HttpRequest req, HttpResponse resp) {
        String resourceUrl;
        if (req.getUrl().getPath() != null) {
            resourceUrl = req.getUrl().getPath().split(req.getHeaders().get("Host").getValue())[1];
        } else {
            resourceUrl = req.getUrl().getRoute().split(req.getHeaders().get("Host").getValue())[1];
        }
        try {
            File file = new File(Constants.PUBLICPATH + resourceUrl);
            if (file.exists()) {
                String responseBody = HttpUtils.toFileText(file);
                HttpResponse.ContentType contentType = resp.switchType(file.getName());
                if (contentType != null) {
                    resp.setCode(HttpResponse.ResponseStatusCode.OK);
                    resp.renderByType(responseBody, contentType);
                    return;
                }
            }
        } catch (Exception e) {
            resp.setCode(HttpResponse.ResponseStatusCode.InternalServerError);
            resp.renderText("ERROR");
            e.printStackTrace();
            return;
        }
        resp.setCode(HttpResponse.ResponseStatusCode.NotFound);
        resp.renderText("404");
        return;
    }

    private void doPost(HttpRequest req, HttpResponse resp) {
        try {
            if (!req.getFileContent().toString().isEmpty()) {
                String[] fcs = req.getFileContent().toString().split(NEWLINE);
                String separator = fcs[0];
                Header header = Header.parse(fcs[1]);
                String filN = header.getValue().split(";")[2];
                String fileName = filN.substring(filN.indexOf("\"") + 1, filN.length() - 1);
                File file = new File(Constants.PUBLICPATH + "\\" + fileName);
                Long tm = null;
                if (file.exists()) {
                    tm = System.currentTimeMillis();
                    file = new File(Constants.PUBLICPATH + "\\" + fileName + "-" + tm);
                }
                PrintWriter writer = new PrintWriter(new FileWriter(file));
                for (int i = 4; i < fcs.length; i++) {
                    if (fcs[i].startsWith(separator)) {
                        break;
                    }
                    if (i < fcs.length - 1) {
                        if (fcs[i + 1].startsWith(separator)) {
                            writer.print(fcs[i]);
                            break;
                        } else {
                            writer.println(fcs[i]);
                        }
                    }
                }
                writer.close();
                resp.setCode(HttpResponse.ResponseStatusCode.OK);
                resp.setContentType(HttpResponse.ContentType.JSON);
                String retName = tm == null ? fileName : fileName + "-" + tm;
                resp.renderText("{\"status\":\"success\",\"filename\":\"" + retName + "\"}");
            } else {
                resp.setCode(HttpResponse.ResponseStatusCode.BadRequest);
                resp.setContentType(HttpResponse.ContentType.JSON);
                resp.renderText("{\"status\":\"fail\",\"mg\":\"without special any request body.\"}");
            }
        } catch (IOException e) {
            resp.setCode(HttpResponse.ResponseStatusCode.InternalServerError);
            resp.renderText("ERROR");
            e.printStackTrace();
            return;
        }
    }

    private void doPut(HttpRequest req, HttpResponse resp) {
        try {
            if (!req.getFileContent().toString().isEmpty()) {
                String[] fcs = req.getFileContent().toString().split(NEWLINE);
                String separator = fcs[0];
                Header header = Header.parse(fcs[1]);
                String filN = header.getValue().split(";")[2];
                String fileName = filN.substring(filN.indexOf("\"")+1, filN.length()-1);
                File file = new File(Constants.PUBLICPATH + "\\" + fileName);
                PrintWriter writer = new PrintWriter(new FileWriter(file));
                for (int i = 4;i<fcs.length;i++) {
                    if (fcs[i].startsWith(separator)) {
                        break;
                    }
                    writer.println(fcs[i]);
                }
                writer.close();
                resp.setCode(HttpResponse.ResponseStatusCode.OK);
                resp.setContentType(HttpResponse.ContentType.JSON);
                resp.renderText("{\"status\":\"success\",\"filename\":\"" + fileName + "\"}");
            } else {
                resp.setCode(HttpResponse.ResponseStatusCode.BadRequest);
                resp.setContentType(HttpResponse.ContentType.JSON);
                resp.renderText("{\"status\":\"fail\",\"mg\":\"without special any request body.\"}");
            }
        } catch (IOException e) {
            resp.setCode(HttpResponse.ResponseStatusCode.InternalServerError);
            resp.renderText("ERROR");
            e.printStackTrace();
            return;
        }
    }

    private void doDelete(HttpRequest req, HttpResponse resp) {
        String resourceUrl;
        if (req.getUrl().getPath() != null) {
            resourceUrl = req.getUrl().getPath().split(req.getHeaders().get("Host").getValue())[1];
        } else {
            resourceUrl = req.getUrl().getRoute().split(req.getHeaders().get("Host").getValue())[1];
        }
        try {
            File file = new File(Constants.PUBLICPATH + resourceUrl);
            if (file.exists()) {
                file.delete();
                resp.setCode(HttpResponse.ResponseStatusCode.OK);
                resp.setContentType(HttpResponse.ContentType.JSON);
                resp.renderText("{\"status\":\"success\",\"filename\":\"" + resourceUrl + "\"}");
            }
        } catch (Exception e) {
            resp.setCode(HttpResponse.ResponseStatusCode.InternalServerError);
            resp.renderText("ERROR");
            e.printStackTrace();
            return;
        }
        resp.setCode(HttpResponse.ResponseStatusCode.NotFound);
        resp.renderText("404");
        return;
    }

    public void log(String msg) {
        //jta.append
        jta.append(sdf.format(new Date()) + " INFO " + " "+msg+"\n");
    }
}

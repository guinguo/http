package top.guinguo.server;

import top.guinguo.http.HttpRequest;
import top.guinguo.http.HttpResponse;

import javax.swing.*;
import java.awt.*;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by guin_guo on 2016/11/7.
 */
public class Server extends JFrame {
    private static int port = 8000;
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
                DataInputStream inputFromClient =
                        new DataInputStream(socket.getInputStream());

                byte[] bytes = new byte[1024];
                int len;
                while ((len = inputFromClient.read(bytes)) >= 0) {
                    jta.append(new String(bytes,0,len));
                    System.out.write(bytes,0,len);
                }

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
    public void handle(HttpRequest request, HttpResponse response) {
    }
    public void log(String msg) {
        //jta.append
        jta.append(sdf.format(new Date()) + " INFO " + " "+msg+"\n");
    }
}

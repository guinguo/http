package top.guinguo.client;

import top.guinguo.http.Header;
import top.guinguo.http.HttpResponse;
import top.guinguo.util.DialogUtil;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;

/**
 * Created by guin_guo on 2016/11/7.
 */
public class Client extends JFrame {
    public static String host;
    public static int port;
    public static String  lastQueryUrl;

    //input for url
    private JTextField urlInput = new JTextField();

    //display for receive from server
    private JTextArea jta = new JTextArea();

    private String labels[] = { "GET", "POST", "DELETE", "PUT"};
    private JComboBox<String> selects = new JComboBox<>(labels);

    private JTextArea header = new JTextArea();
    private JTextArea body = new JTextArea();
    //IO
    private DataOutputStream toServer;
    private InputStream fromServer;

    public static void main(String[] args) {
        new Client();
    }

    public Client() {
        JPanel p = new JPanel();
        p.setBorder(BorderFactory.createTitledBorder("REQUEST"));
        p.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(2, 2, 2, 2); // top padding
        c.gridx = 0;
        c.gridy = 0;
        p.add(new JLabel("HTTP://"),c);
        c.gridx = 1;
        c.weightx = 1.5;
        p.add(urlInput,c);
        c.gridx = 2;
        c.weightx = 0.1;
        p.add(selects,c);
        JButton send = new JButton("Send");
        c.gridx = 3;
        c.weightx = 0.1;
        p.add(send,c);
        header.setPreferredSize(new Dimension(100,230));
        c.gridy = 1;
        c.gridx = 0;
        c.gridwidth = 2;
        header.setBorder(BorderFactory.createTitledBorder("HEADER"));
        p.add(header,c);
        body.setPreferredSize(new Dimension(300,230));
        c.gridy = 1;
        c.gridx = 2;
        c.gridwidth = 4;
        body.setBorder(BorderFactory.createTitledBorder("BODY"));
        p.add(body,c);
        urlInput.setHorizontalAlignment(JTextField.LEFT);

        setLayout(new BorderLayout());
        add(p, BorderLayout.NORTH);
        jta.setEditable(false);
        add(new JScrollPane(jta), BorderLayout.CENTER);

        //addActionListener
        send.addActionListener(new ButtonListener());

        setTitle("Client");
        setSize(800, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setVisible(true);
        setLocation(200,100);
        this.host = "localhost";
        this.port = 8000;
    }

    private class ButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            Socket socket = null;
            try {
                String method = selects.getSelectedItem().toString();
                String url = urlInput.getText();
                String requestContent = header.getText();
                try {
                    socket = new Socket(host,port);
                    socket.setSendBufferSize(8*1024*1024);//8M
                    socket = sendSocket(socket, url, method, requestContent);
                    //send 2 server
                    if (socket != null) {
                        fromServer = socket.getInputStream();

                    }
                } catch (IOException ex) {
                    jta.append(ex.getMessage() + "\n");
                }

                //2.print response
                HttpResponse response = new HttpResponse(socket.getInputStream());//TODO
            } catch (IOException e1) {
                System.err.println(e1.getMessage());
            } finally {
                try {
                    toServer.close();
                    fromServer.close();
                    socket.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        }
    }

    public Socket sendSocket(Socket socket, String url, String method, String content) {
        StringBuffer sb = new StringBuffer();
        //first line   GET url HTTP/1.1 \n\r
        sb.append(method + " ");
        if (url == null || url.isEmpty()) {
            DialogUtil.showMsg("请输入请求地址");
            return null;
        }
        if (!url.startsWith("http://")) {
            url = "http://" + url;
        }
        sb.append(url + " " + "HTTP/1.1").append("\n\r");
        //second header body
        if (method.equals("POST") || method.equals("PUT")) {
            String[] headers = content.split("\n");
            Header header = null;
            for (int i = 0;i<headers.length;i++) {
                if (headers[i].toLowerCase().startsWith("header-type")) {
                    header = Header.parse(headers[i]);
                    break;
                }
            }
            if (header == null) {
                header = Header.parse("Content-Type: application/x-www-form-urlencoded");
            }
            sb.append(header.toString());
        }
        sb.append(content);
        jta.append(sb.toString()+"\n");
        try {
            socket.setSendBufferSize(sb.toString().getBytes().length+1024);
            toServer = new DataOutputStream(socket.getOutputStream());
            toServer.writeBytes(sb.toString());
            toServer.flush();
        } catch (IOException e) {
            e.printStackTrace();
            DialogUtil.showMsg(e.getMessage());
        }
        return socket;
    }
    public void log(String msg) {
        //jta.append
        jta.append(msg);
    }
}

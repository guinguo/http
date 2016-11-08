package top.guinguo.client;

import top.guinguo.http.HttpResponse;
import top.guinguo.util.DialogUtil;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.URL;

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

    private JTextArea content = new JTextArea("请在请求头与请求体之间用空行隔开");
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
        content.setPreferredSize(new Dimension(200,230));
        c.gridy = 1;
        c.gridx = 0;
        c.gridwidth = 4;
        content.setBorder(BorderFactory.createTitledBorder("content"));
        p.add(content,c);
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
        this.content.setText("");
    }

    private class ButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            Socket socket = null;
            try {
                String method = selects.getSelectedItem().toString();
                String url = urlInput.getText();
                String requestContent = content.getText();

                DialogUtil.showMsg(requestContent);
                try {
                    socket = new Socket(host,port);
                    socket = toSocket(socket, url, method, requestContent);
                    //send 2 server
                    if (socket != null) {
                        toServer.flush();
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

    public Socket toSocket(Socket socket, String url, String method, String content) {
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
        URL u = null;
        try {
            u = new URL(url);
        } catch (MalformedURLException e) {
            e.printStackTrace();
            DialogUtil.showMsg(e.getMessage());
        }
        //second header body
        sb.append(content);
        try {
            toServer = new DataOutputStream(socket.getOutputStream());
            toServer.writeBytes(sb.toString());
        } catch (IOException e) {
            e.printStackTrace();
            DialogUtil.showMsg(e.getMessage());
        }
        return socket;
    }
}

package top.guinguo.client;

import top.guinguo.http.HttpResponse;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.DataInputStream;
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

    //input for url
    private JTextField urlInput = new JTextField();

    //display for receive from server
    private JTextArea jta = new JTextArea();

    private String labels[] = { "GET", "POST", "DELETE", "PUT"};
    private JComboBox<String> selects = new JComboBox<>(labels);

    private JTextArea content = new JTextArea();
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
        p.add(content,c);
        urlInput.setHorizontalAlignment(JTextField.LEFT);

        setLayout(new BorderLayout());
        add(p, BorderLayout.NORTH);
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
                String requestContent = content.getText();

                try {
                    socket = new Socket(host,port);
                    socket = toSocket(url, method, requestContent);

                    //send 2 server
                    toServer.flush();
                    fromServer = socket.getInputStream();
                } catch (IOException ex) {
                    jta.append(ex.getMessage() + "\n");
                }


                //1.get response
                //2.print response
                HttpResponse response = new HttpResponse(socket.getOutputStream());//TODO
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

    public Socket toSocket(String url, String method, String content) {
        //first line   GET url HTTP/1.1
        //\\n\\r
        //second header
        //\\n\\r
        //third body
//        toServer = new DataOutputStream(socket.getOutputStream());
        return null;
    }
}

package top.guinguo.client;

import top.guinguo.http.Header;
import top.guinguo.http.HttpResponse;
import top.guinguo.util.DialogUtil;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.Socket;

/**
 * Created by guin_guo on 2016/11/7.
 */
public class Client extends JFrame {
    public static String host;
    public static int port;
    public static String  lastQueryUrl;
    public static String NEWLINE = top.guinguo.util.Constants.NEWLINE;

    //input for url
    private JTextField urlInput = new JTextField();

    //display for receive from server
    private JTextArea jta = new JTextArea();

    JButton send = new JButton("send");
    private JButton openFile = new JButton("open");
    private JButton clear = new JButton("clear");
    private JFileChooser fileChooser = new JFileChooser();
    private File selectFile = null;
    private Long boundary = null;

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
        c.gridx = 3;
        c.weightx = 0.1;
        p.add(send,c);
        header.setPreferredSize(new Dimension(100,230));
        c.gridy = 1;
        c.gridx = 0;
        c.gridwidth = 2;
        header.setBorder(BorderFactory.createTitledBorder("HEADER"));
        p.add(header,c);
        body.setPreferredSize(new Dimension(300,160));
        c.gridy = 1;
        c.gridx = 2;
        c.gridwidth = 4;
        body.setBorder(BorderFactory.createTitledBorder("BODY"));
        p.add(body,c);
        clear.setPreferredSize(new Dimension(50,30));
        c.insets = new Insets(200, 2, 2, 2); // top padding
        c.gridy = 1;
        c.gridx = 2;
        c.gridwidth = 1;
        p.add(clear,c);
        openFile.setPreferredSize(new Dimension(50,30));
        c.insets = new Insets(200, 2, 2, 2); // top padding
        c.gridy = 1;
        c.gridx = 3;
        c.gridwidth = 1;
        p.add(openFile,c);
        urlInput.setHorizontalAlignment(JTextField.LEFT);

        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fileChooser.setFileFilter(new FileNameExtensionFilter("文本文件", "txt", "js", "css",
                "java", "php", "html", "htm"));

        setLayout(new BorderLayout());
        add(p, BorderLayout.NORTH);
        jta.setEditable(false);
        add(new JScrollPane(jta), BorderLayout.CENTER);

        //addActionListener
        openFile.addActionListener(new ButtonListener());
        send.addActionListener(new ButtonListener());
        clear.addActionListener(new ButtonListener());

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
            JButton button = (JButton) e.getSource();
            if (button.getText().equals("send")) {
                Socket socket = null;
                boundary = null;
                try {
                    String method = selects.getSelectedItem().toString();
                    String url = urlInput.getText();
                    String requestHeader = header.getText();
                    String requestBody   = body.getText();
                    try {
                        socket = new Socket(host,port);
                        socket.setSendBufferSize(8*1024*1024);//8M
                        socket = sendSocket(socket, url, method, requestHeader,requestBody);
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
            } else if (button.getText().equals("open")) {
                fileChooser.showDialog(new JLabel(), "选择文件");
                selectFile =fileChooser.getSelectedFile();
            } else if (button.getText().equals("clear")) {
                jta.setText("");
            }
        }
    }

    public Socket sendSocket(Socket socket, String url, String method, String requestHeader, String requestBody) {
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
        sb.append(url + " " + "HTTP/1.1").append(NEWLINE);
        //second header body
        sb = prepareReq(sb, method, requestHeader,requestBody);
        if (!method.equals("GET")) {
            sb.append(NEWLINE);
        }
        if (boundary != null) {
            sb.append("------httpClient" + boundary).append(NEWLINE);
            sb.append(Header.parse("Content-Disposition: form-data; name=\"file\"; filename=\"" + selectFile.getName() + "\"")).append(NEWLINE);
            sb.append(Header.parse("Content-Type: text/html")).append(NEWLINE).append(NEWLINE);
            sb.append(toFileText());
            sb.append("------httpClient" + boundary);
            if (requestBody.isEmpty()) {
                sb.append("--");
            }
            sb.append(NEWLINE);
        }
        if (!requestBody.isEmpty()) {
            if (boundary != null) {
                sb.append(Header.parse("Content-Disposition: form-data;")).append(NEWLINE);
            }
            sb.append(requestBody).append(NEWLINE);
            if (boundary != null) {
                sb.append("------httpClient" + boundary).append("--");
            }
            sb.append(NEWLINE);
        }
        jta.append(sb.toString()+NEWLINE);
        try {
            toServer = new DataOutputStream(socket.getOutputStream());
            toServer.writeBytes(sb.toString());
            toServer.flush();
        } catch (IOException e) {
            e.printStackTrace();
            DialogUtil.showMsg(e.getMessage());
        }
        return socket;
    }

    private String toFileText() {
        if (selectFile != null) {
            StringBuffer sb = new StringBuffer();
            try {
                BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(selectFile)));
                String line = br.readLine();
                while (line != null) {
                    sb.append(line).append(NEWLINE);
                    line = br.readLine();
                }
                return sb.toString();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return "";
    }

    private StringBuffer prepareReq(StringBuffer sb, String method, String requestHeader, String requestBody) {
        if (method.equals("POST") || method.equals("PUT")) {
            String[] headers = requestHeader.split("\n");
            Header header = null;
            for (int i = 0;i<headers.length;i++) {
                if (headers[i].toLowerCase().startsWith("content-type")) {
                    header = Header.parse(headers[i]);
                    break;
                }
            }
            if (header == null && selectFile == null) {
                header = Header.parse("Content-Type: application/x-www-form-urlencoded");
            } else if (selectFile != null) {
                boundary = System.currentTimeMillis();
                header = Header.parse("Content-Type: multipart/form-data; boundary=----httpClient"+boundary);
            }
            sb.append(header.toString()).append(NEWLINE);
            sb.append(Header.parse("Host: "+(urlInput.getText()).trim().split("/")[0])).append(NEWLINE);
            if (!requestHeader.isEmpty()) {
                sb.append(requestHeader).append(NEWLINE);
            }
            if (boundary != null) {
                sb.append(Header.parse("Content-Length: " + selectFile.length()+requestBody.length())).append(NEWLINE);
            }
        } else if (method.equals("GET")) {// GET
            sb.append(Header.parse("Host: "+(urlInput.getText()).trim().split("/")[0])).append(NEWLINE);
            return sb;
        } else{ // DELETE
            sb.append(Header.parse("Host: "+(urlInput.getText()).trim().split("/")[0])).append(NEWLINE);
            return sb;
        }
        return sb;
    }

    public void log(String msg) {
        //jta.append
        jta.append(msg);
    }
}

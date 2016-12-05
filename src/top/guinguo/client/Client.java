package top.guinguo.client;

import top.guinguo.http.Header;
import top.guinguo.util.DialogUtil;
import top.guinguo.util.HttpUtils;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.Socket;

import static top.guinguo.util.Constants.SERVERNAME;

/**
 * Created by guin_guo on 2016/11/7.
 */
public class Client extends JFrame {
    /**
     * 主机地址
     */
    public static String host;
    /**
     * 主机端口
     */
    public static int port;
    /**
     * 空行字符
     */
    public static String NEWLINE = top.guinguo.util.Constants.NEWLINE;
    /**
     * 请求内容
     */
    private StringBuffer stringBuffer;

    //url输入框
    private JTextField urlInput = new JTextField();

    //显示框
    private JTextArea showMsg = new JTextArea();

    private JButton send = new JButton("send");
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
    private PrintWriter toServer;
    private InputStream fromServer;

    public static void main(String[] args) {
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        }
        catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(Client.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(Client.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(Client.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(Client.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        new Client();
    }
//    public static void main(String[] args) {
//        new ();
//    }

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
        showMsg.setEditable(false);
        add(new JScrollPane(showMsg), BorderLayout.CENTER);

        //addActionListener
        openFile.addActionListener(new ButtonListener());
        send.addActionListener(new ButtonListener());
        clear.addActionListener(new ButtonListener());

        setTitle("Client");
        setSize(800, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setVisible(true);
        setLocation(200,100);
        /*this.host = "localhost";
        this.port = 8000;*/
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
                        String host_port = url.split("/")[0];
                        if (host_port.contains(":")) {
                            host = host_port.split(":")[0];
                            port = Integer.parseInt(host_port.split(":")[1]);
                        } else {
                            host = host_port;
                            port = 80;
                        }
                        socket = new Socket(host,port);
                        socket = sendSocket(socket, url, method, requestHeader,requestBody);
                        if (socket != null) {
                            toServer = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF-8"), true);
                            toServer.print(stringBuffer.append(NEWLINE).toString());
                            toServer.flush();
                        }
                        if (socket != null) {
                            printResponse(socket);
                        }
                    } catch (IOException ex) {
                        ex.printStackTrace();
                        DialogUtil.showMsg(ex.getClass()+":"+ex.getLocalizedMessage());
                        showMsg.append(ex.getClass() + ":" + ex.getLocalizedMessage() + "\n");
                    }

                } catch (Exception e1) {
                    e1.printStackTrace();
                    DialogUtil.showMsg(e1.getMessage());
                } finally {
                    try {
                        if (toServer != null) {
                            toServer.close();
                        }
                        if (fromServer != null) {
                            fromServer.close();
                        }
                        if (socket != null) {
                            socket.close();
                        }
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                }
            } else if (button.getText().equals("open")) {
                fileChooser.showDialog(new JLabel(), "选择文件");
                selectFile =fileChooser.getSelectedFile();
            } else if (button.getText().equals("clear")) {
                showMsg.setText("");
            }
        }
    }

    private void printResponse(Socket socket) {
        showMsg.append("----------------Response---------------"+NEWLINE);
        try {
            InputStreamReader raw = new InputStreamReader(socket.getInputStream(),"UTF-8");
            BufferedReader reader = new BufferedReader(raw);
            String inputLine = reader.readLine();
            while (inputLine != null && !inputLine.isEmpty()) {
                log(inputLine);
                if ("Server: HttpServer/1.1".equals(inputLine)) {
                    log("");
                }
                inputLine = reader.readLine();
            }
        } catch (IOException ex) {
            ex.printStackTrace();
            DialogUtil.showMsg(ex.getClass()+":"+ex.getLocalizedMessage());
            showMsg.append(ex.getClass() + ":" + ex.getLocalizedMessage() + "\n");
        }
    }

    public Socket sendSocket(Socket socket, String url, String method, String requestHeader, String requestBody) {
        stringBuffer = new StringBuffer();
        //first line   GET url HTTP/1.1 \n\r
        stringBuffer.append(method + " ");
        if (url == null || url.isEmpty()) {
            DialogUtil.showMsg("请输入请求地址");
            return null;
        }
        if (!url.startsWith("http://")) {
            url = "http://" + url;
        }
        stringBuffer.append(url + " " + "HTTP/1.1").append(NEWLINE);
        //second header body
        stringBuffer = prepareReq(stringBuffer, method, requestHeader,requestBody);
        if (!method.equals("GET")) {
            stringBuffer.append(NEWLINE);
        }
        if (boundary != null) {
            stringBuffer.append("------"+SERVERNAME + boundary).append(NEWLINE);
            stringBuffer.append(Header.parse("Content-Disposition: form-data; name=\"file\"; filename=\"" + selectFile.getName() + "\"")).append(NEWLINE);
            stringBuffer.append(Header.parse("Content-Type: text/html")).append(NEWLINE).append(NEWLINE);
            stringBuffer.append(toFileText());
            stringBuffer.append("------"+SERVERNAME + boundary);
            if (requestBody.isEmpty()) {
                stringBuffer.append("--");
            }
            stringBuffer.append(NEWLINE);
        }
        if (!requestBody.isEmpty()) {
            if (boundary != null) {
                stringBuffer.append(Header.parse("Content-Disposition: form-data;")).append(NEWLINE);
            }
            stringBuffer.append(requestBody).append(NEWLINE);
            if (boundary != null) {
                stringBuffer.append("------"+SERVERNAME + boundary).append("--");
            }
            stringBuffer.append(NEWLINE);
        }
        showMsg.setText("");
        showMsg.append("----------------Request-----------------"+NEWLINE);
        showMsg.append(stringBuffer.toString()+NEWLINE);
        return socket;
    }

    private String toFileText() {
        return HttpUtils.toFileText(selectFile);
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
                header = Header.parse("Content-Type: multipart/form-data; boundary=----"+SERVERNAME+boundary);
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
        //showMsg.append
        showMsg.append(msg+NEWLINE);
    }
}

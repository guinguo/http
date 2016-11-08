import java.net.MalformedURLException;
import java.net.URL;

public class Main {

    public static void main(String[] args) {
        System.out.println("Hello World!");
        try {
            URL url = new URL("http://www.blog.chinaunix.net:8080/uid-10386087-id-2958700.html?a=b&c=d");
            System.out.println(url.getPath());
            System.out.println(url.getHost());
            System.out.println(url.getFile());
            System.out.println(url.getPort());
            System.out.println(url.getProtocol());
            System.out.println(url.getQuery());
            System.out.println(url.getUserInfo());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }
}

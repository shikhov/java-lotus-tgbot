import java.io.*;
import java.util.Calendar;
import java.util.Properties;

class tgbot {

    public static void main(String[] args) throws IOException {
        System.setErr(new PrintStream(new FileOutputStream("tgbot.log", true)));

        String rootPath = Thread.currentThread().getContextClassLoader().getResource("").getPath();
        String appConfigPath = rootPath + "tgbot.properties";
        Properties appProps = new Properties();
        appProps.load(new FileInputStream(appConfigPath));
        String token = appProps.getProperty("token");
        String proxyHost = appProps.getProperty("proxy");
        int proxyPort = Integer.parseInt(appProps.getProperty("port"));

        TelegramBot mybot = new TelegramBot(token, proxyHost, proxyPort);
        try {
            mybot.run();
        } catch (Exception e) {
            System.err.println(Calendar.getInstance().getTime());
            System.err.println(e.getMessage());
        }
        
        
    }
}
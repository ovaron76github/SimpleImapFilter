package ovaron.imapFilter;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Properties;
import javax.mail.Store;

/**
 *
 * @author Stefan.Dietzel
 */
public class SimpleImapFilter {

    private static final String version = "1.02";
    private final Properties props = new Properties();

    private void loadProperties(String propertiesFile) {
        InputStream input = null;

        try {
            input = new FileInputStream(propertiesFile);
            props.load(input);
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void processRules() {
        if (!props.isEmpty()) {
            //variable for all messages processed
            int messagesProcessed = 0;

            //get server list
            String server = props.getProperty("activeServers").trim();
            ArrayList<String> servers = new ArrayList<>();

            if (server.contains(",")) {
                String[] serverList = server.split(",");
                for (int i = 0; i < serverList.length; i++) {
                    servers.add(serverList[i]);
                }
            } else {
                servers.add(server);
            }

            //loop over all servers
            for (int i = 0; i < servers.size(); i++) {
                try {
                    String serverName = servers.get(i);
                    String active = props.getProperty(serverName + ".active");
                    String host = props.getProperty(serverName + ".host");
                    String port = props.getProperty(serverName + ".port");
                    String username = props.getProperty(serverName + ".username");
                    String password = props.getProperty(serverName + ".password");

                    if (active != null && active.toLowerCase().trim().equals("true")) {
                        ImapFilter imf = new ImapFilter();
                        imf.setHost(host);
                        imf.setPort(port);
                        imf.setUsername(username);
                        imf.setPassword(password);

                        Store store = imf.connect();

                        //loop over server rules
                        for (int k = 0; k < 10001; k++) {
                            String ruleActive = props.getProperty(serverName + ".rule." + k + ".active");
                            if ((ruleActive != null) && (ruleActive.trim().toLowerCase().equals("true"))) {
                                String name = props.getProperty(serverName + ".rule." + k + ".name");
                                String searchField = props.getProperty(serverName + ".rule." + k + ".searchField");
                                String searchText = props.getProperty(serverName + ".rule." + k + ".searchText");
                                String action = props.getProperty(serverName + ".rule." + k + ".action");
                                String destFolder = props.getProperty(serverName + ".rule." + k + ".destFolder");

                                if (action.toLowerCase().trim().equals("move")) {
                                    if (name != null && searchField != null && searchText != null && destFolder != null) {
                                        int count = imf.findAndMoveMessages(store, searchField, searchText, destFolder);
                                        messagesProcessed = messagesProcessed + count;
                                        System.out.println(SimpleImapFilter.getTimeStamp() + serverName + " / " + name + ": Moved " + count + " messages to folder \"" + destFolder + "\"");
                                    }
                                } else if (action.toLowerCase().trim().equals("delete")) {
                                    if (name != null && searchField != null && searchText != null) {
                                        int count = imf.findAndDeleteMessages(store, searchField, searchText);
                                        messagesProcessed = messagesProcessed + count;
                                        System.out.println(SimpleImapFilter.getTimeStamp() + serverName + " / " + name + ": Deleted " + count + " messages");
                                    }
                                }
                            }
                        }
                        imf.disconnect(store);
                    } else {
                        //server not active in properties file
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }

            System.out.println(SimpleImapFilter.getTimeStamp() + "Total processed messages: " + messagesProcessed);
        }
    }

    private void run(String propertiesFile) {
        loadProperties(propertiesFile);
        processRules();
    }

    public static String getTimeStamp() {
        return new SimpleDateFormat("[yyyy.MM.dd HH:mm:ss] ").format(new Date());
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        System.out.println(SimpleImapFilter.getTimeStamp() + "SimpleImapFilter v" + version);
        if (args.length == 0) {
            System.out.println(SimpleImapFilter.getTimeStamp() + "Usage: java -jar SimpleImapFilter configFile.");
        } else {
            SimpleImapFilter sif = new SimpleImapFilter();
            try {
                sif.run(args[0]);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

}

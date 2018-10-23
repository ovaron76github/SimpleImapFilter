package ovaron.imapFilter;

import javax.mail.Session;
import javax.mail.Store;
import java.util.Properties;
import javax.mail.Address;
import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.search.SearchTerm;

/**
 *
 * @author Stefan.Dietzel
 */
public class ImapFilter {

    private String host;
    private String port;
    private String provider = "imap";
    private String username;
    private String password;

    private final Properties props = new Properties();

    /**
     * @return the host
     */
    public String getHost() {
        return host;
    }

    /**
     * @param host the host to set
     */
    public void setHost(String host) {
        this.host = host;
    }

    /**
     * @return the port
     */
    public String getPort() {
        return port;
    }

    /**
     * @param port the port to set
     */
    public void setPort(String port) {
        this.port = port;
    }

    /**
     * @return the username
     */
    public String getUsername() {
        return username;
    }

    /**
     * @param username the username to set
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * @return the password
     */
    public String getPassword() {
        return password;
    }

    /**
     * @param password the password to set
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * @return the provider
     */
    public String getProvider() {
        return provider;
    }

    /**
     * @param provider the provider to set
     */
    public void setProvider(String provider) {
        this.provider = provider;
    }

    /**
     *
     * @return @throws Exception
     */
    public Store connect() throws Exception {
        // server setting
        props.put("mail." + provider + ".host", host);
        props.put("mail." + provider + ".port", port);

        // SSL setting
        props.setProperty("mail." + provider + ".socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        props.setProperty("mail." + provider + ".socketFactory.fallback", "true");
        props.setProperty("mail." + provider + ".socketFactory.port", String.valueOf(port));

        Session session = Session.getDefaultInstance(props, null);
        Store store = session.getStore(provider);
        store.connect(host, username, password);
        return store;
    }

    /**
     *
     * @param store
     * @throws Exception
     */
    public void disconnect(Store store) throws Exception {
        if (store.isConnected()) {
            store.close();
        }
    }

    /**
     *
     * @param searchWhere
     * @param searchText
     * @return SearchTerm
     */
    private SearchTerm createSearchTerm(String searchWhere, String searchText) {
        String searchTextLow = searchText.toLowerCase();
        // creates a search criterion
        SearchTerm searchCondition = new SearchTerm() {
            @Override
            public boolean match(Message message) {
                try {
                    //Suject search
                    if (searchWhere.toLowerCase().trim().equals("subject")) {
                        if (message.getSubject().toLowerCase().contains(searchTextLow)) {
                            return true;
                        }
                    } //From search
                    else if (searchWhere.toLowerCase().trim().equals("from")) {
                        Address[] fromAdress = message.getFrom();
                        String mailFrom = "";
                        for (Address address : fromAdress) {
                            mailFrom = address.toString();
                        }
                        if (mailFrom.toLowerCase().contains(searchTextLow)) {
                            return true;
                        }
                    } //To search
                    else if (searchWhere.toLowerCase().trim().equals("to")) {
                        Address[] toAdress = message.getRecipients(Message.RecipientType.TO);
                        String mailTo = "";
                        for (Address address : toAdress) {
                            mailTo = mailTo + ";" + address.toString();
                        }
                        if (mailTo.toLowerCase().contains(searchTextLow)) {
                            return true;
                        }
                    }
                } catch (MessagingException ex) {
                    ex.printStackTrace();
                }
                return false;
            }
        };

        return searchCondition;
    }

    /**
     *
     * @param store
     * @param searchCondition
     * @return Message[]
     * @throws Exception
     */
    private Message[] findMessages(Store store, SearchTerm searchCondition) throws Exception {
        Folder folderInbox = store.getFolder("INBOX");
        folderInbox.open(Folder.READ_WRITE);
        Message[] foundMessages = folderInbox.search(searchCondition);

        return foundMessages;
    }

    /**
     *
     * @param store
     * @param searchWhere
     * @param searchText
     * @return Message[]
     * @throws Exception
     */
    private Message[] findMessages(Store store, String searchWhere, String searchText) throws Exception {
        Message[] foundMessages = findMessages(store, createSearchTerm(searchWhere, searchText));

        return foundMessages;
    }

    /**
     *
     * @param store
     * @param messages
     * @param destinationFolder
     * @throws Exception
     */
    private void moveMessages(Store store, Message[] messages, String destinationFolder) throws Exception {
        if (messages.length > 0) {
            Folder folder = store.getFolder(destinationFolder);
            if (!folder.exists()) {
                folder.create(Folder.HOLDS_MESSAGES);
            }
            folder.open(Folder.READ_WRITE);
            messages[0].getFolder().copyMessages(messages, folder);
            for (Message message : messages) {
                message.setFlag(Flags.Flag.DELETED, true);
            }
            messages[0].getFolder().close(true);
            folder.close();
        }
    }

    /**
     *
     * @param store
     * @param messages
     * @throws Exception
     */    
    private void deleteMessages(Store store, Message[] messages) throws Exception {
        if (messages.length > 0) {
            for (Message message : messages) {
                message.setFlag(Flags.Flag.DELETED, true);
            }
            messages[0].getFolder().close(true);
        }
    }
    
    /**
     *
     * @param store
     * @param searchWhere
     * @param searchText
     * @param destinationFolder
     * @return int
     * @throws Exception
     */
    public int findAndMoveMessages(Store store, String searchWhere, String searchText, String destinationFolder) throws Exception {
        Message[] messages = findMessages(store, searchWhere, searchText);
        int messageCount = messages.length;
        moveMessages(store, messages, destinationFolder);

        return messageCount;
    }

     /**
     *
     * @param store
     * @param searchWhere
     * @param searchText
     * @return int
     * @throws Exception
     */
    public int findAndDeleteMessages(Store store, String searchWhere, String searchText) throws Exception {
        Message[] messages = findMessages(store, searchWhere, searchText);
        int messageCount = messages.length;
        deleteMessages(store, messages);

        return messageCount;
    }
    
    
}

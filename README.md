# SimpleImapFilter (NetBeans project)
Simple java class and app for filtering IMAP accounts (move and delete mails by filter criteria)<br>
The search should be done on server-side.<br>
<br>
Project is using JavaMail (https://javaee.github.io/javamail/)<br>
<br>
<br>
Installation and usage:<br>
<ul>
<li>Copy "SimpleImapFilter.jar" to some directory (for example /usr/local/SimpleImapFilter)</li>
<li>Create Subfolder "lib" copy javax.mail.jar into this directory</li>
<li>Create a config/properites file (for example named settings.properties) and copy it to SimpleImapFilter folder</li>
</ul>
<br>
Description of config file: <a href="https://github.com/ovaron76github/SimpleImapFilter/wiki/Description-for-config-file">Description-for-config-file</a><br>
<br>
Start by using the following command:<br>
<i>java -jar /usr/local/SimpleImapFilter/SimpleImapFilter.jar /usr/local/SimpleImapFilter/settings.properties</i><br>


package modelimport;

import com.jcraft.jsch.*;

import java.util.Properties;

public class FileUploader {

    private final JSch jsch;

    private final String host;
    private final int port;

    private Session session;
    private ChannelSftp channel;

    public FileUploader(String host, int port) {
        this.host = host;
        this.port = port;

        jsch = new JSch();
    }

    public void connect(String username, String password) throws JSchException {
        session = jsch.getSession(username, host, port);
        // disable known hosts checking
        // if you want to set knows hosts file You can set with jsch.setKnownHosts("path to known hosts file");
        Properties config = new Properties();
        config.put("StrictHostKeyChecking", "no");
        session.setConfig(config);
        session.setConfig("PreferredAuthentications", "publickey,password");
        session.setPassword(password);
        session.connect();

        if (session != null) {
            System.out.println("Session connected?: " + session.isConnected());
        }

        channel = (ChannelSftp) session.openChannel("sftp");
        channel.connect();

        if (channel != null) {
            System.out.println("Channel connected?: " + channel.isConnected());
        }
    }

    public void disconnect() {
        if (channel != null) {
            channel.exit();
        }

        if (session != null && session.isConnected()) {
            session.disconnect();
        }
    }

    public void uploadFile(String localPath, String remotePath) throws JSchException, SftpException {
        System.out.printf("Uploading [%s] to [%s]...%n", localPath, remotePath);
        if (channel == null) {
            throw new IllegalArgumentException("Connection is not available");
        }
        channel.put(localPath, remotePath);
    }
}
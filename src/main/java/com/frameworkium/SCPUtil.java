package com.frameworkium;

import com.jcraft.jsch.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.yandex.qatools.allure.annotations.Step;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

/**
 * Provides utility for transferring files to a remote destination via SCP.
 */
public final class SCPUtil {

    private static final Logger LOG = LoggerFactory.getLogger(SCPUtil.class);
    private static final int SSH_PORT = 22;
    private static final int FILE_MOVE_DELAY = 3000;

    /**
     * Private constructor for this utility class
     */
    private SCPUtil() {

    }

    /**
     * Copy a single file to a remote machine
     *
     * @param file The file to copy to the remote machine
     * @param destDir The directory to copy too on the remote machine
     * @param server The name of the target machine
     * @param user The user account to use to connect to the remote machine
     * @param privateKeyPath The path to the private key to use to connect to the remote machine, use null if using
     *        password based authentication
     * @param password The password to use to connect to the remote machine, use null if using certificate based
     *        authentication
     */
    @Step("I copy the file {0} to {1} on {2}")
    public static void copyFile(final File file, final String destDir, final String server, final String user,
            final String privateKeyPath, final String password) {
        copyFile(new File[] { file }, destDir, server, user, privateKeyPath, password);
    }

    /**
     * Copy any number of files to a remote machine
     *
     * @param files The files to copy to the remote machine
     * @param destDir The directory to copy too on the remote machine
     * @param server The name of the target machine
     * @param user The user account to use to connect to the remote machine
     * @param privateKeyPath The path to the private key to use to connect to the remote machine, use null if using
     *        password based authentication
     * @param password The password to use to connect to the remote machine, use null if using certificate based
     *        authentication
     */
    @Step("I copy a list of files to {1} on {2}")
    public static void copyFile(final File[] files, final String destDir, final String server, final String user,
            final String privateKeyPath, final String password) {
        Session session = null;
        ChannelSftp sftpChannel = null;
        Channel channel = null;
        String tempLocation = "/tmp";
        try {
            JSch jsch = new JSch();
            if (privateKeyPath != null) {
                jsch.addIdentity(privateKeyPath);
            }

            // Create ssh session
            session = jsch.getSession(user, server, SSH_PORT);
            session.setConfig("StrictHostKeyChecking", "no");
            if (password != null) {
                session.setPassword(password);
            }
            session.connect();

            sftpChannel = (ChannelSftp) session.openChannel("sftp");
            sftpChannel.connect();
            sftpChannel.cd(tempLocation);
            StringBuilder moveFileCommand = new StringBuilder(files.length * 50);
            for (File file : files) {
                sftpChannel.put(new FileInputStream(file), file.getName()); // Copy file to temp location
                moveFileCommand.append("mv ");
                moveFileCommand.append(tempLocation);
                moveFileCommand.append("/");
                moveFileCommand.append(file.getName());
                moveFileCommand.append(" ");
                moveFileCommand.append(destDir);
                moveFileCommand.append("; ");
            }
            sftpChannel.disconnect();
            Thread.sleep(FILE_MOVE_DELAY);

            channel = session.openChannel("exec");
            // Add the command to the channel before connecting as the command is run upon connection
            ((ChannelExec) channel).setCommand(moveFileCommand.toString());
            channel.connect();
        } catch (JSchException e) {
            LOG.error("JSch Problem scp'ing file", e);
        } catch (SftpException e) {
            LOG.error("SFtp Problem scp'ing file", e);
        } catch (FileNotFoundException e) {
            LOG.error("Problem finding file", e);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            if (channel != null) {
                channel.disconnect();
            }
            if (session != null) {
                session.disconnect();
            }
        }
    }

    /**
     * Copy a single file from a remote machine
     *
     * @param remoteFilePath The path to the remote file on the remote machine
     * @param localFilePath The path to copy the file to
     * @param server The name of the remote machine
     * @param user The user account to use to connect to the remote machine
     * @param privateKeyPath The path to the private key to use to connect to the remote machine, use null if using
     *        password based authentication
     * @param password The password to use to connect to the remote machine, use null if using certificate based
     *        authentication
     */
    @Step("I make a copy of file {0} to {1} on server {2}")
    public static void retrieveFile(final String remoteFilePath, final String localFilePath, final String server,
            final String user, final String privateKeyPath, final String password) {
        Session session = null;
        ChannelSftp sftpChannel = null;
        Channel channel = null;

        try {
            JSch jsch = new JSch();
            if (privateKeyPath != null) {
                jsch.addIdentity(privateKeyPath);
            }

            // Create ssh session
            session = jsch.getSession(user, server, SSH_PORT);
            session.setConfig("StrictHostKeyChecking", "no");
            if (password != null) {
                session.setPassword(password);
            }
            session.connect();

            sftpChannel = (ChannelSftp) session.openChannel("sftp");
            sftpChannel.connect();

            sftpChannel.get(remoteFilePath, localFilePath);

            sftpChannel.exit();

            sftpChannel.disconnect();

        } catch (JSchException e) {
            LOG.error("JSch Problem scp'ing file", e);
        } catch (SftpException e) {
            LOG.error("SFtp Problem scp'ing file", e);
        } finally {
            if (channel != null) {
                channel.disconnect();
            }
            if (session != null) {
                session.disconnect();
            }
        }
    }
}

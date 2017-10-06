package com.frameworkium;

import com.jcraft.jsch.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.yandex.qatools.allure.annotations.Step;

import java.io.ByteArrayOutputStream;

/**
 * Provides utility executing commands on a remote server
 */
public final class SSHUtil {

    private static final Logger LOG = LoggerFactory.getLogger(SSHUtil.class);

    private static final String JSCH_EXEC_CHANNEL_NAME = "exec";
    private static final int ONE_SECOND_IN_MS = 1000;
    private static final int SSH_PORT = 22;

    /**
     * Private constructor for this utility class
     */
    private SSHUtil() {

    }

    /**
     * Executes a command on a remote server using SSH
     *
     * @param serverName The name of the server to connect to
     * @param SSHUser The username to connect with
     * @param SSHPassword The password to connect with
     * @param command The command to execute
     */
    @Step("I execute the command {3} on {0}")
    public static String executeCommand(final String serverName, final String SSHUser, final String SSHPassword,
            final String command) {
        Session session = null;
        Channel channel = null;
        String commandOutput = "";
        try {
            JSch jsch = new JSch();

            session = jsch.getSession(SSHUser, serverName, SSH_PORT);
            session.setConfig("StrictHostKeyChecking", "no");
            session.setPassword(SSHPassword);
            session.connect();

            LOG.info("Connected via SSH to server {} as user {}", serverName, SSHUser);

            final ByteArrayOutputStream baos = new ByteArrayOutputStream();
            channel = session.openChannel(JSCH_EXEC_CHANNEL_NAME);
            channel.setOutputStream(baos);
            ((ChannelExec) channel).setCommand(command);
            channel.connect();

            while (!channel.isClosed()) {
                try {
                    Thread.sleep(ONE_SECOND_IN_MS);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            commandOutput = new String(baos.toByteArray());

            channel.disconnect();

            session.disconnect();
        } catch (JSchException e) {
            throw new RuntimeException(e);
        } finally {
            if (channel != null) {
                channel.disconnect();
            }
            if (session != null) {
                session.disconnect();
            }
        }

        return commandOutput;
    }
}

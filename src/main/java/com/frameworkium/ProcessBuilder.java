package com.frameworkium;

import ru.yandex.qatools.allure.annotations.Step;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.logging.Logger;

/**
 *  Provides utility running a proccess on the operating system
 */
public class ProcessBuilder {

    /**
     * Create an operating system process
     * @param command to run
     * @param LOGGER to use to log output
     * @return exiut code of the process
     * @throws IOException
     * @throws InterruptedException
     */
    @Step("run command: {0}")
    public static int runCommand(String command, Logger LOGGER)
            throws IOException, InterruptedException {
        Process p = Runtime.getRuntime().exec(command);
        p.waitFor();

        BufferedReader reader =
                new BufferedReader(new InputStreamReader(p.getErrorStream()));

        String line = reader.readLine();
        while (line != null) {
            LOGGER.info(line);
            line = reader.readLine();
        }

        return p.exitValue();
    }

}

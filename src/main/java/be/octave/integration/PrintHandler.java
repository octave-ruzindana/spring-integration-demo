package be.octave.integration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.integration.handler.GenericHandler;
import org.springframework.messaging.MessageHeaders;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class PrintHandler implements GenericHandler<File> {
    private Logger logger = LoggerFactory.getLogger(PrintHandler.class);

    @Override
    public Object handle(File file, MessageHeaders messageHeaders) {
        try {
            logger.info(new String(Files.readAllBytes(Paths.get(file.getPath()))));
        } catch (IOException e) {
            logger.error("Error : ", e);
        }
        return file;
    }
}

package be.octave.integration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.integration.file.FileHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHandler;
import org.springframework.messaging.MessagingException;

public class ExceptionHandler implements MessageHandler {

    Logger logger = LoggerFactory.getLogger(ExceptionHandler.class);

    @Override
    public void handleMessage(Message<?> message) throws MessagingException {
        if(message.getPayload() instanceof MessagingException){
            MessagingException msgException = (MessagingException) message.getPayload();
            logger.error("Exception " + msgException.getMessage() + " occured on file " +  message.getHeaders().get(FileHeaders.FILENAME) + " has be handled");
        }
    }
}

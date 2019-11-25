package be.octave.integration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.integration.annotation.*;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.channel.PublishSubscribeChannel;
import org.springframework.integration.context.IntegrationContextUtils;
import org.springframework.integration.core.MessageSource;
import org.springframework.integration.file.DefaultDirectoryScanner;
import org.springframework.integration.file.DirectoryScanner;
import org.springframework.integration.file.FileReadingMessageSource;
import org.springframework.integration.file.FileWritingMessageHandler;
import org.springframework.integration.file.filters.AcceptOnceFileListFilter;
import org.springframework.integration.file.filters.CompositeFileListFilter;
import org.springframework.integration.file.filters.RegexPatternFileListFilter;
import org.springframework.integration.file.support.FileExistsMode;
import org.springframework.integration.handler.GenericHandler;
import org.springframework.integration.handler.LoggingHandler;
import org.springframework.integration.router.ErrorMessageExceptionTypeRouter;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHandler;
import org.springframework.messaging.MessagingException;

import java.io.File;
import java.util.Arrays;

// https://github.com/iainporter/spring-file-poller/
// https://www.baeldung.com/spring-integration-java-dsl
// https://github.com/spring-projects/spring-integration-extensions/tree/master/spring-integration-zip

@SpringBootApplication
public class SpringIntegrationDemoApplication {

	private static final String ERROR_CHANNEL = IntegrationContextUtils.ERROR_CHANNEL_BEAN_NAME;
	private static final String UNEXPECTED_ERROR_CHANNEL = "unexpected-error-channel";
	private static final String ILLEGAL_ARGUMENT_ERROR_CHANNEL = "illegal-argument-channel";
	private static final String INPUT_DIRECTORY_CHANNEL = "input-directory-channel";
	private  static final String PRINT_CHANNEL = "print-channel";
	private static final String COPY_CHANNEL = "copy-channel";

	private static final String UNEXPECTED_ERROR_HANDLER = "unexpected-error-handler";



	private static Logger logger = LoggerFactory.getLogger(SpringIntegrationDemoApplication.class);

	@Value("${user.home}")
	String INPUT_DIR;

	@Value("${java.io.tmpdir}")
	String OUTPUT_DIR;

	public static void main(String[] args) {
		SpringApplication.run(SpringIntegrationDemoApplication.class, args);
	}


	@Bean
	@InboundChannelAdapter( value = INPUT_DIRECTORY_CHANNEL, poller = @Poller(fixedDelay = "5000", errorChannel = ERROR_CHANNEL))
	@EndpointId("input-directory-source")
	public MessageSource<File> sourceDirectory() {
		logger.info("Input dir : " + INPUT_DIR);
		FileReadingMessageSource messageSource = new FileReadingMessageSource();
		messageSource.setDirectory(new File(INPUT_DIR));
		messageSource.setScanner(directoryScanner());
		return messageSource;
	}

	@Bean(ERROR_CHANNEL)
	@EndpointId(ERROR_CHANNEL)
	public MessageChannel errorChannel(){
		return new PublishSubscribeChannel();
	}

	@Bean
	@Router(inputChannel = ERROR_CHANNEL)
	public ErrorMessageExceptionTypeRouter errorHandler() {
		ErrorMessageExceptionTypeRouter router = new ErrorMessageExceptionTypeRouter();
		router.setChannelMapping(IllegalArgumentException.class.getName(), ILLEGAL_ARGUMENT_ERROR_CHANNEL);
		router.setDefaultOutputChannel(unexpectedErrorChannel());
		return router;
	}

	@Bean(ILLEGAL_ARGUMENT_ERROR_CHANNEL)
	@EndpointId(ILLEGAL_ARGUMENT_ERROR_CHANNEL)
	public MessageChannel illegalArgument() {
		return new DirectChannel();
	}

	@Bean
	@EndpointId("illegal-handler")
	@ServiceActivator(inputChannel = ILLEGAL_ARGUMENT_ERROR_CHANNEL)
	public MessageHandler illegalArgumentHandler() {
		return  new ExceptionHandler();
	}

	@Bean
	@EndpointId(UNEXPECTED_ERROR_CHANNEL)
	public MessageChannel unexpectedErrorChannel() {
		return new DirectChannel();
	}

	@Bean
	@EndpointId(UNEXPECTED_ERROR_HANDLER)
	public MessageHandler unexpectedErrorHandler() {
		return  new LoggingHandler(LoggingHandler.Level.ERROR);
	}

	@Bean
	@EndpointId("print-handler")
	@ServiceActivator(inputChannel = INPUT_DIRECTORY_CHANNEL, outputChannel = PRINT_CHANNEL)
	public GenericHandler<File> printContent() {
		return new PrintHandler();
	}

	@Bean
	@EndpointId("copy-handler")
	@ServiceActivator(inputChannel = PRINT_CHANNEL)
	public MessageHandler targetDirectory() {
		logger.info("Output dir : " + OUTPUT_DIR);
		FileWritingMessageHandler handler = new FileWritingMessageHandler(new File(OUTPUT_DIR));
		handler.setFileExistsMode(FileExistsMode.REPLACE);
		handler.setExpectReply(false);
		handler.setOutputChannelName(COPY_CHANNEL);
		return handler;
	}

	@Bean(COPY_CHANNEL)
	@EndpointId(COPY_CHANNEL)
	public MessageChannel copyChannel(){
		return new DirectChannel();
	}


	@Bean
	public DirectoryScanner directoryScanner() {
		// use RecursiveDirectoryScanner if necessary
		DirectoryScanner scanner = new DefaultDirectoryScanner();

		CompositeFileListFilter<File> filter = new CompositeFileListFilter<>(
				Arrays.asList(new AcceptOnceFileListFilter<>(),
						new RegexPatternFileListFilter(".*txt$"))
		);

		scanner.setFilter(filter);
		return scanner;
	}

}

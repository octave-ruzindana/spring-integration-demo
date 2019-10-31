package be.octave.integration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.integration.annotation.EndpointId;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.core.MessageSource;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.dsl.Pollers;
import org.springframework.integration.file.DefaultDirectoryScanner;
import org.springframework.integration.file.DirectoryScanner;
import org.springframework.integration.file.FileReadingMessageSource;
import org.springframework.integration.file.FileWritingMessageHandler;
import org.springframework.integration.file.filters.AcceptOnceFileListFilter;
import org.springframework.integration.file.filters.CompositeFileListFilter;
import org.springframework.integration.file.filters.RegexPatternFileListFilter;
import org.springframework.integration.file.support.FileExistsMode;
import org.springframework.integration.handler.GenericHandler;
import org.springframework.messaging.MessageHandler;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;

// https://github.com/iainporter/spring-file-poller/
// https://www.baeldung.com/spring-integration-java-dsl
// https://github.com/spring-projects/spring-integration-extensions/tree/master/spring-integration-zip

@SpringBootApplication
public class SpringIntegrationDemoApplication {

	static Logger logger = LoggerFactory.getLogger(SpringIntegrationDemoApplication.class);

	@Value("${user.home}")
	String INPUT_DIR;

	@Value("${java.io.tmpdir}")
	String OUTPUT_DIR;

	public static void main(String[] args) {
		SpringApplication.run(SpringIntegrationDemoApplication.class, args);
	}

	@Bean
	public IntegrationFlow copyTextFiles() {
		return IntegrationFlows.from(sourceDirectory(), configurer -> configurer.poller(Pollers.fixedDelay(5000)))
				.handle(printContent())
				.channel(printChannel())
				.handle(targetDirectory())
				.channel(copyChannel())
				.get();
	}

	@Bean
	public DirectChannel printChannel(){
		return new DirectChannel();
	}

	@Bean
	public DirectChannel copyChannel() {
		return new DirectChannel();
	}

	@Bean
	@EndpointId("print.handler")
	public GenericHandler<File> printContent() {
		return (file, messageHeaders) -> {
			try {
				logger.info(new String(Files.readAllBytes(Paths.get(file.getPath()))));
			} catch (IOException e) {
				logger.error("Error : ", e);
			}
			return file;
		};
	}

	@Bean
	@EndpointId("copy.handler")
	public MessageHandler targetDirectory() {
		logger.info("Output dir : " + OUTPUT_DIR);
		FileWritingMessageHandler handler = new FileWritingMessageHandler(new File(OUTPUT_DIR));
		handler.setFileExistsMode(FileExistsMode.REPLACE);
		handler.setExpectReply(false);
		return handler;
	}


	@Bean
	public MessageSource<File> sourceDirectory() {
		logger.info("Input dir : " + INPUT_DIR);
		FileReadingMessageSource messageSource = new FileReadingMessageSource();
		messageSource.setDirectory(new File(INPUT_DIR));
		messageSource.setScanner(directoryScanner());
		return messageSource;
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

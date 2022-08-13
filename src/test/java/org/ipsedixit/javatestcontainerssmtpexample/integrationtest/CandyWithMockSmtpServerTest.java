package org.ipsedixit.javatestcontainerssmtpexample.integrationtest;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;
import java.time.Duration;
import java.util.Collections;
import java.util.List;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Logger;
import org.ipsedixit.javatestcontainerssmtpexample.CandyBox;
import org.ipsedixit.javatestcontainerssmtpexample.ConfEmail;
import org.ipsedixit.javatestcontainerssmtpexample.Dashboard;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.DockerComposeContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
class CandyWithMockSmtpServerTest {
	private static final String EXP_LOG_ERROR_MSG = "An error occurred in sending email for NO more candy";

	@Container
	private DockerComposeContainer composeContainer = new DockerComposeContainer(
			new File("src/test/resources/integration/docker-compose.yml"))
					.withExposedService("mock-smtp", 8000,
							Wait.forListeningPort().withStartupTimeout(Duration.ofMinutes(1)))
					.withExposedService("mock-smtp", 25,
							Wait.forListeningPort().withStartupTimeout(Duration.ofMinutes(1)));

	private String host;

	private int apiPort;
	private int smtpPort;
	private ConfEmail confEmail;
	private Dashboard dashboard;
	private CandyBox candyBox;
	private MockSmtp mockSmtp;

	@BeforeEach
	void setup() {
		host = composeContainer.getServiceHost("mock-smtp", 8000);
		apiPort = composeContainer.getServicePort("mock-smtp", 8000);
		smtpPort = composeContainer.getServicePort("mock-smtp", 25);
		confEmail = new ConfEmail(true, host, smtpPort, "me@example.com", List.of("you@example.com"));
		dashboard = new Dashboard(confEmail);
		candyBox = new CandyBox(dashboard, 3);
		MockSmtpConf mockSmtpConf = new MockSmtpConf(host, smtpPort, apiPort);
		mockSmtp = new MockSmtp(mockSmtpConf);
		mockSmtp.cleanUp();
	}

	@AfterEach
	void tearDown() {
		mockSmtp.cleanUp();
		// logger.removeAppender(mockAppender);
	}

	@Test
	void testCandyBoxNoMail() throws Exception {
		candyBox.eatOne();
		assertEquals(Collections.EMPTY_LIST, mockSmtp.getMails());
	}

	@Test
	void testCandyBoxTwoMails() throws Exception {
		candyBox.eatOne();
		candyBox.eatOne();
		candyBox.eatOne();
		candyBox.eatOne();
		candyBox.eatOne();
		assertEquals(2, mockSmtp.getMails().size());
		// Compare first Mail
		compareMail(mockSmtp.getMails().get(0));
		// Compare second mail
		compareMail(mockSmtp.getMails().get(1));
	}

	private void compareMail(Mail mail) {
		assertEquals("me@example.com", mail.fromAddr());
		assertEquals(List.of("you@example.com"), mail.toAddrs());
		assertEquals("No more candy!!!", mail.subject());
		assertEquals("Oh no! No more candy...", mail.body());
	}

	record SMTPBehaviour2(SMTPCommand command, SMTPCondition condition, SMTPResponse response) {
	}

	@Test
	void testCandyBoxFailure() throws Exception {
		Logger logger = (Logger) LogManager.getRootLogger();
		// Create a String Appender to capture log output
		StringAppender appender = StringAppender.createStringAppender("[] %m");
		appender.addToLogger(logger.getName(), Level.INFO);
		appender.start();
		mockSmtp.setBehaviours(List.of(new SMTPBehaviour(SMTPCommand.MAIL_FROM, SMTPMatchContains.of("me@example.com"),
				new SMTPResponse(SMTPStatusCode.COMMAND_NOT_IMPLEMENTED_502, "Fake server error"))));

		candyBox.eatOne();
		candyBox.eatOne();
		candyBox.eatOne();
		candyBox.eatOne();

		assertEquals(0, mockSmtp.getMails().size());

		assert (appender.getOutput().contains(EXP_LOG_ERROR_MSG));
		appender.removeFromLogger(LogManager.getRootLogger().getName());
	}

}

package org.ipsedixit.javatestcontainerssmtpexample.integrationtest;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CompletionException;
import java.util.stream.Collectors;

import org.apache.commons.io.IOUtils;
import org.apache.james.mime4j.dom.Message;
import org.apache.james.mime4j.dom.TextBody;
import org.apache.james.mime4j.message.BodyPart;
import org.apache.james.mime4j.message.DefaultMessageBuilder;
import org.apache.james.mime4j.message.MultipartImpl;
import org.apache.james.mime4j.stream.MimeConfig;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

public class MockSmtp {
	private static final Logger LOGGER = LogManager.getLogger();
	private final static int STATUS_CODE_OK = 200;
	private final static int STATUS_CODE_NO_CONTENT = 204;
	private final static int TIMEOUT_API_SECONDS = 10;
	private final MockSmtpConf mockSmtpConf;

	public MockSmtp(final MockSmtpConf mockSmtpConf) {
		this.mockSmtpConf = mockSmtpConf;
	}

	private String mailUrl() {
		return String.format("http://%s:%s/smtpMails", mockSmtpConf.host(), mockSmtpConf.apiPort());
	}

	private String behaviorsUrl() {
		return String.format("http://%s:%s/smtpBehaviors", mockSmtpConf.host(), mockSmtpConf.apiPort());
	}

	public void cleanUp() {
		cleanUpSingleEndPoint("mail", mailUrl());
		cleanUpSingleEndPoint("behaviors", behaviorsUrl());
	}

	private void cleanUpSingleEndPoint(String requestType, String url) {
		HttpResponse<String> response;
		try {
			LOGGER.debug("Try to delete {}", requestType);
			HttpClient client = HttpClient.newHttpClient();
			HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url))
					.timeout(Duration.ofSeconds(TIMEOUT_API_SECONDS)).DELETE().build();
			response = client.send(request, BodyHandlers.ofString());
		} catch (Exception exc) {
			throw new RuntimeException("An error occurred in execute " + requestType, exc);
		}
		LOGGER.info("Delete {} - response code: {} - text: {}", requestType, response.statusCode(), response.body());
		if (response.statusCode() != STATUS_CODE_NO_CONTENT && response.statusCode() != STATUS_CODE_OK) {
			throw new RuntimeException("Response delete " + requestType + " KO");
		}

	}

	public List<Mail> getMails() {
		MailObjectMapper objectMapper = new MailObjectMapper();

		try {
			LOGGER.debug("Try to retrieve all mails");
			HttpClient client = HttpClient.newHttpClient();
			HttpRequest request = HttpRequest.newBuilder().uri(URI.create(mailUrl()))
					.timeout(Duration.ofSeconds(TIMEOUT_API_SECONDS)).GET().build();
			// TODO Check http status code = STATUS_CODE_OK via accept
			List<MockResponseMail> response = client.sendAsync(request, BodyHandlers.ofString())
					.thenApply(HttpResponse::body).thenApply(objectMapper::readValue).get();

			return response.stream().map(this::getMail).collect(Collectors.toList());

		} catch (Exception exc) {
			throw new RuntimeException("An error occurred in get mails", exc);
		}

	}

	private Mail getMail(MockResponseMail mockMail) {
		DefaultMessageBuilder defaultMessageBuilder = new DefaultMessageBuilder();
		defaultMessageBuilder.setMimeEntityConfig(MimeConfig.PERMISSIVE);

		Message message;
		try {
			message = defaultMessageBuilder
					.parseMessage(new ByteArrayInputStream(mockMail.message().getBytes(StandardCharsets.UTF_8)));
			MultipartImpl multipartBody = (MultipartImpl) message.getBody();
			BodyPart bodyPart = (BodyPart) multipartBody.getBodyParts().get(0);

			TextBody textBody = (TextBody) bodyPart.getBody();

			String body = IOUtils.toString(textBody.getInputStream(), textBody.getMimeCharset());

			return new Mail(mockMail.from(),
					message.getTo().stream().map(addr -> String.valueOf(addr)).collect(Collectors.toList()),
					message.getDate(), message.getSubject(), body);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	public void setBehaviours(List<SMTPBehaviour> behaviours) {
		HttpResponse<String> response;
		try {
			LOGGER.debug("Try to set behaviours {}", behaviours);
			HttpClient client = HttpClient.newHttpClient();

			HttpRequest request = HttpRequest.newBuilder().uri(URI.create(behaviorsUrl()))
					.header("Content-Type", "application/json").timeout(Duration.ofSeconds(TIMEOUT_API_SECONDS))
					.PUT(BodyPublishers.ofString(new ObjectMapper().writeValueAsString(behaviours))).build();
			response = client.send(request, BodyHandlers.ofString());
		} catch (Exception exc) {
			throw new RuntimeException("An error occurred in execute set behaviours", exc);
		}
		LOGGER.info("Set behaviours {} - response code: {} - text: {}", behaviours, response.statusCode(),
				response.body());
		if (response.statusCode() != STATUS_CODE_NO_CONTENT && response.statusCode() != STATUS_CODE_OK) {
			throw new RuntimeException("Response set behaviours KO");
		}
	}

}

record MockSmtpConf(String host, int smtpPort, int apiPort) {
}

@JsonIgnoreProperties(ignoreUnknown = true)
record MockResponseMail(String from, List<MockResponseRecipient> recipients, String message) {
}

@JsonIgnoreProperties(ignoreUnknown = true)
record MockResponseRecipient(String address) {
}

record Mail(String fromAddr, List<String> toAddrs, Date when, String subject, String body) {
}

enum SMTPCommand {
	RCPT_TO("RCPT TO"), EHLO("EHLO"), MAIL_FROM("MAIL FROM"), DATA("DATA"), RSET("RSET"), VRFY("VRFY"), NOOP(
			"NOOP"), QUIT("QUIT");

	private final String label;

	private SMTPCommand(String label) {
		this.label = label;
	}

	public String getLabel() {
		return label;
	}

	@JsonValue
	public String getName() {
		return label;
	}
}

enum SMTPStatusCode {
	OK_200(200), SYSTEM_STATUS_211(211), HELP_214(214), SERVICE_READY(220), SERVICE_CLOSING_CHANNEL_221(
			221), ACTION_COMPLETE_250(250), USER_NOT_LOCAL_251(251), UNKNOW_USER_252(252), START_MAIL_INPUT_354(
					354), SERVICE_NOT_AVAILABLE_421(421), REQUESTED_MAIL_ACTION_NOT_TAKEN_450(
							450), REQUESTED_ACTION_ABORTED_451(451), REQUESTED_ACTION_NOT_TAKEN_452(
									452), SYNTAX_ERROR_500(500), SYNTAX_ERROR_IN_PARAMETERS_OR_ARGUMENTS_501(
											501), COMMAND_NOT_IMPLEMENTED_502(502), BAD_SEQUENCE_OF_COMMANDS_503(
													503), COMMAND_PARAMETER_NOT_IMPLEMENTED_504(
															504), DOES_NOT_ACCEPT_MAIL_521(521), ACCESS_DENIED_530(
																	530), REQUESTED_ACTION_NOT_TAKEN_550(
																			550), USER_NOT_LOCAL_551(
																					551), REQUESTED_MAIL_ACTION_ABORTED_552(
																							552), REQUESTED_ACTION_NOT_TAKEN_553(
																									553), TRANSACTION_FAILED_554(
																											554);

	private final int code;

	private SMTPStatusCode(int code) {
		this.code = code;
	}

	public int getCode() {
		return code;
	}

	@JsonValue
	public String getName() {
		return String.valueOf(code);
	}
}

class MailObjectMapper extends com.fasterxml.jackson.databind.ObjectMapper {
	private static final long serialVersionUID = 1L;

	List<MockResponseMail> readValue(String content) {
		try {
			return this.readValue(content, new TypeReference<List<MockResponseMail>>() {
			});
		} catch (IOException ioe) {
			throw new CompletionException(ioe);
		}
	}
}

interface SMTPCondition {
}
record SMTPMatchAllCondition(String operator) implements SMTPCondition {
	public static SMTPMatchAllCondition matchAll() {
		return new SMTPMatchAllCondition("matchAll");
	}
}

record SMTPMatchContains(String operator, String matchingValue) implements SMTPCondition {
	public static SMTPMatchContains of(String matchingValue) {
		return new SMTPMatchContains("contains", matchingValue);
	}
}

record SMTPResponse(SMTPStatusCode code, String message) {

}

record SMTPBehaviour(SMTPCommand command, SMTPCondition condition, SMTPResponse response) {
}

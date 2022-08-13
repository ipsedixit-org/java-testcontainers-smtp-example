package org.ipsedixit.javatestcontainerssmtpexample;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/***
 * Dashboard is generic class responsible to notify final user or have a
 * FrontEnd to show the problems. I choose dashboard name because it is a pretty
 * common name for this purpose. In this case dashboard send an email in case
 * there is no more candy.
 */
public class Dashboard {
	private static final Logger LOGGER = LogManager.getLogger();
	public static final String ALARM_CANDY = "No more candy!!!";
	public static final String MAIL_MESSAGE_BODY = "Oh no! No more candy...";

	private final List<String> alarms;
	private final ConfEmail confEmail;

	public Dashboard(ConfEmail confEmail) {
		this.alarms = new ArrayList<>();
		this.confEmail = confEmail;
	}

	public void alarmNoCandy() {
		this.alarms.add(ALARM_CANDY);
		if (this.confEmail.enableSendEmail()) {

			try {
				LOGGER.info("{} Sending email!!", ALARM_CANDY);
				MailUtil.send(this.confEmail.host(), this.confEmail.port(), this.confEmail.fromAddr(),
						this.confEmail.toAddrs(), ALARM_CANDY, MAIL_MESSAGE_BODY);
			} catch (Exception exc) {
				LOGGER.error("An error occurred in sending email for NO more candy", exc);
			}
		}
	}

	List<String> getAllAlarms() {
		// Defensive copy because list in Java is mutable
		return alarms.stream().toList();
	}
}

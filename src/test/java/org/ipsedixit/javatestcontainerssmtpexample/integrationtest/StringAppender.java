package org.ipsedixit.javatestcontainerssmtpexample.integrationtest;

import java.io.*;

import org.apache.logging.log4j.*;
import org.apache.logging.log4j.core.*;
import org.apache.logging.log4j.core.appender.*;
import org.apache.logging.log4j.core.config.*;
import org.apache.logging.log4j.core.config.plugins.PluginFactory;
import org.apache.logging.log4j.core.layout.PatternLayout;

/**
 * A Custom Appender for Log4j2 that logs to a String. This is useful for
 * testing logging.
 *
 * @author rewolf
 */
public class StringAppender extends AbstractOutputStreamAppender<StringAppender.StringOutputStreamManager> {
	private static LoggerContext context = (LoggerContext) LogManager.getContext(false);
	private static Configuration configuration = context.getConfiguration();
	private StringOutputStreamManager manager;

	private StringAppender(String name, Layout<? extends Serializable> layout, StringOutputStreamManager manager,
			boolean ignoreExceptions, boolean immediateFlush) {
		super(name, layout, null, ignoreExceptions, immediateFlush, null, manager);
		this.manager = manager;
	}

	/**
	 * Create a StringAppender with a given output format
	 * 
	 * @param nullablePatternString
	 *            Can be {@code null}. The PatternLayout string for log output.
	 * @return a new StringAppender
	 */
	@PluginFactory
	public static StringAppender createStringAppender(final String nullablePatternString) {
		final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		final PatternLayout layout;

		if (nullablePatternString == null) {
			layout = PatternLayout.createDefaultLayout();
		} else {
			layout = PatternLayout.newBuilder().withPattern(nullablePatternString).build();
		}

		return new StringAppender("StringAppender", layout,
				new StringOutputStreamManager(outputStream, "StringStream", layout), false, true);
	}

	public void addToLogger(final String loggerName, final Level level) {
		LoggerConfig loggerConfig = configuration.getLoggerConfig(loggerName);
		loggerConfig.addAppender(this, level, null);
		context.updateLoggers();
	}

	public void removeFromLogger(final String loggerName) {
		LoggerConfig loggerConfig = configuration.getLoggerConfig(loggerName);
		loggerConfig.removeAppender("StringAppender");
		context.updateLoggers();
	}

	public String getOutput() {
		manager.flush();
		return new String(manager.getStream().toByteArray());
	}

	/**
	 * StringOutputStreamManager to manage an in memory byte-stream representing our
	 * stream
	 */
	static class StringOutputStreamManager extends OutputStreamManager {
		ByteArrayOutputStream stream;

		StringOutputStreamManager(ByteArrayOutputStream os, String streamName, Layout<?> layout) {
			super(os, streamName, layout, false);
			stream = os;
		}

		ByteArrayOutputStream getStream() {
			return stream;
		}
	}
}

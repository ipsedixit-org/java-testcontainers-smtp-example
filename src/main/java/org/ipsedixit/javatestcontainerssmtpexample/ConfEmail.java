package org.ipsedixit.javatestcontainerssmtpexample;

import java.util.List;

public record ConfEmail(boolean enableSendEmail, String host, int port, String fromAddr, List<String> toAddrs) {
}

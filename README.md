# Java testcontainers smtp example

A full example of sending an email using testcontainers and a mock smtp server.

In a nutshell there is a candy box with a limited number of candies.
When all candies are eat then an alarm is sent to the dashboard and then an email.

For SMTP server is used a [James Mock server](https://medium.com/linagora-engineering/a-mock-smtp-server-for-remote-mail-delivery-testing-2d1a2cfd2798), as described in [my previous post](https://ipsedixit.org/blog/2021/2021-06-03-mock-smtp-server.html).

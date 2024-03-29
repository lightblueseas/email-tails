/**
 * The MIT License
 *
 * Copyright (C) 2015 Asterios Raptis
 *
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
 * LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 * OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package io.github.astrapi69.email;

import static org.junit.jupiter.api.Assertions.assertFalse;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.util.Properties;

import javax.mail.Address;
import javax.mail.BodyPart;
import javax.mail.MessagingException;
import javax.mail.internet.AddressException;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMultipart;

import lombok.experimental.ExtensionMethod;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import io.github.astrapi69.email.messages.EmailConstants;
import io.github.astrapi69.email.messages.EmailMessage;
import io.github.astrapi69.email.messages.EmailMessageWithAttachments;
import io.github.astrapi69.email.send.SendEmail;
import io.github.astrapi69.email.send.SendMail;
import io.github.astrapi69.email.utils.EmailExtensions;
import io.github.astrapi69.lang.ClassExtensions;
import io.github.astrapi69.lang.PackageExtensions;
import io.github.astrapi69.string.StringExtensions;

/**
 * The unit test class for the class {@link SendEmail}.
 *
 * @version 1.0
 * @author Asterios Raptis
 */
@ExtensionMethod(StringExtensions.class)
public class SendEmailTest
{

	/** The email properties. */
	private Properties emailProperties = null;

	/**
	 * Sets the up.
	 *
	 * @throws Exception
	 *             the exception
	 */
	@BeforeEach
	public void setUp() throws Exception
	{
		// emailProperties = new Properties();
		// emailProperties.put( "mail.smtp.host", "localhost" );
	}

	/**
	 * Tear down.
	 *
	 * @throws Exception
	 *             the exception
	 */
	@AfterEach
	public void tearDown() throws Exception
	{
		emailProperties = null;
	}

	@Test
	@Disabled
	public void testSendEmailEmailAttachments_01()
		throws AddressException, UnsupportedEncodingException, MessagingException
	{
		final EmailMessage message = new EmailMessage();
		final SendMail sender = new SendEmail();
		final Address from = EmailExtensions.newAddress("asterios.raptis@gmx.net", null);
		message.addFrom(EmailExtensions.getAddressArray(from));
		message
			.addTo(EmailExtensions.newAddress("asterios.raptis@yahoo.gr", "Asterios Raptis", null));
		message.setSubject("Text/plain Attachments");

		message.setText("This is the text from the email.");
		final MimeMultipart content = new MimeMultipart();
		message.setContent(content);

		final BodyPart part = new MimeBodyPart();
		part.setFileName("text.txt");
		part.setDescription("One textfile");
		part.setDisposition(javax.mail.Part.ATTACHMENT);
		part.setText("This is an email with unicode characters. ÄÖÜ_äöüß.");

		content.addBodyPart(part);
		final String messageId = sender.sendEmailMessage(message);
		System.out.println("messageId:" + messageId);
	}

	@Test
	@Disabled
	public void testSendEmailEmailAttachments_02()
		throws IOException, MessagingException, URISyntaxException
	{
		final SendMail sender = new SendEmail();

		final EmailMessage message = new EmailMessage();
		final Address from = EmailExtensions.newAddress("asterios.raptis@gmx.net", null);
		message.addFrom(EmailExtensions.getAddressArray(from));
		message
			.addTo(EmailExtensions.newAddress("asterios.raptis@yahoo.gr", "Asterios Raptis", null));
		message.setSubject("mailerWithNewMethods.GIF");
		final EmailMessageWithAttachments mailer = new EmailMessageWithAttachments(message);

		final BodyPart main = new MimeBodyPart();
		main.setText("This is an email with unicode characters. ÄÖÜ_äöüß. _1");

		mailer.addAttachment(main);

		final BodyPart gif = new MimeBodyPart();
		gif.setFileName("green.gif");
		final String path = PackageExtensions.getPackagePathWithSlash(this);
		final String filePath = path + "test.gif";
		System.out.println("filePath:" + filePath);
		final File giffile = ClassExtensions.getResourceAsFile(filePath, this);

		mailer.addAttachment(giffile);

		final String messageId = sender.sendEmailMessage(message);
		System.out.println("messageId:" + messageId);
	}

	@Test
	@Disabled
	public void testSendEmailEmailMessage()
		throws AddressException, UnsupportedEncodingException, MessagingException
	{
		final SendMail sender = new SendEmail(emailProperties);
		final EmailMessage message = new EmailMessage();
		final Address from = EmailExtensions.newAddress("asterios.raptis@gmx.net",
			"Asterios Raptis");
		message.addFrom(EmailExtensions.getAddressArray(from));
		message.addTo(EmailExtensions.newAddress("asterios.raptis@yahoo.gr", "Asterios Raptis",
			EmailConstants.CHARSET_UTF8));
		message.addTo(EmailExtensions.newAddress("asterios.raptis@web.de", "Asterios Raptis",
			EmailConstants.CHARSET_UTF8));
		message.addTo(EmailExtensions.newAddress("asterios.raptis@gmx.net", "Asterios Raptis",
			EmailConstants.CHARSET_UTF8));
		message.setSubject("Simple email with unicod characters. ÄÖÜ_äöüß.", "UTF-8");
		message.setContent("This is a simple email send with SendEmail. ÄÖÜ_äöüß.",
			EmailConstants.MIMETYPE_TEXT_PLAIN + "; charset=UTF-8");
		final String messageId = sender.sendEmailMessage(message);
		System.out.println("messageId:" + messageId);
		assertFalse(messageId.isNullOrEmpty());
	}

	@Test
	@Disabled
	public void testSendEmailStringStringStringString()
		throws AddressException, UnsupportedEncodingException, MessagingException
	{
		final SendMail sender = new SendEmail(emailProperties);
		final EmailMessage message = new EmailMessage();
		final Address from = EmailExtensions.newAddress("asterios.raptis@gmx.net",
			"Asterios Raptis");
		message.addFrom(EmailExtensions.getAddressArray(from));
		message.addTo(EmailExtensions.newAddress("asterios.raptis@yahoo.gr", "Asterios Raptis",
			EmailConstants.CHARSET_UTF8));
		message.addTo(EmailExtensions.newAddress("asterios.raptis@web.de", "Asterios Raptis",
			EmailConstants.CHARSET_UTF8));
		message.addTo(EmailExtensions.newAddress("asterios.raptis@gmx.net", "Asterios Raptis",
			EmailConstants.CHARSET_UTF8));
		final String messageId = sender.sendEmail("asterios.raptis@yahoo.gr",
			"asterios.raptis@gmx.net", "Test method testSendEmailStringStringStringString().",
			"This is a simple email send with SendEmail. ÄÖÜ_äöüß.");
		System.out.println("messageId:" + messageId);
		assertFalse(messageId.isNullOrEmpty());
	}

}

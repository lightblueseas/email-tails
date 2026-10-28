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
package io.github.astrapi69.email.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Properties;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;

import jakarta.mail.Address;
import jakarta.mail.Message;
import jakarta.mail.Session;
import jakarta.mail.internet.ContentType;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;

/**
 * {@link MimeMessageUtils} on a message that already carries one recipient of every type: each
 * operation is run for TO, CC and BCC alike.
 */
class MimeMessageUtilsTest
{

	static Stream<Message.RecipientType> recipientTypes()
	{
		return Stream.of(Message.RecipientType.TO, Message.RecipientType.CC,
			Message.RecipientType.BCC);
	}

	private MimeMessage message;

	@BeforeEach
	void setUp() throws Exception
	{
		message = new MimeMessage(Session.getDefaultInstance(new Properties()));
		message.setFrom(new InternetAddress("sender@example.com"));
		message.setRecipient(Message.RecipientType.TO, new InternetAddress("to@example.com"));
		message.setRecipient(Message.RecipientType.CC, new InternetAddress("cc@example.com"));
		message.setRecipient(Message.RecipientType.BCC, new InternetAddress("bcc@example.com"));
		message.setSubject("Subject", "UTF-8");
		message.setText("Body", "UTF-8");
		message.saveChanges();
	}

	@ParameterizedTest(name = "{0}")
	@MethodSource("recipientTypes")
	void addRecipientsKeepsTheExistingOnes(Message.RecipientType type) throws Exception
	{
		MimeMessageUtils.addRecipients(message, type,
			List.of(new InternetAddress("added@example.com")));

		List<String> recipients = addresses(message.getRecipients(type));
		assertEquals(2, recipients.size());
		assertTrue(recipients.contains("added@example.com"));
	}

	@ParameterizedTest(name = "{0}")
	@MethodSource("recipientTypes")
	void addRecipientsWithNullAddsNothing(Message.RecipientType type) throws Exception
	{
		MimeMessageUtils.addRecipients(message, type, null);

		assertEquals(1, message.getRecipients(type).length);
	}

	@ParameterizedTest(name = "{0}")
	@MethodSource("recipientTypes")
	void setRecipientsReplacesTheExistingOnes(Message.RecipientType type) throws Exception
	{
		MimeMessageUtils.setRecipients(message, type,
			List.of(new InternetAddress("only@example.com")));

		assertEquals(List.of("only@example.com"), addresses(message.getRecipients(type)));
	}

	@ParameterizedTest(name = "{0}")
	@MethodSource("recipientTypes")
	void setRecipientsWithEmptyListRemovesThem(Message.RecipientType type) throws Exception
	{
		MimeMessageUtils.setRecipients(message, type, List.of());

		assertNull(message.getRecipients(type));
	}

	@ParameterizedTest(name = "{0}")
	@MethodSource("recipientTypes")
	void clearRecipientsRemovesOnlyThatType(Message.RecipientType type) throws Exception
	{
		MimeMessageUtils.clearRecipients(message, type);

		assertNull(message.getRecipients(type));
		int remaining = 0;
		for (Message.RecipientType other : recipientTypes().toList())
		{
			remaining += message.getRecipients(other) == null ? 0 : 1;
		}
		assertEquals(2, remaining, "the other two types are untouched");
	}

	@Test
	void setSubjectReplacesAndNullKeeps() throws Exception
	{
		MimeMessageUtils.setSubject(message, "Grüße");
		assertEquals("Grüße", message.getSubject());

		MimeMessageUtils.setSubject(message, null);
		assertEquals("Grüße", message.getSubject());
	}

	@Test
	void setFromReplacesAndNullKeeps() throws Exception
	{
		MimeMessageUtils.setFrom(message, new InternetAddress("new@example.com"));
		assertEquals(List.of("new@example.com"), addresses(message.getFrom()));

		MimeMessageUtils.setFrom(message, null);
		assertEquals(List.of("new@example.com"), addresses(message.getFrom()));
	}

	@ParameterizedTest
	@NullAndEmptySource
	void appendNothingLeavesTheBody(String text) throws Exception
	{
		MimeMessageUtils.appendTextContent(message, text, "html");
		message.saveChanges();

		assertEquals("Body", message.getContent());
		assertEquals("text/plain", new ContentType(message.getContentType()).getBaseType());
	}

	@Test
	void appendToTextBodySeparatesWithABlankLine() throws Exception
	{
		MimeMessageUtils.appendTextContent(message, "More", null);
		// setText invalidates the Content-Type header until saveChanges() rewrites it, which is
		// what EmlModifier does before it writes the file
		message.saveChanges();

		assertEquals("Body\n\nMore", message.getContent());
		assertEquals("text/plain", new ContentType(message.getContentType()).getBaseType());
	}

	@Test
	void appendWithHtmlSubtypeChangesTheContentType() throws Exception
	{
		MimeMessageUtils.appendTextContent(message, "<p>More</p>", "HTML");
		message.saveChanges();

		assertEquals("Body\n\n<p>More</p>", message.getContent());
		assertEquals("text/html", new ContentType(message.getContentType()).getBaseType());
	}

	@Test
	void appendToMultipartAddsATextPart() throws Exception
	{
		MimeMultipart multipart = new MimeMultipart();
		MimeBodyPart first = new MimeBodyPart();
		first.setText("First part", "UTF-8");
		multipart.addBodyPart(first);
		message.setContent(multipart);
		message.saveChanges();

		MimeMessageUtils.appendTextContent(message, "Second part", "plain");

		MimeMultipart content = (MimeMultipart)message.getContent();
		assertEquals(2, content.getCount());
		assertEquals("First part", content.getBodyPart(0).getContent());
		assertEquals("\n\nSecond part", content.getBodyPart(1).getContent());
		assertEquals("text/plain",
			new ContentType(content.getBodyPart(1).getContentType()).getBaseType());
	}

	@Test
	void appendToUnsupportedContentIsRefused() throws Exception
	{
		message.setContent(new byte[] { 1, 2, 3 }, "application/octet-stream");
		message.saveChanges();

		IllegalStateException refused = assertThrows(IllegalStateException.class,
			() -> MimeMessageUtils.appendTextContent(message, "text", "plain"));

		assertTrue(refused.getMessage().contains("[B"), "names the content class");
	}

	private static List<String> addresses(Address[] addresses)
	{
		if (addresses == null)
		{
			return List.of();
		}
		return java.util.Arrays.stream(addresses)
			.map(address -> ((InternetAddress)address).getAddress()).toList();
	}
}

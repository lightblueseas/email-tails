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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Properties;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import jakarta.mail.Address;
import jakarta.mail.Message;
import jakarta.mail.Session;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

class EmlModifierTest
{

	@TempDir
	Path tempDir;

	private Path inputEml;
	private Path outputEml;

	@BeforeEach
	void setUp() throws Exception
	{
		inputEml = tempDir.resolve("test-input.eml");
		outputEml = tempDir.resolve("test-output.eml");

		Session session = Session.getDefaultInstance(new Properties());
		MimeMessage message = new MimeMessage(session);
		message.setFrom(new InternetAddress("sender@example.com"));
		message.setRecipient(Message.RecipientType.TO,
			new InternetAddress("recipient@example.com"));
		message.setSubject("Original Subject", "UTF-8");
		message.setText("Original Body Content", "UTF-8");
		message.saveChanges();

		try (OutputStream out = Files.newOutputStream(inputEml))
		{
			message.writeTo(out);
		}
	}

	@Test
	void testModifyEmlSubjectAndRecipient() throws Exception
	{
		EmailModificationRequest request = EmailModificationRequest.builder()
			.subject("Modified: Original Subject")
			.addCc(List.of(new InternetAddress("cc@example.com"))).build();

		EmlModifier.modifyEml(inputEml, outputEml, request);

		assertTrue(Files.exists(outputEml), "Die modifizierte EML-Datei muss existieren.");

		Session session = Session.getDefaultInstance(new Properties());
		try (InputStream in = Files.newInputStream(outputEml))
		{
			MimeMessage modifiedMessage = new MimeMessage(session, in);

			assertEquals("Modified: Original Subject", modifiedMessage.getSubject());

			Address[] ccRecipients = modifiedMessage.getRecipients(Message.RecipientType.CC);
			assertNotNull(ccRecipients, "CC-Empfaenger duerfen nicht null sein.");
			assertEquals(1, ccRecipients.length);
			assertEquals("cc@example.com", ((InternetAddress)ccRecipients[0]).getAddress());
		}
	}

	@Test
	void testModifyEmlAppendBodyWithSubtype() throws Exception
	{
		EmailModificationRequest request = EmailModificationRequest.builder()
			.bodyAppend("\n\nZusatztext im HTML-Format.").bodySubtype("html").build();

		EmlModifier.modifyEml(inputEml, outputEml, request);

		try (InputStream in = Files.newInputStream(outputEml))
		{
			MimeMessage modifiedMessage = new MimeMessage(
				Session.getDefaultInstance(new Properties()), in);
			Object content = modifiedMessage.getContent();

			assertTrue(content instanceof String, "Inhalt sollte ein String sein.");
			String contentString = (String)content;
			assertTrue(contentString.contains("Zusatztext im HTML-Format."),
				"Der angehaengte Text muss im Inhalt enthalten sein.");
		}
	}

	@Test
	void testModifyEmlComprehensive() throws Exception
	{
		InternetAddress newFrom = new InternetAddress("new-sender@example.com", "Neuer Absender");
		InternetAddress newTo = new InternetAddress("new-to@example.com");
		InternetAddress newCc = new InternetAddress("new-cc@example.com");
		InternetAddress newBcc = new InternetAddress("new-bcc@example.com");

		EmailModificationRequest request = EmailModificationRequest.builder()
			.subject("Komplett neuer Betreff").from(newFrom).addTo(List.of(newTo))
			.addCc(List.of(newCc)).addBcc(List.of(newBcc)).bodyAppend("\n\nVertraulich.")
			.bodySubtype("plain").build();

		EmlModifier.modifyEml(inputEml, outputEml, request);

		try (InputStream in = Files.newInputStream(outputEml))
		{
			MimeMessage msg = new MimeMessage(Session.getDefaultInstance(new Properties()), in);
			Address[] recipients = msg.getRecipients(Message.RecipientType.TO);
			Address[] from = msg.getFrom();
			assertEquals("Komplett neuer Betreff", msg.getSubject());
			assertEquals("new-sender@example.com", ((InternetAddress)from[0]).getAddress());
			assertEquals("new-to@example.com", ((InternetAddress)recipients[1]).getAddress());
			assertEquals("new-cc@example.com",
				((InternetAddress)msg.getRecipients(Message.RecipientType.CC)[0]).getAddress());

			String content = msg.getContent().toString();
			assertTrue(content.contains("Vertraulich."));
		}
	}
}

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

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

import jakarta.mail.Message;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;

public final class EmlModifier
{

	private EmlModifier()
	{
	}

	public static void modifyEml(Path inputPath, Path outputPath, EmailModificationRequest request)
		throws Exception
	{
		Properties properties = new Properties();
		Session session = Session.getDefaultInstance(properties);

		try (InputStream in = Files.newInputStream(inputPath))
		{
			MimeMessage message = new MimeMessage(session, in);

			MimeMessageUtils.setSubject(message, request.subject());
			MimeMessageUtils.setFrom(message, request.from());
			MimeMessageUtils.addRecipients(message, Message.RecipientType.TO, request.addTo());
			MimeMessageUtils.addRecipients(message, Message.RecipientType.CC, request.addCc());
			MimeMessageUtils.addRecipients(message, Message.RecipientType.BCC, request.addBcc());
			MimeMessageUtils.appendTextContent(message, request.bodyAppend(),
				request.bodySubtype());

			message.saveChanges();

			try (OutputStream out = Files.newOutputStream(outputPath))
			{
				message.writeTo(out);
			}
		}
	}

	public static void modifyEml(String inputPath, String outputPath,
		EmailModificationRequest request) throws Exception
	{
		modifyEml(Path.of(inputPath), Path.of(outputPath), request);
	}
}

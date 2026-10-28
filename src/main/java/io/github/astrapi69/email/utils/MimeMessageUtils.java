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

import java.util.List;

import jakarta.mail.Address;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;

/**
 * Static helpers that change single parts of a {@link MimeMessage} in place: the subject, the
 * sender, the recipients of one type, and the body text. Every method tolerates a null argument and
 * then leaves the message as it is, so a caller can pass through whatever a request carries.
 */
public final class MimeMessageUtils
{

	private MimeMessageUtils()
	{
	}

	/**
	 * Sets the subject, encoded as UTF-8. A null subject leaves the current one in place.
	 *
	 * @param message
	 *            the message
	 * @param subject
	 *            the new subject, or null to keep the current one
	 * @throws MessagingException
	 *             if the header cannot be set
	 */
	public static void setSubject(MimeMessage message, String subject) throws MessagingException
	{
		if (subject != null)
		{
			message.setSubject(subject, "UTF-8");
		}
	}

	/**
	 * Replaces the sender. A null address leaves the current one in place.
	 *
	 * @param message
	 *            the message
	 * @param from
	 *            the new sender, or null to keep the current one
	 * @throws MessagingException
	 *             if the header cannot be set
	 */
	public static void setFrom(MimeMessage message, InternetAddress from) throws MessagingException
	{
		if (from != null)
		{
			message.setFrom(from);
		}
	}

	/**
	 * Removes every recipient of the given type (TO, CC or BCC).
	 *
	 * @param message
	 *            the message
	 * @param type
	 *            the recipient type
	 * @throws MessagingException
	 *             if the header cannot be removed
	 */
	public static void clearRecipients(MimeMessage message, Message.RecipientType type)
		throws MessagingException
	{
		message.setRecipients(type, (Address[])null);
	}

	/**
	 * Sets the recipients of the given type, replacing the ones the message has. A null or empty
	 * list leaves the message without recipients of that type.
	 *
	 * @param message
	 *            the message
	 * @param type
	 *            the recipient type
	 * @param addresses
	 *            the new recipients
	 * @throws MessagingException
	 *             if the header cannot be set
	 */
	public static void setRecipients(MimeMessage message, Message.RecipientType type,
		List<InternetAddress> addresses) throws MessagingException
	{
		clearRecipients(message, type);
		if (addresses != null && !addresses.isEmpty())
		{
			message.setRecipients(type, addresses.toArray(new InternetAddress[0]));
		}
	}

	/**
	 * Adds recipients of the given type, keeping the ones the message has. A null list adds
	 * nothing.
	 *
	 * @param message
	 *            the message
	 * @param type
	 *            the recipient type
	 * @param addresses
	 *            the recipients to add
	 * @throws MessagingException
	 *             if the header cannot be set
	 */
	public static void addRecipients(MimeMessage message, Message.RecipientType type,
		List<InternetAddress> addresses) throws MessagingException
	{
		if (addresses != null)
		{
			for (InternetAddress address : addresses)
			{
				message.addRecipient(type, address);
			}
		}
	}

	/**
	 * Appends text to the body. A text body becomes a UTF-8 text body of the given subtype with the
	 * text appended after a blank line; a multipart body gets a new text part of that subtype. A
	 * null or empty text leaves the body as it is.
	 *
	 * @param message
	 *            the message
	 * @param textToAppend
	 *            the text to append, or null
	 * @param subtype
	 *            the text subtype, for example "plain" or "html"; null or blank means "plain"
	 * @throws IllegalStateException
	 *             if the body is neither text nor multipart, for example a binary attachment-only
	 *             message
	 * @throws Exception
	 *             if the body cannot be read or written
	 */
	public static void appendTextContent(MimeMessage message, String textToAppend, String subtype)
		throws Exception
	{
		if (textToAppend == null || textToAppend.isEmpty())
		{
			return;
		}

		String effectiveSubtype = (subtype == null || subtype.isBlank())
			? "plain"
			: subtype.toLowerCase();
		String contentType = "text/" + effectiveSubtype + "; charset=UTF-8";

		Object content = message.getContent();
		if (content instanceof String)
		{
			message.setText(content + "\n\n" + textToAppend, "UTF-8", effectiveSubtype);
		}
		else if (content instanceof MimeMultipart multipart)
		{
			MimeBodyPart newTextPart = new MimeBodyPart();
			newTextPart.setContent("\n\n" + textToAppend, contentType);
			multipart.addBodyPart(newTextPart);
			message.setContent(multipart);
		}
		else
		{
			throw new IllegalStateException(
				"Unsupported content type for appending text: " + content.getClass().getName());
		}
	}
}

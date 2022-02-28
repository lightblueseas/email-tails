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
package io.github.astrapi69.email.send;

import java.io.UnsupportedEncodingException;
import java.util.Collection;

import javax.mail.MessagingException;
import javax.mail.internet.AddressException;

import io.github.astrapi69.email.messages.EmailMessage;
import io.github.astrapi69.email.messages.EmailMessageWithAttachments;

/**
 * The Interface SendMail.
 */
public interface SendMail
{

	/**
	 * Checks the debug flag if it is true or false.
	 *
	 * @return true, if the debug flag is true otherwise false.
	 */
	boolean isDebug();

	/**
	 * Sets the debug flag.
	 *
	 * @param debug
	 *            The debug flag to set.
	 */
	void setDebug(final boolean debug);

	/**
	 * Sends a simple plain text email.
	 *
	 * @param to
	 *            The to Address.
	 * @param from
	 *            The from Address.
	 * @param subject
	 *            The subject from the email.
	 * @param message
	 *            The Message from the email.
	 * @return 's the messageid.
	 * @throws AddressException
	 *             the address exception
	 * @throws UnsupportedEncodingException
	 *             is thrown if the encoding not supported
	 * @throws MessagingException
	 *             is thrown if the underlying implementation does not support modification of
	 *             existing values
	 */
	String sendEmail(final String to, final String from, final String subject, final String message)
		throws AddressException, UnsupportedEncodingException, MessagingException;

	/**
	 * Sends the given EmailMessage.
	 *
	 * @param emailMessage
	 *            The EmailMessage to send.
	 *
	 * @return 's the messageid.
	 *
	 * @throws MessagingException
	 *             is thrown if the underlying implementation does not support modification of
	 *             existing values
	 */
	String sendEmailMessage(final EmailMessage emailMessage) throws MessagingException;

	/**
	 * Send email messages.
	 *
	 * @param emailMessages
	 *            the email messages
	 * @return the collection
	 * @throws MessagingException
	 *             is thrown if the underlying implementation does not support modification of
	 *             existing values
	 */
	Collection<String> sendEmailMessages(final Collection<EmailMessage> emailMessages)
		throws MessagingException;

	/**
	 * Send email messages with atachments.
	 *
	 * @param emailAttachments
	 *            the email attachments
	 * @return the collection
	 * @throws MessagingException
	 *             is thrown if the underlying implementation does not support modification of
	 *             existing values
	 */
	Collection<String> sendEmailMessagesWithAtachments(
		final Collection<EmailMessageWithAttachments> emailAttachments) throws MessagingException;

	/**
	 * Sends the EmailMessage thats wrappes from the EmailMessagWithAttachments.
	 *
	 * @param emailAttachments
	 *            The EmailAttachments who wrappes the EmailMessage.
	 *
	 * @return 's the messageid.
	 *
	 * @throws MessagingException
	 *             is thrown if the underlying implementation does not support modification of
	 *             existing values
	 */
	String sendEmailMessageWithAttachments(final EmailMessageWithAttachments emailAttachments)
		throws MessagingException;

}

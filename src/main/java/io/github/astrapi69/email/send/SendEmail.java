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

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Properties;

import javax.mail.Address;
import javax.mail.Authenticator;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;

import lombok.Getter;
import io.github.astrapi69.email.messages.EmailConstants;
import io.github.astrapi69.email.messages.EmailMessage;
import io.github.astrapi69.email.messages.EmailMessageWithAttachments;
import io.github.astrapi69.email.utils.EmailExtensions;
import io.github.astrapi69.resourcebundle.properties.PropertiesFileExtensions;

/**
 * Helper-class for sending emails.
 *
 * @author Asterios Raptis
 */
public class SendEmail implements SendMail
{


	private Authenticator authenticator;

	/** The debug. */
	@Getter
	private boolean debug;

	/** The properties file. */
	private Properties properties;

	/** The Session-object. */
	private Session session;

	/**
	 * The Constructor.
	 */
	public SendEmail()
	{
		loadProperties();
	}

	/**
	 * The Constructor.
	 *
	 * @param properties
	 *            the properties
	 */
	public SendEmail(final Properties properties)
	{
		this.properties = properties;
	}

	/**
	 * The Constructor.
	 *
	 * @param properties
	 *            the properties
	 * @param authenticator
	 *            the authenticator
	 */
	public SendEmail(final Properties properties, final Authenticator authenticator)
	{
		this.properties = properties;
		this.authenticator = authenticator;
	}

	public Authenticator getAuthenticator()
	{
		return authenticator;
	}

	public void setAuthenticator(final Authenticator authenticator)
	{
		this.authenticator = authenticator;
	}

	/**
	 * Gets the Session-object.
	 *
	 * @return 's the Session-object.
	 */
	public synchronized Session getSession()
	{
		if (session == null)
		{
			if (authenticator != null)
			{
				session = Session.getInstance(properties, authenticator);
			}
			else
			{
				session = Session.getInstance(properties);
			}
		}
		return session;
	}

	/**
	 * Load properties.
	 */
	private void loadProperties()
	{
		loadPropertiesQueitly();
	}

	/**
	 * On load properties.
	 */
	private void loadPropertiesQueitly()
	{
		try
		{
			properties = PropertiesFileExtensions.loadProperties(this,
				EmailConstants.PROPERTIES_FILENAME);
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String sendEmail(final String to, final String from, final String subject,
		final String message)
		throws AddressException, UnsupportedEncodingException, MessagingException
	{
		String messageId;
		final Address[] fromAddress = EmailExtensions
			.getAddressArray(EmailExtensions.newAddress(from, null));
		final Address toAddress = EmailExtensions.newAddress(to, null, null);
		final EmailMessage email = new EmailMessage();
		email.addFrom(fromAddress);
		email.addTo(toAddress);
		email.setSubject(subject);
		email.setContent(message, EmailConstants.MIMETYPE_TEXT_PLAIN);
		messageId = sendEmailMessage(email);
		return messageId;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String sendEmailMessage(final EmailMessage emailMessage) throws MessagingException
	{
		Transport.send(emailMessage);
		return emailMessage.getMessageID();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Collection<String> sendEmailMessages(final Collection<EmailMessage> emailMessages)
		throws MessagingException
	{
		final List<String> messageIds = new ArrayList<>();
		for (final EmailMessage emailMessage : emailMessages)
		{
			final String messageId = sendEmailMessage(emailMessage);
			messageIds.add(messageId);
		}
		return messageIds;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Collection<String> sendEmailMessagesWithAtachments(
		final Collection<EmailMessageWithAttachments> emailAttachments) throws MessagingException
	{
		final List<String> messageIds = new ArrayList<>();
		for (final EmailMessageWithAttachments emailAttachment : emailAttachments)
		{
			final String messageId = sendEmailMessageWithAttachments(emailAttachment);
			messageIds.add(messageId);
		}
		return messageIds;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String sendEmailMessageWithAttachments(
		final EmailMessageWithAttachments emailAttachments) throws MessagingException
	{
		return sendEmailMessage(emailAttachments.getEmailMessage());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setDebug(final boolean debug)
	{
		this.debug = debug;
		getSession().setDebug(debug);
	}

}

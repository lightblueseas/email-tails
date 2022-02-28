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

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.Base64;

import javax.activation.DataHandler;
import javax.mail.Address;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

import lombok.experimental.ExtensionMethod;
import io.github.astrapi69.email.messages.EmailConstants;
import io.github.astrapi69.email.messages.EmailMessage;
import io.github.astrapi69.file.read.ReadFileExtensions;
import io.github.astrapi69.string.StringExtensions;

/**
 * The class EmailExtensions provides methods for create email addresses and validate email
 * addresses.
 */
@ExtensionMethod(StringExtensions.class)
public class EmailExtensions
{

	/**
	 * Adds a 'to' recipient to the email message.
	 *
	 * @param recipientEmail
	 *            the recipient email
	 * @param recipientPersonal
	 *            the recipient personal
	 * @param recipientCharset
	 *            the recipient charset
	 * @param emailMessage
	 *            the email message
	 * @return the email message
	 * @throws UnsupportedEncodingException
	 *             is thrown if the encoding not supported
	 * @throws MessagingException
	 *             is thrown if the underlying implementation does not support modification of
	 *             existing values
	 */
	public static EmailMessage addToRecipientToEmailMessage(final String recipientEmail,
		final String recipientPersonal, final String recipientCharset,
		final EmailMessage emailMessage) throws UnsupportedEncodingException, MessagingException
	{

		// Try to create the recipient Address
		Address recipientAddress = EmailExtensions.newAddress(recipientEmail, recipientPersonal,
			recipientCharset);


		// Set recipient
		if (null != recipientAddress)
		{

			emailMessage.addTo(recipientAddress);
		}
		else
		{
			emailMessage.setRecipients(Message.RecipientType.TO, recipientEmail);

		}
		return emailMessage;
	}

	/**
	 * Gets the address array from the given address objects.
	 *
	 * @param address
	 *            the address
	 * @return the address array
	 */
	public static Address[] getAddressArray(final Address... address)
	{
		return address;
	}

	/**
	 * Gets the encoding from the header.
	 *
	 * @param type
	 *            The string where the encoding is in.
	 *
	 * @return 's the encoding or null if its nothing found.
	 */
	public static String getCharsetFromContentType(final String type)
	{
		if (!type.isNullOrEmpty())
		{
			int start = type.indexOf(EmailConstants.CHARSET_PREFIX);
			if (start > 0)
			{
				start += EmailConstants.CHARSET_PREFIX.length();
				final int offset = type.indexOf(" ", start);
				if (offset > 0)
				{
					return type.substring(start, offset);
				}
				else
				{
					return type.substring(start);
				}
			}
		}
		return null;
	}

	/**
	 * Gets the string from the given {@link DataHandler}.
	 *
	 * @param dataHandler
	 *            the data handler
	 * @return the string
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	public static String getString(final DataHandler dataHandler) throws IOException
	{
		if (dataHandler != null)
		{
			final InputStream input = dataHandler.getDataSource().getInputStream();
			final byte[] data = ReadFileExtensions.toByteArray(input);
			// TODO check if the output is the same...
			return Base64.getEncoder().encodeToString(data);
		}
		return "";
	}

	/**
	 * Creates an Address from the given the email address as String object.
	 *
	 * @param address
	 *            The address in RFC822 format.
	 *
	 * @return The created InternetAddress-object from the given address.
	 *
	 * @throws UnsupportedEncodingException
	 *             is thrown if the encoding not supported
	 * @throws AddressException
	 *             is thrown if the parse failed
	 */
	public static Address newAddress(final String address)
		throws AddressException, UnsupportedEncodingException
	{
		return newAddress(address, null, null);
	}

	/**
	 * Creates from the given the address and personal name an Adress-object.
	 *
	 * @param emailAddress
	 *            The address in RFC822 format.
	 * @param personal
	 *            The personal name.
	 *
	 * @return The created Adress-object from the given address and personal name.
	 *
	 * @throws UnsupportedEncodingException
	 *             is thrown if the encoding not supported
	 * @throws AddressException
	 *             is thrown if the parse failed
	 */
	public static Address newAddress(final String emailAddress, final String personal)
		throws AddressException, UnsupportedEncodingException
	{
		return newAddress(emailAddress, personal, null);
	}

	/**
	 * Creates an Address from the given the address and personal name.
	 *
	 * @param address
	 *            The address in RFC822 format.
	 * @param personal
	 *            The personal name.
	 * @param charset
	 *            MIME charset to be used to encode the name as per RFC 2047.
	 *
	 * @return The created InternetAddress-object from the given address and personal name.
	 *
	 * @throws AddressException
	 *             is thrown if the parse failed
	 * @throws UnsupportedEncodingException
	 *             is thrown if the encoding not supported
	 */
	public static Address newAddress(final String address, String personal, final String charset)
		throws AddressException, UnsupportedEncodingException
	{
		if (personal.isNullOrEmpty())
		{
			personal = address;
		}
		final InternetAddress internetAdress = new InternetAddress(address);
		if (charset.isNullOrEmpty())
		{
			internetAdress.setPersonal(personal);
		}
		else
		{
			internetAdress.setPersonal(personal, charset);
		}
		return internetAdress;
	}


	/**
	 * Sets the from to the email message.
	 *
	 * @param senderEmail
	 *            the sender email
	 * @param senderPersonal
	 *            the sender personal
	 * @param senderCharset
	 *            the sender charset
	 * @param emailMessage
	 *            the email message
	 * @return the email message
	 * @throws UnsupportedEncodingException
	 *             is thrown if the encoding not supported
	 * @throws MessagingException
	 *             is thrown if the underlying implementation does not support modification of
	 *             existing values
	 */
	public static EmailMessage setFromToEmailMessage(final String senderEmail,
		final String senderPersonal, final String senderCharset, final EmailMessage emailMessage)
		throws UnsupportedEncodingException, MessagingException
	{
		// Try to create the sender Address
		Address senderAddress = null;
		senderAddress = EmailExtensions.newAddress(senderEmail, senderPersonal, senderCharset);


		// Set sender
		if (null != senderAddress)
		{
			emailMessage.setFrom(senderAddress);
		}
		else
		{
			emailMessage.setFrom(senderEmail);
		}
		return emailMessage;
	}

	/**
	 * Validate the given email address.
	 *
	 * @param emailAddress
	 *            the email address
	 *
	 * @return true, if successful
	 */
	public static boolean validateEmailAdress(final String emailAddress)
	{
		boolean isValid = true;
		try
		{
			final InternetAddress internetAddress = new InternetAddress(emailAddress);
			internetAddress.validate();
		}
		catch (final AddressException e)
		{
			isValid = false;
		}
		return isValid;
	}
}

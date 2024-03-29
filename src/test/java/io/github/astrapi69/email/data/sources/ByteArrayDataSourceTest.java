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
package io.github.astrapi69.email.data.sources;


import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;

import javax.activation.DataHandler;
import javax.activation.DataSource;

import org.apache.commons.codec.binary.Base64;
import org.junit.jupiter.api.Test;

import io.github.astrapi69.email.messages.Mimetypes;
import io.github.astrapi69.email.utils.EmailExtensions;

/**
 * The unit test class for the class {@link ByteArrayDataSource}.
 */
public class ByteArrayDataSourceTest
{

	/**
	 * Test method for the constructor of {@link ByteArrayDataSource}.
	 *
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	@Test
	public void testByteArrayDataSource() throws IOException
	{
		final String expected = "Sample Data";
		final DataSource dataSource = new ByteArrayDataSource(expected.getBytes(),
			Mimetypes.TEXT_PLAIN.getMimetype());
		final DataHandler dataHandler = new DataHandler(dataSource);
		final String rawString = EmailExtensions.getString(dataHandler);
		final String actual = new String(Base64.decodeBase64(rawString));
		assertEquals("Sample Data", expected, actual);
	}

}

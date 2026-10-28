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

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import jakarta.mail.internet.InternetAddress;

/**
 * The normalization {@link EmailModificationRequest} promises: never-null unmodifiable lists and a
 * lower-cased subtype that falls back to "plain".
 */
class EmailModificationRequestTest
{

	static Stream<Arguments> subtypes()
	{
		return Stream.of(Arguments.of(null, "plain"), Arguments.of("", "plain"),
			Arguments.of("   ", "plain"), Arguments.of("plain", "plain"),
			Arguments.of("Plain", "plain"), Arguments.of("html", "html"),
			Arguments.of("HTML", "html"));
	}

	@ParameterizedTest(name = "[{index}] \"{0}\" -> {1}")
	@MethodSource("subtypes")
	void bodySubtypeIsLowerCasedAndFallsBackToPlain(String given, String expected)
	{
		assertEquals(expected,
			EmailModificationRequest.builder().bodySubtype(given).build().bodySubtype(), "builder");
		assertEquals(expected,
			new EmailModificationRequest(null, null, null, null, null, null, given).bodySubtype(),
			"constructor");
	}

	@Test
	void nullListsBecomeEmptyLists()
	{
		EmailModificationRequest request = new EmailModificationRequest("subject", null, null, null,
			null, null, null);

		assertEquals(List.of(), request.addTo());
		assertEquals(List.of(), request.addCc());
		assertEquals(List.of(), request.addBcc());
		assertEquals("subject", request.subject());
		assertNull(request.from());
		assertNull(request.bodyAppend());
	}

	@Test
	void builderStartsWithNoChanges()
	{
		EmailModificationRequest request = EmailModificationRequest.builder().build();

		assertNull(request.subject());
		assertNull(request.from());
		assertNull(request.bodyAppend());
		assertEquals("plain", request.bodySubtype());
		assertTrue(request.addTo().isEmpty());
		assertTrue(request.addCc().isEmpty());
		assertTrue(request.addBcc().isEmpty());
	}

	@Test
	void listsAreCopiedAndCannotBeChangedAfterwards() throws Exception
	{
		List<InternetAddress> to = new ArrayList<>();
		to.add(new InternetAddress("one@example.com"));
		EmailModificationRequest request = EmailModificationRequest.builder().addTo(to).build();

		to.add(new InternetAddress("two@example.com"));

		assertEquals(1, request.addTo().size(), "a later change of the source list is not seen");
		assertThrows(UnsupportedOperationException.class,
			() -> request.addTo().add(new InternetAddress("three@example.com")));
		assertThrows(UnsupportedOperationException.class,
			() -> request.addCc().add(new InternetAddress("three@example.com")));
		assertThrows(UnsupportedOperationException.class,
			() -> request.addBcc().add(new InternetAddress("three@example.com")));
	}

	@Test
	void builderCarriesEveryField() throws Exception
	{
		InternetAddress from = new InternetAddress("from@example.com", "From");
		InternetAddress to = new InternetAddress("to@example.com");
		InternetAddress cc = new InternetAddress("cc@example.com");
		InternetAddress bcc = new InternetAddress("bcc@example.com");

		EmailModificationRequest request = EmailModificationRequest.builder().subject("s")
			.from(from).addTo(List.of(to)).addCc(List.of(cc)).addBcc(List.of(bcc))
			.bodyAppend("text").bodySubtype("html").build();

		assertEquals("s", request.subject());
		assertEquals(from, request.from());
		assertEquals(List.of(to), request.addTo());
		assertEquals(List.of(cc), request.addCc());
		assertEquals(List.of(bcc), request.addBcc());
		assertEquals("text", request.bodyAppend());
		assertEquals("html", request.bodySubtype());
	}
}

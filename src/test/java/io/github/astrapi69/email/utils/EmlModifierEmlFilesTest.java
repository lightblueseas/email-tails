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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.stream.Stream;

import org.junit.jupiter.api.Named;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;

import io.github.astrapi69.file.search.PathFinder;
import jakarta.mail.Address;
import jakarta.mail.Message;
import jakarta.mail.Multipart;
import jakarta.mail.Part;
import jakarta.mail.Session;
import jakarta.mail.internet.ContentType;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

/**
 * Drives {@link EmlModifier} over every .eml file under src/test/resources/eml, one folder per MIME
 * shape (plain-text, html, multipart-alternative, multipart-mixed, headers, encodings). Each
 * fixture outside the unsupported folder has a .properties sidecar of the same name that states
 * what a correct parser must find in it; every change is checked on every fixture, together with
 * what must stay as it was. Fixtures under unsupported have no sidecar: appending text to them must
 * be refused and write nothing. A new fixture is a file and its sidecar, nothing here changes.
 */
class EmlModifierEmlFilesTest
{

	static final String UNSUPPORTED = "unsupported";

	/** One .eml file and what a correct parser must find in it, as its sidecar states. */
	record EmlFixture(String name, Path path, String subject, String bodyMarker, int to, int cc,
		int bcc, boolean multipart) {
		static EmlFixture read(Path root, Path eml)
		{
			Path sidecar = sidecarOf(eml);
			Properties expected = new Properties();
			try (BufferedReader reader = Files.newBufferedReader(sidecar, StandardCharsets.UTF_8))
			{
				expected.load(reader);
			}
			catch (IOException e)
			{
				throw new UncheckedIOException("sidecar of " + relative(root, eml), e);
			}
			return new EmlFixture(relative(root, eml), eml, required(expected, "subject", eml),
				required(expected, "bodyMarker", eml),
				Integer.parseInt(required(expected, "to", eml)),
				Integer.parseInt(required(expected, "cc", eml)),
				Integer.parseInt(required(expected, "bcc", eml)),
				Boolean.parseBoolean(required(expected, "multipart", eml)));
		}

		private static String required(Properties expected, String key, Path eml)
		{
			String value = expected.getProperty(key);
			if (value == null)
			{
				throw new IllegalStateException(
					"the sidecar of " + eml.getFileName() + " has no '" + key + "'");
			}
			return value.trim();
		}

		@Override
		public String toString()
		{
			return name;
		}
	}

	static Path root()
	{
		return new File(PathFinder.getSrcTestResourcesDir(), "eml").toPath();
	}

	static Path sidecarOf(Path eml)
	{
		String file = eml.getFileName().toString();
		return eml
			.resolveSibling(file.substring(0, file.length() - ".eml".length()) + ".properties");
	}

	static String relative(Path root, Path file)
	{
		return root.relativize(file).toString().replace(File.separatorChar, '/');
	}

	static boolean isUnsupported(Path root, Path eml)
	{
		return root.relativize(eml).getName(0).toString().equals(UNSUPPORTED);
	}

	static List<Path> emlFiles() throws IOException
	{
		try (Stream<Path> files = Files.walk(root()))
		{
			return files.filter(path -> path.toString().endsWith(".eml")).sorted().toList();
		}
	}

	static Stream<EmlFixture> fixtures() throws IOException
	{
		Path root = root();
		return emlFiles().stream().filter(eml -> !isUnsupported(root, eml))
			.map(eml -> EmlFixture.read(root, eml));
	}

	static Stream<EmlFixture> textFixtures() throws IOException
	{
		return fixtures().filter(fixture -> !fixture.multipart());
	}

	static Stream<Named<Path>> unsupportedFixtures() throws IOException
	{
		Path root = root();
		return emlFiles().stream().filter(eml -> isUnsupported(root, eml))
			.map(eml -> Named.of(relative(root, eml), eml));
	}

	@TempDir
	Path tempDir;

	@Test
	void theFixtureTreeIsComplete() throws IOException
	{
		Path root = root();
		List<String> complaints = new ArrayList<>();
		List<Path> emls = emlFiles();
		assertFalse(emls.isEmpty(), "no .eml fixture under " + root);
		for (Path eml : emls)
		{
			String text = Files.readString(eml, StandardCharsets.ISO_8859_1);
			if (text.replace("\r\n", "").contains("\n"))
			{
				complaints
					.add(relative(root, eml) + ": a line ends with a bare LF, RFC 5322 wants CRLF");
			}
			if (isUnsupported(root, eml))
			{
				if (Files.exists(sidecarOf(eml)))
				{
					complaints.add(relative(root, eml) + ": a fixture under " + UNSUPPORTED
						+ " has a sidecar, but nothing reads it there");
				}
			}
			else if (!Files.exists(sidecarOf(eml)))
			{
				complaints
					.add(relative(root, eml) + ": no sidecar " + sidecarOf(eml).getFileName());
			}
		}
		try (Stream<Path> files = Files.walk(root))
		{
			for (Path file : files.filter(Files::isRegularFile).toList())
			{
				String name = file.getFileName().toString();
				if (name.endsWith(".properties")
					&& !Files.exists(file.resolveSibling(name.replace(".properties", ".eml"))))
				{
					complaints.add(relative(root, file) + ": a sidecar without its .eml");
				}
				else if (!name.endsWith(".eml") && !name.endsWith(".properties")
					&& !name.equals("README.md"))
				{
					complaints
						.add(relative(root, file) + ": neither .eml, .properties nor README.md");
				}
			}
		}
		assertTrue(complaints.isEmpty(), String.join("\n", complaints));
	}

	@ParameterizedTest(name = "{0}")
	@MethodSource("fixtures")
	void fixtureParsesAsItsSidecarStates(EmlFixture fixture) throws Exception
	{
		MimeMessage original = read(fixture.path());

		assertEquals(fixture.subject(), original.getSubject());
		assertEquals(fixture.to(), count(original.getRecipients(Message.RecipientType.TO)), "to");
		assertEquals(fixture.cc(), count(original.getRecipients(Message.RecipientType.CC)), "cc");
		assertEquals(fixture.bcc(), count(original.getRecipients(Message.RecipientType.BCC)),
			"bcc");
		assertEquals(fixture.multipart(), original.getContent() instanceof Multipart, "multipart");
		assertTrue(bodyText(original).contains(fixture.bodyMarker()),
			"body contains '" + fixture.bodyMarker() + "'");
	}

	@ParameterizedTest(name = "{0}")
	@MethodSource("fixtures")
	void subjectIsReplacedAndEverythingElseKept(EmlFixture fixture) throws Exception
	{
		Path input = input(fixture);
		Path output = output(fixture);
		MimeMessage original = read(input);
		EmailModificationRequest request = EmailModificationRequest.builder()
			.subject("Modified: " + fixture.subject()).build();

		EmlModifier.modifyEml(input, output, request);

		MimeMessage modified = read(output);
		assertEquals("Modified: " + fixture.subject(), modified.getSubject());
		assertEquals(addresses(original.getFrom()), addresses(modified.getFrom()));
		for (Message.RecipientType type : List.of(Message.RecipientType.TO,
			Message.RecipientType.CC, Message.RecipientType.BCC))
		{
			assertEquals(addresses(original.getRecipients(type)),
				addresses(modified.getRecipients(type)), type.toString());
		}
		assertEquals(baseType(original), baseType(modified));
		assertTrue(bodyText(modified).contains(fixture.bodyMarker()));
	}

	@ParameterizedTest(name = "{0}")
	@MethodSource("fixtures")
	void recipientsAreAddedNotReplaced(EmlFixture fixture) throws Exception
	{
		Path input = input(fixture);
		Path output = output(fixture);
		MimeMessage original = read(input);
		EmailModificationRequest request = EmailModificationRequest.builder()
			.addTo(List.of(new InternetAddress("new-to@example.com")))
			.addCc(List.of(new InternetAddress("new-cc@example.com")))
			.addBcc(List.of(new InternetAddress("new-bcc@example.com"))).build();

		EmlModifier.modifyEml(input, output, request);

		MimeMessage modified = read(output);
		assertRecipientsAdded(original, modified, Message.RecipientType.TO, fixture.to(),
			"new-to@example.com");
		assertRecipientsAdded(original, modified, Message.RecipientType.CC, fixture.cc(),
			"new-cc@example.com");
		assertRecipientsAdded(original, modified, Message.RecipientType.BCC, fixture.bcc(),
			"new-bcc@example.com");
		assertEquals(fixture.subject(), modified.getSubject());
	}

	@ParameterizedTest(name = "{0}")
	@MethodSource("fixtures")
	void senderIsReplaced(EmlFixture fixture) throws Exception
	{
		Path input = input(fixture);
		Path output = output(fixture);
		EmailModificationRequest request = EmailModificationRequest.builder()
			.from(new InternetAddress("new-sender@example.com", "Neuer Absender")).build();

		EmlModifier.modifyEml(input, output, request);

		Address[] from = read(output).getFrom();
		assertEquals(1, from.length);
		assertEquals("new-sender@example.com", ((InternetAddress)from[0]).getAddress());
		assertEquals("Neuer Absender", ((InternetAddress)from[0]).getPersonal());
	}

	@ParameterizedTest(name = "{0}")
	@MethodSource("fixtures")
	void textIsAppendedToTheBody(EmlFixture fixture) throws Exception
	{
		Path input = input(fixture);
		Path output = output(fixture);
		MimeMessage original = read(input);
		String appended = "Appended by EmlModifier.";
		EmailModificationRequest request = EmailModificationRequest.builder().bodyAppend(appended)
			.build();

		EmlModifier.modifyEml(input, output, request);

		MimeMessage modified = read(output);
		String body = bodyText(modified);
		assertTrue(body.contains(fixture.bodyMarker()), "the original text stays");
		assertTrue(body.contains(appended), "the appended text arrives");
		if (fixture.multipart())
		{
			Multipart originalParts = (Multipart)original.getContent();
			Multipart modifiedParts = (Multipart)modified.getContent();
			assertEquals(originalParts.getCount() + 1, modifiedParts.getCount());
			Part last = modifiedParts.getBodyPart(modifiedParts.getCount() - 1);
			assertEquals("text/plain", baseType(last));
			assertTrue(((String)last.getContent()).contains(appended));
		}
		else
		{
			assertEquals("text/plain", baseType(modified));
			assertTrue(body.indexOf(fixture.bodyMarker()) < body.indexOf(appended),
				"appended after the original text");
		}
	}

	@ParameterizedTest(name = "{0}")
	@MethodSource("textFixtures")
	void htmlSubtypeTurnsTheTextBodyIntoHtml(EmlFixture fixture) throws Exception
	{
		Path input = input(fixture);
		Path output = output(fixture);
		EmailModificationRequest request = EmailModificationRequest.builder()
			.bodyAppend("<p>Appended paragraph</p>").bodySubtype("HTML").build();

		EmlModifier.modifyEml(input, output, request);

		MimeMessage modified = read(output);
		assertEquals("text/html", baseType(modified));
		String body = bodyText(modified);
		assertTrue(body.contains(fixture.bodyMarker()));
		assertTrue(body.contains("<p>Appended paragraph</p>"));
	}

	@ParameterizedTest(name = "{0}")
	@MethodSource("fixtures")
	void emptyRequestKeepsTheMessage(EmlFixture fixture) throws Exception
	{
		Path input = input(fixture);
		Path output = output(fixture);
		MimeMessage original = read(input);

		EmlModifier.modifyEml(input, output, EmailModificationRequest.builder().build());

		MimeMessage modified = read(output);
		assertEquals(original.getSubject(), modified.getSubject());
		assertEquals(addresses(original.getFrom()), addresses(modified.getFrom()));
		for (Message.RecipientType type : List.of(Message.RecipientType.TO,
			Message.RecipientType.CC, Message.RecipientType.BCC))
		{
			assertEquals(addresses(original.getRecipients(type)),
				addresses(modified.getRecipients(type)), type.toString());
		}
		assertEquals(baseType(original), baseType(modified));
		assertEquals(bodyText(original), bodyText(modified));
	}

	@ParameterizedTest
	@NullAndEmptySource
	void nothingToAppendLeavesTheBodyAsItIs(String bodyAppend) throws Exception
	{
		EmlFixture fixture = fixtures().findFirst().orElseThrow();
		Path input = input(fixture);
		Path output = output(fixture);
		MimeMessage original = read(input);

		EmlModifier.modifyEml(input, output,
			EmailModificationRequest.builder().bodyAppend(bodyAppend).bodySubtype("html").build());

		MimeMessage modified = read(output);
		assertEquals(original.getContent(), modified.getContent());
		assertEquals(baseType(original), baseType(modified));
	}

	@Test
	void stringPathsWorkLikePaths() throws Exception
	{
		EmlFixture fixture = fixtures().findFirst().orElseThrow();
		Path input = input(fixture);
		Path output = output(fixture);

		EmlModifier.modifyEml(input.toString(), output.toString(),
			EmailModificationRequest.builder().subject("Via string paths").build());

		assertEquals("Via string paths", read(output).getSubject());
	}

	@ParameterizedTest(name = "{0}")
	@MethodSource("unsupportedFixtures")
	void appendingToAnUnsupportedBodyIsRefusedAndWritesNothing(Path eml) throws Exception
	{
		Path input = copy(eml);
		Path output = tempDir.resolve("modified-" + input.getFileName());
		EmailModificationRequest request = EmailModificationRequest.builder()
			.bodyAppend("cannot go here").build();

		assertThrows(IllegalStateException.class,
			() -> EmlModifier.modifyEml(input, output, request));

		assertFalse(Files.exists(output), "a refused change leaves no output file behind");
	}

	private Path input(EmlFixture fixture) throws IOException
	{
		return copy(fixture.path());
	}

	private Path output(EmlFixture fixture)
	{
		return tempDir.resolve("modified-" + flat(fixture.path()));
	}

	/** Copies a fixture into the temporary directory under a name that keeps its folder. */
	private Path copy(Path eml) throws IOException
	{
		Path target = tempDir.resolve(flat(eml));
		Files.copy(eml, target);
		return target;
	}

	private static String flat(Path eml)
	{
		return relative(root(), eml).replace('/', '_');
	}

	private static void assertRecipientsAdded(MimeMessage original, MimeMessage modified,
		Message.RecipientType type, int originalCount, String added) throws Exception
	{
		List<String> before = addresses(original.getRecipients(type));
		List<String> after = addresses(modified.getRecipients(type));
		assertEquals(originalCount, before.size(), type.toString());
		assertEquals(originalCount + 1, after.size(), type.toString());
		assertTrue(after.containsAll(before), type + " keeps the original recipients");
		assertTrue(after.contains(added), type + " gains " + added);
	}

	static MimeMessage read(Path path) throws Exception
	{
		try (InputStream in = Files.newInputStream(path))
		{
			return new MimeMessage(Session.getDefaultInstance(new Properties()), in);
		}
	}

	static String baseType(Part part) throws Exception
	{
		return new ContentType(part.getContentType()).getBaseType();
	}

	static String bodyText(Part part) throws Exception
	{
		Object content = part.getContent();
		if (content instanceof String text)
		{
			return text;
		}
		if (content instanceof Multipart multipart)
		{
			StringBuilder text = new StringBuilder();
			for (int i = 0; i < multipart.getCount(); i++)
			{
				text.append(bodyText(multipart.getBodyPart(i)));
			}
			return text.toString();
		}
		return "";
	}

	static List<String> addresses(Address[] addresses)
	{
		if (addresses == null)
		{
			return List.of();
		}
		return Arrays.stream(addresses).map(address -> ((InternetAddress)address).getAddress())
			.toList();
	}

	static int count(Address[] addresses)
	{
		return addresses == null ? 0 : addresses.length;
	}
}

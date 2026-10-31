<div style="text-align: center">

[![Java CI with Gradle](https://github.com/lightblueseas/email-tails/actions/workflows/gradle.yml/badge.svg)](https://github.com/lightblueseas/email-tails/actions/workflows/gradle.yml)
[![Coverage Status](https://codecov.io/gh/lightblueseas/email-tails/branch/develop/graph/badge.svg)](https://codecov.io/gh/lightblueseas/email-tails)
[![Open Issues](https://img.shields.io/github/issues/lightblueseas/email-tails.svg?style=flat)](https://github.com/lightblueseas/email-tails/issues)
[![Maven Central](https://img.shields.io/maven-central/v/io.github.astrapi69/email-tails.svg?label=Maven%20Central)](https://central.sonatype.com/artifact/io.github.astrapi69/email-tails)
[![Javadocs](http://www.javadoc.io/badge/io.github.astrapi69/email-tails.svg)](http://www.javadoc.io/doc/io.github.astrapi69/email-tails)
[![MIT License](http://img.shields.io/badge/license-MIT-brightgreen.svg?style=flat)](http://opensource.org/licenses/MIT)
[![Hits Of Code](https://hitsofcode.com/github/lightblueseas/email-tails?branch=develop)](https://hitsofcode.com/github/lightblueseas/email-tails/view?branch=develop)

</div>

# email-tails

A small Java library for sending e-mail with attachments and for rewriting existing `.eml` files,
built on Jakarta Mail 2.1 and Jakarta Activation 2.1 with the Eclipse Angus implementations at
runtime.

Version 7 requires Java 25 or newer and replaces `javax.mail` with `jakarta.mail`. A project that
still uses `javax.mail` stays on version 6; every public signature that named a `javax` type names
the `jakarta` one now.

## What you get

- **Sending:** `SendEmail` (interface `SendMail`) sends one message or a collection, with or
  without attachments, through a `jakarta.mail.Session` built from your properties or from the
  bundled `mail.properties`.
- **Messages:** `EmailMessage` extends `MimeMessage` with `addTo`, `addCc`, `addBcc`, `addFrom`,
  `setUtf8Content` and a charset that follows the content type; `EmailMessageWithAttachments` and
  `EmailWithAttachments` add files, byte arrays or `DataHandler`s as parts.
- **Rewriting .eml files:** `EmlModifier` opens an `.eml`, applies an `EmailModificationRequest`
  (new subject, new sender, recipients to add as To/Cc/Bcc, text to append as plain or html) and
  writes the result to another file, only after every change succeeded. `MimeMessageUtils` holds
  the single operations.
- **Addresses:** `EmailExtensions` builds `InternetAddress`es with personal name and charset,
  validates an address and reads the charset out of a content type.
- **Constants:** `Mimetypes`, `HeaderNames`, `PropertyKeys` (every `mail.*` property Jakarta Mail
  reads) and `EmailConstants`.

## Usage

Send a message with an attachment:

```java
Properties properties = new Properties();
properties.put("mail.smtp.host", "smtp.example.com");
properties.put("mail.smtp.port", "587");
properties.put("mail.smtp.starttls.enable", "true");
SendEmail sender = new SendEmail(properties, authenticator); // or new SendEmail(properties)

EmailMessage message = new EmailMessage(sender.getSession());
message.addFrom(EmailExtensions.newAddress("alice@example.com", "Alice"));
message.addTo(EmailExtensions.newAddress("bob@example.com", "Bob"));
message.setSubject("Report", "UTF-8");

EmailMessageWithAttachments withAttachments = new EmailMessageWithAttachments(message);
MimeBodyPart text = new MimeBodyPart();
text.setText("Hello Bob, the report is attached.", "UTF-8");
withAttachments.addAttachment(text);
withAttachments.addAttachment(new File("report.csv"));

String messageId = sender.sendEmailMessage(message);
```

Rewrite an existing `.eml` file:

```java
EmailModificationRequest request = EmailModificationRequest.builder()
    .subject("Fwd: Report")
    .from(new InternetAddress("alice@example.com", "Alice"))
    .addCc(List.of(new InternetAddress("carol@example.com")))
    .bodyAppend("Forwarded by Alice.")
    .bodySubtype("plain")
    .build();

EmlModifier.modifyEml(Path.of("in.eml"), Path.of("out.eml"), request);
```

A null subject, sender or text leaves that part of the message as it is; the recipient lists are
added to the message, never replace its recipients. A body that is neither text nor multipart is
refused with an `IllegalStateException`, and no output file is written.

## Install

Replace `${latestVersion}` with the version on Maven Central:
[![Maven Central](https://img.shields.io/maven-central/v/io.github.astrapi69/email-tails.svg?label=Maven%20Central)](https://central.sonatype.com/artifact/io.github.astrapi69/email-tails)

<details>
  <summary>Gradle (click to expand)</summary>

In `gradle/libs.versions.toml`:

```toml
[versions]
email-tails-version = "${latestVersion}"

[libraries]
email-tails = { module = "io.github.astrapi69:email-tails", version.ref = "email-tails-version" }
```

and in the dependencies:

```groovy
implementation libs.email.tails
```

Or, without a catalog, with `emailTailsVersion=${latestVersion}` in `gradle.properties`:

```groovy
implementation("io.github.astrapi69:email-tails:$emailTailsVersion")
```

</details>

<details>
  <summary>Maven (click to expand)</summary>

```xml
<properties>
    <email-tails.version>${latestVersion}</email-tails.version>
</properties>

<dependency>
    <groupId>io.github.astrapi69</groupId>
    <artifactId>email-tails</artifactId>
    <version>${email-tails.version}</version>
</dependency>
```

</details>

The Jakarta Mail and Activation APIs come as `api` dependencies, the Angus implementations as
runtime dependencies; a consumer needs to declare neither. Snapshots of the next version are in
the [Central Portal snapshot repository](https://central.sonatype.com/repository/maven-snapshots/):

```groovy
repositories {
    maven {
        url = "https://central.sonatype.com/repository/maven-snapshots/"
        mavenContent { snapshotsOnly() }
    }
}
```

## Development

Gradle 9 with a version catalog in `gradle/libs.versions.toml`; the build logic sits in
`gradle/*.gradle`, listed in `gradle/gradle-files.list`. `make help` lists the make targets that
wrap the everyday Gradle calls:

```
make build             # clean build: tests, Jacoco, Spotless check, API compatibility against the last release
make spotless          # format, order imports, add the MIT header
make pitest            # mutation testing; the floor stands in build.gradle
make dependency-updates
```

The tests of `EmlModifier` run over the `.eml` fixtures under `src/test/resources/eml`, one folder
per MIME shape, each fixture with a `.properties` sidecar stating what a parser must find in it; a
new fixture is a file and its sidecar, see the [README there](src/test/resources/eml/README.md).

Install the commit hook once after cloning; it refuses a commit that credits a non-human
collaborator, the same rule the commit-provenance workflow enforces on pull requests:

```
make install-hooks
```

Releasing is a manual step: push a `RELEASE-*` tag (`make tag-release CONFIRM=yes`), and the
publish workflow uploads the signed artifacts to the Central Portal. A push to develop publishes a
snapshot.

## Security

Security reports go through [SECURITY.md](SECURITY.md) and GitHub's private vulnerability
reporting, not through the issue tracker.

## Support this project

email-tails is built and maintained by a single developer, like the rest of the astrapi69
family. If it helps you, support goes directly into development time:

- [GitHub Sponsors](https://github.com/sponsors/astrapi69)
- [Liberapay](https://liberapay.com/astrapi69)
- [Ko-fi](https://ko-fi.com/astrapi69)
- [PayPal](https://www.paypal.com/donate/?cmd=_s-xclick&hosted_button_id=MJ7V43GU2H386)

A GitHub star and a mention to someone who sends mail from Java help too.

## Contributing

Pull requests are welcome; please add tests for what you change. The commit hook above and the
workflows on a pull request (build, mutation score, commit provenance, closing references) say
what a change has to pass. Questions, bug reports and feature requests go to the
[issues](https://github.com/lightblueseas/email-tails/issues).

## License

The source code comes under the liberal MIT License, see [LICENSE](LICENSE).

No animals were harmed in the making of this library.

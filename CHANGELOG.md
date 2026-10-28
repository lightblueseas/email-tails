## Change log
----------------------

Version 7-SNAPSHOT
-------------

BREAKING:

- javax.mail and javax.activation are replaced by Jakarta Mail 2.1 and Jakarta Activation 2.1
  (packages jakarta.mail and jakarta.activation), with the Eclipse Angus implementations at
  runtime. Every public signature that named a javax.mail or javax.activation type now names the
  jakarta one, which is why this is 7 and not 6.1. EmailMessage.setFrom(String) now overrides
  MimeMessage.setFrom(String) and therefore declares MessagingException only; an
  UnsupportedEncodingException arrives wrapped in it
- the minimum Java version is 17 (was 1.8)
- the default mail.properties moved from de/alpharogroup/email/send to
  io/github/astrapi69/email/send, the package the classes have lived in since version 6;
  EmailConstants.PROPERTIES_PACKAGE_PATH names it

ADDED:

- EmlModifier, MimeMessageUtils and EmailModificationRequest: open an .eml file, change subject,
  sender and recipients, append text to the body and write it back. Covered by parameterized tests
  that run every change over the .eml fixtures under src/test/resources/eml: plain text, html,
  multipart/alternative, multipart/mixed with an attachment, existing CC and BCC with an
  encoded-word subject, and quoted-printable Latin-1
- Makefile with the family's targets: build, spotless, pitest, api-compatibility, publish-local,
  publish-snapshot and publish-central (the uploads need CONFIRM=yes), install-hooks; `make help`
  lists them
- gradle: version catalog (gradle/libs.versions.toml), build split into gradle/*.gradle files,
  spotless with the shared formatter, import order and MIT header, jacoco, PIT mutation testing
  with a floor in build.gradle, japicmp API compatibility gate against the last release
  (apiBaselineVersion in gradle.properties), publishing to the Central Portal through nmcp with
  signing that follows the key, mavenLocal only with -PuseMavenLocal
- github: CodeQL, commit provenance, closing references and release freeze workflows, a publish
  workflow for RELEASE-* tags, codecov configuration, SECURITY.md, a commit-msg hook under
  .githooks (install with `git config core.hooksPath .githooks`)

CHANGED:

- update gradle to version 9.7.0; the wrapper script, which was an empty file, is restored
- update of resourcebundle-core to 6.1, file-worker to 19.1, silly-strings to 9.2, lombok to
  1.18.48, junit-jupiter to 6.1.3, jobj-core to 9.1 and test-objects to its successor
  test-object 9.1
- jakarta.mail-api and jakarta.activation-api are api dependencies, since this library's public
  types extend and expose them; angus-mail and angus-activation are runtimeOnly
- mime-util no longer drags the slf4j-log4j12 binding and log4j 1.2.14 onto the classpath

REMOVED:

- the dependencies on xstream, silly-collections, net-extensions, meanbean and commons-codec: no
  class in this project used them
- the Eclipse launch configurations of the maven era under src/launch


Version 6
-------------

ADDED:

- new github-action workflow for 'Java CI with Gradle'
- new gradle plugin dependency of 'com.diffplug.spotless:spotless-plugin-gradle' to minor version 6.3.0

CHANGED:

- update gradle to new version 7.4
- moved all classes to new package 'io.github.astrapi69'


Version 5.0.2
-------------

CHANGED:

- update of file-worker version to 5.0.2
- update of xstream version to 1.4.11.1

Version 5.0.1
-------------

CHANGED:

- excluded logging dependencies

Version 5
-------------

CHANGED:

- update of parent version to 4.5
- update of silly-collections version to 5
- update of test-objects version to 5
- update of file-worker version to 5.0.1
- update of net-extensions version to 5
- update of resourcebundle-core version to 3.0.1

Version 4.18
-------------

CHANGED:

- update of parent version to 4
- update of silly-collections version to 4.31
- update of test-objects version to 4.28
- update of file-worker version to 4.22
- update of xstream version to 1.4.10
- update of resourcebundle-core version to 2.22

Version 4.17
-------------

CHANGED:

- update of parent version to 3.11
- update of resourcebundle-core version to 2.21
- removed unneeded .0 at the end of version
- update of silly-collections version to 4.28.0
- update of test-objects version to 4.24.0
- update of file-worker version to 4.20

Version 4.16.0
-------------

ADDED:

- this changelog file
- provide package.html for the javadoc of new packages
- Donation buttons extended for paypal and bitcoin
- added new meanbean dependency for better unit testing of beans

CHANGED:

- update of parent version and of dependencies versions
- javadoc extended and improved

Notable links:
[keep a changelog](http://keepachangelog.com/en/1.0.0/) Don’t let your friends dump git logs into changelogs

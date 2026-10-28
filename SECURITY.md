# Security Policy

`email-tails` builds and sends mail through Jakarta Mail and rewrites existing `.eml` files. A
defect here can send a message to the wrong recipient, leak an attachment or let header content
through unchecked, so the reporting path below is deliberately not the public issue tracker.

## Supported Versions

Only the current minor line receives security fixes. This is a single-maintainer project, and
supporting several lines at once is a promise it could not keep.

| Version | Supported          | Required JDK | Mail API                 |
|---------|--------------------|--------------|--------------------------|
| 7.x     | :white_check_mark: | 25 and above | jakarta.mail 2.1 (Angus) |
| 6.x     | :x:                | 8 and above  | javax.mail 1.4           |
| < 6     | :x:                | 8 and above  | javax.mail 1.4           |

The current release is the latest one on the
[releases page](https://github.com/lightblueseas/email-tails/releases) and on Maven Central. If you
are on an older line, the fix for a reported vulnerability will be an upgrade to the current one
rather than a backport.

## Reporting a Vulnerability

**Do not open a public issue.** Use GitHub's private vulnerability reporting:

[Report a vulnerability](https://github.com/lightblueseas/email-tails/security/advisories/new)

That keeps the report between you and the maintainer until a fix exists, and it creates the draft
advisory a CVE can later be issued from.

Please include, as far as you can:

- the version you found it in, and whether the current release is affected
- which class or method is involved
- what an attacker gains - a message delivered elsewhere, a header injected, an attachment read
- a way to reproduce it: a failing test is ideal, since it becomes the regression guard

### What happens next

This is a spare-time project, so no response time is promised that could not be met. What is
promised instead:

- a reply acknowledging the report, and whether it is reproducible
- if it is: a fix on the current line, a release, and an advisory crediting you unless you prefer
  otherwise
- if it is not, or it turns out to be intended behaviour: an explanation of why, rather than
  silence

### What belongs elsewhere

This library does not parse MIME or speak SMTP itself; both come from the Jakarta Mail API and
its Eclipse Angus implementation, and the code here only orchestrates them. A flaw in the parser
or the transport belongs there:

- Jakarta Mail API: <https://github.com/jakartaee/mail-api/security/policy>
- Eclipse Angus Mail: <https://github.com/eclipse-ee4j/angus-mail/security/policy>

A flaw in *how this library uses* them - a recipient list that is not cleared before it is
replaced, a subject that is not encoded, a file written where the caller did not ask - is squarely
in scope here.

## What this project already does

- **CodeQL** runs on every push and pull request to `master` and `develop`, plus weekly.
- **Dependabot security updates** are enabled.
- The build gate `apiCompatibility` compares every build with the last release, so a breaking
  change cannot slip into a minor version.
- PIT mutation testing runs in CI with the floor recorded in `build.gradle`. The floor is low
  because the sending tests need a mail server and are disabled; that is a documented gap, not
  an accident.

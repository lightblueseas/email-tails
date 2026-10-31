# EML fixtures

One folder per MIME shape, one `.eml` per variant, and next to every `.eml` outside `unsupported/`
a `.properties` file of the same name that states what a correct parser must find in it:

```
subject=Plain text mail                     the decoded Subject header
bodyMarker=this is a plain text mail ...    a piece of text the decoded body must contain
to=1  cc=0  bcc=0                           how many recipients of each type the message carries
multipart=false                             whether the body is a Multipart
```

`EmlModifierEmlFilesTest` discovers every `.eml` under this directory: a new fixture is a file and
its sidecar, no Java changes. Every change EmlModifier can make is run over every fixture, and the
sidecar is what the parse of the untouched file is checked against. Fixtures under `unsupported/`
have no sidecar: the test expects EmlModifier to refuse appending text to them and to write nothing.

| folder                  | what belongs there                                                          |
|-------------------------|-----------------------------------------------------------------------------|
| `plain-text/`           | `text/plain` bodies                                                         |
| `html/`                 | `text/html` bodies                                                          |
| `multipart-alternative/`| `multipart/alternative`, the same content in several forms                  |
| `multipart-mixed/`      | `multipart/mixed`, a body with attachments                                  |
| `headers/`              | header situations: existing Cc and Bcc, encoded words, Reply-To, ...        |
| `encodings/`            | transfer encodings and charsets: quoted-printable, base64, Latin-1, ...     |
| `unsupported/`          | bodies that are neither text nor multipart, which appending must refuse     |

Lines end with CRLF as RFC 5322 wants; `.gitattributes` (`*.eml -text`) keeps them so, and the test
refuses a fixture with bare LF. The sidecars are read as UTF-8.

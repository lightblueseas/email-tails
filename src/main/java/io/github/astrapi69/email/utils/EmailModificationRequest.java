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

import java.util.List;

import jakarta.mail.internet.InternetAddress;

/**
 * What {@link EmlModifier} changes in a message. A null subject, sender or body text means "keep
 * what the message has"; the recipient lists are added to the message, never replace its
 * recipients, and are normalized to unmodifiable, never-null lists. The body subtype falls back to
 * "plain" and is lower-cased, so "HTML" and "html" mean the same.
 *
 * @param subject
 *            the new subject, or null to keep the current one
 * @param from
 *            the new sender, or null to keep the current one
 * @param addTo
 *            recipients to add as TO
 * @param addCc
 *            recipients to add as CC
 * @param addBcc
 *            recipients to add as BCC
 * @param bodyAppend
 *            text to append to the body, or null to leave the body alone
 * @param bodySubtype
 *            the text subtype of the appended text, "plain" or "html"
 */
public record EmailModificationRequest(String subject, InternetAddress from,
	List<InternetAddress> addTo, List<InternetAddress> addCc, List<InternetAddress> addBcc,
	String bodyAppend, String bodySubtype) {
	public EmailModificationRequest {
		addTo = addTo == null ? List.of() : List.copyOf(addTo);
		addCc = addCc == null ? List.of() : List.copyOf(addCc);
		addBcc = addBcc == null ? List.of() : List.copyOf(addBcc);
		bodySubtype = (bodySubtype == null || bodySubtype.isBlank())
			? "plain"
			: bodySubtype.toLowerCase();
	}

	public static Builder builder()
	{
		return new Builder();
	}

	public static class Builder
	{
		private String subject;
		private InternetAddress from;
		private List<InternetAddress> addTo = List.of();
		private List<InternetAddress> addCc = List.of();
		private List<InternetAddress> addBcc = List.of();
		private String bodyAppend;
		private String bodySubtype = "plain";

		public Builder subject(String subject)
		{
			this.subject = subject;
			return this;
		}

		public Builder from(InternetAddress from)
		{
			this.from = from;
			return this;
		}

		public Builder addTo(List<InternetAddress> addTo)
		{
			this.addTo = List.copyOf(addTo);
			return this;
		}

		public Builder addCc(List<InternetAddress> addCc)
		{
			this.addCc = List.copyOf(addCc);
			return this;
		}

		public Builder addBcc(List<InternetAddress> addBcc)
		{
			this.addBcc = List.copyOf(addBcc);
			return this;
		}

		public Builder bodyAppend(String bodyAppend)
		{
			this.bodyAppend = bodyAppend;
			return this;
		}

		public Builder bodySubtype(String bodySubtype)
		{
			this.bodySubtype = bodySubtype;
			return this;
		}

		public EmailModificationRequest build()
		{
			return new EmailModificationRequest(subject, from, addTo, addCc, addBcc, bodyAppend,
				bodySubtype);
		}
	}
}

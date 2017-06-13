/*******************************************************************************
 * Copyright 2014 uniVocity Software Pty Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package com.univocity.parsers.ssv;

import com.univocity.parsers.common.AbstractParser;

/**
 * A very fast TSV parser implementation.
 *
 * @author uniVocity Software Pty Ltd - <a href="mailto:parsers@univocity.com">parsers@univocity.com</a>
 * @see SsvFormat
 * @see SsvParserSettings
 * @see SsvWriter
 * @see AbstractParser
 */
public class SsvParser extends AbstractParser<SsvParserSettings> {

	private final boolean ignoreTrailingWhitespace;
	private final boolean ignoreLeadingWhitespace;
	private final boolean joinLines;

	private final char newLine;
	private final char escapeChar;
	private final char escapedTabChar;

	/**
	 * The TsvParser supports all settings provided by {@link SsvParserSettings}, and requires this configuration to be properly initialized.
	 *
	 * @param settings the parser configuration
	 */
	public SsvParser(SsvParserSettings settings) {
		super(settings);
		ignoreTrailingWhitespace = settings.getIgnoreTrailingWhitespaces();
		ignoreLeadingWhitespace = settings.getIgnoreLeadingWhitespaces();
		joinLines = settings.isLineJoiningEnabled();

		SsvFormat format = settings.getFormat();
		newLine = format.getNormalizedNewline();
		escapeChar = settings.getFormat().getEscapeChar();
		escapedTabChar = format.getEscapedTabChar();
	}

	@Override
	protected void initialize() {
		output.trim = ignoreTrailingWhitespace;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void parseRecord() {
		if (ignoreLeadingWhitespace && ch != '\t' && ch <= ' ' && whitespaceRangeStart < ch) {
			ch = input.skipWhitespace(ch, '\t', escapeChar);
		}

		while (ch != newLine) {

			if (ch != newLine) {
				ch = input.nextChar();
				if (ch == newLine) {
					output.emptyParsed();
				}
			}
		}
	}

}

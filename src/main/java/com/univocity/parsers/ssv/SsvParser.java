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

import com.univocity.parsers.common.*;
import com.univocity.parsers.common.input.*;

import java.io.*;

import static com.univocity.parsers.ssv.UnescapedQuoteHandling.*;

/**
 * A very fast CSV parser implementation.
 *
 * @author uniVocity Software Pty Ltd - <a href="mailto:parsers@univocity.com">parsers@univocity.com</a>
 * @see SsvFormat
 * @see SsvParserSettings
 * @see SsvWriter
 * @see AbstractParser
 */
public final class SsvParser extends AbstractParser<SsvParserSettings> {
// TODO: docs/comments
// TODO: consider switching delimiter to char[] Need to investigate performance difference between char[]
    // and String in order to decide on this.
	private final boolean ignoreTrailingWhitespace;
	private final boolean ignoreLeadingWhitespace;
	private boolean parseUnescapedQuotes;
	private boolean parseUnescapedQuotesUntilDelimiter;
	private final boolean doNotEscapeUnquotedValues;
	private final boolean keepEscape;
	private final boolean keepQuotes;

	private boolean unescaped;
	private char prev;
	private String delimiter;
	private char quote;
	private char quoteEscape;
	private final char escapeEscape;
	private final char newLine;
	private final DefaultCharAppender whitespaceAppender;
	private final boolean normalizeLineEndingsInQuotes;
	private UnescapedQuoteHandling quoteHandling;
	private final String nullValue;
	private final int maxColumnLength;

	private int delimiterPointer;
	private String partialDelimiter;

	/**
	 * The CsvParser supports all settings provided by {@link SsvParserSettings}, and requires this configuration to be properly initialized.
	 *
	 * @param settings the parser configuration
	 */
	public SsvParser(SsvParserSettings settings) {
		super(settings);
		ignoreTrailingWhitespace = settings.getIgnoreTrailingWhitespaces();
		ignoreLeadingWhitespace = settings.getIgnoreLeadingWhitespaces();
		parseUnescapedQuotes = settings.isParseUnescapedQuotes();
		parseUnescapedQuotesUntilDelimiter = settings.isParseUnescapedQuotesUntilDelimiter();
		doNotEscapeUnquotedValues = !settings.isEscapeUnquotedValues();
		keepEscape = settings.isKeepEscapeSequences();
		keepQuotes = settings.getKeepQuotes();
		normalizeLineEndingsInQuotes = settings.isNormalizeLineEndingsWithinQuotes();
		nullValue = settings.getNullValue();
		maxColumnLength = settings.getMaxCharsPerColumn();
		delimiterPointer = 0;
		partialDelimiter = "";


		SsvFormat format = settings.getFormat();
		delimiter = format.getDelimiter();
		quote = format.getQuote();
		quoteEscape = format.getQuoteEscape();
		escapeEscape = format.getCharToEscapeQuoteEscaping();
		newLine = format.getNormalizedNewline();

		whitespaceAppender = new ExpandingCharAppender(10, "", whitespaceRangeStart);

		this.quoteHandling = settings.getUnescapedQuoteHandling();
		if (quoteHandling == null) {
			if (parseUnescapedQuotes) {
				if (parseUnescapedQuotesUntilDelimiter) {
					quoteHandling = STOP_AT_DELIMITER;
				} else {
					quoteHandling = STOP_AT_CLOSING_QUOTE;
				}
			} else {
				quoteHandling = RAISE_ERROR;
			}
		} else {
			parseUnescapedQuotesUntilDelimiter = quoteHandling == STOP_AT_DELIMITER || quoteHandling == SKIP_VALUE;
			parseUnescapedQuotes = quoteHandling != RAISE_ERROR;
		}
	}


	@Override
	// TODO: this is where the magic needs to happen!!
	protected final void parseRecord() {
		if (ch <= ' ' && ignoreLeadingWhitespace && whitespaceRangeStart < ch) {
			ch = input.skipWhitespace(ch, delimiter.charAt(0), quote);
		}

		while (ch != newLine) {
			// we are now parsing a single line.

			// first check if the current character is empty
			if (ch <= ' ' && ignoreLeadingWhitespace && whitespaceRangeStart < ch) {
				ch = input.skipWhitespace(ch, delimiter.charAt(0), quote);
			}

			// now we need to first process through the delimiter, before moving on to parsing an actual value.
            // Use a pointer to check where in the delimiter we are.
			// case 1: delimiter first char is match, n-th isn't -> we need to parse char 1 as normal value.
			// case 2: delimiter first char is match, others are too -> treat string as delimiter, do not parse.
			while(ch == delimiter.charAt(delimiterPointer)){
				// this means we are still parsing the delimiter.
				partialDelimiter += delimiter.charAt(delimiterPointer);
				if(partialDelimiter.equals(delimiter)){
					// we have a full delimiter now.
					output.emptyParsed();
					break;
				} else {
					// keep going with delimiter parsing
					delimiterPointer++;
					ch = input.nextChar();
				}
			}
			// the delimiter ended, so reset the pointer and partial delimiter
				delimiterPointer = 0;
				partialDelimiter = "";

				if(ch == newLine) {
					output.emptyParsed();
				}
				unescaped = false;
				prev = '\0';
				if (ch == quote) {
					output.trim = false;
					if (normalizeLineEndingsInQuotes) {
						parseQuotedValue();
					} else {
						input.enableNormalizeLineEndings(false);
						parseQuotedValue();
						input.enableNormalizeLineEndings(true);
					}
					output.valueParsed();
				} else if (doNotEscapeUnquotedValues) {
					String value = null;
					if (output.appender.length() == 0) {
						// this function comes from AbstractCharInputReader. It gets there because DefaultCharInputReader extends this abstract class,
						// which in turn implements the Interface CharInputReader.
						// This is where the library actually gets the value out of the text file
						value = input.getString(ch, delimiter.charAt(0), ignoreTrailingWhitespace, nullValue, maxColumnLength);
						// TODO: the reason this causes problems, is that the 'second' part of the delimiter does not
						// get included. It may be wortwhile updating the getString method to make it easier to call here.
					}

					// check if there was any value, if so, append it to the output.
					if (value != null) {
						output.valueParsed(value);
						ch = input.getChar();
					} else {
						output.trim = ignoreTrailingWhitespace;
						System.out.println(ch);
						ch = output.appender.appendUntil(ch, input, delimiter.charAt(0), newLine);
						output.valueParsed();
					}
				} else {
					// TODO: figure out how this one works.
					output.trim = ignoreTrailingWhitespace;
					parseValueProcessingEscape();
					output.valueParsed();
				}
			}
			if (ch != newLine) {
				ch = input.nextChar();
				if (ch == newLine) {
					output.emptyParsed();
				}
			}

	}

	private void skipValue() {
		output.appender.reset();
		ch = NoopCharAppender.getInstance().appendUntil(ch, input, delimiter.charAt(0), newLine);
	}

	private void handleValueSkipping(boolean quoted) {
		switch (quoteHandling) {
			case SKIP_VALUE:
				skipValue();
				break;
			case RAISE_ERROR:
				throw new TextParsingException(context, "Unescaped quote character '" + quote
						+ "' inside " + (quoted ? "quoted" : "") + " value of CSV field. To allow unescaped quotes, set 'parseUnescapedQuotes' to 'true' in the CSV parser settings. Cannot parse CSV input.");
		}
	}

	private void handleUnescapedQuoteInValue() {
		switch (quoteHandling) {
			case STOP_AT_CLOSING_QUOTE:
			case STOP_AT_DELIMITER:
				output.appender.append(quote);
				prev = ch;
				parseValueProcessingEscape();
				break;
			default:
				handleValueSkipping(false);
				break;
		}
	}

	private boolean handleUnescapedQuote() {
		unescaped = true;
		switch (quoteHandling) {
			case STOP_AT_CLOSING_QUOTE:
			case STOP_AT_DELIMITER:
				output.appender.append(quote);
				output.appender.append(ch);
				prev = ch;
				parseQuotedValue();
				return true; //continue;
			default:
				handleValueSkipping(true);
				return false;
		}
	}

	private void processQuoteEscape() {
		if (ch == quoteEscape && prev == escapeEscape && escapeEscape != '\0') {
			if (keepEscape) {
				output.appender.append(escapeEscape);
			}
			output.appender.append(quoteEscape);
			ch = '\0';
		} else if (prev == quoteEscape) {
			if (ch == quote) {
				if (keepEscape) {
					output.appender.append(quoteEscape);
				}
				output.appender.append(quote);
				ch = '\0';
			} else {
				output.appender.append(prev);
			}
		} else if (ch == quote && prev == quote) {
			output.appender.append(quote);
		} else if (prev == quote) { //unescaped quote detected
			handleUnescapedQuoteInValue();
		}
	}

	private void parseValueProcessingEscape() {
		while (ch != delimiter.charAt(0) && ch != newLine) {
			if (ch != quote && ch != quoteEscape) {
				if (prev == quote) { //unescaped quote detected
					handleUnescapedQuoteInValue();
					return;
				}
				output.appender.append(ch);
			} else {
				processQuoteEscape();
			}
			prev = ch;
			ch = input.nextChar();
		}
	}

	private void parseQuotedValue() {
		if (prev != '\0' && parseUnescapedQuotesUntilDelimiter) {
			if (quoteHandling == SKIP_VALUE) {
				skipValue();
				return;
			}
			if (!keepQuotes) {
				output.appender.prepend(quote);
			}
			ch = input.nextChar();
			output.trim = ignoreTrailingWhitespace;
			ch = output.appender.appendUntil(ch, input, delimiter.charAt(0), newLine);
		} else {
			if (keepQuotes && prev == '\0') {
				output.appender.append(quote);
			}
			ch = input.nextChar();
			while (true) {
				if (prev == quote && (ch <= ' ' && whitespaceRangeStart < ch || ch == delimiter.charAt(0) || ch == newLine)) {
					break;
				}

				if (ch != quote && ch != quoteEscape) {
					if (prev == quote) { //unescaped quote detected
						if (handleUnescapedQuote()) {
							break;
						} else {
							return;
						}
					}
					if (prev == quoteEscape && quoteEscape != '\0') {
						output.appender.append(quoteEscape);
					}
					ch = output.appender.appendUntil(ch, input, quote, quoteEscape, escapeEscape);
					prev = ch;
					ch = input.nextChar();
				} else {
					processQuoteEscape();
					prev = ch;
					ch = input.nextChar();
					if(unescaped && ch == delimiter.charAt(0) || ch == newLine){
						return;
					}
				}
			}

			// handles whitespaces after quoted value: whitespaces are ignored. Content after whitespaces may be parsed if 'parseUnescapedQuotes' is enabled.
			if (ch != delimiter.charAt(0) && ch != newLine && ch <= ' ' && whitespaceRangeStart < ch) {
				whitespaceAppender.reset();
				do {
					//saves whitespaces after value
					whitespaceAppender.append(ch);
					ch = input.nextChar();
					//found a new line, go to next record.
					if (ch == newLine) {
						return;
					}
				} while (ch <= ' ' && whitespaceRangeStart < ch);

				//there's more stuff after the quoted value, not only empty spaces.
				if (ch != delimiter.charAt(0) && parseUnescapedQuotes) {
					if (output.appender instanceof DefaultCharAppender) {
						//puts the quote before whitespaces back, then restores the whitespaces
						output.appender.append(quote);
						((DefaultCharAppender) output.appender).append(whitespaceAppender);
					}
					//the next character is not the escape character, put it there
					if (parseUnescapedQuotesUntilDelimiter || ch != quote && ch != quoteEscape) {
						output.appender.append(ch);
					}

					//sets this character as the previous character (may be escaping)
					//calls recursively to keep parsing potentially quoted content
					prev = ch;
					parseQuotedValue();
				} else if (keepQuotes) {
					output.appender.append(quote);
				}
			} else if (keepQuotes) {
				output.appender.append(quote);
			}

			if (ch != delimiter.charAt(0) && ch != newLine) {
				throw new TextParsingException(context, "Unexpected character '" + ch + "' following quoted value of CSV field. Expecting '" + delimiter + "'. Cannot parse CSV input.");
			}
		}
	}

	/**
	 * Returns the CSV format detected when one of the following settings is enabled:
	 * <ul>
	 * <li>{@link CommonParserSettings#isLineSeparatorDetectionEnabled()}</li>
	 * <li>{@link SsvParserSettings#isDelimiterDetectionEnabled()}</li>
	 * <li>{@link SsvParserSettings#isQuoteDetectionEnabled()}</li>
	 * </ul>
	 *
	 * The detected format will be available once the parsing process is initialized (i.e. when {@link AbstractParser#beginParsing(Reader) runs}.
	 *
	 * @return the detected CSV format, or {@code null} if no detection has been enabled or if the parsing process has not been started yet.
	 */
	public final SsvFormat getDetectedFormat() {
		SsvFormat out = null;
		if (settings.isDelimiterDetectionEnabled()) {
			out = settings.getFormat().clone();
			out.setDelimiter(this.delimiter);
		}
		if (settings.isQuoteDetectionEnabled()) {
			out = out == null ? settings.getFormat().clone() : out;
			out.setQuote(quote);
			out.setQuoteEscape(quoteEscape);
		}
		if (settings.isLineSeparatorDetectionEnabled()) {
			out = out == null ? settings.getFormat().clone() : out;
			out.setLineSeparator(input.getLineSeparator());
		}
		return out;
	}

	@Override
	protected final boolean consumeValueOnEOF() {
		if (ch == quote) {
			if (prev == quote) {
				if (keepQuotes) {
					output.appender.append(quote);
				}
				return true;
			} else {
				output.appender.append(quote);
			}
		}
		return false;
	}
}

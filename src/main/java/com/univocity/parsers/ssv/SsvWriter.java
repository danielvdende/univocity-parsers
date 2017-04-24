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

import com.univocity.parsers.common.AbstractWriter;

import java.io.File;
import java.io.OutputStream;
import java.io.Writer;
import java.nio.charset.Charset;

/**
 * A powerful and flexible TSV writer implementation.
 *
 * @author uniVocity Software Pty Ltd - <a href="mailto:parsers@univocity.com">parsers@univocity.com</a>
 * @see SsvFormat
 * @see SsvWriterSettings
 * @see SsvParser
 * @see AbstractWriter
 */
public class SsvWriter extends AbstractWriter<SsvWriterSettings> {

	private boolean ignoreLeading;
	private boolean ignoreTrailing;
	private boolean joinLines;

	private char escapeChar;
	private char escapedTabChar;
	private char newLine;

	/**
	 * The TsvWriter supports all settings provided by {@link SsvWriterSettings}, and requires this configuration to be properly initialized.
	 * <p><strong>Important: </strong> by not providing an instance of {@link Writer} to this constructor, only the operations that write to Strings are available.</p>
	 *
	 * @param settings the TSV writer configuration
	 */
	public SsvWriter(SsvWriterSettings settings) {
		this((Writer) null, settings);
	}

	/**
	 * The TsvWriter supports all settings provided by {@link SsvWriterSettings}, and requires this configuration to be properly initialized.
	 *
	 * @param writer   the output resource that will receive TSV records produced by this class.
	 * @param settings the TSV writer configuration
	 */
	public SsvWriter(Writer writer, SsvWriterSettings settings) {
		super(writer, settings);
	}

	/**
	 * The TsvWriter supports all settings provided by {@link SsvWriterSettings}, and requires this configuration to be properly initialized.
	 *
	 * @param file     the output file that will receive TSV records produced by this class.
	 * @param settings the TSV writer configuration
	 */
	public SsvWriter(File file, SsvWriterSettings settings) {
		super(file, settings);
	}

	/**
	 * The TsvWriter supports all settings provided by {@link SsvWriterSettings}, and requires this configuration to be properly initialized.
	 *
	 * @param file     the output file that will receive TSV records produced by this class.
	 * @param encoding the encoding of the file
	 * @param settings the TSV writer configuration
	 */
	public SsvWriter(File file, String encoding, SsvWriterSettings settings) {
		super(file, encoding, settings);
	}

	/**
	 * The TsvWriter supports all settings provided by {@link SsvWriterSettings}, and requires this configuration to be properly initialized.
	 *
	 * @param file     the output file that will receive TSV records produced by this class.
	 * @param encoding the encoding of the file
	 * @param settings the TSV writer configuration
	 */
	public SsvWriter(File file, Charset encoding, SsvWriterSettings settings) {
		super(file, encoding, settings);
	}

	/**
	 * The TsvWriter supports all settings provided by {@link SsvWriterSettings}, and requires this configuration to be properly initialized.
	 *
	 * @param output   the output stream that will be written with the TSV records produced by this class.
	 * @param settings the TSV writer configuration
	 */
	public SsvWriter(OutputStream output, SsvWriterSettings settings) {
		super(output, settings);
	}

	/**
	 * The TsvWriter supports all settings provided by {@link SsvWriterSettings}, and requires this configuration to be properly initialized.
	 *
	 * @param output   the output stream that will be written with the TSV records produced by this class.
	 * @param encoding the encoding of the stream
	 * @param settings the TSV writer configuration
	 */
	public SsvWriter(OutputStream output, String encoding, SsvWriterSettings settings) {
		super(output, encoding, settings);
	}

	/**
	 * The TsvWriter supports all settings provided by {@link SsvWriterSettings}, and requires this configuration to be properly initialized.
	 *
	 * @param output   the output stream that will be written with the TSV records produced by this class.
	 * @param encoding the encoding of the stream
	 * @param settings the TSV writer configuration
	 */
	public SsvWriter(OutputStream output, Charset encoding, SsvWriterSettings settings) {
		super(output, encoding, settings);
	}

	/**
	 * Initializes the TSV writer with TSV-specific configuration
	 *
	 * @param settings the TSV writer configuration
	 */
	protected final void initialize(SsvWriterSettings settings) {
		this.escapeChar = settings.getFormat().getEscapeChar();
		this.escapedTabChar = settings.getFormat().getEscapedTabChar();
		this.ignoreLeading = settings.getIgnoreLeadingWhitespaces();
		this.ignoreTrailing = settings.getIgnoreTrailingWhitespaces();
		this.joinLines = settings.isLineJoiningEnabled();
		this.newLine = settings.getFormat().getNormalizedNewline();
	}

	@Override
	protected void processRow(Object[] row) {
		for (int i = 0; i < row.length; i++) {
			if (i != 0) {
				appendToRow('\t');
			}

			String nextElement = getStringValue(row[i]);

			int originalLength = appender.length();
			append(nextElement);

			//skipped all whitespaces and wrote nothing
			if (appender.length() == originalLength && nullValue != null && !nullValue.isEmpty()) {
				append(nullValue);
			}

			appendValueToRow();
		}
	}

	private void append(String element) {
		if (element == null) {
			element = nullValue;
		}

		if (element == null) {
			return;
		}

		int start = 0;
		if (this.ignoreLeading) {
			start = skipLeadingWhitespace(whitespaceRangeStart, element);
		}

		final int length = element.length();

		int i = start;
		char ch = '\0';
		for (; i < length; i++) {
			ch = element.charAt(i);
			if (ch == '\t' || ch == '\n' || ch == '\r' || ch == '\\') {
				appender.append(element, start, i);
				start = i + 1;
				appender.append(escapeChar);
				if (ch == '\t') {
					appender.append(escapedTabChar);
				} else if (ch == '\n') {
					appender.append(joinLines ? newLine : 'n');
				} else if (ch == '\\') {
					appender.append('\\');
				} else {
					appender.append(joinLines ? newLine : 'r');
				}
			}
		}
		appender.append(element, start, i);
		if (ch <= ' ' && ignoreTrailing && whitespaceRangeStart  < ch) {
			appender.updateWhitespace();
		}
	}
}
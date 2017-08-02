/*******************************************************************************
 * Copyright 2015 uniVocity Software Pty Ltd
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

import com.univocity.parsers.common.routine.*;

import java.io.*;
// TODO: docs/comments
/**
 * A collection of common routines involving the processing of CSV data.
 */
public class SsvRoutines extends AbstractRoutines<SsvParserSettings, SsvWriterSettings> {

	/**
	 * Creates a new instance of the CSV routine class without any predefined parsing/writing configuration.
	 */
	public SsvRoutines() {
		this(null, null);
	}

	/**
	 * Creates a new instance of the CSV routine class.
	 *
	 * @param parserSettings configuration to use for CSV parsing
	 */
	public SsvRoutines(SsvParserSettings parserSettings) {
		this(parserSettings, null);
	}

	/**
	 * Creates a new instance of the CSV routine class.
	 *
	 * @param writerSettings configuration to use for CSV writing
	 */
	public SsvRoutines(SsvWriterSettings writerSettings) {
		this(null, writerSettings);
	}

	/**
	 * Creates a new instance of the CSV routine class.
	 *
	 * @param parserSettings configuration to use for CSV parsing
	 * @param writerSettings configuration to use for CSV writing
	 */
	public SsvRoutines(SsvParserSettings parserSettings, SsvWriterSettings writerSettings) {
		super("CSV parsing/writing routine", parserSettings, writerSettings);
	}

	@Override
	protected SsvParser createParser(SsvParserSettings parserSettings) {
		return new SsvParser(parserSettings);
	}

	@Override
	protected SsvWriter createWriter(Writer output, SsvWriterSettings writerSettings) {
		return new SsvWriter(output, writerSettings);
	}

	@Override
	protected SsvParserSettings createDefaultParserSettings() {
		return new SsvParserSettings();
	}

	@Override
	protected SsvWriterSettings createDefaultWriterSettings() {
		return new SsvWriterSettings();
	}
}

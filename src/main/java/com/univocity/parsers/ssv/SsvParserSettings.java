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

import com.univocity.parsers.common.CommonParserSettings;

import java.util.Map;

/**
 * This is the configuration class used by the SSV parser ({@link SsvParser})
 *
 * <p>It supports the configuration options provided by {@link CommonParserSettings} only
 *
 * @author uniVocity Software Pty Ltd - <a href="mailto:parsers@univocity.com">parsers@univocity.com</a>
 * @see SsvParser
 * @see SsvFormat
 * @see CommonParserSettings
 */
public class SsvParserSettings extends CommonParserSettings<SsvFormat> {

	private boolean lineJoiningEnabled = false;

	/**
	 * Identifies whether or lines ending with the escape character (defined by {@link SsvFormat#getEscapeChar()}
	 * and followed by a line separator character should be joined with the following line.
	 *
	 * Typical examples include inputs where lines end with sequences such as: {@code '\'+'\n'} and {@code '\'+'\r'+'\n'}.
	 *
	 * When line joining is disabled (the default), the {@link SsvParser} converts sequences containing
	 * the escape character (typically '\') followed by characters 'n' or 'r' into a '\n' or '\r' character.
	 * It will continue processing the contents found in the same line, until a new line character is found.
	 *
	 * If line joining is enabled, the {@link SsvParser} will convert sequences containing
	 * the escape character, followed by characters '\n', '\r' or '\r\n', into a '\n' or '\r' character.
	 * It will continue processing the contents found in the next line, until a new line character is found, given it is
	 * not preceded by another escape character.
	 *
	 * @return {@code true} if line joining is enabled, otherwise {@code false}
	 */
	public boolean isLineJoiningEnabled() {
		return lineJoiningEnabled;
	}

	/**
	 * Defines how the parser should handle escaped line separators. By enabling lines joining,
	 * lines ending with the escape character (defined by {@link SsvFormat#getEscapeChar()}
	 * and followed by a line separator character will be joined with the following line.
	 *
	 * Typical examples include inputs where lines end with sequences such as: {@code '\'+'\n'} and {@code '\'+'\r'+'\n'}.
	 *
	 * When line joining is disabled (the default), the {@link SsvParser} converts sequences containing
	 * the escape character (typically '\') followed by characters 'n' or 'r' into a '\n' or '\r' character.
	 * It will continue processing the contents found in the same line, until a new line character is found.
	 *
	 * If line joining is enabled, the {@link SsvParser} will convert sequences containing
	 * the escape character, followed by characters '\n', '\r' or '\r\n', into a '\n' or '\r' character.
	 * It will continue processing the contents found in the next line, until a new line character is found, given it is
	 * not preceded by another escape character.
	 *
	 * @param lineJoiningEnabled a flag indicating whether or not to enable line joining.
	 */
	public void setLineJoiningEnabled(boolean lineJoiningEnabled) {
		this.lineJoiningEnabled = lineJoiningEnabled;
	}

	/**
	 * Returns the default TsvFormat configured to handle TSV inputs
	 *
	 * @return and instance of TsvFormat configured to handle TSV
	 */
	@Override
	protected SsvFormat createDefaultFormat() {
		return new SsvFormat();
	}

	@Override
	protected void addConfiguration(Map<String, Object> out) {
		super.addConfiguration(out);
	}

	@Override
	public final SsvParserSettings clone() {
		return (SsvParserSettings) super.clone();
	}

	@Override
	public final SsvParserSettings clone(boolean clearInputSpecificSettings) {
		return (SsvParserSettings) super.clone(clearInputSpecificSettings);
	}
}

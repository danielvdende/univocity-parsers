/*******************************************************************************
 * Copyright 2016 uniVocity Software Pty Ltd
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
// todo: docs/comments

import com.univocity.parsers.common.*;

/**
 * This enumeration is used to determine how the ({@link SsvParser}) will handle values with unescaped quotes.
 *
 * Use {@link SsvParserSettings#setUnescapedQuoteHandling(UnescapedQuoteHandling)} to configure the appropriate
 * handling of unescaped quotes on your input.
 *
 * @author uniVocity Software Pty Ltd - <a href="mailto:parsers@univocity.com">parsers@univocity.com</a>
 * @see com.univocity.parsers.ssv.SsvParserSettings
 * @see com.univocity.parsers.ssv.SsvFormat
 * @see com.univocity.parsers.ssv.SsvParser
 * @see com.univocity.parsers.common.CommonParserSettings
 */
public enum UnescapedQuoteHandling {

    /**
     * If unescaped quotes are found in the input, accumulate the quote character and proceed parsing the value
     * as a quoted value, until a closing quote is found.
     */
    STOP_AT_CLOSING_QUOTE,

    /**
     * If unescaped quotes are found in the input, consider the value as an unquoted value. This will make the parser
     * accumulate all characters until the delimiter defined by {@link SsvFormat#getDelimiter()} is found in the input.
     */
    STOP_AT_DELIMITER,

    /**
     * If unescaped quotes are found in the input, the content parsed for the given value will be skipped
     * (until the next delimiter is found) and the value set in {@link CommonSettings#getNullValue()} will be produced instead.
     */
    SKIP_VALUE,

    /**
     * If unescaped quotes are found in the input, a {@link TextParsingException} will be thrown.
     */
    RAISE_ERROR,
}

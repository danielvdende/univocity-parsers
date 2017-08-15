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

import com.univocity.parsers.*;
import com.univocity.parsers.common.*;
import com.univocity.parsers.common.processor.*;
import com.univocity.parsers.common.record.*;
import org.testng.annotations.*;

import java.io.*;
import java.net.*;
import java.util.*;

import static org.testng.Assert.*;

public class SsvParserTest extends ParserTestCase {

	@DataProvider(name = "testProvider")
	public Object[][] testProvider() {
		return new Object[][]{
				{"/csv/test.csv", new char[]{'\n'}},
				{"/csv/test.csv", null}
		};
	}

	@DataProvider(name = "csvProvider")
	public Object[][] csvProvider() {
		return new Object[][]{
				{"/csv/essential.csv", new char[]{'\n'}},
				{"/csv/essential-dos.csv", new char[]{'\r', '\n'}},
				{"/csv/essential-mac.csv", new char[]{'\r'}},
				{"/csv/essential.csv", null},
				{"/csv/essential-dos.csv", null},
				{"/csv/essential-mac.csv", null}
		};
	}

	@Test(enabled = true, dataProvider = "csvProvider")
	public void parseIgnoringWhitespaces(String csvFile, char[] lineSeparator) throws Exception {
		SsvParserSettings settings = newSsvInputSettings(lineSeparator);
		settings.setCommentCollectionEnabled(true);
		settings.setRowProcessor(processor);
		settings.setHeaderExtractionEnabled(true);
		settings.setIgnoreLeadingWhitespaces(true);
		settings.setIgnoreTrailingWhitespaces(true);

		SsvParser parser = new SsvParser(settings);
		parser.parse(newReader(csvFile));

		String[] expectedHeaders = new String[]{"Year", "Make", "Model", "Description", "Price"};

		String[][] expectedResult = new String[][]{
				{"1997", "Ford", "E350", "ac, abs, moon", "3000.00"},
				{"1999", "Chevy", "Venture \"Extended Edition\"", null, "4900.00"},
				{"1996", "Jeep", "Grand Cherokee", "MUST SELL!\nair, moon roof, loaded", "4799.00"},
				{"1999", "Chevy", "Venture \"Extended Edition, Very Large\"", null, "5000.00"},
				{null, null, "Venture \"Extended Edition\"", null, "4900.00"},
				{null, null, null, null, null},
				{null, null, null, null, null},
				{null, null, "5", null, null},
				{"1997", "Ford", "E350", "ac, abs, moon", "3000.00"},
				{"1997", "Ford", "E350", " ac, abs, moon ", "3000.00"},
				{"1997", "Ford", "E350", " ac, abs, moon ", "3000.00"},
				{"19 97", "Fo rd", "E350", " ac, abs, moon ", "3000.00"},
				{null, " ", null, "  ", "30 00.00"},
				{"1997", "Ford", "E350", " \" ac, abs, moon \" ", "3000.00"},
				{"1997", "Ford", "E350", "\" ac, abs, moon \" ", "3000.00"},
		};

		assertHeadersAndValuesMatch(expectedHeaders, expectedResult);

		Map<Long, String> comments = parser.getContext().comments();
		assertEquals(comments.size(), 1);
		assertEquals(comments.keySet().iterator().next().longValue(), 6L);
		assertEquals(comments.values().iterator().next(), parser.getContext().lastComment());
		assertEquals(parser.getContext().lastComment(), "this is a comment and should be ignored");
	}

	protected SsvParserSettings newSsvInputSettings(char[] lineSeparator) {
		SsvParserSettings out = new SsvParserSettings();
		if (lineSeparator == null) {
			out.setLineSeparatorDetectionEnabled(true);
		} else {
			out.getFormat().setLineSeparator(lineSeparator);
		}
		out.getFormat().setNormalizedNewline('\n');
		return out;
	}

	@Test(enabled = true, dataProvider = "csvProvider")
	public void parseUsingWhitespaces(String csvFile, char[] lineSeparator) throws Exception {
		SsvParserSettings settings = newSsvInputSettings(lineSeparator);
		settings.setRowProcessor(processor);
		settings.setHeaderExtractionEnabled(true);
		settings.setNullValue("?????");
		settings.setEmptyValue("XXXXX");
		settings.setIgnoreLeadingWhitespaces(false);
		settings.setIgnoreTrailingWhitespaces(false);

		SsvParser parser = new SsvParser(settings);
		parser.parse(newReader(csvFile));

		String[] expectedHeaders = new String[]{"Year", "Make", "Model", "Description", "Price"};

		String[][] expectedResult = new String[][]{
				{"1997", "Ford", "E350", "ac, abs, moon", "3000.00"},
				{"1999", "Chevy", "Venture \"Extended Edition\"", "XXXXX", "4900.00"},
				{"1996", "Jeep", "Grand Cherokee", "MUST SELL!\nair, moon roof, loaded", "4799.00"},
				{"1999", "Chevy", "Venture \"Extended Edition, Very Large\"", "?????", "5000.00"},
				{"?????", "?????", "Venture \"Extended Edition\"", "XXXXX", "4900.00"},
				{"?????", "?????", "?????", "?????", "?????"},
				{" ", " ", " ", " ", " "},
				{"?????", "?????", " 5 ", "?????", "?????"},
				{"  "},
				{"1997 ", " Ford ", "E350", "ac, abs, moon", " \"3000.00\" \t"},
				{"1997", " Ford ", "E350", " ac, abs, moon ", "3000.00  \t"},
				{"  1997", " Ford ", "E350", " ac, abs, moon ", "3000.00"},
				{"    19 97 ", " Fo rd ", "E350", " ac, abs, moon ", "3000.00"},
				{"\t\t", " ", "  ", " \"  \"\t", "30 00.00\t"},
				{"1997", "Ford", "E350", " \" ac, abs, moon \" ", "3000.00"},
				{"1997", "Ford", "E350", "\" ac, abs, moon \" ", "3000.00"},
		};

		assertHeadersAndValuesMatch(expectedHeaders, expectedResult);
	}

	@Test(enabled = true, dataProvider = "csvProvider")
	public void parseColumns(String csvFile, char[] lineSeparator) throws Exception {
		SsvParserSettings settings = newSsvInputSettings(lineSeparator);
		settings.setRowProcessor(processor);
		settings.setHeaderExtractionEnabled(true);
		settings.setIgnoreLeadingWhitespaces(true);
		settings.setIgnoreTrailingWhitespaces(true);
		settings.selectFields("Year");
		settings.setColumnReorderingEnabled(false);

		SsvParser parser = new SsvParser(settings);
		parser.parse(newReader(csvFile));

		String[] expectedHeaders = new String[]{"Year", "Make", "Model", "Description", "Price"};

		String[][] expectedResult = new String[][]{
				{"1997", null, null, null, null},
				{"1999", null, null, null, null},
				{"1996", null, null, null, null},
				{"1999", null, null, null, null},
				{null, null, null, null, null},
				{null, null, null, null, null},
				{null, null, null, null, null},
				{null, null, null, null, null},
				{"1997", null, null, null, null},
				{"1997", null, null, null, null},
				{"1997", null, null, null, null},
				{"19 97", null, null, null, null},
				{null, null, null, null, null},
				{"1997", null, null, null, null},
				{"1997", null, null, null, null},
		};

		assertHeadersAndValuesMatch(expectedHeaders, expectedResult);
	}

	private String[] process(String input, Integer[] indexesToExclude, Integer[] indexesToSelect, String[] fieldsToExclude, String[] fieldsToSelect) {
		SsvParserSettings settings = new SsvParserSettings();
		settings.getFormat().setLineSeparator("\n");
		settings.setHeaderExtractionEnabled(fieldsToExclude != null || fieldsToSelect != null);

		if (indexesToExclude != null) {
			settings.excludeIndexes(indexesToExclude);
		} else if (fieldsToExclude != null) {
			settings.excludeFields(fieldsToExclude);
		} else if (indexesToSelect != null) {
			settings.selectIndexes(indexesToSelect);
		} else if (fieldsToSelect != null) {
			settings.selectFields(fieldsToSelect);
		}

		SsvParser parser = new SsvParser(settings);
		return parser.parseLine(input);
	}

	@Test
	public void columnSelectionTest() {
		String[] result;
		String input = "a,b,c,d,e";

		Integer[] indexesToExclude = new Integer[]{0, 4};
		result = process(input, indexesToExclude, null, null, null);
		assertEquals(result, new String[]{"b", "c", "d"});

		Integer[] indexesToSelect = new Integer[]{0, 4};
		result = process(input, null, indexesToSelect, null, null);
		assertEquals(result, new String[]{"a", "e"});

		input = "ha,hb,hc,hd,he\na,b,c,d,e";

		String[] fieldsToExclude = new String[]{"hb", "hd"};
		result = process(input, null, null, fieldsToExclude, null);
		assertEquals(result, new String[]{"a", "c", "e"});

		String[] fieldsToSelect = new String[]{"hb", "hd"};
		result = process(input, null, null, null, fieldsToSelect);
		assertEquals(result, new String[]{"b", "d"});
	}

	@Override
	protected RowListProcessor newRowListProcessor() {
		return new RowListProcessor() {
			@Override
			public void processStarted(ParsingContext context) {
				super.processStarted(context);
				context.skipLines(2);
			}

			@Override
			public void rowProcessed(String[] row, ParsingContext context) {
				super.rowProcessed(row, context);

				// for (int i = 0; i < row.length; i++) {
				// row[i] = ">>" + row[i] + "<<";
				// }
				// System.out.println(context.currentLine() + " => " + Arrays.toString(row));

				if (context.currentLine() == 9) {
					context.skipLines(1);
				}
			}
		};
	}

	@Test(enabled = true, dataProvider = "csvProvider")
	public void parseOneByOne(String csvFile, char[] lineSeparator) throws Exception {
		SsvParserSettings settings = newSsvInputSettings(lineSeparator);
		settings.setRowProcessor(processor);
		settings.setHeaderExtractionEnabled(true);
		settings.setIgnoreLeadingWhitespaces(true);
		settings.setIgnoreTrailingWhitespaces(true);
		settings.setHeaders("YR", "MK", "MDL", "DSC", "PRC");

		List<Object[]> results = new ArrayList<Object[]>();
		SsvParser parser = new SsvParser(settings);
		try {
			parser.beginParsing(newReader(csvFile));

			Object[] row;
			while ((row = parser.parseNext()) != null) {
				if (row.length == 5) {
					results.add(row);
				}
			}
		} finally {
			parser.stopParsing();
		}

		String[] expectedHeaders = new String[]{"YR", "MK", "MDL", "DSC", "PRC"};

		String[][] expectedResult = new String[][]{
				{"1997", "Ford", "E350", "ac, abs, moon", "3000.00"},
				{"1999", "Chevy", "Venture \"Extended Edition\"", null, "4900.00"},
				{"1996", "Jeep", "Grand Cherokee", "MUST SELL!\nair, moon roof, loaded", "4799.00"},
				{"1999", "Chevy", "Venture \"Extended Edition, Very Large\"", null, "5000.00"},
				{null, null, "Venture \"Extended Edition\"", null, "4900.00"},
				{null, null, null, null, null},
				{null, null, null, null, null},
				{null, null, "5", null, null},
				{"1997", "Ford", "E350", "ac, abs, moon", "3000.00"},
				{"1997", "Ford", "E350", " ac, abs, moon ", "3000.00"},
				{"1997", "Ford", "E350", " ac, abs, moon ", "3000.00"},
				{"19 97", "Fo rd", "E350", " ac, abs, moon ", "3000.00"},
				{null, " ", null, "  ", "30 00.00"},
				{"1997", "Ford", "E350", " \" ac, abs, moon \" ", "3000.00"},
				{"1997", "Ford", "E350", "\" ac, abs, moon \" ", "3000.00"},
		};

		Object[] headers = processor.getHeaders();
		assertEquals(headers, expectedHeaders);

		assertEquals(results.size(), expectedResult.length);

		for (int i = 0; i < expectedResult.length; i++) {
			Object[] result = results.get(i);
			String[] expectedRow = expectedResult[i];
			assertEquals(result, expectedRow);
		}
	}

	@Test(enabled = true, dataProvider = "csvProvider")
	public void parse3Records(String csvFile, char[] lineSeparator) throws Exception {
		SsvParserSettings settings = newSsvInputSettings(lineSeparator);
		settings.setRowProcessor(processor);
		settings.setHeaderExtractionEnabled(true);
		settings.setIgnoreLeadingWhitespaces(true);
		settings.setIgnoreTrailingWhitespaces(true);
		settings.setNumberOfRecordsToRead(3);

		SsvParser parser = new SsvParser(settings);
		parser.parse(newReader(csvFile));

		String[] expectedHeaders = new String[]{"Year", "Make", "Model", "Description", "Price"};

		String[][] expectedResult = new String[][]{
				{"1997", "Ford", "E350", "ac, abs, moon", "3000.00"},
				{"1999", "Chevy", "Venture \"Extended Edition\"", null, "4900.00"},
				{"1996", "Jeep", "Grand Cherokee", "MUST SELL!\nair, moon roof, loaded", "4799.00"},
		};

		assertHeadersAndValuesMatch(expectedHeaders, expectedResult);
	}

	@Test
	public void parseBrokenQuoteEscape() {
		SsvParserSettings settings = newSsvInputSettings(new char[]{'\n'});
		settings.setParseUnescapedQuotesUntilDelimiter(false);
		settings.setHeaderExtractionEnabled(false);
		SsvParser parser = new SsvParser(settings);

		parser.beginParsing(new StringReader(""
				+ "something,\"a quoted value \"with unescaped quotes\" can be parsed\", something\n"
				+ "1997 , Ford ,E350,\"s, m\"\"\"	, \"3000.00\"\n"
				+ "1997 , Ford ,E350,\"ac, abs, moon\"	, \"3000.00\" \n"
				+ "something,\"a \"quoted\" \"\"value\"\" \"\"with unescaped quotes\"\" can be parsed\" , something\n"));

		String[] row = parser.parseNext();

		assertEquals(row[0], "something");
		assertEquals(row[2], "something");
		assertEquals(row[1], "a quoted value \"with unescaped quotes\" can be parsed");

		row = parser.parseNext();

		assertEquals(row, new String[]{"1997", "Ford", "E350", "s, m\"", "3000.00"});

		row = parser.parseNext();
		assertEquals(row, new String[]{"1997", "Ford", "E350", "ac, abs, moon", "3000.00"});

		row = parser.parseNext();
		assertEquals(row[0], "something");
		assertEquals(row[2], "something");
		assertEquals(row[1], "a \"quoted\" \"value\" \"with unescaped quotes\" can be parsed");

	}

	@Test
	public void testReadEmptyValue() {
		SsvParserSettings settings = newSsvInputSettings(new char[]{'\n'});
		settings.setEmptyValue("");
		settings.setHeaderExtractionEnabled(false);
		SsvParser parser = new SsvParser(settings);

		parser.beginParsing(new StringReader("a,b,,c,\"\",\r\n"));
		String[] row = parser.parseNext();

		assertEquals(row[0], "a");
		assertEquals(row[1], "b");
		assertEquals(row[2], null);
		assertEquals(row[3], "c");
		assertEquals(row[4], "");
		assertEquals(row[5], null);
	}

	@DataProvider
	public Object[][] escapeHandlingProvider() {
		return new Object[][]{
				//parsing a line with the following content: ||,|| |"," |" B |" "," |" ||"
				{false, false, new String[]{"||", "|| |\"", " \" B \" ", " \" |"}}, // process escapes on quoted values only: 		||	, || |"	, " B "			,	" |
				{false, true, new String[]{"|", "| \"", " \" B \" ", " \" |"}}, // process escapes quoted and unquoted: 			|	, | "	, " B "			,	" |
				{true, false, new String[]{"||", "|| |\"", " |\" B |\" ", " |\" ||"}}, // keep escape on quoted values only:		||	, || |"	, " |" B |" "	,  |" ||"
				{true, true, new String[]{"||", "|| |\"", " |\" B |\" ", " |\" ||"}} // keep escape on everything: 				||	, || |"	, " |" B |" "	,  |" ||"
		};
	}

	@Test(dataProvider = "escapeHandlingProvider")
	public void testHandlingOfEscapeSequences(boolean keepEscape, boolean escapeUnquoted, String[] expected) throws Exception {
		SsvParserSettings settings = new SsvParserSettings();
		settings.setKeepEscapeSequences(keepEscape);
		settings.setEscapeUnquotedValues(escapeUnquoted);
		settings.getFormat().setCharToEscapeQuoteEscaping('|');
		settings.getFormat().setQuoteEscape('|');

		String line = "||,|| |\",\" |\" B |\" \",\" |\" ||\"";

		SsvParser parser = new SsvParser(settings);
		String[] result = parser.parseLine(line); // ||, || |", " |" B |" ", " |" ||"
		assertEquals(result, expected);
	}

	@Test
	public void testEscapedLineEndingsAreNotModified() {
		SsvParserSettings settings = new SsvParserSettings();
		settings.setNormalizeLineEndingsWithinQuotes(false);
		settings.getFormat().setLineSeparator("\r\n");

		SsvParser parser = new SsvParser(settings);
		String input = "1,\" Line1 \r\n Line2 \r Line3 \n Line4 \n\r \"\r\n" +
				"2,\" Line10 \r\n Line11 \"";


		List<String[]> result = parser.parseAll(new StringReader(input)); // ||, || |", " |" B |" ", " |" ||"
		assertEquals(result.size(), 2);
		assertEquals(result.get(0).length, 2);
		assertEquals(result.get(1).length, 2);

		assertEquals(result.get(0), new String[]{"1", " Line1 \r\n Line2 \r Line3 \n Line4 \n\r "});
		assertEquals(result.get(1), new String[]{"2", " Line10 \r\n Line11 "});

	}

	public char[] getLineSeparator() {
		return new char[]{'\n'};
	}

	@Test
	public void shouldNotAllowParseUnescapedQuotes() throws UnsupportedEncodingException {
		SsvParserSettings settings = newSsvInputSettings(getLineSeparator());
		settings.setRowProcessor(new RowListProcessor()); //Default used by CsvParserTest skip 2 lines
		settings.setParseUnescapedQuotes(false); //To force exception

		SsvParser parser = new SsvParser(settings);
		try {
			parser.parse(new StringReader("1997,\"TV 29\"LED\"\n"));
			fail("Expected exception to be thrown here");
		} catch (TextParsingException ex) {
			assertTrue(ex.getMessage().contains("Unescaped quote character"));
		}
	}

	@Test
	public void parseQuotedStringFollowedByBlankSpace() throws UnsupportedEncodingException {
		RowListProcessor processor = new RowListProcessor();
		SsvParserSettings settings = newSsvInputSettings(getLineSeparator());
		settings.setRowProcessor(processor); //Default used by CsvParserTest skip 2 lines
		settings.setParseUnescapedQuotes(true);
		settings.setParseUnescapedQuotesUntilDelimiter(false);

		SsvParser parser = new SsvParser(settings);
		parser.parse(new StringReader("1997,\"TV 29\" LED\"\n"));

		List<String[]> rows = processor.getRows();

		assertEquals(rows.size(), 1);

		String[] firstRow = rows.get(0);
		assertEquals(firstRow[0], "1997");
		assertEquals(firstRow[1], "TV 29\" LED");
	}

	@Test(dataProvider = "testProvider")
	public void shouldNotAllowUnexpectedCharacterAfterQuotedValue(String csvFile, char[] lineSeparator) throws UnsupportedEncodingException {
		SsvParserSettings settings = newSsvInputSettings(lineSeparator);
		settings.setParseUnescapedQuotes(false);

		SsvParser parser = new SsvParser(settings);
		try {
			parser.parseLine("1997,\"value\"x");
			fail("Expected exception to be thrown here");
		} catch (TextParsingException ex) {
			assertTrue(ex.getMessage().contains("Unescaped quote character '\"' inside quoted value of CSV field"));
		}
	}


	@Test
	public void parseValueProcessingEscapeNotIgnoringWhitespace() {
		RowListProcessor processor = new RowListProcessor();
		SsvParserSettings settings = newSsvInputSettings(getLineSeparator());
		settings.setRowProcessor(processor); //Default used by CsvParserTest skip 2 lines
		settings.setKeepEscapeSequences(true);
		settings.setIgnoreTrailingWhitespaces(false);
		settings.setEscapeUnquotedValues(true);

		SsvFormat format = new SsvFormat();
		format.setQuoteEscape('\'');
		format.setCharToEscapeQuoteEscaping('\\');
		settings.setFormat(format);

		SsvParser parser = new SsvParser(settings);
		parser.parse(new StringReader("'\\\"a\n")); //goes into the else statement of CsvParser.parseValueProcessingEscape() method.

		List<String[]> rows = processor.getRows();

		assertEquals(rows.size(), 1);
		String[] firstRow = rows.get(0);

		assertEquals(firstRow[0], "\\\"a");
	}

	@Test
	public void parseValueProcessingEscapeNotIgnoringWhitespacePrevQuoteEscape2() {
		RowListProcessor processor = new RowListProcessor();
		SsvParserSettings settings = newSsvInputSettings(getLineSeparator());
		settings.setRowProcessor(processor); //Default used by CsvParserTest skip 2 lines
		settings.setKeepEscapeSequences(true);
		settings.setIgnoreTrailingWhitespaces(false);
		settings.setEscapeUnquotedValues(true);

		SsvFormat format = new SsvFormat();
		format.setQuoteEscape('\'');
		format.setCharToEscapeQuoteEscaping('\\');
		settings.setFormat(format);

		SsvParser parser = new SsvParser(settings);
		parser.parse(new StringReader("\\\'\n"));

		List<String[]> rows = processor.getRows();

		assertEquals(rows.size(), 1);
		String[] firstRow = rows.get(0);

		assertEquals(firstRow[0], "\\\\'");
	}

	@Test
	public void parseValueProcessingEscapeNotIgnoringWhitespacePrevQuoteEscape() {
		RowListProcessor processor = new RowListProcessor();
		SsvParserSettings settings = newSsvInputSettings(getLineSeparator());
		settings.setRowProcessor(processor); //Default used by CsvParserTest skip 2 lines
		settings.setKeepEscapeSequences(true);
		settings.setIgnoreTrailingWhitespaces(false);
		settings.setEscapeUnquotedValues(true);

		SsvFormat format = new SsvFormat();
		format.setQuoteEscape('\'');
		format.setCharToEscapeQuoteEscaping('\\');
		settings.setFormat(format);

		SsvParser parser = new SsvParser(settings);
		parser.parse(new StringReader("'\"a\n"));

		List<String[]> rows = processor.getRows();

		assertEquals(rows.size(), 1);
		String[] firstRow = rows.get(0);

		assertEquals(firstRow[0], "'\"a");
	}

	@DataProvider
	public Object[][] skipLinesProvider() {
		return new Object[][]{
				{0, "1234"},
				{1, "234"},
				{2, "34"},
				{3, "4"},
				{4, null},
				{5, "BOOM"},
		};
	}

	@Test(dataProvider = "skipLinesProvider")
	public void testSkipLines(int rowsToSkip, String expectedResult) {
		SsvParserSettings settings = new SsvParserSettings();
		settings.getFormat().setLineSeparator("\n");

		settings.setNumberOfRowsToSkip(rowsToSkip);

		SsvParser parser = new SsvParser(settings);
		String input = "1\n2\n3\n4\n";

		try {
			List<String[]> result = parser.parseAll(new StringReader(input));
			StringBuilder out = null;
			for (String row[] : result) {
				if (out == null) {
					out = new StringBuilder();
				}
				assertEquals(row.length, 1);
				out.append(row[0]);
			}
			assertEquals(out == null ? null : out.toString(), expectedResult);
		} catch (Exception ex) {
			assertEquals(expectedResult, "BOOM");
		}
	}

	@Test
	public void testParseUnescapedQuotesWithStop() {
		SsvParserSettings settings = new SsvParserSettings();
		settings.setParseUnescapedQuotesUntilDelimiter(true);
		settings.getFormat().setLineSeparator("\n");

		SsvParser parser = new SsvParser(settings);
		String input = "field1,\"inner quote\" field2,\"12,34\",\",5\",";

		String[] values = parser.parseLine(input);

		assertEquals(values[0], "field1");
		assertEquals(values[1], "\"inner quote\" field2");
		assertEquals(values[2], "12,34");
		assertEquals(values[3], ",5");
		assertEquals(values[4], null);
	}

	@Test
	public void parseIgnoreTrailingWhitespace() {
		SsvParserSettings settings = new SsvParserSettings();
		settings.getFormat().setLineSeparator("\n");
		settings.setIgnoreTrailingWhitespaces(true);
		SsvParser parser = new SsvParser(settings);

		String[] value = parser.parseLine("b ");
		assertEquals(value[0], "b");
	}

	@Test
	public void parseWithAutoExpansion() {
		SsvParserSettings settings = new SsvParserSettings();
		settings.setMaxCharsPerColumn(-1);

		StringBuilder in = new StringBuilder(100000);
		for (int i = 0; i < 100000; i++) {
			in.append(i % 10);
			if (i % 10000 == 0) {
				in.append(',');
			}
		}

		String[] result = new SsvParser(settings).parseLine(in.toString());
		StringBuilder out = new StringBuilder();
		for (String value : result) {
			if (out.length() > 0) {
				out.append(',');
			}
			out.append(value);
		}

		assertEquals(out.toString(), in.toString());
	}

	@Test
	public void testErrorMessageRestrictions() {
		SsvParserSettings settings = new SsvParserSettings();
		settings.setMaxCharsPerColumn(3);
		settings.setErrorContentLength(0);

		try {
			new SsvParser(settings).parseLine("abcde");
			fail("Expecting an exception here");
		} catch (TextParsingException ex) {
			assertFalse(ex.getMessage().contains("abc"));
			assertNull(ex.getParsedContent());
		}

		settings.setErrorContentLength(2);
		try {
			new SsvParser(settings).parseLine("abcde");
			fail("Expecting an exception here");
		} catch (TextParsingException ex) {
			assertTrue(ex.getMessage().contains("...bc"));
			assertEquals(ex.getParsedContent(), "abc");
		}
	}

	@Test
	public void testKeepQuotes() {
		SsvParserSettings settings = new SsvParserSettings();
		settings.setKeepQuotes(true);
		settings.getFormat().setQuote('\'');
		settings.getFormat().setQuoteEscape('\'');
		SsvParser parser = new SsvParser(settings);

		String[] result = parser.parseLine("a,'b', '', '' c '', '' ' '', ''''");
		assertEquals(result[0], "a");
		assertEquals(result[1], "'b'");
		assertEquals(result[2], "''");
		assertEquals(result[3], "'' c ''");
		assertEquals(result[4], "'' ' ''");
		assertEquals(result[5], "'''");
	}

	@Test
	public void testNullValue() {
		SsvParserSettings settings = new SsvParserSettings();
		settings.setIgnoreLeadingWhitespaces(false);
		settings.setIgnoreTrailingWhitespaces(true);
		settings.setNullValue("NULL");
		SsvParser parser = new SsvParser(settings);


		String[] result = parser.parseLine(", ,");
		assertEquals(result.length, 3);
		assertEquals(result[0], "NULL");
		assertEquals(result[1], "NULL");
		assertEquals(result[2], "NULL");
	}

	@Test
	public void testColumnReorderingWithUserProvidedHeaders() throws Exception {
		SsvParserSettings settings = new SsvParserSettings();
		settings.setHeaders("a", "b", "c");

		settings.setColumnReorderingEnabled(false);
		settings.selectFields("a", "c");

		String[] values = new SsvParser(settings).parseLine("1,2,3");
		assertEquals(values, new String[]{"1", null, "3"});
	}

	@Test
	public void testEscapeCharacter() {
		SsvParserSettings parserSettings = new SsvParserSettings();
		parserSettings.getFormat().setQuoteEscape('/');

		SsvParser parser = new SsvParser(parserSettings);
		String[] line;

		line = parser.parseLine("\"a ,/,b/,\",c");
		assertEquals(line.length, 2);
		assertEquals(line[0], "a ,/,b/,");
		assertEquals(line[1], "c");

		line = parser.parseLine("\"a ,//,b//,\",c");
		assertEquals(line.length, 2);
		assertEquals(line[0], "a ,/,b/,");
		assertEquals(line[1], "c");
	}

	@Test
	public void testBitsAreNotDiscardedWhenParsing() {
		SsvParserSettings parserSettings = new SsvParserSettings();
		parserSettings.setSkipBitsAsWhitespace(false);

		SsvParser parser = new SsvParser(parserSettings);
		String[] line;

		line = parser.parseLine("\0 a, b");
		assertEquals(line.length, 2);
		assertEquals(line[0], "\0 a");
		assertEquals(line[1], "b");

		line = parser.parseLine("\1 a, b \0");
		assertEquals(line.length, 2);
		assertEquals(line[0], "\1 a");
		assertEquals(line[1], "b \0");

		line = parser.parseLine("\2 a, \"b, \1\"");
		assertEquals(line.length, 2);
		assertEquals(line[0], "a");
		assertEquals(line[1], "b, \1");
	}

	@Test
	public void testParserIteratorOnFile() throws Exception {
		SsvParserSettings parserSettings = new SsvParserSettings();
		parserSettings.setLineSeparatorDetectionEnabled(true);

		SsvParser parser = new SsvParser(parserSettings);

		String[][] correctRows = {
				{"a", "b", "c"},
				{"d", "e", "f"},
				{"g", "h", "i"},
				{"j", null},
				{"k", "l"},
				{"m", "n", "o", "p", "q", "r"}
		};

		File input = getFile("/csv/iterating_test.csv");
		int i = 0;
		for (String[] row : parser.iterate(input, "UTF-8")) {
			assertEquals(row, correctRows[i++]);
		}
		i = 0;
		for (Record row : parser.iterateRecords(new FileInputStream(input), "UTF-8")) {
			assertEquals(row.getValues(), correctRows[i++]);
		}

		for (Record row : parser.iterateRecords(new StringReader(""))) {
			fail("Empty input, should not get here");
		}
	}
}

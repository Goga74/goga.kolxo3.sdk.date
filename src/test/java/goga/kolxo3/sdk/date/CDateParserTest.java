package goga.kolxo3.sdk.date;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
public class CDateParserTest {
	
	private static final String EMPTY_STRING = "";
	
	private static Stream<Arguments> provideCorrectDateValuesForUS()
	{
		return Stream.of(
				Arguments.of("1974-Apr-05", "04/05/1974"),
				Arguments.of("Jan/2/2020", "01/02/2020"),
				
				Arguments.of("19/1/2016", "01/19/2016"), // fixed wrong sequence in date - 19 is not a month!
				Arguments.of("10/1/2016", "10/01/2016"),
				
				Arguments.of("1974-04-06", "04/06/1974"),
				Arguments.of("20-02-02", "02/02/2020"), // extend year to 4 characters
				Arguments.of("98-02-02", "02/02/1998"), // extend year to 4 characters
				Arguments.of("19-04-03", "04/03/2019"), // extend year to 4 characters
				Arguments.of("1989", "1989"),
				Arguments.of("1974-06", "06/1974"),
				Arguments.of("1974", "1974"),
				Arguments.of("2/2/2020", "02/02/2020"),
				Arguments.of("02/02/2020", "02/02/2020"),
				Arguments.of("2/2020", "02/2020"),
				Arguments.of("1974-4-6", "04/06/1974"),
				Arguments.of("1974-04-6", "04/06/1974"),
				Arguments.of("1974-6", "06/1974"),
				Arguments.of("1974-06-04", "06/04/1974"),
				Arguments.of("1/19/2016", "01/19/2016"),
				Arguments.of("01-02-03", "02/03/2001") // 2001-02-03 means 03 Feb 2001
		);
	}
	
	private static Stream<Arguments> provideWrongDateValues()
	{
		return Stream.of(
				Arguments.of("1995 (not used)", "1995 (not used)"), // keep the same value in error
				Arguments.of("blabla 1987", "blabla 1987"),
				Arguments.of("bla 1988 bla", "bla 1988 bla"),
				Arguments.of("", ""),
				Arguments.of(null, null),
				Arguments.of("1979-NaN-NaN", "1979-NaN-NaN")
		);
	}
	
	private static Stream<Arguments> provideCorrectDateValuesForEU()
	{
		return Stream.of(
				Arguments.of("1974-Apr-05", "05.04.1974"),
				Arguments.of("Jan/2/2020", "02.01.2020"),
				
				Arguments.of("19/1/2016", "19.01.2016"), // fixed wrong sequence in date - 19 is not a month!
				Arguments.of("10/1/2016", "01.10.2016"),
				
				Arguments.of("1974-04-06", "06.04.1974"),
				Arguments.of("20-02-21", "21.02.2020"), // extend year to 4 characters
				Arguments.of("98-01-02", "02.01.1998"), // extend year to 4 characters
				Arguments.of("19-04-03", "03.04.2019"), // extend year to 4 characters
				Arguments.of("1989", "1989"),
				Arguments.of("1974-06", "06.1974"),
				Arguments.of("1974", "1974"),
				Arguments.of("2/3/2020", "03.02.2020"), // in input american format month is first!
				Arguments.of("02/01/2020", "01.02.2020"),
				Arguments.of("2/2020", "02.2020"),
				Arguments.of("1974-4-6", "06.04.1974"),
				Arguments.of("1974-04-6", "06.04.1974"),
				Arguments.of("1974-6", "06.1974"),
				Arguments.of("1974-07-04", "04.07.1974"),
				Arguments.of("1/19/2016", "19.01.2016"),
				Arguments.of("01-02-03", "03.02.2001") // 2001-02-03 means 03 Feb 2001
		);
	}
	
	private static Stream<Arguments> shortYearValuesToCheck()
	{
		return Stream.of(
				// non-valid values
				Arguments.of("", false),
				Arguments.of(" ", false),
				Arguments.of(null, false),
				Arguments.of("198-05-11", false),
				Arguments.of("198", false),
				Arguments.of("cvba", false),
				Arguments.of("not known", false),
				Arguments.of("20-0", false),
				// valid values
				Arguments.of("99", true),
				Arguments.of("20", true),
				Arguments.of("00", true),
				Arguments.of("10", true),
				Arguments.of("01", true)
		);
	}
	
	private static Stream<Arguments> alphabeticMonthValuesToCheck()
	{
		return Stream.of(
				// non-valid values
				Arguments.of("", 0),
				Arguments.of(" ", 0),
				Arguments.of(null, 0),
				Arguments.of("Ap", 0),
				Arguments.of("Aprilis", 0),
				Arguments.of("Dicemb", 0),
				Arguments.of("not known", 0),
				Arguments.of("02", 0), // numeric months not supports here
				// valid values
				Arguments.of("February", 2),
				Arguments.of("Jan", 1),
				Arguments.of("August", 8),
				Arguments.of("march", 3),
				Arguments.of("jul", 7)
		);
	}
	
	private static Stream<Arguments> shortNumericMonthValuesToCheck()
	{
		return Stream.of(
				// non-valid values
				Arguments.of("", false),
				Arguments.of(" ", false),
				Arguments.of(null, false),
				Arguments.of("Ap", false),
				Arguments.of("Aprilis", false),
				Arguments.of("Dicemb", false),
				Arguments.of("not known", false),
				Arguments.of("00", false),
				Arguments.of("1", false),
				Arguments.of("-1", false),
				Arguments.of("1000", false),
				Arguments.of("13", false),
				Arguments.of("3", false),
				Arguments.of("5", false),
				// valid values
				Arguments.of("01", true),
				Arguments.of("02", true),
				Arguments.of("12", true)
		);
	}
	
	private static Stream<Arguments> shortNumericDayValuesToCheck()
	{
		return Stream.of(
				// non-valid values
				Arguments.of("", false),
				Arguments.of(" ", false),
				Arguments.of(null, false),
				Arguments.of("00", false),
				Arguments.of("1", false),
				Arguments.of("-1", false),
				Arguments.of("1000", false),
				Arguments.of("32", false),
				Arguments.of("3", false),
				Arguments.of("5", false),
				// valid values
				Arguments.of("01", true),
				Arguments.of("02", true),
				Arguments.of("12", true)
		);
	}
	
	@ParameterizedTest
	@MethodSource("provideCorrectDateValuesForUS")
	@DisplayName("Test date parser with list of valid values, output date format is american MM/dd/yyyy")
	public void testDateParserWithAmericanFormat_OK(final String sDate, final String sExpectedDate)
	{
		System.out.println("American, input date: " + sDate + ", expected date: " + sExpectedDate);
		CDateParser parser = new CDateParser();
		assertThat(parser.getDate(sDate, "MM/dd/yyyy")).isEqualTo(sExpectedDate);
	}
	
	@ParameterizedTest
	@MethodSource("provideCorrectDateValuesForEU")
	@DisplayName("Test date parser with list of valid values, output date format is european dd.MM.yyyy")
	public void testDateParserWithEUFormat_OK(final String sDate, final String sExpectedDate)
	{
		System.out.println("EU, input date: " + sDate + ", expected date: " + sExpectedDate);
		CDateParser parser = new CDateParser();
		assertThat(parser.getDate(sDate, "dd.MM.yyyy")).isEqualTo(sExpectedDate);
	}
	
	@ParameterizedTest
	@MethodSource("provideWrongDateValues")
	@DisplayName("Test date parser with some list of INVALID values, output date format is american MM/dd/yyyy")
	public void testDateParser_WrongDate(final String sDate, final String sExpectedDate)
	{
		System.out.println("American, input date: " + sDate + ", expected date: " + sExpectedDate);
		CDateParser parser = new CDateParser();
		assertThat(parser.getDate(sDate, "MM/dd/yyyy")).isEqualTo(sExpectedDate);
	}
	
	@ParameterizedTest
	@MethodSource("shortYearValuesToCheck")
	@DisplayName("= test date parser component - shot year (2 digits)")
	public void testValidShortYear(final String sYear, final boolean controlResult)
	{
		assertThat(CDateParser.isShortYear(sYear)).isEqualTo(controlResult);
	}
	
	@ParameterizedTest
	@MethodSource("alphabeticMonthValuesToCheck")
	@DisplayName("= test date parser component - alphabetic months (like Apr for example)")
	public void testValidAlphabeticMonth(final String sMonth, final int controlResult)
	{
		assertThat(CDateParser.getAlphabeticMonth(sMonth)).isEqualTo(controlResult);
	}
	
	@ParameterizedTest
	@MethodSource("shortNumericMonthValuesToCheck")
	@DisplayName("= test date parser component - month as 2 digits")
	public void testValidShortNumericMonth(final String sMonth, final boolean controlResult)
	{
		assertThat(CDateParser.isNumericMonth(sMonth)).isEqualTo(controlResult);
	}
	
	@ParameterizedTest
	@MethodSource("shortNumericDayValuesToCheck")
	@DisplayName("= test date parser component - day as 2 digits")
	public void testValidShortNumericDay(final String sMonth, final boolean controlResult)
	{
		assertThat(CDateParser.isNumericDay(sMonth)).isEqualTo(controlResult);
	}
	
	@Test
	@DisplayName("= test internal date parser component - months enum")
	public void testValidShortNumericDay()
	{
		assertThat(CDateParser.getNumericMonthByIndex(1)).isEqualTo("01");
		assertThat(CDateParser.getNumericMonthByIndex(2)).isEqualTo("02");
		assertThat(CDateParser.getNumericMonthByIndex(11)).isEqualTo("11");
		assertThat(CDateParser.getNumericMonthByIndex(0)).isEqualTo("");
		assertThat(CDateParser.getNumericMonthByIndex(12)).isEqualTo("12");
		assertThat(CDateParser.getNumericMonthByIndex(-1)).isEqualTo("");
	}

}

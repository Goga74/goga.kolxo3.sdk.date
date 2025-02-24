package goga.kolxo3.sdk.date;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Objects;
import java.util.regex.Pattern;
import java.util.Locale;

/**
 * @author Igor Zamiatin
 */
public class CDateParser {
	public static final String[] MONTHS = {
			"January", "February", "March", "April", "May", "June",
			"July", "August", "September", "October", "November", "December"
	};
	public static final char[] DELIMITERS = {'.', '-', '/', ' '};
	public static final Pattern NUMERIC_DAY_PATTERN = Pattern.compile("^0[1-9]|[12][0-9]|3[01]$");
	public static final Pattern NUMERIC_MONTH_PATTERN = Pattern.compile("^0[1-9]|1[0-2]$");
	public static final Pattern LONG_YEAR_PATTERN = Pattern.compile("^19[0-9]{2}|20[0-9]{2}$");
	public static final Pattern SHORT_YEAR_PATTERN = Pattern.compile("^\\d{2}$");
	
	private static final char DEFAULT_DELIMITER = '/';
	private static final String FORMAT_TWO_DIGITS = "%02d";
	private static final String FORMAT_FOUR_DIGITS = "%04d";
	
	
	/**
	 * Convert any input date to date with needed pattern if possible
	 *
	 * @param sInputDate - input date as string in various formats
	 * @param format - patter for output string, "MM/dd/yyyy" or "dd.mm.yyyy" - for example
	 * @return string with date in specified format if possible, on any error return input date w/o changes
	 */
	public String getDate(final String sInputDate, final String format)
	{
		String day = "";
		String month = "";
		String year = "";
		
		int iYearIndex = -1;
		
		boolean isFoundMonth = false;
		int iMonthIndex = -1;
		
		boolean isFoundDay = false;
		int iDayIndex = -1;
		
		boolean dateIsFull = false; // date is full when is has all 3 parts - day, month, and year
		
		if (null != sInputDate)
		{
			if (!sInputDate.trim().isEmpty())
			{
				final char delimiter = detectDelimiter(sInputDate.trim());
				final String[] parts = extendOneCharNumbers(sInputDate.trim().split(Character.toString(delimiter)));

				if (parts.length == 1 && isValidLongYear(parts[0])) // when date is year only
				{
					return parts[0];
				} else if (parts.length == 3) // when date is full: has 3 parts - day, month, and year
				{
					dateIsFull = true;
				} else if (parts.length != 2) {
					return sInputDate; // return input data AS IS
				}
				
				// get year position
				iYearIndex = getYearIndex(parts, delimiter);
				if (iYearIndex == -1) // year does not found
				{
					return sInputDate; // return input data AS IS
				}
				year = parts[iYearIndex]; // year detected, was found
				if (isShortYear(year)) {
					year = convertToFullYearFormat(year);
				}
				
				// try to get day position
				iDayIndex = foundDayIndex(parts, iYearIndex);
				if (iDayIndex != -1 && iDayIndex != iYearIndex) { // day still NOT found
					isFoundDay = true;
					day = parts[iDayIndex]; // day detected
				}
				
				// try to get month position
				iMonthIndex = foundAlphabeticalMonthIndex(parts);
				if (iMonthIndex != -1 && iMonthIndex != iYearIndex)
				{
					isFoundMonth = true;
					month = parts[iMonthIndex]; // month detected
					int temp_number_of_month = getAlphabeticMonth(month);
					String num_month = getNumericMonthByIndex(temp_number_of_month);
					int i_month = getNumericMonth(num_month);
					month = String.format(FORMAT_TWO_DIGITS, i_month);
				}
				
				if (iYearIndex == 0) // year at begin of the date
				{
					if (!isFoundMonth)
					{
						iMonthIndex = iYearIndex + 1; // month is next after year
						isFoundMonth = true;
						month = parts[iMonthIndex];
					}
					if (!isFoundDay && dateIsFull)
					{ // day is next after month
						iDayIndex = iMonthIndex + 1;
						isFoundDay = true;
						day = parts[iDayIndex];
					}
				} else if (iYearIndex == parts.length - 1) { // year is the last part of the date
					// position of month depends on day and delimiter
					if (!isFoundDay && dateIsFull)
					{
						if (!isFoundMonth)
						{ // month still not found
							switch(delimiter)
							{
								case ' ':
								case '-':
								case '/':
									iMonthIndex = 0; // month is next after year
									isFoundMonth = true;
									month = parts[iMonthIndex];
									iDayIndex = 1;
									isFoundDay = true;
									day = parts[iDayIndex];
									break;
								case '.':
									iDayIndex = 0;
									isFoundDay = true;
									day = parts[iDayIndex];
									iMonthIndex = 1; // month is next after year
									isFoundMonth = true;
									month = parts[iMonthIndex];
									break;
							}
						} else {
							if (iMonthIndex == 0) // month was found before
							{
								iDayIndex = 1;
							} else {
								iDayIndex = 0;
							}
							isFoundDay = true;
							day = parts[iDayIndex];
						}
					} else if (isFoundDay && dateIsFull) {
						if (!isFoundMonth) { // month still NOT found
							if (iDayIndex == 0) // day is the first, so month is next
							{
								iMonthIndex = 1;
							} else {
								iMonthIndex = 0;
							}
							isFoundMonth = true;
							month = parts[iMonthIndex];
						}
					}
					if (!isFoundMonth) // month still NOT found
					{
						iMonthIndex = iYearIndex - 1;
						month = parts[iMonthIndex];
						if (isNumericMonth(month) || (getAlphabeticMonth(month) >0))
						{
							isFoundMonth = true;
						}
					}
				}
				
				if (dateIsFull)
				{
					if (isFoundDay && isFoundMonth)
					{
						LocalDate date = getLocalDate(day, month, year);
						return date != null ?
								formatLocalDate(Objects.requireNonNull(getLocalDate(day, month, year)), format) :
								sInputDate;
					}
				} else { // short date
					if (isFoundMonth) {
						char formatDelimiter = detectDelimiter(format); // use delimiter from input format!
						return month + (formatDelimiter != 0 ? formatDelimiter : DEFAULT_DELIMITER) + year;
					} else { // found year only
						return sInputDate; // return input data AS IS
					}
				}
			}
		}
		return sInputDate; // return input data AS IS
	}
	
	/**
	 * Return index of a year in date parts array
	 * @param parts list parts of the date
	 * @param delimiter current delimiter uses in this date string
	 * @return -1 if not found
	 */
	private static int getYearIndex(final String[] parts, final char delimiter) {
		int iYearIndex = -1;
		//where is year?
		for (int i = 0; i < parts.length; i++)
		{
			final String part = parts[i];
			if (isValidLongYear(part))
			{
				return i;
			}
		}
		for (int i = 0; i < parts.length; i++)
		{
			final String part = parts[i];
			// Example: "67-03-02" or "04-06-67" - year is 67
			if (isShortYear(part) && !isNumericMonth(part) && !isNumericDay(part))
			{
				return i;
			}
		}
		// heuristic assumption by delimiter:
		// if delimiter is '-'  and year still does not detect - year is first
		// other delimiters - year is last
		if (delimiter == '-')
		{
			iYearIndex = 0;
		}
		else
		{
			iYearIndex = parts.length - 1;
		}
		return iYearIndex;
	}
	
	/**
	 * Try to found index of a day in date parts array
	 * @param iYearIndex except index of year - because it's already found
	 * @return -1 if not found
	 */
	private static int foundDayIndex(final String[] parts, final int iYearIndex)
	{
		for (int i = 0; i < parts.length; i++)
		{
			if (i == iYearIndex) break;
			final String part = parts[i];
			if (isNumericDay(part) && !isNumericMonth(part))
			{ // if we have day like 26 - obviously this cannot be the number of the month
				return i;
			}
		}
		return -1;
	}
	private static int foundAlphabeticalMonthIndex(final String[] parts)
	{
		for (int i = 0; i < parts.length; i++)
		{
			final String part = parts[i];
			if (getAlphabeticMonth(part) > 0)
			{
				return i; // index of part with alphabetical month
			}
		}
		return -1;
	}
	
	/**
	 * Extend day or month like 1 or 2 to '01' or '02' values
	 */
	private String[] extendOneCharNumbers(String[] parts)
	{
		try
		{
			for (int i = 0; i < parts.length; i++)
			{
				if (isSingleDigit(parts[i])) {
					parts[i] = String.format(FORMAT_TWO_DIGITS, Integer.parseInt(parts[i]));
				}
			}
		}
		catch(NumberFormatException e)
		{
			return parts;
		}
		return parts;
	}
	
	/**
	 * Detect which delimiter uses in this date string - slash '/', dot '.', minus '-' or space ' '
	 * @param str input date string
	 * @return delimiter
	 */
	public static char detectDelimiter(final String str)
	{
		if (null != str && !str.trim().isEmpty())
		{
			for (final char d : DELIMITERS)
			{
				if (checkDelimiter(str, d))
				{
					return d;
				}
			}
		}
		return 0;
	}
	
	/**
	 * Year in valid format with 4 characters like 1974
	 */
	public static boolean isValidLongYear(final String str)
	{
		return null != str && LONG_YEAR_PATTERN.matcher(str).matches();
	}
	
	public static boolean isShortYear(final String str)
	{
		return null != str && SHORT_YEAR_PATTERN.matcher(str).matches();
	}
	public static boolean isNumericMonth(final String str)
	{
		if (null != str && NUMERIC_MONTH_PATTERN.matcher(str).matches()) {
			try {
				final int intMonth = Integer.parseInt(str);
				if (intMonth > 0 && intMonth < 13) {
					return true;
				}
			} catch (NumberFormatException e) {
				return false;
			}
		}
		return false;
	}
	
	public static int getNumericMonth(final String str)
	{
		if (null != str && NUMERIC_MONTH_PATTERN.matcher(str).matches())
		{
			try
			{
				final int intMonth = Integer.parseInt(str);
				if (intMonth > 0 && intMonth < 13)
				{
					return intMonth;
				}
			}
			catch (NumberFormatException e)
			{
				return 0;
			}
		}
		return 0;
	}
	
	public static boolean isNumericDay(final String str)
	{
		if (null != str && NUMERIC_DAY_PATTERN.matcher(str).matches())
		{
			try
			{
				final int intMonth = Integer.parseInt(str);
				if (intMonth > 0 && intMonth < 32)
				{
					return true;
				}
			}
			catch (NumberFormatException e)
			{
				return false;
			}
		}
		return false;
	}
	public static int getAlphabeticMonth(final String str)
	{
		if (str == null || str.length() < 3)
		{
			return 0;
		}
		final String testSubString = str.toLowerCase();
		int iMonth = 0;
		boolean isFound = false;
		for (final String month : MONTHS)
		{
			iMonth++;
			if (month.toLowerCase().startsWith(testSubString))
			{
				isFound = true;
				break;
			}
		}
		return isFound ? iMonth : 0;
	}
	
	public static String getNumericMonthByIndex(final int index)
	{
		return (index > 0 && index <= MONTHS.length) ?
				String.format(FORMAT_TWO_DIGITS, getAlphabeticMonth(MONTHS[index - 1].substring(0,3))) : "";
	}
	
	public static boolean isSingleDigit(final String str)
	{
		return (str != null && (str.length() == 1 && Character.isDigit(str.charAt(0))));
	}
	private static boolean checkDelimiter(final String str, final char cDelimiter)
	{
		return (str != null && (str.indexOf(cDelimiter) != -1));
	}
	
	private static String convertToFullMonthFormat(final String inputMonth)
	{
		if (isSingleDigit(inputMonth))
		{
			try
			{
				return String.format(FORMAT_TWO_DIGITS, Integer.parseInt(inputMonth));
			}
			catch(NumberFormatException e)
			{
				return inputMonth;
			}
		}
		return inputMonth;
	}
	
	/**
	 * Convert year like 02 to 4 digits format like 2002
	 * @param inputYear year as string
	 * @return year as string
	 */
	private static String convertToFullYearFormat(final String inputYear)
	{
		if (null == inputYear || inputYear.length() != 2)
		{
			return inputYear;
		}
		Calendar ca = Calendar.getInstance();
		final int nCurrentYear = ca.get(Calendar.YEAR);
		final int nCurrentCentury = nCurrentYear / 100 + 1;
		try
		{
			final int nInputYear = (nCurrentCentury - 1) * 100 + Integer.parseInt(inputYear);
			int nFourDigitYear = nInputYear;
			if (nInputYear >= nCurrentYear)
			{
				nFourDigitYear = nInputYear - 100;
			}
			return String.format(FORMAT_FOUR_DIGITS, nFourDigitYear);
		} catch (NumberFormatException ex) {
			return inputYear;
		}
	}
	
	private LocalDate getLocalDate(final String day, final String month, final String year)
	{
		try
		{
			return LocalDate.of(Integer.parseInt(year), Integer.parseInt(month), Integer.parseInt(day));
		}
		catch(NumberFormatException e)
		{
			return null;
		}
	}
	public static String formatLocalDate(final LocalDate date, final String pattern)
	{
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern, Locale.US);
		return date.format(formatter);
	}
	
}

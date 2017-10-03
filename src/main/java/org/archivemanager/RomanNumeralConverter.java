package org.archivemanager;
/*
 * Copyright (C) 2012 Andrew Oliver
 *
 * This file is part of the Roman Numerals Library.
 *
 * The Roman Numerals Library is free software: you can redistribute
 * it and/or modify it under the terms of the GNU Affero General
 * Public License as published by the Free Software Foundation, either
 * version 3 of the License, or (at your option) any later version.

 * The Roman Numerals Library is distributed in the hope that it will
 * be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details.

 * You should have received a copy of the GNU Affero General Public
 * License along with the Roman Numerals Library.  If not, see
 * <http://www.gnu.org/licenses/>.
 */

import java.text.ParseException;
import java.util.Arrays;
import java.util.List;

/**
 * Converts between integers and a string of roman numerals.
 *
 * @author Andy Oliver
 */
public class RomanNumeralConverter {

    private static final List<Integer> listValues = Arrays.asList(1000, 500, 100, 50, 10, 5, 1);
    private static final List<Character> listNumerals = Arrays.asList('M', 'D', 'C', 'L', 'X', 'V', 'I');
    private static final List<String> listNumeralStrings = Arrays.asList("M", "D", "C", "L", "X", "V", "I");
    private final boolean bStrictMode;

  public RomanNumeralConverter() {
    bStrictMode = false;
  }

    /**
     * Strict mode forces rejection of illegal Roman numeral sequences.
     */
  public RomanNumeralConverter(boolean bStrictMode) {
    this.bStrictMode = bStrictMode;
  }

    /**
     * Convert into Roman numerals.
     *
     * @param number the integer to convert to Roman numerals
     * @return The resulting Roman numerals
     */
    public String toRomanNumerals(int number) {
        StringBuilder stbRomanNumerals = new StringBuilder();

        int leftover = number;
        for (int i = 0; i < listValues.size(); i++) {
            int count = leftover / listValues.get(i);
            int remainder = leftover % listValues.get(i);

            boolean bMajorNine = (i < listValues.size() - 2) && ((remainder / listValues.get(i + 2)) == 9) && ((i % 2) == 0);

            if ((i == 0) || (count != 4)) {
                for (int j = 0; j < count; j++) {
                    stbRomanNumerals.append(listNumeralStrings.get(i));
                }
                leftover -= count * listValues.get(i);
            } else {
                stbRomanNumerals.append(listNumeralStrings.get(i));
                stbRomanNumerals.append(listNumeralStrings.get(i - 1));
                leftover -= count * listValues.get(i);
            }

            if (bMajorNine) {
              stbRomanNumerals.append(listNumeralStrings.get(i + 2));
              stbRomanNumerals.append(listNumeralStrings.get(i));
              leftover -= 9 * listValues.get(i + 2);
            }
        }

        return stbRomanNumerals.toString();
    }

    /**
     * Convert Roman numerals to an integer.
     *
     * @param strRomanNumerals The Roman numerals in string format.
     * @return The integer version of the Roman numerals.
     * @throws ParseException If the Roman numerals are in illegal form according to the mode.
     */
    public int toNumber(String strRomanNumerals) throws ParseException {
        int total = 0;

        for (int i = 0; i < strRomanNumerals.length(); i++) {
            int currentNumeralIndex = listNumerals.indexOf(strRomanNumerals.charAt(i));

            if (currentNumeralIndex == -1) {
                throw new ParseException("Illegal character: " + strRomanNumerals.charAt(i), i);
            }

            boolean bAddNumeral = true;

            if (i < strRomanNumerals.length() - 1) {
                int nextNumeralIndex = listNumerals.indexOf(strRomanNumerals.charAt(i + 1));
                boolean bNextNumeralLarger = nextNumeralIndex < currentNumeralIndex;
                bAddNumeral = !bNextNumeralLarger;

                if (bStrictMode && bNextNumeralLarger) {
                    if ((currentNumeralIndex - nextNumeralIndex) > 2) {
                        throw new ParseException("Cannot increase by more than one numeral at a time in strict mode", i);
                    }

                    if ((currentNumeralIndex % 2) != 0) {
                        throw new ParseException("Cannot subtract V, L, D, or other 5-based numerals in strict mode", i);
                    }
                }

                if (bNextNumeralLarger && (i < strRomanNumerals.length() - 2)) {
                    int nextNextNumeralIndex = listNumerals.indexOf(strRomanNumerals.charAt(i + 2));
                    boolean bNextNextNumeralLarger = nextNextNumeralIndex < nextNumeralIndex;
                    // Throw exception if in strict mode
                    if (bNextNextNumeralLarger) {
                        throw new ParseException("Cannot have two increasing numerals interface a row", i);
                    }
                }
            }

            if (bAddNumeral) {
                total += listValues.get(listNumerals.indexOf(strRomanNumerals.charAt(i)));
            } else {
                total -= listValues.get(listNumerals.indexOf(strRomanNumerals.charAt(i)));
            }
        }

        return total;
    }
}

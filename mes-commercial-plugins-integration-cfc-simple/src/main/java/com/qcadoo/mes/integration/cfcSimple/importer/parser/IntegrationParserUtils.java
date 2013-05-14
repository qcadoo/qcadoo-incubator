/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo Framework
 * Version: 1.2.0
 *
 * This file is part of Qcadoo.
 *
 * Qcadoo is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation; either version 3 of the License,
 * or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 * ***************************************************************************
 */
package com.qcadoo.mes.integration.cfcSimple.importer.parser;

import java.math.BigDecimal;
import java.util.Date;

import org.joda.time.DateTime;

public final class IntegrationParserUtils {

    private IntegrationParserUtils() {

    }

    public static Date parseDateWithoutException(final String dateStr) {
        try {
            return parseDate(dateStr);
        } catch (IntegrationParserException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    public static Date parseDate(final String dateStr) throws IntegrationParserException {
        if (dateStr.length() != 14) {
            throw IntegrationParserException
                    .createParseError(IntegrationParseMessages.WRONG_DATE_FORMAT.getMessageKey(), dateStr);
        }
        try {
            int year = Integer.parseInt(dateStr.substring(0, 4));
            int month = Integer.parseInt(dateStr.substring(4, 6));
            int day = Integer.parseInt(dateStr.substring(6, 8));
            DateTime dateTime = new DateTime(year, month, day, 0, 0, 0, 0);
            return dateTime.toDate();
        } catch (NumberFormatException e) {
            throw IntegrationParserException.createParseError(e, IntegrationParseMessages.WRONG_DATE_FORMAT.getMessageKey(),
                    dateStr);
        }
    }

    public static int parseIntegerWithoutException(final String integerStr) {
        try {
            return parseInteger(integerStr);
        } catch (IntegrationParserException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    public static int parseInteger(final String integerStr) throws IntegrationParserException {
        try {
            return Integer.parseInt(integerStr);
        } catch (NumberFormatException e) {
            throw IntegrationParserException.createParseError(e, IntegrationParseMessages.WRONG_INTEGER_FORMAT.getMessageKey(),
                    integerStr);
        }
    }

    public static BigDecimal parseBigDecimalWithoutException(final String numberStr) {
        try {
            return parseBigDecimal(numberStr);
        } catch (IntegrationParserException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    public static BigDecimal parseBigDecimal(final String numberStr) throws IntegrationParserException {
        try {
            return new BigDecimal(numberStr);
        } catch (NumberFormatException e) {
            throw IntegrationParserException.createParseError(e, IntegrationParseMessages.WRONG_NUMBER_FORMAT.getMessageKey(),
                    numberStr);
        }
    }

    public static void checkRequiredParameter(final Object requiredParameter, final String name)
            throws IntegrationParserException {
        if (requiredParameter == null) {
            throw IntegrationParserException.createParseError(IntegrationParseMessages.PARAMETER_REQUIRED.getMessageKey(), name);
        }
    }

    public static void checkParameterLength(final String value, final String name, final int maxLength, final boolean notEmpty)
            throws IntegrationParserException {
        if (notEmpty && "".equals(value)) {
            throw IntegrationParserException.createParseError(IntegrationParseMessages.EMPTY_FIELD.getMessageKey(), name);
        }
        if (value.length() > maxLength) {
            throw IntegrationParserException.createParseError(IntegrationParseMessages.TOO_LONG_FIELD.getMessageKey(), name);
        }
    }

    public static void checkParametersNumber(final String[] parameters, final String name, final int length)
            throws IntegrationParserException {
        if (parameters.length != length) {
            throw IntegrationParserException.createParseError(IntegrationParseMessages.WRONG_PARAMETERS_NUMBER.getMessageKey(),
                    name, length, parameters.length);
        }
    }

}

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

public enum IntegrationParseMessages {
    IO_EXCEPTION("ioException", "file IO exception: {0}"), ENCODING_NOT_SUPPORTED("unsupportedEncoding",
            "unsupported encoding value - {0}"), HEADER_EXPECTED("headerExpected", "header expected"), HEADER_VALUE_EXPECTED(
            "headerValueExpected", "[{0}] header expected"), FILE_VERSION_NOT_SUPPORTED("fileVersionNotSupported",
            "file version is not supported (should be >= 1.05)"), PRODUCT_NOT_DEFINED("productNotDefined",
            "product '{0}' is not defined"), UNKNOWN_PRODUCT_TYPE("unknownProductType", "unknown product type '{0}'"), CHARACTER_EXPECTED(
            "characterExpected", "character [{0}] - '{1}' expected"), CHARACTERS_EXPECTED("charactersExpected",
            "character [{0}] - '{1}' or '{2}' expected"), UNEXPECTED_END_OF_LINE("unexpectedEndOfLine", "unexpected end of line"), UNEXPECTED_END_OF_FILE(
            "unexpectedEndOfFile", "unexpected end of file"), WRONG_DATE_FORMAT("wrongDateFormat",
            "wrong date field format '{0}'"), WRONG_INTEGER_FORMAT("wrongIntegerFormat", "wrong integer field format '{0}'"), WRONG_NUMBER_FORMAT(
            "wrongNumberFormat", "wrong number field format '{0}'"), EMPTY_FIELD("emptyField", "field '{0}' is empty"), TOO_LONG_FIELD(
            "tooLongField", "field '{0}' too long"), WRONG_PARAMETERS_NUMBER("wrongParametersNumber",
            "'{0}' definition should have {1} parameters instead of {2}"), PARAMETER_REQUIRED("parameterRequired", "Parameter not found: {0}");

    private final String message;

    private final String messageKey;

    private IntegrationParseMessages(final String messageKey, final String message) {
        this.messageKey = messageKey;
        this.message = message;
    }

    public String getMessageKey() {
        return messageKey;
    }

    public String getMessage() {
        return message;
    }
}

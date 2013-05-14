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

import java.util.Arrays;

public final class IntegrationParserException extends Exception {

    private static final long serialVersionUID = 5884036428089718209L;

    private Object[] args;

    private Integer line;

    private String message;

    private IntegrationParserException(final String message, final Exception cause, final Integer line, final Object... args) {
        super(message, cause);
        this.message = message;
        this.line = line;
        this.args = args;
    }

    @Override
    public String getMessage() {
        StringBuilder builder = new StringBuilder("File parse error");
        if (line != null) {
            builder.append(" [");
            builder.append(line);
            builder.append("]");
        }
        builder.append(": ");
        String innerMessage = message;
        for (int i = 0; i < args.length; i++) {
            innerMessage = innerMessage.replaceAll("\\{" + i + "\\}", args[i].toString());
        }
        builder.append(innerMessage);
        return builder.toString();
    }

    public String getMessageKey() {
        return message;
    }

    public Object[] getMessageArgs() {
        return Arrays.copyOf(args, args.length);
    }

    public Integer getLine() {
        return line;
    }

    public static IntegrationParserException createParseError(final String message, final Object... args) {
        return createParseError(null, message, args);
    }

    public static IntegrationParserException createParseError(final Exception cause, final String message, final Object... args) {
        return new IntegrationParserException(message, cause, null, args);
    }

    public static IntegrationParserException createParseErrorOnLine(final int line, final Exception cause, final String message,
            final Object... args) {
        return new IntegrationParserException(message, cause, line, args);
    }

}

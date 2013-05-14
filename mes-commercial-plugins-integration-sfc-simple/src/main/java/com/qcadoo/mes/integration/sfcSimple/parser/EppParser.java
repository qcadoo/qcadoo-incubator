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
package com.qcadoo.mes.integration.sfcSimple.parser;

import static com.qcadoo.mes.integration.sfcSimple.constants.SfcSimpleConstants.FIELD_TYPE;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.apache.poi.util.IOUtils;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.integration.cfcSimple.importer.model.ParsedOrder;
import com.qcadoo.mes.integration.cfcSimple.importer.model.ParsedOrderItem;
import com.qcadoo.mes.integration.cfcSimple.importer.model.ParsedProduct;
import com.qcadoo.mes.integration.cfcSimple.importer.parser.IntegrationParseMessages;
import com.qcadoo.mes.integration.cfcSimple.importer.parser.IntegrationParser;
import com.qcadoo.mes.integration.cfcSimple.importer.parser.IntegrationParserException;
import com.qcadoo.mes.integration.cfcSimple.importer.parser.IntegrationParserResult;
import com.qcadoo.mes.integration.cfcSimple.importer.parser.IntegrationParserUtils;

@Service
public class EppParser implements IntegrationParser {

    private enum ParserState {
        INIT, AFTER_INFO_HEADER, EXPECT_HEADER_NAGLOWEK, EXPECT_HEADER_NAGLOWEK_CONTENT, EXPECT_HEADER_ZAWARTOSC, EXPECT_HEADER_ZAWARTOSC_CONTENT
    };

    private enum ParserContentType {
        IGNORE, ORDER, PRODUCT
    };

    private enum LineParserState {
        BEFORE_ARGUMENT, READING_ARGUMENT, READING_COMA_ARGUMENT, EXPECTING_END_COMA_ARGUMENT, END_ARGUMENT
    };

    public enum ProductType {
        ARTICLE, SERVICE, PACK, SET
    };

    public IntegrationParserResult parse(final InputStream stream) throws IntegrationParserException {
        int currentEncoding = 1250;

        byte[] bytes = getByteArray(stream);
        BufferedReader reader = createReader(bytes, currentEncoding);

        String streamLine;
        ParserState currentState = ParserState.INIT;
        ParserContentType contentType = ParserContentType.IGNORE;

        IntegrationParserResult result = new IntegrationParserResult();
        ParsedOrder currentOrder = new ParsedOrder();

        int lineNumber = 0;
        try {
            while ((streamLine = reader.readLine()) != null) {

                lineNumber++;
                streamLine = streamLine.trim();
                if ("".equals(streamLine)) {
                    continue;
                }

                switch (currentState) {

                    case INIT:
                        checkHeader(streamLine, "INFO");
                        currentState = ParserState.AFTER_INFO_HEADER;
                        break;

                    case AFTER_INFO_HEADER:
                        int encoding = validateInfoLine(streamLine);
                        if (encoding == currentEncoding) {
                            currentState = ParserState.EXPECT_HEADER_NAGLOWEK;
                        } else {
                            currentEncoding = encoding;
                            reader = createReader(bytes, currentEncoding);
                            currentState = ParserState.INIT;
                        }
                        break;

                    case EXPECT_HEADER_NAGLOWEK:
                        checkHeader(streamLine, "NAGLOWEK");
                        currentState = ParserState.EXPECT_HEADER_NAGLOWEK_CONTENT;
                        break;

                    case EXPECT_HEADER_NAGLOWEK_CONTENT:
                        String[] infoLine = readDataLine(streamLine);
                        if (infoLine.length == 1 && "TOWARY".equals(infoLine[0])) {
                            contentType = ParserContentType.PRODUCT;
                        } else if (infoLine.length > 1 && "ZK".equals(infoLine[0])) {
                            currentOrder = parseOrder(streamLine);
                            result.addParsedOrder(currentOrder);
                            contentType = ParserContentType.ORDER;
                        } else {
                            contentType = ParserContentType.IGNORE;
                        }
                        currentState = ParserState.EXPECT_HEADER_ZAWARTOSC;
                        break;

                    case EXPECT_HEADER_ZAWARTOSC:
                        checkHeader(streamLine, "ZAWARTOSC");
                        currentState = ParserState.EXPECT_HEADER_ZAWARTOSC_CONTENT;
                        break;

                    case EXPECT_HEADER_ZAWARTOSC_CONTENT:
                        if (isHeader(streamLine)) {
                            checkHeader(streamLine, "NAGLOWEK");
                            currentState = ParserState.EXPECT_HEADER_NAGLOWEK_CONTENT;
                        } else {
                            if (ParserContentType.PRODUCT.equals(contentType)) {
                                result.addParsedProduct(parseProduct(streamLine));
                            } else if (ParserContentType.ORDER.equals(contentType)) {
                                currentOrder.addItem(parseOrderItem(streamLine));
                            }
                        }
                        break;
                    default:
                        throw new IllegalStateException("Incorrect state");
                }
            }

            if (!ParserState.EXPECT_HEADER_NAGLOWEK.equals(currentState)
                    && !ParserState.EXPECT_HEADER_ZAWARTOSC_CONTENT.equals(currentState)) {
                throw IntegrationParserException
                        .createParseError(IntegrationParseMessages.UNEXPECTED_END_OF_FILE.getMessageKey());
            }

            stream.close();

            performFinalValidation(result);
        } catch (IntegrationParserException e) {
            throw IntegrationParserException.createParseErrorOnLine(lineNumber, e, e.getMessageKey(), e.getMessageArgs());
        } catch (IOException e) {
            throw IntegrationParserException.createParseError(e, IntegrationParseMessages.IO_EXCEPTION.getMessageKey(),
                    e.getMessage());
        }

        return result;
    }

    private byte[] getByteArray(final InputStream stream) throws IntegrationParserException {
        try {
            return IOUtils.toByteArray(stream);
        } catch (IOException e) {
            throw IntegrationParserException.createParseError(e, IntegrationParseMessages.IO_EXCEPTION.getMessageKey(),
                    e.getMessage());
        }
    }

    private BufferedReader createReader(final byte[] bytes, final int encoding) throws IntegrationParserException {
        String encodingName = null;
        if (encoding == 1250) {
            encodingName = "windows-1250";
        } else if (encoding == 852) {
            encodingName = "ISO-8859-2";
        } else {
            throw IntegrationParserException.createParseError(IntegrationParseMessages.ENCODING_NOT_SUPPORTED.getMessageKey(),
                    encoding);
        }
        try {
            return new BufferedReader(new InputStreamReader(new ByteArrayInputStream(bytes), encodingName));
        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException("Epp file IO exception: UnsupportedEncodingException", e);
        }
    }

    private void checkHeader(final String line, final String expectedheader) throws IntegrationParserException {
        if (!expectedheader.equals(readHeader(line))) {
            throw IntegrationParserException.createParseError(IntegrationParseMessages.HEADER_VALUE_EXPECTED.getMessageKey(),
                    expectedheader);
        }
    }

    private boolean isHeader(final String line) {
        return line.charAt(0) == '[' && line.charAt(line.length() - 1) == ']';
    }

    private String readHeader(final String line) throws IntegrationParserException {
        if (line.charAt(0) != '[' || line.charAt(line.length() - 1) != ']') {
            throw IntegrationParserException.createParseError(IntegrationParseMessages.HEADER_EXPECTED.getMessageKey());
        }
        return line.substring(1, line.length() - 1);
    }

    private int validateInfoLine(final String line) throws IntegrationParserException {
        String[] infoLine = readDataLine(line);
        IntegrationParserUtils.checkParametersNumber(infoLine, "info", 24);
        if (IntegrationParserUtils.parseBigDecimal(infoLine[0]).compareTo(new BigDecimal("1.05")) < 0) {
            throw IntegrationParserException
                    .createParseError(IntegrationParseMessages.FILE_VERSION_NOT_SUPPORTED.getMessageKey());
        }
        return IntegrationParserUtils.parseInteger(infoLine[2]);
    }

    private void performFinalValidation(final IntegrationParserResult parseResult) throws IntegrationParserException {
        Set<String> definedProducts = new HashSet<String>();
        for (ParsedProduct product : parseResult.getProducts()) {
            definedProducts.add(product.getIdentificationCode());
        }
        for (ParsedOrder order : parseResult.getOrders()) {
            for (ParsedOrderItem item : order.getItems()) {
                if (!definedProducts.contains(item.getProductIdentificationCode())) {
                    throw IntegrationParserException.createParseError(
                            IntegrationParseMessages.PRODUCT_NOT_DEFINED.getMessageKey(), item.getProductIdentificationCode());
                }
            }
        }
    }

    private ParsedProduct parseProduct(final String line) throws IntegrationParserException {
        String[] infoLine = readDataLine(line);

        IntegrationParserUtils.checkParametersNumber(infoLine, "product", 42);
        IntegrationParserUtils.checkParameterLength(infoLine[0], "product type", 1, true);
        IntegrationParserUtils.checkParameterLength(infoLine[1], "product identification code", 20, true);
        IntegrationParserUtils.checkParameterLength(infoLine[3], "product ean", 50, false);
        IntegrationParserUtils.checkParameterLength(infoLine[4], "product name", 100, false);
        IntegrationParserUtils.checkParameterLength(infoLine[5], "product description", 255, false);
        IntegrationParserUtils.checkParameterLength(infoLine[9], "product unit", 50, true);

        ParsedProduct product = new ParsedProduct();

        int integerProductType = IntegrationParserUtils.parseInteger(infoLine[0]);
        switch (integerProductType) {
            case 1:
                product.setField(FIELD_TYPE, ProductType.ARTICLE.toString());
                break;
            case 2:
                product.setField(FIELD_TYPE, ProductType.SERVICE.toString());
                break;
            case 4:
                product.setField(FIELD_TYPE, ProductType.PACK.toString());
                break;
            case 8:
                product.setField(FIELD_TYPE, ProductType.SET.toString());
                break;
            default:
                throw IntegrationParserException.createParseError(IntegrationParseMessages.UNKNOWN_PRODUCT_TYPE.getMessageKey(),
                        integerProductType);
        }
        product.setIdentificationCode(infoLine[1]);
        product.setField("ean", infoLine[3]);
        product.setField("name", infoLine[4]);
        product.setField("description", infoLine[5]);
        product.setField("unit", infoLine[9]);
        return product;
    }

    private ParsedOrder parseOrder(final String line) throws IntegrationParserException {
        String[] infoLine = readDataLine(line);

        IntegrationParserUtils.checkParametersNumber(infoLine, "order", 62);
        IntegrationParserUtils.checkParameterLength(infoLine[6], "order number", 30, true);
        IntegrationParserUtils.checkParameterLength(infoLine[12], "order client name", 50, false);
        IntegrationParserUtils.checkParameterLength(infoLine[14], "order client city", 50, false);
        IntegrationParserUtils.checkParameterLength(infoLine[15], "order client post address", 50, false);
        IntegrationParserUtils.checkParameterLength(infoLine[16], "order client address", 100, false);
        IntegrationParserUtils.checkParameterLength(infoLine[17], "order client NIP number", 50, false);

        ParsedOrder order = new ParsedOrder();
        order.setField("number", "ZK " + infoLine[6]);
        order.setField("clientName", infoLine[12]);
        order.setField("clientAddress", infoLine[16] + ",\n" + infoLine[15] + " " + infoLine[14] + ",\n" + infoLine[17]);
        IntegrationParserUtils.parseDate(infoLine[21]);
        IntegrationParserUtils.parseDate(infoLine[23]);
        order.setField("drawDate", infoLine[21]);
        order.setField("realizationDate", infoLine[23]);
        return order;
    }

    private ParsedOrderItem parseOrderItem(final String line) throws IntegrationParserException {
        String[] infoLine = readDataLine(line);

        IntegrationParserUtils.checkParametersNumber(infoLine, "order item", 22);
        IntegrationParserUtils.checkParameterLength(infoLine[2], "order item product identification code", 20, true);

        ParsedOrderItem orderItem = new ParsedOrderItem();
        orderItem.setProductIdentificationCode(infoLine[2]);
        IntegrationParserUtils.parseInteger(infoLine[0]);
        IntegrationParserUtils.parseBigDecimal(infoLine[10]);
        orderItem.setField("number", infoLine[0]);
        orderItem.setField("quantity", infoLine[10]);
        return orderItem;
    }

    private String[] readDataLine(final String line) throws IntegrationParserException {
        StringBuilder currentWord = new StringBuilder();
        List<String> words = new LinkedList<String>();
        LineParserState state = LineParserState.BEFORE_ARGUMENT;
        int charNumber = 1;
        for (char c : line.toCharArray()) {
            switch (state) {
                case BEFORE_ARGUMENT:
                    if (c == ',') {
                        words.add(currentWord.toString());
                        currentWord = new StringBuilder();
                    } else if (c == '"') {
                        state = LineParserState.READING_COMA_ARGUMENT;
                    } else if (!(c == ' ')) {
                        currentWord.append(c);
                        state = LineParserState.READING_ARGUMENT;
                    }
                    break;
                case READING_ARGUMENT:
                    if (c == ',') {
                        state = LineParserState.BEFORE_ARGUMENT;
                        words.add(currentWord.toString());
                        currentWord = new StringBuilder();
                    } else if (c == ' ') {
                        state = LineParserState.END_ARGUMENT;
                    } else {
                        currentWord.append(c);
                    }
                    break;
                case READING_COMA_ARGUMENT:
                    if (c == '"') {
                        state = LineParserState.EXPECTING_END_COMA_ARGUMENT;
                    } else {
                        currentWord.append(c);
                    }
                    break;
                case EXPECTING_END_COMA_ARGUMENT:
                    if (c == ',') {
                        words.add(currentWord.toString());
                        currentWord = new StringBuilder();
                        state = LineParserState.BEFORE_ARGUMENT;
                    } else if (c == '"') {
                        currentWord.append('"');
                        state = LineParserState.READING_COMA_ARGUMENT;
                    } else if (c == ' ') {
                        state = LineParserState.EXPECTING_END_COMA_ARGUMENT;
                    } else {
                        throw IntegrationParserException.createParseError(
                                IntegrationParseMessages.CHARACTERS_EXPECTED.getMessageKey(), charNumber, ",", "\"");
                    }
                    break;
                case END_ARGUMENT:
                    if (c == ',') {
                        words.add(currentWord.toString());
                        currentWord = new StringBuilder();
                        state = LineParserState.BEFORE_ARGUMENT;
                    } else if (!(c == ' ')) {
                        throw IntegrationParserException.createParseError(
                                IntegrationParseMessages.CHARACTER_EXPECTED.getMessageKey(), charNumber, ",");
                    }
                    break;
                default:
            }
            charNumber++;
        }
        if (state == LineParserState.READING_COMA_ARGUMENT) {
            throw IntegrationParserException.createParseError(IntegrationParseMessages.UNEXPECTED_END_OF_LINE.getMessageKey());
        }
        words.add(currentWord.toString());
        return words.toArray(new String[words.size()]);
    }

}

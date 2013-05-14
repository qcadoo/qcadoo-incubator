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
package com.qcadoo.mes.integration.efcSimple.parser;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.jdom.Attribute;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.Namespace;
import org.jdom.input.SAXBuilder;

import com.google.common.base.Charsets;
import com.qcadoo.mes.integration.cfcSimple.importer.model.ParsedOrder;
import com.qcadoo.mes.integration.cfcSimple.importer.model.ParsedOrderItem;
import com.qcadoo.mes.integration.cfcSimple.importer.model.ParsedProduct;
import com.qcadoo.mes.integration.cfcSimple.importer.parser.IntegrationParseMessages;
import com.qcadoo.mes.integration.cfcSimple.importer.parser.IntegrationParser;
import com.qcadoo.mes.integration.cfcSimple.importer.parser.IntegrationParserException;
import com.qcadoo.mes.integration.cfcSimple.importer.parser.IntegrationParserResult;
import com.qcadoo.mes.integration.cfcSimple.importer.parser.IntegrationParserUtils;

public class EnovaParser implements IntegrationParser {

    private static final String TYPE_L = "type";

    private static final String UNCHECKED = "unchecked";

    private static final Namespace DEFAULT_NAMESPACE = Namespace.getNamespace("http://www.soneta.pl/schema/business");

    public enum ProductType {
        PRODUCT, SERVICE, GOOD, COMPONENT;
    };

    public enum StateType {
        UNCONFIRMED, CONFIRMED, APPROVED;
    }

    private enum Nodes {
        ORDER("DokumentHandlowy"), PRODUCT("Towar"), UNIT("Jednostka"), ORDER_ID("id"), ORDER_NR("Numer"), ORDER_NR_FULL("Pelny"), ORDER_STATE(
                "Stan"), PRODUCT_GUID("guid"), PRODUCT_CODE("Kod"), PRODUCT_TYPE("Typ"), PRODUCT_NAME("Nazwa"), PRODUCT_EAN("EAN"), PRODUCT_UNIT(
                "Jednostka"), UNIT_GUID("guid"), UNIT_CODE("Kod"), ORDER_ITEMS("Pozycje"), ORDER_ITEM("PozycjaDokHandlowego"), PRODUCT_QUANTITY(
                "IloscZasobuValue"), PRODUCT_IDENT("Ident");

        private Nodes(final String name) {
            this.name = name;
        }

        private String name;

        public String getName() {
            return name;
        }

    }

    @Override
    public IntegrationParserResult parse(final InputStream stream) throws IntegrationParserException {

        Map<String, String> productGuidToCodesMap = new HashMap<String, String>();

        IntegrationParserResult result = new IntegrationParserResult();
        try {
            InputStream decodedStream = decodeInputStream(stream);

            SAXBuilder builder = new SAXBuilder();
            Document document = builder.build(decodedStream);
            Element root = document.getRootElement();

            @SuppressWarnings(UNCHECKED)
            List<Element> orders = root.getChildren(Nodes.ORDER.getName(), DEFAULT_NAMESPACE);

            @SuppressWarnings(UNCHECKED)
            List<Element> products = root.getChildren(Nodes.PRODUCT.getName(), DEFAULT_NAMESPACE);

            @SuppressWarnings(UNCHECKED)
            List<Element> units = root.getChildren(Nodes.UNIT.getName(), DEFAULT_NAMESPACE);

            if (orders.isEmpty() && products.isEmpty()) {
                throw IntegrationParserException
                        .createParseError(EnovaIntegrationParseMessages.NO_DATA_TO_IMPORT.getMessageKey());
            }

            if (units.isEmpty()) {
                throw IntegrationParserException.createParseError(EnovaIntegrationParseMessages.NO_UNITS.getMessageKey());
            }

            for (Element element : products) {
                result.addParsedProduct(parseProduct(element, units, productGuidToCodesMap));
            }

            for (Element order : orders) {
                ParsedOrder parsedOrder = parseOrder(order);

                @SuppressWarnings(UNCHECKED)
                List<Element> orderProducts = order.getChild(Nodes.ORDER_ITEMS.getName(), DEFAULT_NAMESPACE).getChildren(
                        Nodes.ORDER_ITEM.getName(), DEFAULT_NAMESPACE);

                for (Element orderProduct : orderProducts) {
                    if (checkIfProductWasDefined(orderProduct, products)) {
                        parsedOrder.addItem(getOrderItem(orderProduct, productGuidToCodesMap));
                    }
                }

                result.addParsedOrder(parsedOrder);
            }

            performFinalValidation(result);

        } catch (JDOMException e) {
            throw IntegrationParserException.createParseError(e, EnovaIntegrationParseMessages.JDOM_EXCEPTION.getMessageKey(),
                    e.getMessage());
        } catch (IOException e) {
            throw IntegrationParserException.createParseError(e, IntegrationParseMessages.IO_EXCEPTION.getMessageKey(),
                    e.getMessage());
        }

        return result;
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

    private InputStream decodeInputStream(final InputStream stream) throws IOException {
        Reader reader = new InputStreamReader(stream, Charsets.UTF_16);
        StringWriter writer = new StringWriter();
        IOUtils.copy(reader, writer);
        String xml = writer.toString();

        return new ByteArrayInputStream(xml.getBytes(Charsets.UTF_16));
    }

    private boolean checkIfProductWasDefined(final Element orderProduct, final List<Element> products) {
        for (Element product : products) {
            if (product.getAttribute(Nodes.PRODUCT_GUID.getName()).getValue()
                    .equals(orderProduct.getChild(Nodes.PRODUCT.getName(), DEFAULT_NAMESPACE).getValue())) {
                return true;
            }
        }
        return false;
    }

    private ParsedOrderItem getOrderItem(final Element orderProduct, final Map<String, String> productGuidToCodesMap)
            throws IntegrationParserException {

        IntegrationParserUtils.checkRequiredParameter(orderProduct.getChild(Nodes.PRODUCT.getName(), DEFAULT_NAMESPACE), "towar");
        IntegrationParserUtils.checkRequiredParameter(orderProduct.getChild(Nodes.PRODUCT_QUANTITY.getName(), DEFAULT_NAMESPACE),
                "IloscZasobuValue");
        IntegrationParserUtils.checkRequiredParameter(orderProduct.getChild(Nodes.PRODUCT_IDENT.getName(), DEFAULT_NAMESPACE),
                "Ident");

        ParsedOrderItem orderItem = new ParsedOrderItem();

        String productGuid = orderProduct.getChild("Towar", DEFAULT_NAMESPACE).getValue();

        orderItem.setProductIdentificationCode(productGuidToCodesMap.get(productGuid));
        orderItem.setField("quantity", orderProduct.getChild(Nodes.PRODUCT_QUANTITY.getName(), DEFAULT_NAMESPACE).getValue());
        orderItem.setField("number", orderProduct.getChild(Nodes.PRODUCT_IDENT.getName(), DEFAULT_NAMESPACE).getValue());
        return orderItem;
    }

    private ParsedOrder parseOrder(final Element order) throws IntegrationParserException {
        Attribute id = order.getAttribute(Nodes.ORDER_ID.getName());
        Element drawDate = order.getChild("Data", DEFAULT_NAMESPACE);
        Element shipmentDate = order.getChild("DataOperacji", DEFAULT_NAMESPACE);
        Element orderNr = order.getChild(Nodes.ORDER_NR.getName(), DEFAULT_NAMESPACE);
        Element fullOrderNr = order.getChild(Nodes.ORDER_NR.getName(), DEFAULT_NAMESPACE).getChild(Nodes.ORDER_NR_FULL.getName(),
                DEFAULT_NAMESPACE);
        Element orderState = order.getChild(Nodes.ORDER_STATE.getName(), DEFAULT_NAMESPACE);

        IntegrationParserUtils.checkRequiredParameter(id, "Product");
        IntegrationParserUtils.checkRequiredParameter(drawDate, "Date");
        IntegrationParserUtils.checkRequiredParameter(shipmentDate, "Shipment date");
        IntegrationParserUtils.checkRequiredParameter(orderNr, "Order number");
        IntegrationParserUtils.checkRequiredParameter(fullOrderNr, "Full order number");
        IntegrationParserUtils.checkRequiredParameter(orderState, "Order state");

        ParsedOrder parsedOrder = new ParsedOrder();
        parsedOrder.setField("name", id.getValue());
        parsedOrder.setField("drawDate", drawDate.getValue());
        parsedOrder.setField("realizationDate", shipmentDate.getValue());
        parsedOrder.setField("clientName", getClientName(getContractorDetails(order)));
        parsedOrder.setField("clientAddress", getClientAddress(getContractorDetails(order)));
        parsedOrder.setField("number", fullOrderNr.getValue());

        if ("potwierdzone".equals(order.getChild(Nodes.ORDER_STATE.getName(), DEFAULT_NAMESPACE).getValue()
                .toLowerCase(Locale.getDefault()))) {
            parsedOrder.setField("state", StateType.CONFIRMED.toString());
        } else if ("zatwierdzony".equals(order.getChild(Nodes.ORDER_STATE.getName(), DEFAULT_NAMESPACE).getValue()
                .toLowerCase(Locale.getDefault()))) {
            parsedOrder.setField("state", StateType.APPROVED.toString());
        } else {
            parsedOrder.setField("state", StateType.UNCONFIRMED.toString());
        }
        return parsedOrder;
    }

    private String getClientAddress(final Element contractor) throws IntegrationParserException {
        if (contractor != null) {
            Element addressElement = contractor.getChild("Adres", DEFAULT_NAMESPACE);

            IntegrationParserUtils.checkRequiredParameter(addressElement, "Address");

            Element street = addressElement.getChild("Ulica", DEFAULT_NAMESPACE);
            IntegrationParserUtils.checkRequiredParameter(street, "Street");

            Element houseNumber = addressElement.getChild("NrDomu", DEFAULT_NAMESPACE);
            IntegrationParserUtils.checkRequiredParameter(houseNumber, "House number");

            Element postalCode = addressElement.getChild("KodPocztowy", DEFAULT_NAMESPACE);
            IntegrationParserUtils.checkRequiredParameter(postalCode, "Postal code");

            Element town = addressElement.getChild("Miejscowosc", DEFAULT_NAMESPACE);
            IntegrationParserUtils.checkRequiredParameter(town, "Town");

            return street.getValue() + " " + houseNumber.getValue() + " "
                    + addressElement.getChild("NrLokalu", DEFAULT_NAMESPACE).getValue() + "\n"
                    + postalCode.getValue().substring(0, 2) + "-" + postalCode.getValue().substring(2) + " " + town.getValue();
        }
        return "";
    }

    private String getClientName(final Element contractor) throws IntegrationParserException {
        if (contractor != null) {
            IntegrationParserUtils.checkRequiredParameter(contractor.getChild("Nazwa", DEFAULT_NAMESPACE), "Contractor name");
            return contractor.getChild("Nazwa", DEFAULT_NAMESPACE).getValue();
        }
        return "";
    }

    private Element getContractorDetails(final Element order) {
        Element contractorSection = order.getChild("Dane_kontrahentów", DEFAULT_NAMESPACE);
        @SuppressWarnings(UNCHECKED)
        List<Element> contractors = contractorSection.getChildren();

        for (Element contractor : contractors) {
            if ("DaneKontrahenta".equals(contractor.getName())
                    && "0".equals(contractor.getChild("Typ", DEFAULT_NAMESPACE).getValue())) {
                return contractor;
            }
        }
        return null;
    }

    private ParsedProduct parseProduct(final Element element, final List<Element> units,
            final Map<String, String> productGuidToCodesMap) throws IntegrationParserException {
        ParsedProduct product = new ParsedProduct();

        Attribute productGuid = element.getAttribute(Nodes.PRODUCT_GUID.getName());
        IntegrationParserUtils.checkRequiredParameter(productGuid, "Product guid not present");

        Element code = element.getChild(Nodes.PRODUCT_CODE.getName(), DEFAULT_NAMESPACE);
        IntegrationParserUtils.checkRequiredParameter(code, "Product code not present");
        productGuidToCodesMap.put(productGuid.getValue(), code.getValue());
        product.setIdentificationCode(code.getValue());

        Element type = element.getChild(Nodes.PRODUCT_TYPE.getName(), DEFAULT_NAMESPACE);
        IntegrationParserUtils.checkRequiredParameter(type, "Type not present");

        if ("Produkt".equals(type.getValue())) {
            product.setField(TYPE_L, ProductType.PRODUCT.toString());
        } else if ("Usługa".equals(type.getValue())) {
            product.setField(TYPE_L, ProductType.SERVICE.toString());
        } else if ("Towar".equals(type.getValue())) {
            product.setField(TYPE_L, ProductType.GOOD.toString());
        } else if ("Składnik".equals(type.getValue())) {
            product.setField(TYPE_L, ProductType.COMPONENT.toString());
        }

        Element productName = element.getChild(Nodes.PRODUCT_NAME.getName(), DEFAULT_NAMESPACE);
        IntegrationParserUtils.checkRequiredParameter(productName, "Product name not present");
        product.setField("name", productName.getValue());

        Element ean = element.getChild(Nodes.PRODUCT_EAN.getName(), DEFAULT_NAMESPACE);
        IntegrationParserUtils.checkRequiredParameter(productName, "Product name not present");
        product.setField("ean", ean.getValue());

        Element unit = element.getChild(Nodes.PRODUCT_UNIT.getName(), DEFAULT_NAMESPACE);
        IntegrationParserUtils.checkRequiredParameter(productName, "Unit name not present");
        product.setField("unit", getUnitForUnitGuidOrId(units, unit.getValue()));

        return product;
    }

    private String getUnitForUnitGuidOrId(final List<Element> units, final String unit) throws IntegrationParserException {
        for (Element element : units) {
            IntegrationParserUtils.checkRequiredParameter(element.getAttribute(Nodes.UNIT_GUID.getName()),
                    "Guid for unit not present");
            IntegrationParserUtils.checkRequiredParameter(element.getChild(Nodes.UNIT_CODE.getName(), DEFAULT_NAMESPACE),
                    "Unit code not present");

            if (unit.equals(element.getAttribute(Nodes.UNIT_GUID.getName()).getValue())
                    || unit.equals(element.getAttribute("id").getValue())) {
                return element.getChild(Nodes.UNIT_CODE.getName(), DEFAULT_NAMESPACE).getValue();
            }
        }
        return null;
    }
}

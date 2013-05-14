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

import java.util.LinkedList;
import java.util.List;

import com.qcadoo.mes.integration.cfcSimple.importer.model.ParsedOrder;
import com.qcadoo.mes.integration.cfcSimple.importer.model.ParsedProduct;

public class IntegrationParserResult {

    private final List<ParsedOrder> orders = new LinkedList<ParsedOrder>();

    private final List<ParsedProduct> products = new LinkedList<ParsedProduct>();

    public void addParsedOrder(final ParsedOrder order) {
        orders.add(order);
    }

    public void addParsedProduct(final ParsedProduct product) {
        products.add(product);
    }

    public List<ParsedOrder> getOrders() {
        return orders;
    }

    public List<ParsedProduct> getProducts() {
        return products;
    }
}

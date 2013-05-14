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
package com.qcadoo.mes.integration.cfcSimple.importer;

import com.qcadoo.mes.integration.cfcSimple.importer.parser.IntegrationParserException;

public class DataImportResult {

    private int importedProductsNumber;

    private int importedOrdersNumber;

    private int updatedProductsNumber;

    private int updatedOrdersNumber;

    private IntegrationParserException parserException;

    public DataImportResult parseExceptionOccured(final IntegrationParserException parserException) {
        this.parserException = parserException;
        return this;
    }

    public DataImportResult importedProduct() {
        importedProductsNumber++;
        return this;
    }

    public DataImportResult updatedProduct() {
        updatedProductsNumber++;
        return this;
    }

    public DataImportResult importedOrder() {
        importedOrdersNumber++;
        return this;
    }

    public DataImportResult updatedOrder() {
        updatedOrdersNumber++;
        return this;
    }

    public int getImportedProductsNumber() {
        return importedProductsNumber;
    }

    public int getImportedOrdersNumber() {
        return importedOrdersNumber;
    }

    public int getUpdatedProductsNumber() {
        return updatedProductsNumber;
    }

    public int getUpdatedOrdersNumber() {
        return updatedOrdersNumber;
    }

    public IntegrationParserException getParserException() {
        return parserException;
    }

    public boolean isError() {
        return parserException != null;
    }
}

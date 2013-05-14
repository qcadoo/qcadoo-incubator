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
package com.qcadoo.mes.integration.cfcSimple.converter;

import com.qcadoo.mes.integration.cfcSimple.converter.internal.DataConverterAddProductResult;
import com.qcadoo.model.api.Entity;

public class DataConverterResult {

    public static enum Error {
        ORDER_EXIST_AND_DIFFER
    }

    private Error error;

    private Entity errorOrder;

    private int addedUnitsNumber;

    private int addedProductsNumber;

    private int updatedProductsNumber;

    private int addedOrdersNumber;

    public DataConverterResult addedProduct(final DataConverterAddProductResult result) {
        if (!result.hasError()) {
            if (result.getUnitEntity() != null) {
                addedUnitsNumber++;
            }
            if (DataConverterAddProductResult.Action.PRODUCT_ADDED.equals(result.getAction())) {
                addedProductsNumber++;
            } else if (DataConverterAddProductResult.Action.PRODUCT_UPDATED.equals(result.getAction())) {
                updatedProductsNumber++;
            }
        }
        return this;
    }

    public DataConverterResult addedOrder() {
        addedOrdersNumber++;
        return this;
    }

    public DataConverterResult orderExistsAndDiffer(final Entity errorOrder) {
        error = Error.ORDER_EXIST_AND_DIFFER;
        this.errorOrder = errorOrder;
        return this;
    }

    public int getAddedUnitsNumber() {
        return addedUnitsNumber;
    }

    public int getAddedProductsNumber() {
        return addedProductsNumber;
    }

    public int getUpdatedProductsNumber() {
        return updatedProductsNumber;
    }

    public int getAddedOrdersNumber() {
        return addedOrdersNumber;
    }

    public boolean hasError() {
        return error != null;
    }

    public Error getError() {
        return error;
    }

    public Entity getErrorOrder() {
        return errorOrder;
    }

}

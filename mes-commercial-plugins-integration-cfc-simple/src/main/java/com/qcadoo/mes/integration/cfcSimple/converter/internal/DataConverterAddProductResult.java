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
package com.qcadoo.mes.integration.cfcSimple.converter.internal;

import com.qcadoo.mes.integration.cfcSimple.converter.model.ImportedProduct;
import com.qcadoo.model.api.Entity;

public class DataConverterAddProductResult {

    public static enum Error {
        PRODUCT_WAS_SERVICE
    }

    public static enum Action {
        PRODUCT_ADDED, PRODUCT_UPDATED, PRODUCT_EXISTED
    }

    private Entity productEntity;

    private Entity addedUnitEntity;

    private final ImportedProduct importedProduct;

    private Error error;

    private Action action;

    public DataConverterAddProductResult(final ImportedProduct importedProduct) {
        this.importedProduct = importedProduct;
    }

    public DataConverterAddProductResult importedUnit(final Entity addedUnitEntity) {
        this.addedUnitEntity = addedUnitEntity;
        return this;
    }

    public DataConverterAddProductResult importedProduct(final Entity addedEntity) {
        action = Action.PRODUCT_ADDED;
        this.productEntity = addedEntity;
        return this;
    }

    public DataConverterAddProductResult updatedProduct(final Entity addedEntity) {
        action = Action.PRODUCT_UPDATED;
        this.productEntity = addedEntity;
        return this;
    }

    public DataConverterAddProductResult productExisted(final Entity addedEntity) {
        action = Action.PRODUCT_EXISTED;
        this.productEntity = addedEntity;
        return this;
    }

    public DataConverterAddProductResult productWasService() {
        error = Error.PRODUCT_WAS_SERVICE;
        return this;
    }

    public boolean shouldPerformSaveUnit() {
        if (hasError() || action == null || productEntity == null || addedUnitEntity == null) {
            return false;
        }
        return true;
    }

    public boolean shouldPerformSaveProduct() {
        if (hasError() || action == null || productEntity == null) {
            return false;
        }
        if (Action.PRODUCT_EXISTED.equals(action)) {
            return false;
        }
        return true;
    }

    public ImportedProduct getImportedProduct() {
        return importedProduct;
    }

    public Entity getProductEntity() {
        return productEntity;
    }

    public Entity getUnitEntity() {
        return addedUnitEntity;
    }

    public Action getAction() {
        return action;
    }

    public boolean hasError() {
        return error != null;
    }

    public Error getError() {
        return error;
    }

}

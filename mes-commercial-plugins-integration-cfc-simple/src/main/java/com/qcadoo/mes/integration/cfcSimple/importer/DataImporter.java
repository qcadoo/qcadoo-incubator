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

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.integration.cfcSimple.ImportedDataManager;
import com.qcadoo.mes.integration.cfcSimple.importer.model.ParsedOrder;
import com.qcadoo.mes.integration.cfcSimple.importer.model.ParsedOrderItem;
import com.qcadoo.mes.integration.cfcSimple.importer.model.ParsedProduct;
import com.qcadoo.mes.integration.cfcSimple.importer.parser.IntegrationParser;
import com.qcadoo.mes.integration.cfcSimple.importer.parser.IntegrationParserException;
import com.qcadoo.mes.integration.cfcSimple.importer.parser.IntegrationParserResult;
import com.qcadoo.model.api.Entity;

@Service
public class DataImporter {

    private static final Logger LOG = LoggerFactory.getLogger(DataImporter.class);

    public DataImportResult performImport(final InputStream stream, final IntegrationParser parser,
            final ImportedDataManager importedDataManager) {
        IntegrationParserResult parseResult = null;
        DataImportResult importResult = new DataImportResult();

        try {
            parseResult = parser.parse(stream);
        } catch (IntegrationParserException e) {
            LOG.info(e.getMessage());
            return importResult.parseExceptionOccured(e);
        }

        Map<String, Entity> productEntities = performImportProducts(parseResult, importResult, importedDataManager);

        performImportOrders(parseResult, importResult, productEntities, importedDataManager);

        return importResult;
    }

    private Map<String, Entity> performImportProducts(final IntegrationParserResult parseResult,
            final DataImportResult importResult, final ImportedDataManager importedDataManager) {
        Map<String, Entity> productEntities = new HashMap<String, Entity>();
        for (ParsedProduct product : parseResult.getProducts()) {

            Entity productEntity = importedDataManager.createProductEntity(product);
            Entity existingProduct = importedDataManager.getExistingProduct(product);
            Entity savedProductEntity;

            if (existingProduct == null) {
                savedProductEntity = importedDataManager.saveProduct(productEntity);
                importResult.importedProduct();
            } else {
                if (importedDataManager.isEqualProducts(existingProduct, productEntity)) {
                    savedProductEntity = existingProduct;
                } else {
                    productEntity.setId(existingProduct.getId());
                    savedProductEntity = importedDataManager.saveProduct(productEntity);
                    importResult.updatedProduct();
                }
            }

            productEntities.put(product.getIdentificationCode(), savedProductEntity);
        }
        return productEntities;
    }

    private void performImportOrders(final IntegrationParserResult parseResult, final DataImportResult importResult,
            final Map<String, Entity> productEntities, final ImportedDataManager importedDataManager) {
        for (ParsedOrder order : parseResult.getOrders()) {
            Entity orderEntity = importedDataManager.createOrderEntity(order);
            Entity existingOrder = importedDataManager.getExistingOrder(order);
            boolean shouldSave = false;

            if (existingOrder == null) {
                shouldSave = true;
                importResult.importedOrder();
            } else {
                if (!importedDataManager.isEqualOrders(existingOrder, order)) {
                    orderEntity.setId(existingOrder.getId());
                    importedDataManager.deleteOrderProducts(existingOrder);
                    shouldSave = true;
                    importResult.updatedOrder();
                }
            }

            if (shouldSave) {
                Entity savedOrderEntity = importedDataManager.saveOrder(orderEntity);
                for (ParsedOrderItem item : order.getItems()) {
                    Entity orderProductEntity = importedDataManager.createOrderProductEntity(item, savedOrderEntity,
                            productEntities);
                    importedDataManager.saveOrderProduct(orderProductEntity);
                }
            }
        }
    }
}

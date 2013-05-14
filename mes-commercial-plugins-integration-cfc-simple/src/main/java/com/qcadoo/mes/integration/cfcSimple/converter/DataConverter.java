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

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.localization.api.TranslationService;
import com.qcadoo.mes.integration.cfcSimple.ImportedDataManager;
import com.qcadoo.mes.integration.cfcSimple.converter.internal.DataConverterAddProductResult;
import com.qcadoo.mes.integration.cfcSimple.converter.model.ImportedOrder;
import com.qcadoo.mes.integration.cfcSimple.converter.model.ImportedOrderItem;
import com.qcadoo.mes.integration.cfcSimple.converter.model.ImportedProduct;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchCriteriaBuilder;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.model.api.search.SearchResult;
import com.qcadoo.report.api.Pair;
import com.qcadoo.view.api.utils.NumberGeneratorService;

@Service
public class DataConverter {

    private static final String NUMBER = "number";

    private static final String NAME = "name";

    private static final String PRODUCT = "product";

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private NumberGeneratorService numberGeneratorService;

    @Autowired
    private TranslationService translationService;

    private Entity unitsDictionary;

    private DataDefinition productDataDefinition = null;

    private DataDefinition orderDataDefinition = null;

    private DataDefinition dictionaryItemDataDefinition = null;

    private boolean servicePrepared = false;

    public void preparService() {
        productDataDefinition = dataDefinitionService.get("basic", PRODUCT);
        orderDataDefinition = dataDefinitionService.get("orders", "order");
        dictionaryItemDataDefinition = dataDefinitionService.get("qcadooModel", "dictionaryItem");

        DataDefinition dictionaryDataDefinition = dataDefinitionService.get("qcadooModel", "dictionary");
        SearchCriteriaBuilder dictionaryBuilder = dictionaryDataDefinition.find()
                .add(SearchRestrictions.eq("pluginIdentifier", "basic")).add(SearchRestrictions.eq(NAME, "units"))
                .setMaxResults(1);
        unitsDictionary = dictionaryBuilder.list().getEntities().get(0);

        servicePrepared = true;
    }

    public DataConverterResult convertProducts(final Set<ImportedProduct> importedProducts,
            final ImportedDataManager importedDataManager, final Locale locale) {
        if (!servicePrepared) {
            preparService();
        }

        DataConverterResult result = new DataConverterResult();

        for (ImportedProduct importedProduct : importedProducts) {

            DataConverterAddProductResult addProductResult = addProduct(importedProduct, false, locale, null);

            result.addedProduct(addProductResult);

            performSaveProduct(addProductResult, importedDataManager);

        }

        return result;
    }

    public DataConverterResult convertOrders(final Set<ImportedOrder> importedOrders,
            final ImportedDataManager importedDataManager, final Locale locale) {
        if (!servicePrepared) {
            preparService();
        }

        DataConverterResult result = new DataConverterResult();

        List<DataConverterAddProductResult> productsToSave = new LinkedList<DataConverterAddProductResult>();
        List<Pair<Entity, String>> ordersToSave = new LinkedList<Pair<Entity, String>>();
        List<ImportedOrder> convertedEppOrderEntities = new LinkedList<ImportedOrder>();

        for (ImportedOrder importedOrder : importedOrders) {
            for (ImportedOrderItem orderItem : importedOrder.getItems()) {

                DataConverterAddProductResult addProductResult = addProduct(orderItem.getProduct(), true, locale, productsToSave);
                result.addedProduct(addProductResult);

                if (addProductResult.hasError()) {
                    continue;
                }

                productsToSave.add(addProductResult);

                Entity orderEntity = createOrderEntity(importedOrder, orderItem);
                Entity existingOrder = getExistingOrderEntity(orderEntity);

                if (existingOrder == null) {
                    ordersToSave.add(Pair.of(orderEntity, addProductResult.getProductEntity().getStringField(NUMBER)));
                    result.addedOrder();
                } else {
                    if (!isEqualOrders(existingOrder, orderEntity)
                            || existingOrder.getBelongsToField(PRODUCT) == null
                            || !existingOrder.getBelongsToField(PRODUCT).getStringField(NUMBER)
                                    .equals(addProductResult.getProductEntity().getStringField(NUMBER))) {
                        return result.orderExistsAndDiffer(existingOrder);
                    }
                }
            }
            convertedEppOrderEntities.add(importedOrder);
        }

        Map<String, Entity> savedProducts = new HashMap<String, Entity>();
        for (DataConverterAddProductResult addedProductResult : productsToSave) {
            Entity savedProduct = performSaveProduct(addedProductResult, importedDataManager);
            savedProducts.put(savedProduct.getStringField(NUMBER), savedProduct);
        }
        for (Pair<Entity, String> orderEntity : ordersToSave) {
            orderEntity.getKey().setField(NUMBER, numberGeneratorService.generateNumber("orders", "order"));
            orderEntity.getKey().setField(PRODUCT, savedProducts.get(orderEntity.getValue()));
            orderDataDefinition.save(orderEntity.getKey());
        }
        for (ImportedOrder importedOrder : convertedEppOrderEntities) {
            importedDataManager.setOrderAsConverted(importedOrder);
        }

        return result;
    }

    private DataConverterAddProductResult addProduct(final ImportedProduct importedProduct, final boolean orderConversion,
            final Locale locale, final List<DataConverterAddProductResult> productsToSave) {

        DataConverterAddProductResult result = new DataConverterAddProductResult(importedProduct);

        if (productsToSave != null) {
            for (DataConverterAddProductResult productToSave : productsToSave) {
                if (productToSave.getProductEntity() != null
                        && productToSave.getProductEntity().getField(NUMBER).equals(importedProduct.getNumber())) {
                    return result.productExisted(productToSave.getProductEntity());
                }
            }
        }

        if (!unitExists(importedProduct.getUnit(), productsToSave)) {
            result.importedUnit(createUnitEntity(importedProduct.getUnit(), locale));
        }

        Entity productEntity = createProductEntity(importedProduct);

        Entity existingProduct = getExistingProductEntity(productEntity);

        if (existingProduct == null) {
            result.importedProduct(productEntity);
        } else {
            if (orderConversion || isEqualProducts(existingProduct, productEntity)) {
                result.productExisted(existingProduct);
            } else {
                productEntity.setId(existingProduct.getId());
                result.updatedProduct(productEntity);
            }
        }
        return result;
    }

    private Entity performSaveProduct(final DataConverterAddProductResult addProductResult,
            final ImportedDataManager importedDataManager) {
        Entity savedProduct;
        if (addProductResult.shouldPerformSaveProduct()) {
            if (addProductResult.shouldPerformSaveUnit()) {
                dictionaryItemDataDefinition.save(addProductResult.getUnitEntity());
            }
            savedProduct = productDataDefinition.save(addProductResult.getProductEntity());
        } else {
            savedProduct = addProductResult.getProductEntity();
        }
        importedDataManager.setProductAsConverted(addProductResult.getImportedProduct());
        return savedProduct;
    }

    private boolean unitExists(final String unitName, final List<DataConverterAddProductResult> productsToSave) {
        if (productsToSave != null) {
            for (DataConverterAddProductResult productToSave : productsToSave) {
                if (productToSave.getUnitEntity() != null && productToSave.getUnitEntity().getField(NAME).equals(unitName)) {
                    return true;
                }
            }
        }

        SearchCriteriaBuilder dictionaryItemBuilder = dictionaryItemDataDefinition.find()
                .add(SearchRestrictions.belongsTo("dictionary", unitsDictionary)).add(SearchRestrictions.eq(NAME, unitName))
                .setMaxResults(1);

        SearchResult existingDictionaryItems = dictionaryItemBuilder.list();

        if (existingDictionaryItems.getTotalNumberOfEntities() == 0) {
            return false;
        }
        return true;
    }

    private Entity createUnitEntity(final String unitName, final Locale locale) {
        Entity unitEntity = dictionaryItemDataDefinition.create();
        unitEntity.setField(NAME, unitName);
        unitEntity.setField("description", translationService.translate("cfcSimple.conversion.unitDescription", locale));
        unitEntity.setField("dictionary", unitsDictionary);
        return unitEntity;
    }

    private Entity getExistingProductEntity(final Entity productEntity) {
        SearchCriteriaBuilder productBuilder = productDataDefinition.find()
                .add(SearchRestrictions.eq(NUMBER, productEntity.getStringField(NUMBER))).setMaxResults(1);
        SearchResult existingProducts = productBuilder.list();
        if (existingProducts.getTotalNumberOfEntities() > 0) {
            return existingProducts.getEntities().get(0);
        }
        return null;
    }

    private Entity getExistingOrderEntity(final Entity orderEntity) {
        SearchCriteriaBuilder ordersBuilder = orderDataDefinition.find()
                .add(SearchRestrictions.eq(NAME, orderEntity.getStringField(NAME))).setMaxResults(1);
        SearchResult existingOrders = ordersBuilder.list();
        if (existingOrders.getTotalNumberOfEntities() > 0) {
            return existingOrders.getEntities().get(0);
        }
        return null;
    }

    private Entity createProductEntity(final ImportedProduct importedProduct) {
        Entity productEntity = productDataDefinition.create();
        productEntity.setField(NUMBER, importedProduct.getNumber());
        productEntity.setField(NAME, importedProduct.getName());
        productEntity.setField("ean", importedProduct.getEan());
        productEntity.setField("typeOfMaterial", importedProduct.getTypeOfMaterial());
        productEntity.setField("unit", importedProduct.getUnit());
        return productEntity;
    }

    private Entity createOrderEntity(final ImportedOrder importedOrder, final ImportedOrderItem importedOrderItem) {
        Entity orderEntity = orderDataDefinition.create();
        orderEntity.setField(NAME, importedOrder.getNumber() + " - " + importedOrderItem.getProduct().getNumber());
        orderEntity.setField("dateFrom", importedOrder.getDrawDate());
        orderEntity.setField("dateTo", importedOrder.getRealizationDate());
        orderEntity.setField("state", importedOrder.getState() == null ? "01new" : importedOrder.getState());
        orderEntity.setField("plannedQuantity", importedOrderItem.getQuantity());
        return orderEntity;
    }

    public boolean isEqualProducts(final Entity product1, final Entity product2) {
        if (isEqualFields(product1, product2, NUMBER)) {
            return true;
        }
        if (isEqualFields(product1, product2, NAME)) {
            return true;
        }
        if (isEqualFields(product1, product2, "globalTypeOfMaterial")) {
            return true;
        }
        if (isEqualFields(product1, product2, "ean")) {
            return true;
        }
        if (isEqualFields(product1, product2, "unit")) {
            return true;
        }
        return false;
    }

    public boolean isEqualOrders(final Entity order1, final Entity order2) {
        if (isEqualFields(order1, order2, NAME)) {
            return true;
        }
        if (isEqualFields(order1, order2, "dateFrom")) {
            return true;
        }
        if (isEqualFields(order1, order2, "dateTo")) {
            return true;
        }
        if (isEqualFields(order1, order2, "plannedQuantity")) {
            return true;
        }
        return false;
    }

    private boolean isEqualFields(final Entity entity1, final Entity entity2, final String field) {
        if (entity1.getField(field).equals(entity2.getField(field))) {
            return true;
        }
        return false;
    }
}

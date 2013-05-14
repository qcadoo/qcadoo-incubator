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

import static com.qcadoo.mes.integration.efcSimple.constants.EfcSimpleConstants.FIELD_CLIENT_ADDRESS;
import static com.qcadoo.mes.integration.efcSimple.constants.EfcSimpleConstants.FIELD_CLIENT_NAME;
import static com.qcadoo.mes.integration.efcSimple.constants.EfcSimpleConstants.FIELD_CONVERTED;
import static com.qcadoo.mes.integration.efcSimple.constants.EfcSimpleConstants.FIELD_DRAW_DATE;
import static com.qcadoo.mes.integration.efcSimple.constants.EfcSimpleConstants.FIELD_IDENTIFICATION_CODE;
import static com.qcadoo.mes.integration.efcSimple.constants.EfcSimpleConstants.FIELD_NAME;
import static com.qcadoo.mes.integration.efcSimple.constants.EfcSimpleConstants.FIELD_NUMBER;
import static com.qcadoo.mes.integration.efcSimple.constants.EfcSimpleConstants.FIELD_ORDER_NUMBER;
import static com.qcadoo.mes.integration.efcSimple.constants.EfcSimpleConstants.FIELD_QUANITY;
import static com.qcadoo.mes.integration.efcSimple.constants.EfcSimpleConstants.FIELD_REALIZATION_DATE;
import static com.qcadoo.mes.integration.efcSimple.constants.EfcSimpleConstants.FIELD_STATE;
import static com.qcadoo.mes.integration.efcSimple.constants.EfcSimpleConstants.FIELD_TYPE;
import static com.qcadoo.mes.integration.efcSimple.constants.EfcSimpleConstants.FIELD_UNIT;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;

import com.qcadoo.mes.integration.cfcSimple.ImportedDataManager;
import com.qcadoo.mes.integration.cfcSimple.converter.model.ImportedOrder;
import com.qcadoo.mes.integration.cfcSimple.converter.model.ImportedOrderItem;
import com.qcadoo.mes.integration.cfcSimple.converter.model.ImportedProduct;
import com.qcadoo.mes.integration.cfcSimple.importer.model.ParsedOrder;
import com.qcadoo.mes.integration.cfcSimple.importer.model.ParsedOrderItem;
import com.qcadoo.mes.integration.cfcSimple.importer.model.ParsedProduct;
import com.qcadoo.mes.integration.cfcSimple.importer.parser.IntegrationParserException;
import com.qcadoo.mes.integration.cfcSimple.importer.parser.IntegrationParserUtils;
import com.qcadoo.mes.integration.efcSimple.constants.EfcSimpleConstants;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchCriteriaBuilder;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.model.api.search.SearchResult;

public class EnovaImportedDataManager implements ImportedDataManager {

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Override
    public Entity saveProduct(final Entity productEntity) {
        return dataDefinitionService.get(EfcSimpleConstants.PLUGIN_IDENTIFIER, EfcSimpleConstants.MODEL_IMPORTED_PRODUCT).save(
                productEntity);
    }

    @Override
    public Entity saveOrder(final Entity orderEntity) {
        return dataDefinitionService.get(EfcSimpleConstants.PLUGIN_IDENTIFIER, EfcSimpleConstants.MODEL_IMPORTED_ORDER).save(
                orderEntity);
    }

    @Override
    public Entity saveOrderProduct(final Entity orderProductEntity) {
        return dataDefinitionService.get(EfcSimpleConstants.PLUGIN_IDENTIFIER, EfcSimpleConstants.MODEL_IMPORTED_ORDER_PRODUCT)
                .save(orderProductEntity);
    }

    @Override
    public void deleteOrderProducts(final Entity orderEntity) {
        for (Entity orrderProductEntity : orderEntity.getHasManyField("orderProducts")) {
            dataDefinitionService.get(EfcSimpleConstants.PLUGIN_IDENTIFIER, EfcSimpleConstants.MODEL_IMPORTED_ORDER_PRODUCT)
                    .delete(orrderProductEntity.getId());
        }
    }

    @Override
    public Entity createProductEntity(final ParsedProduct product) {
        Entity productEntity = dataDefinitionService.get(EfcSimpleConstants.PLUGIN_IDENTIFIER,
                EfcSimpleConstants.MODEL_IMPORTED_PRODUCT).create();

        switch (EnovaParser.ProductType.valueOf(product.getField(FIELD_TYPE))) {
            case PRODUCT:
                productEntity.setField(FIELD_TYPE, "01product");
                break;
            case SERVICE:
                productEntity.setField(FIELD_TYPE, "02service");
                break;
            case GOOD:
                productEntity.setField(FIELD_TYPE, "03good");
                break;
            case COMPONENT:
                productEntity.setField(FIELD_TYPE, "04component");
                break;
            default:
                throw new IllegalStateException("unknown product type");
        }

        productEntity.setField(FIELD_IDENTIFICATION_CODE, product.getIdentificationCode());
        productEntity.setField("ean", product.getField("ean"));
        productEntity.setField(FIELD_NAME, product.getField(FIELD_NAME));
        productEntity.setField("description", product.getField("description"));
        productEntity.setField(FIELD_UNIT, product.getField(FIELD_UNIT));
        productEntity.setField(FIELD_CONVERTED, 0);
        return productEntity;
    }

    @Override
    public Entity createOrderEntity(final ParsedOrder order) {
        Entity orderEntity = dataDefinitionService.get(EfcSimpleConstants.PLUGIN_IDENTIFIER,
                EfcSimpleConstants.MODEL_IMPORTED_ORDER).create();
        orderEntity.setField(FIELD_NUMBER, order.getField(FIELD_NUMBER));
        orderEntity.setField(FIELD_CLIENT_NAME, order.getField(FIELD_CLIENT_NAME));
        orderEntity.setField(FIELD_CLIENT_ADDRESS, order.getField(FIELD_CLIENT_ADDRESS));
        orderEntity.setField(FIELD_DRAW_DATE, order.getField(FIELD_DRAW_DATE));
        orderEntity.setField(FIELD_REALIZATION_DATE, order.getField(FIELD_REALIZATION_DATE));

        switch (EnovaParser.StateType.valueOf(order.getField(FIELD_STATE))) {
            case UNCONFIRMED:
                orderEntity.setField(FIELD_STATE, "03unconfirmed");
                break;
            case CONFIRMED:
                orderEntity.setField(FIELD_STATE, "02confirmed");
                break;
            default:
                orderEntity.setField(FIELD_STATE, "01approved");
                break;
        }

        orderEntity.setField(FIELD_CONVERTED, 0);
        return orderEntity;
    }

    @Override
    public Entity createOrderProductEntity(final ParsedOrderItem item, final Entity order,
            final Map<String, Entity> productEntities) {
        Entity orderProductEntity = dataDefinitionService.get(EfcSimpleConstants.PLUGIN_IDENTIFIER,
                EfcSimpleConstants.MODEL_IMPORTED_ORDER_PRODUCT).create();
        try {
            orderProductEntity.setField(FIELD_ORDER_NUMBER, IntegrationParserUtils.parseInteger(item.getField(FIELD_NUMBER)));
            orderProductEntity.setField(FIELD_QUANITY, IntegrationParserUtils.parseBigDecimal(item.getField(FIELD_QUANITY)));
        } catch (IntegrationParserException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }

        Entity itemProduct = productEntities.get(item.getProductIdentificationCode());
        orderProductEntity.setField("product", itemProduct);
        orderProductEntity.setField("order", order);
        return orderProductEntity;
    }

    @Override
    public Entity getExistingProduct(final ParsedProduct productEntity) {
        SearchCriteriaBuilder productBuilder = dataDefinitionService
                .get(EfcSimpleConstants.PLUGIN_IDENTIFIER, EfcSimpleConstants.MODEL_IMPORTED_PRODUCT).find()
                .add(SearchRestrictions.eq(FIELD_IDENTIFICATION_CODE, productEntity.getIdentificationCode())).setMaxResults(1);
        SearchResult existingProducts = productBuilder.list();
        if (existingProducts.getTotalNumberOfEntities() > 0) {
            return existingProducts.getEntities().get(0);
        }
        return null;
    }

    @Override
    public Entity getExistingOrder(final ParsedOrder order) {
        SearchCriteriaBuilder ordersBuilder = dataDefinitionService
                .get(EfcSimpleConstants.PLUGIN_IDENTIFIER, EfcSimpleConstants.MODEL_IMPORTED_ORDER).find()
                .add(SearchRestrictions.eq(FIELD_NUMBER, order.getField(FIELD_NUMBER))).setMaxResults(1);
        SearchResult existingOrders = ordersBuilder.list();
        if (existingOrders.getTotalNumberOfEntities() > 0) {
            return existingOrders.getEntities().get(0);
        }
        return null;
    }

    @Override
    public boolean isEqualOrders(final Entity existingOrder, final ParsedOrder order) {
        if (!existingOrder.getField(FIELD_NUMBER).equals(order.getField(FIELD_NUMBER))) {
            return false;
        }
        if (!existingOrder.getField(FIELD_CLIENT_NAME).equals(order.getField(FIELD_CLIENT_NAME))) {
            return false;
        }
        if (!existingOrder.getField(FIELD_CLIENT_ADDRESS).equals(order.getField(FIELD_CLIENT_ADDRESS))) {
            return false;
        }
        if (!existingOrder.getField(FIELD_DRAW_DATE).equals(
                IntegrationParserUtils.parseDateWithoutException(order.getField(FIELD_DRAW_DATE)))) {
            return false;
        }
        if (!existingOrder.getField(FIELD_REALIZATION_DATE).equals(
                IntegrationParserUtils.parseDateWithoutException(order.getField(FIELD_REALIZATION_DATE)))) {
            return false;
        }

        List<Entity> existingOrderProducts = new ArrayList<Entity>(existingOrder.getHasManyField("orderProducts"));
        List<ParsedOrderItem> orderProducts = new ArrayList<ParsedOrderItem>(order.getItems());
        if (existingOrderProducts.size() == orderProducts.size()) {
            Collections.sort(existingOrderProducts, new OrderProductEntityComparator());
            Collections.sort(orderProducts, new ParsedOrderProductComparator());
            for (int i = 0; i < existingOrderProducts.size(); i++) {
                Entity existingOrderProduct = existingOrderProducts.get(i);
                ParsedOrderItem orderProduct = orderProducts.get(i);
                if (!existingOrderProduct.getField(FIELD_ORDER_NUMBER).equals(
                        IntegrationParserUtils.parseIntegerWithoutException(orderProduct.getField(FIELD_NUMBER)))) {
                    return false;
                }
                if (((BigDecimal) existingOrderProduct.getField(FIELD_QUANITY)).compareTo(IntegrationParserUtils
                        .parseBigDecimalWithoutException(orderProduct.getField(FIELD_QUANITY))) != 0) {
                    return false;
                }
                if (!existingOrderProduct.getBelongsToField("product").getField(FIELD_IDENTIFICATION_CODE)
                        .equals(orderProduct.getProductIdentificationCode())) {
                    return false;
                }
            }
            return true;
        }

        return false;
    }

    private static class OrderProductEntityComparator implements Comparator<Entity>, Serializable {

        private static final long serialVersionUID = -8061281567598245754L;

        @Override
        public int compare(final Entity o1, final Entity o2) {
            return ((Integer) o1.getField(FIELD_ORDER_NUMBER)).compareTo((Integer) o1.getField(FIELD_ORDER_NUMBER));
        }

    }

    private static class ParsedOrderProductComparator implements Comparator<ParsedOrderItem>, Serializable {

        private static final long serialVersionUID = -4917753461632364612L;

        @Override
        public int compare(final ParsedOrderItem o1, final ParsedOrderItem o2) {
            return Integer.valueOf(o1.getField(FIELD_NUMBER)).compareTo(Integer.valueOf(o1.getField(FIELD_NUMBER)));
        }
    }

    @Override
    public boolean isEqualProducts(final Entity product1, final Entity product2) {
        if (!isEqualFields(product1, product2, FIELD_TYPE)) {
            return false;
        }
        if (!isEqualFields(product1, product2, FIELD_IDENTIFICATION_CODE)) {
            return false;
        }
        if (!isEqualFields(product1, product2, FIELD_NAME)) {
            return false;
        }
        if (!isEqualFields(product1, product2, "description")) {
            return false;
        }
        if (!isEqualFields(product1, product2, FIELD_UNIT)) {
            return false;
        }
        return true;
    }

    private boolean isEqualFields(final Entity entity1, final Entity entity2, final String field) {
        if (entity1.getField(field) == null) {
            return entity2.getField(field) == null;
        }
        if (entity1.getField(field).equals(entity2.getField(field))) {
            return true;
        }
        return false;
    }

    @Override
    public Set<ImportedProduct> getImportedProducts(final Set<Long> productIds) {
        Set<ImportedProduct> result = new HashSet<ImportedProduct>();
        for (Long importedProductId : productIds) {
            Entity productEntity = dataDefinitionService.get(EfcSimpleConstants.PLUGIN_IDENTIFIER,
                    EfcSimpleConstants.MODEL_IMPORTED_PRODUCT).get(importedProductId);

            ImportedProduct product = createImportedProduct(productEntity, false);
            if (product != null) {
                result.add(product);
            }
        }

        return result;
    }

    private ImportedProduct createImportedProduct(final Entity productEntity, final boolean isOrderConversion) {
        ImportedProduct product = new ImportedProduct(productEntity.getId());

        if ("02service".equals(productEntity.getField(FIELD_TYPE))) {
            return null;
        } else if (isOrderConversion || "04set".equals(productEntity.getField(FIELD_TYPE))) {
            product.setTypeOfMaterial("03finalProduct");
        } else {
            product.setTypeOfMaterial("01component");
        }

        product.setNumber(productEntity.getStringField(FIELD_IDENTIFICATION_CODE));
        product.setName(productEntity.getStringField(FIELD_NAME));
        product.setEan(productEntity.getStringField("ean"));
        product.setUnit(productEntity.getStringField(FIELD_UNIT));

        return product;
    }

    @Override
    public Set<ImportedOrder> getImportedOrders(final Set<Long> orderIds) {
        Set<ImportedOrder> result = new HashSet<ImportedOrder>();
        for (Long importedOrderId : orderIds) {
            Entity orderEntity = dataDefinitionService.get(EfcSimpleConstants.PLUGIN_IDENTIFIER,
                    EfcSimpleConstants.MODEL_IMPORTED_ORDER).get(importedOrderId);

            ImportedOrder order = new ImportedOrder(importedOrderId);

            order.setNumber(orderEntity.getStringField(FIELD_NUMBER));
            order.setDrawDate((Date) orderEntity.getField(FIELD_DRAW_DATE));
            order.setRealizationDate((Date) orderEntity.getField(FIELD_REALIZATION_DATE));

            if ("01approved".equals(orderEntity.getStringField(FIELD_STATE))) {
                order.setState("02accepted");
            } else if ("02confirmed".equals(orderEntity.getStringField(FIELD_STATE))) {
                order.setState("03inProgress");
            } else if ("03unconfirmed".equals(orderEntity.getStringField(FIELD_STATE))) {
                order.setState("01new");
            }

            for (Entity orderProductEntity : orderEntity.getHasManyField("orderProducts")) {

                ImportedProduct orderItemProduct = createImportedProduct(orderProductEntity.getBelongsToField("product"), true);
                if (orderItemProduct != null) {
                    ImportedOrderItem orderItem = new ImportedOrderItem();
                    orderItem.setProduct(orderItemProduct);
                    orderItem.setQuantity((BigDecimal) orderProductEntity.getField(FIELD_QUANITY));
                    order.addItem(orderItem);
                }
            }

            result.add(order);
        }

        return result;
    }

    @Override
    public void setProductAsConverted(final ImportedProduct product) {
        DataDefinition importedProductDataDefinition = dataDefinitionService.get(EfcSimpleConstants.PLUGIN_IDENTIFIER,
                EfcSimpleConstants.MODEL_IMPORTED_PRODUCT);
        Entity importedProduct = importedProductDataDefinition.get(product.getOriginalEntityId());
        importedProduct.setField(FIELD_CONVERTED, "1");
        importedProductDataDefinition.save(importedProduct);
    }

    @Override
    public void setOrderAsConverted(final ImportedOrder order) {
        DataDefinition importedOrderDataDefinition = dataDefinitionService.get(EfcSimpleConstants.PLUGIN_IDENTIFIER,
                EfcSimpleConstants.MODEL_IMPORTED_ORDER);
        Entity importedOrder = importedOrderDataDefinition.get(order.getOriginalEntityId());
        importedOrder.setField(FIELD_CONVERTED, "1");
        importedOrderDataDefinition.save(importedOrder);
    }

}

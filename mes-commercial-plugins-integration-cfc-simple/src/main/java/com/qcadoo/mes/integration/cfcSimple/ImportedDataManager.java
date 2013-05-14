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
package com.qcadoo.mes.integration.cfcSimple;

import java.util.Map;
import java.util.Set;

import com.qcadoo.mes.integration.cfcSimple.converter.model.ImportedOrder;
import com.qcadoo.mes.integration.cfcSimple.converter.model.ImportedProduct;
import com.qcadoo.mes.integration.cfcSimple.importer.model.ParsedOrder;
import com.qcadoo.mes.integration.cfcSimple.importer.model.ParsedOrderItem;
import com.qcadoo.mes.integration.cfcSimple.importer.model.ParsedProduct;
import com.qcadoo.model.api.Entity;

public interface ImportedDataManager {

    Entity saveProduct(Entity productEntity);

    Entity saveOrder(Entity orderEntity);

    Entity saveOrderProduct(Entity orderProductEntity);

    void deleteOrderProducts(Entity orderEntity);

    Entity createProductEntity(ParsedProduct product);

    Entity createOrderEntity(ParsedOrder order);

    Entity createOrderProductEntity(ParsedOrderItem item, Entity order, Map<String, Entity> productEntities);

    Entity getExistingProduct(ParsedProduct productEntity);

    Entity getExistingOrder(ParsedOrder order);

    boolean isEqualOrders(Entity existingOrder, ParsedOrder order);

    boolean isEqualProducts(Entity product1, Entity product2);

    Set<ImportedProduct> getImportedProducts(Set<Long> productIds);

    Set<ImportedOrder> getImportedOrders(Set<Long> orderIds);

    void setProductAsConverted(ImportedProduct product);

    void setOrderAsConverted(ImportedOrder order);

}

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

import java.io.InputStream;
import java.util.Locale;
import java.util.Set;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;

import com.google.common.base.Preconditions;
import com.qcadoo.mes.integration.cfcSimple.converter.DataConverter;
import com.qcadoo.mes.integration.cfcSimple.converter.DataConverterResult;
import com.qcadoo.mes.integration.cfcSimple.importer.DataImportResult;
import com.qcadoo.mes.integration.cfcSimple.importer.DataImporter;
import com.qcadoo.mes.integration.cfcSimple.importer.parser.IntegrationParser;
import com.qcadoo.mes.integration.cfcSimple.importer.parser.IntegrationParserException;

public class IntegrationPerformer {

    private IntegrationParser parser = null;

    private ImportedDataManager importedDataManager = null;

    private String pluginIdentifier = null;

    @Autowired
    private DataImporter dataImporter;

    @Autowired
    private DataConverter dataConverter;

    @PostConstruct
    public void checkState() {
        Preconditions.checkNotNull(parser, "IntegrationParser not defined");
        Preconditions.checkNotNull(importedDataManager, "ImportedDataManager not defined");
        Preconditions.checkNotNull(pluginIdentifier, "PluginIdentifier not defined");
    }

    public String performConvertOrders(final Set<Long> orderIds, final Locale locale) {
        DataConverterResult conversionResult = dataConverter.convertOrders(importedDataManager.getImportedOrders(orderIds),
                importedDataManager, locale);
        if (conversionResult.hasError() && DataConverterResult.Error.ORDER_EXIST_AND_DIFFER.equals(conversionResult.getError())) {
            return createErrorPageUrl("conversion", "orderExistAndDiffer", null, conversionResult.getErrorOrder()
                    .getField("name"));
        }
        return createSuccessPageUrl("ordersConversionSuccess", conversionResult.getAddedUnitsNumber(),
                conversionResult.getAddedProductsNumber(), conversionResult.getAddedOrdersNumber());
    }

    public String performConvertProducts(final Set<Long> productIds, final Locale locale) {
        DataConverterResult conversionResult = dataConverter.convertProducts(importedDataManager.getImportedProducts(productIds),
                importedDataManager, locale);
        return createSuccessPageUrl("productsConversionSuccess", conversionResult.getAddedUnitsNumber(),
                conversionResult.getAddedProductsNumber(), conversionResult.getUpdatedProductsNumber());
    }

    public String performImport(final InputStream stream) {
        DataImportResult result = dataImporter.performImport(stream, parser, importedDataManager);
        if (result.isError()) {
            IntegrationParserException parserException = result.getParserException();
            return createErrorPageUrl("import", parserException.getMessageKey(), parserException.getLine(),
                    parserException.getMessageArgs());
        } else {
            return createSuccessPageUrl("successImport", result.getImportedOrdersNumber(), result.getImportedProductsNumber(),
                    result.getUpdatedOrdersNumber(), result.getUpdatedProductsNumber());
        }
    }

    private String createErrorPageUrl(final String type, final String message, final Integer line, final Object... args) {
        StringBuilder url = new StringBuilder("${root}/" + pluginIdentifier + "/infoPage.html?type=error&status=");
        url.append(message);
        url.append("&errorType=");
        url.append(type);
        if (line != null) {
            url.append("&line=");
            url.append(line);
        }
        url.append(createUrlArgs(args));
        return url.toString();
    }

    private String createSuccessPageUrl(final String message, final Object... args) {
        return "${root}/" + pluginIdentifier + "/infoPage.html?type=success&status=" + message + createUrlArgs(args);
    }

    private String createUrlArgs(final Object... args) {
        StringBuilder urlArgs = new StringBuilder();
        for (Object arg : args) {
            urlArgs.append("&arg=");
            urlArgs.append(arg);
        }
        return urlArgs.toString();
    }

    public void setParser(final IntegrationParser parser) {
        this.parser = parser;
    }

    public void setImportedDataManager(final ImportedDataManager importedDataManager) {
        this.importedDataManager = importedDataManager;
    }

    public void setPluginIdentifier(final String pluginIdentifier) {
        this.pluginIdentifier = pluginIdentifier;
    }

    public String getPluginIdentifier() {
        return pluginIdentifier;
    }
}

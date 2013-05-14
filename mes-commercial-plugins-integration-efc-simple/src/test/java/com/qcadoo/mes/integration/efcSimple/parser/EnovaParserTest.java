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

import java.io.InputStream;

import org.junit.Assert;
import org.junit.Test;

import com.qcadoo.mes.integration.cfcSimple.importer.parser.IntegrationParserException;
import com.qcadoo.mes.integration.cfcSimple.importer.parser.IntegrationParserResult;

public class EnovaParserTest {

    @Test
    public void shouldParseOneOrder() throws IntegrationParserException {
        // given
        InputStream xml = getClass().getClassLoader().getResourceAsStream("Dane_towar_jednostka.xml");
        EnovaParser parser = new EnovaParser();

        // when
        IntegrationParserResult result = parser.parse(xml);

        // then
        Assert.assertEquals(1, result.getOrders().size());
    }

    @Test
    public void shouldParseTwoProducts() throws IntegrationParserException {
        // given
        InputStream xml = getClass().getClassLoader().getResourceAsStream("Dane_towar_jednostka.xml");
        EnovaParser parser = new EnovaParser();

        // when
        IntegrationParserResult result = parser.parse(xml);

        // then
        Assert.assertEquals(2, result.getOrders().get(0).getItems().size());
    }

    @Test(expected = IntegrationParserException.class)
    public void shouldThrowExceptionOnNoUnitSection() throws Exception {
        // given
        InputStream xml = getClass().getClassLoader().getResourceAsStream("Dane_towar_jednostka_bez_jednostek.xml");
        EnovaParser parser = new EnovaParser();

        // when
        parser.parse(xml);

        // then
    }

    @Test(expected = IntegrationParserException.class)
    public void shouldThrowExceptionOnNoProductSection() throws Exception {
        // given
        InputStream xml = getClass().getClassLoader().getResourceAsStream("Dane_towar_jednostka_bez_produktow.xml");
        EnovaParser parser = new EnovaParser();

        // when
        parser.parse(xml);

        // then
    }

    @Test
    public void shouldReturnMultipleOrdersFromOneFile() throws IntegrationParserException {
        // given
        InputStream xml = getClass().getClassLoader().getResourceAsStream("Dane_zam_towar_jedn_multi.xml");
        EnovaParser parser = new EnovaParser();

        // when
        IntegrationParserResult result = parser.parse(xml);

        // then
        Assert.assertEquals(6, result.getProducts().size());
        Assert.assertEquals(3, result.getOrders().size());
    }

    @Test
    public void shouldImportOnlyProductsAndUnits() throws IntegrationParserException {
        // given
        InputStream xml = getClass().getClassLoader().getResourceAsStream("Dane_prod.xml");
        EnovaParser parser = new EnovaParser();

        // when
        IntegrationParserResult result = parser.parse(xml);

        // then
        Assert.assertEquals(14, result.getProducts().size());
    }
}

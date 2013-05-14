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
package com.qcadoo.mes.integration.cfcSimple.importer.model;

import java.util.HashMap;
import java.util.Map;

public class ParsedProduct {

    private String identificationCode;

    private Map<String, String> fields = new HashMap<String, String>();

    public String getIdentificationCode() {
        return identificationCode;
    }

    public void setIdentificationCode(final String identificationCode) {
        this.identificationCode = identificationCode;
    }

    public void setField(final String key, final String value) {
        fields.put(key, value);
    }

    public String getField(final String key) {
        return fields.get(key);
    }

    @Override
    public String toString() {
        return "ParsedProduct [identificationCode=" + identificationCode + ", fields=" + fields + "]";
    }

}

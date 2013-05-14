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
package com.qcadoo.mes.integration.cfcSimple.converter.model;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;

public class ImportedOrder {

    private List<ImportedOrderItem> items = new LinkedList<ImportedOrderItem>();

    private String number;

    private Date drawDate;

    private Date realizationDate;

    private String state;

    private final Long originalEntityId;

    public ImportedOrder(final Long originalEntityId) {
        this.originalEntityId = originalEntityId;
    }

    public List<ImportedOrderItem> getItems() {
        return items;
    }

    public void addItem(final ImportedOrderItem item) {
        items.add(item);
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(final String number) {
        this.number = number;
    }

    public Date getDrawDate() {
        return new Date(drawDate.getTime());
    }

    public void setDrawDate(final Date drawDate) {
        if (drawDate == null) {
            this.drawDate = null;
        } else {
            this.drawDate = new Date(drawDate.getTime());
        }
    }

    public Date getRealizationDate() {
        return new Date(realizationDate.getTime());
    }

    public void setRealizationDate(final Date realizationDate) {
        if (realizationDate == null) {
            this.realizationDate = null;
        } else {
            this.realizationDate = new Date(realizationDate.getTime());
        }
    }

    public Long getOriginalEntityId() {
        return originalEntityId;
    }

    public String getState() {
        return state;
    }

    public void setState(final String state) {
        this.state = state;
    }

}

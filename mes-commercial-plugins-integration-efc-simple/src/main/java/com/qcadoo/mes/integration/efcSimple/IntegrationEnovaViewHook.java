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
package com.qcadoo.mes.integration.efcSimple;

import java.util.Collections;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.google.common.base.Preconditions;
import com.qcadoo.mes.integration.cfcSimple.IntegrationPerformer;
import com.qcadoo.mes.integration.efcSimple.constants.EfcSimpleConstants;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.components.GridComponent;

@Service
public class IntegrationEnovaViewHook {

    @Autowired
    @Qualifier("enovaIntegrationPerformer")
    private IntegrationPerformer integrationPerformer;

    public void onUploadButtonClick(final ViewDefinitionState viewDefinitionState, final ComponentState triggerState,
            final String[] args) {
        viewDefinitionState.openModal("${root}/" + EfcSimpleConstants.PLUGIN_IDENTIFIER + "/uploadPage.html");
    }

    public void convertOrdersClick(final ViewDefinitionState viewDefinitionState, final ComponentState triggerState,
            final String[] args) {
        viewDefinitionState.openModal(integrationPerformer.performConvertOrders(
                getSelectedIdsFromGrid(viewDefinitionState, "ordersGrid"), viewDefinitionState.getLocale()));
    }

    public void convertProductsClick(final ViewDefinitionState viewDefinitionState, final ComponentState triggerState,
            final String[] args) {
        viewDefinitionState.openModal(integrationPerformer.performConvertProducts(
                getSelectedIdsFromGrid(viewDefinitionState, "productsGrid"), viewDefinitionState.getLocale()));
    }

    public void convertOrderClick(final ViewDefinitionState viewDefinitionState, final ComponentState triggerState,
            final String[] args) {
        viewDefinitionState.redirectTo(
                integrationPerformer.performConvertOrders(getSelectedIdsFromForm(viewDefinitionState),
                        viewDefinitionState.getLocale()), false, false);
    }

    public void convertProductClick(final ViewDefinitionState viewDefinitionState, final ComponentState triggerState,
            final String[] args) {
        viewDefinitionState.redirectTo(
                integrationPerformer.performConvertProducts(getSelectedIdsFromForm(viewDefinitionState),
                        viewDefinitionState.getLocale()), false, false);
    }

    private Set<Long> getSelectedIdsFromGrid(final ViewDefinitionState viewDefinitionState, final String gridName) {
        GridComponent grid = (GridComponent) viewDefinitionState.getComponentByReference(gridName);
        Preconditions.checkState(!grid.getSelectedEntitiesIds().isEmpty(), "No record selected");
        return grid.getSelectedEntitiesIds();
    }

    private Set<Long> getSelectedIdsFromForm(final ViewDefinitionState viewDefinitionState) {
        FormComponent form = (FormComponent) viewDefinitionState.getComponentByReference("form");
        Preconditions.checkState(form.getEntityId() != null, "No record");
        return Collections.singleton(form.getEntityId());
    }

}

package com.warehousecorporation.warehouse;

import java.math.BigDecimal;
import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.security.api.SecurityService;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.components.WindowComponent;
import com.qcadoo.view.api.ribbon.RibbonActionItem;

/**
 * A service which implements more complex business logic of the warehouse plugin
 * This type of class must always be a Spring service beans. Spring scans for modules
 * service classes in the package specified in root-context.xml
 */
@Service
public class WarehouseService {

    /**
     * We inject some service classes from the qcadoo Framework
     */
    @Autowired
    private DataDefinitionService dataDefinitionService;
    @Autowired
    private SecurityService securityService;

    /**
     * onCreate hook
     */
    public void setTransferRequestData(final DataDefinition dataDefinition, final Entity transfer) {
        if (transfer.getId() == null) {
            transfer.setField("requestWorker", securityService.getCurrentUserName());
            transfer.setField("requestDate", new Date());
        }
    }

    /**
     * business method - implements pure business logic which is not mixed up with GUI code
     */
    public void executeTransfer(final Entity transfer) throws TransferRequirementsException {
        transfer.setField("confirmWorker", securityService.getCurrentUserName());
        transfer.setField("confirmDate", new Date());

        DataDefinition resourceDataDefinition = dataDefinitionService.get("warehouse", "resource");
        DataDefinition transferDataDefinition = dataDefinitionService.get("warehouse", "transfer");

        Entity resource = transfer.getBelongsToField("resource");

        BigDecimal currentQuantity = (BigDecimal) resource.getField("quantity");
        BigDecimal transferQuantity = (BigDecimal) transfer.getField("quantity");
        BigDecimal newQuantity;

        if (transferQuantity.compareTo(currentQuantity) > 0) {
            transfer.addError(transferDataDefinition.getField("quantity"), "warehouse.not.enought.resource.error");
            throw new TransferRequirementsException();
        }

        if ("incoming".equals(transfer.getField("type"))) {
            newQuantity = new BigDecimal(currentQuantity.doubleValue() - transferQuantity.doubleValue());
        } else {
            newQuantity = new BigDecimal(currentQuantity.doubleValue() + transferQuantity.doubleValue());
        }

        if (newQuantity.doubleValue() >= 0) {
            resource.setField("quantity", newQuantity);
            resourceDataDefinition.save(resource);
        }
    }

    /**
     * GUI event hook - combines GUI logic with the business method
     */
    public void executeTransferForForm(final ViewDefinitionState state, final ComponentState componentState, final String[] args) throws TransferRequirementsException {
        Long transferId = (Long) state.getFieldValue();
        DataDefinition transferDataDefinition = dataDefinitionService.get("warehouse", "transfer");
        Entity transfer = transferDataDefinition.get(transferId);
        executeTransfer(transfer);
    }

    /**
     * validator
     */
    public boolean checkIfPlanedDateInFuture(final DataDefinition dataDefinition, final Entity transfer) {
        Date plannedDate = (Date) transfer.getField("plannedDate");

        if (plannedDate != null) {
            return plannedDate.after(new Date());
        } else {
            return true;
        }
    }
}

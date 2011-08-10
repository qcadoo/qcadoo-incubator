package com.warehousecorporation.warehouse;

import java.math.BigDecimal;
import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.localization.api.TranslationService;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.security.api.SecurityService;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ComponentState.MessageType;
import com.qcadoo.view.api.ViewDefinitionState;
import java.util.Locale;


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

    @Autowired
    private TranslationService translationService;

    /**
     * onCreate hook
     */
    public void setTransferRequestDate(final DataDefinition dataDefinition, final Entity transfer) {
        if (transfer.getId() == null) {
            transfer.setField("requestWorker", securityService.getCurrentUserName());
            transfer.setField("requestDate", new Date());
        }
    }

    /**
     * business method - implements pure business logic which is not mixed up with GUI code
     */
    public void executeTransfer(final Entity transfer) throws NonExecutableTransferException {

        if(!"01planned".equals(transfer.getField("status"))) {
            throw new NonExecutableTransferException("warehouse.status.not.in.planning");
        }
        transfer.setField("confirmWorker", securityService.getCurrentUserName());
        transfer.setField("confirmDate", new Date());

        DataDefinition resourceDataDefinition = dataDefinitionService.get("warehouse", "resource");
        DataDefinition transferDataDefinition = dataDefinitionService.get("warehouse", "transfer");

        Entity resource = transfer.getBelongsToField("resource");

        BigDecimal currentQuantity = (BigDecimal) resource.getField("quantity");
        BigDecimal transferQuantity = (BigDecimal) transfer.getField("quantity");
        BigDecimal newQuantity;

        if ("01incoming".equals(transfer.getField("type"))) {
            newQuantity = new BigDecimal(currentQuantity.doubleValue() + transferQuantity.doubleValue());
        } else {
            if (transferQuantity.compareTo(currentQuantity) > 0) {
                throw new NonExecutableTransferException("warehouse.not.enought.resource.error");
            }
            newQuantity = new BigDecimal(currentQuantity.doubleValue() + transferQuantity.doubleValue());
        }

        resource.setField("quantity", newQuantity);
        resourceDataDefinition.save(resource);

        transfer.setField("status", "02done");
        transferDataDefinition.save(transfer);
    }

    /**
     * GUI event hook - combines GUI logic with the business method
     */
    public void executeTransferForForm(final ViewDefinitionState viewDefinitionState, final ComponentState state,
        final String[] args) throws NonExecutableTransferException {
        
        try {
            Long transferId = (Long) state.getFieldValue();
            DataDefinition transferDataDefinition = dataDefinitionService.get("warehouse", "transfer");
            Entity transfer = transferDataDefinition.get(transferId);
            executeTransfer(transfer);
            state.performEvent(viewDefinitionState, "refresh", new String[0]);
        } catch(NonExecutableTransferException ex) {
            state.addMessage(translationService.translate(ex.getMessageKey(), state.getLocale()),
                MessageType.FAILURE);
            throw ex;
        }
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

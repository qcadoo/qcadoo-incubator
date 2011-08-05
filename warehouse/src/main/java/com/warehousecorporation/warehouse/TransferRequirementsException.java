/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.warehousecorporation.warehouse;

/**
 *
 * @author walec51
 */
public class TransferRequirementsException extends Exception {

    public TransferRequirementsException(Throwable cause) {
        super(cause);
    }

    public TransferRequirementsException(String message, Throwable cause) {
        super(message, cause);
    }

    public TransferRequirementsException(String message) {
        super(message);
    }

    public TransferRequirementsException() {
    }

}

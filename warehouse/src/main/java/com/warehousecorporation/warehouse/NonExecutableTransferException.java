/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.warehousecorporation.warehouse;

/**
 *
 * @author walec51
 */
public class NonExecutableTransferException extends Exception {
    
    private String messageKey;

    public NonExecutableTransferException(String messageKey) {
        this.messageKey = messageKey;
    }

    public String getMessageKey() {
        return messageKey;
    }
}

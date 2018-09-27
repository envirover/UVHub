/*
 * Envirover confidential
 * 
 *  [2018] Envirover
 *  All Rights Reserved.
 * 
 * NOTICE:  All information contained herein is, and remains the property of 
 * Envirover and its suppliers, if any.  The intellectual and technical concepts
 * contained herein are proprietary to Envirover and its suppliers and may be 
 * covered by U.S. and Foreign Patents, patents in process, and are protected
 * by trade secret or copyright law.
 * 
 * Dissemination of this information or reproduction of this material
 * is strictly forbidden unless prior written permission is obtained
 * from Envirover.
 */

package com.envirover.spl.uvtracks;

/**
 * REST service error object.
 * 
 * @author Pavel Bobov
 *
 */
public class RestError {   

    private final int code;
    private final String message;

    public RestError(int code, String message) {
        this.code = code;
        this.message = message;
    }

    /**
     * Returns HTTP error code.
     * 
     * @return HTTP error code
     */
    public int getCode() {
        return code;
    }

    /**
     * Returns error message.
     * 
     * @return error message
     */
    public String getMessage() {
        return message;
    }

}

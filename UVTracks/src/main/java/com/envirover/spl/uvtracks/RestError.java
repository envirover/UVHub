/*
 * Copyright 2016-2020 Pavel Bobov
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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

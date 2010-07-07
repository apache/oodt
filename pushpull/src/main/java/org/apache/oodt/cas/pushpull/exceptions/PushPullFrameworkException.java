/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


package org.apache.oodt.cas.pushpull.exceptions;

/**
 * 
 * @author bfoster
 * @version $Revision$
 * 
 * <p>
 * Describe your class here
 * </p>.
 */
public class PushPullFrameworkException extends Exception {

    private static final long serialVersionUID = 1500358666243383567L;

    public PushPullFrameworkException() {
        super();
    }

    public PushPullFrameworkException(String msg) {
        super(PushPullFrameworkException.addCallingClassToMsg(msg));
    }

    private static String addCallingClassToMsg(String msg) {
        try {
            Throwable stack = new Throwable();
            stack.fillInStackTrace();
            String[] splitName = stack.getStackTrace()[3].getClassName().split(
                    "\\.");
            return "[" + splitName[splitName.length - 1] + "] " + msg;
        } catch (Exception e) {
            return "[Unknown] " + msg;
        }
    }
}

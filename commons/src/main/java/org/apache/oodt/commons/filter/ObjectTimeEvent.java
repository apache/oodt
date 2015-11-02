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


package org.apache.oodt.commons.filter;

/**
 * 
 * @author bfoster
 * @version $Revision$
 *
 * <p>A {@link TimeEvent} associated with a particular {@link Object}</p>.
 */
public class ObjectTimeEvent<objType> extends TimeEvent {

    private objType timeObj;

    public ObjectTimeEvent(long startTime, long endTime, objType timeObj) {
        super(startTime, endTime);
        this.timeObj = timeObj;
    }

    public ObjectTimeEvent(long startTime, long endTime, double priority,
            objType timeObj) {
        super(startTime, endTime, priority);
        this.timeObj = timeObj;
    }

    public objType getTimeObject() {
        return this.timeObj;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ObjectTimeEvent) {
            ObjectTimeEvent<?> ote = (ObjectTimeEvent<?>) obj;
            return super.equals(obj) && this.timeObj.equals(ote.timeObj);
        } else {
            return false;
        }
    }
    
    @Override
    public String toString() {
        return super.toString() + " - " + timeObj;
    }

    @Override
    public int hashCode() {
        return timeObj != null ? timeObj.hashCode() : 0;
    }
}

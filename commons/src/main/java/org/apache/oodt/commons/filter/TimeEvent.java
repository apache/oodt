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

//JDK imports
import java.util.Arrays;
import java.util.List;

/**
 * 
 * @author bfoster
 * @version $Revision$
 *
 * <p>
 * A basis for time-based queries results over a start/end datetime with
 * a duration
 * </p>.
 */
public class TimeEvent implements Comparable<TimeEvent> {

    protected long startTime, endTime, dur;

    protected double priority;

    public TimeEvent(long startTime, long endTime) {
        this(startTime, endTime, 0);
    }

    public TimeEvent(long startTime, long endTime, double priority) {
        this.startTime = startTime;
        this.endTime = endTime;
        this.dur = this.endTime - this.startTime;
        this.priority = priority;
    }

    public void setPriority(double priority) {
        this.priority = priority;
    }

    public double getPriority() {
        return this.priority;
    }

    public long getStartTime() {
        return this.startTime;
    }

    public long getEndTime() {
        return this.endTime;
    }

    public long getDuration() {
        return this.dur;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof TimeEvent) {
            TimeEvent te = (TimeEvent) obj;
            return te.startTime == this.startTime && te.endTime == this.endTime;
        } else {
            return false;
        }
    }

    @Override
    public String toString() {
        return "[" + this.startTime + "," + this.endTime + "] - "
                + this.priority;
    }
    
    public static boolean happenAtSameTime(TimeEvent te1, TimeEvent te2) {
        return te1.getStartTime() == te2.getStartTime() && te1.getEndTime() == te2.getEndTime();
    }

    public int compareTo(TimeEvent te) {
        return Long.valueOf(this.startTime).compareTo(te.startTime);
    }
 
    public static List<? extends TimeEvent> getTimeOrderedEvents(List<? extends TimeEvent> events) {
        TimeEvent[] eventsArray = events
                .toArray(new TimeEvent[events.size()]);
        Arrays.sort(eventsArray);
        return Arrays.asList(eventsArray);
    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        result = (int) (startTime ^ (startTime >>> 32));
        result = 31 * result + (int) (endTime ^ (endTime >>> 32));
        result = 31 * result + (int) (dur ^ (dur >>> 32));
        temp = Double.doubleToLongBits(priority);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        return result;
    }
}

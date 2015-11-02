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

package org.apache.oodt.cas.workflow.structs;

/**
 * 
 * Priority of a WorkflowProcessor.
 * 
 * @author bfoster
 * @version $Revision$
 * 
 */
public abstract class Priority implements Comparable<Priority> {

  /**
   * Big the better (that is higher the number higher the priority)
   * 
   * @return
   */
  public abstract double getValue();

  public abstract String getName();

  public static final Priority LOW = new Priority() {
    public double getValue() {
      return 0;
    }

    public String getName() {
      return "LOW";
    }
  };
  public static final double DOUBLE = 2.5;
  public static final Priority MEDIUM_LOW = new Priority() {
    public double getValue() {
      return DOUBLE;
    }

    public String getName() {
      return "MEDIUM_LOW";
    }
  };
  public static final Priority MEDIUM = new Priority() {
    public double getValue() {
      return 5;
    }

    public String getName() {
      return "MEDIUM";
    }
  };
  public static final double DOUBLE1 = 7.5;
  public static final Priority MEDIUM_HIGH = new Priority() {
    public double getValue() {
      return DOUBLE1;
    }

    public String getName() {
      return "MEDIUM_HIGH";
    }
  };
  public static final Priority HIGH = new Priority() {
    public double getValue() {
      return 10;
    }

    public String getName() {
      return "HIGH";
    }
  };

  public static Priority getDefault() {
    return MEDIUM;
  }

  public static Priority getPriority(final double priority) {
    if (priority == LOW.getValue()) {
      return LOW;
    } else if (priority == MEDIUM_LOW.getValue()) {
      return MEDIUM_LOW;
    } else if (priority == MEDIUM.getValue()) {
      return MEDIUM;
    } else if (priority == MEDIUM_HIGH.getValue()) {
      return MEDIUM_HIGH;
    } else if (priority == HIGH.getValue()) {
      return HIGH;
    } else {
      return new Priority() {
        public double getValue() {
          return priority;
        }

        public String getName() {
          return "CUSTOM";
        }
      };
    }
  }

  @Override
  public int hashCode() {
    return new Double(this.getValue()).hashCode();
  }

  @Override
  public boolean equals(Object obj) {
    if (obj instanceof Priority) {
      return new Double(this.getValue()).equals(((Priority) obj).getValue());
    } else {
      return false;
    }
  }

  @Override
  public int compareTo(Priority priority) {
    return new Double(this.getValue()).compareTo(priority.getValue());
  }

  @Override
  public String toString() {
    return this.getName() + " : " + Double.toString(this.getValue());
  }

}

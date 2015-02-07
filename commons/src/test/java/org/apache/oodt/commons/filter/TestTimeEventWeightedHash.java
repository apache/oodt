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
import java.util.LinkedList;
import java.util.List;

//Junit imports
import junit.framework.TestCase;

/**
 * @author bfoster
 * @version $Revision$
 * 
 * <p>
 * Test case for {@link TimeEventWeightedHash}
 * </p>.
 */
public class TestTimeEventWeightedHash extends TestCase {

	public void testBuildHash() {
	      List<TimeEvent> events = new LinkedList<TimeEvent>();
	      events.add(new ObjectTimeEvent<String>(31, 32, 2, "1"));
	      events.add(new ObjectTimeEvent<String>(20, 30, 1, "2"));
	      events.add(new ObjectTimeEvent<String>(1, 8, 2, "3"));
	      events.add(new ObjectTimeEvent<String>(8, 15, 1, "4"));
	      events.add(new ObjectTimeEvent<String>(18, 20, 3, "5"));
	      events.add(new ObjectTimeEvent<String>(10, 12, 1, "6"));
	      events.add(new ObjectTimeEvent<String>(20, 30, 2, "7"));
	      events.add(new ObjectTimeEvent<String>(1, 20, 1, "8"));
	      events.add(new ObjectTimeEvent<String>(11, 13, 1, "9"));
	      events.add(new ObjectTimeEvent<String>(15, 20, 1, "10"));
	      events.add(new ObjectTimeEvent<String>(18, 20, 2, "11"));
	      events.add(new ObjectTimeEvent<String>(1, 15, 1, "12"));
	      events.add(new ObjectTimeEvent<String>(1, 18, 2, "13"));
	      TimeEventWeightedHash timeEventHash = TimeEventWeightedHash.buildHash(events);
	      
	      List<ObjectTimeEvent> orderedEvents = (List<ObjectTimeEvent>) timeEventHash.getGreatestWeightedPathAsOrderedList();
	      assertEquals(orderedEvents.size(), 3);
	      assertEquals(orderedEvents.get(0).getTimeObject().toString(), "13");
	      assertEquals(orderedEvents.get(1).getTimeObject().toString(), "7");
	      assertEquals(orderedEvents.get(2).getTimeObject().toString(), "1");
	      
	      events = new LinkedList<TimeEvent>();
	      events.add(new ObjectTimeEvent<String>(33, 215, 1, "1"));
	      events.add(new ObjectTimeEvent<String>(215, 359, 1, "2"));
	      events.add(new ObjectTimeEvent<String>(358, 541, 1, "3"));
	      events.add(new ObjectTimeEvent<String>(541, 723, 1, "4"));
	      events.add(new ObjectTimeEvent<String>(723, 904, 1, "5"));
	      events.add(new ObjectTimeEvent<String>(904, 904, 1, "6"));
	      events.add(new ObjectTimeEvent<String>(904, 1045, 1, "7"));
	      events.add(new ObjectTimeEvent<String>(905, 905, 1, "8"));
	      events.add(new ObjectTimeEvent<String>(906, 907, 1, "9"));
	      events.add(new ObjectTimeEvent<String>(907, 908, 1, "10"));
	      events.add(new ObjectTimeEvent<String>(908, 908, 1, "11"));
	      events.add(new ObjectTimeEvent<String>(908, 909, 1, "12"));
	      events.add(new ObjectTimeEvent<String>(923, 924, 1, "13"));
	      events.add(new ObjectTimeEvent<String>(926, 927, 1, "14"));
	      events.add(new ObjectTimeEvent<String>(927, 928, 1, "15"));
	      events.add(new ObjectTimeEvent<String>(942, 943, 1, "16"));
	      events.add(new ObjectTimeEvent<String>(945, 946, 1, "17"));
	      events.add(new ObjectTimeEvent<String>(946, 946, 1, "18"));
	      events.add(new ObjectTimeEvent<String>(947, 947, 1, "19"));
	      events.add(new ObjectTimeEvent<String>(1004, 1005, 1, "20"));
	      events.add(new ObjectTimeEvent<String>(1005, 1005, 1, "21"));
	      events.add(new ObjectTimeEvent<String>(1006, 1006, 1, "22"));
	      events.add(new ObjectTimeEvent<String>(1007, 1007, 1, "23"));
	      events.add(new ObjectTimeEvent<String>(1025, 1026, 1, "24"));
	      events.add(new ObjectTimeEvent<String>(1026, 1026, 1, "25"));
	      events.add(new ObjectTimeEvent<String>(1027, 1028, 1, "26"));
	      events.add(new ObjectTimeEvent<String>(1045, 1046, 1, "27"));
	      events.add(new ObjectTimeEvent<String>(1045, 1225, 1, "28"));
	      events.add(new ObjectTimeEvent<String>(1225, 1406, 1, "29"));
	      events.add(new ObjectTimeEvent<String>(1225, 1226, 1, "30"));
	      events.add(new ObjectTimeEvent<String>(1226, 1227, 1, "31"));
	      events.add(new ObjectTimeEvent<String>(1227, 1227, 1, "32"));
	      events.add(new ObjectTimeEvent<String>(1228, 1228, 1, "33"));
	      events.add(new ObjectTimeEvent<String>(1406, 1546, 1, "34"));
	      events.add(new ObjectTimeEvent<String>(1545, 1725, 1, "35"));
	      events.add(new ObjectTimeEvent<String>(1545, 1554, 1, "36"));
	      events.add(new ObjectTimeEvent<String>(1725, 1906, 1, "37"));
	      events.add(new ObjectTimeEvent<String>(1906, 2047, 1, "38"));
	      events.add(new ObjectTimeEvent<String>(1906, 1922, 1, "39"));
	      events.add(new ObjectTimeEvent<String>(2047, 2228, 1, "40"));
	      events.add(new ObjectTimeEvent<String>(2228, 2412, 1, "41"));
	      timeEventHash = TimeEventWeightedHash.buildHash(events, 2);
	      
	      orderedEvents = (List<ObjectTimeEvent>) timeEventHash.getGreatestWeightedPathAsOrderedList();
	      assertEquals(orderedEvents.size(), 14);
	      assertEquals(orderedEvents.get(0).getTimeObject().toString(), "1");
	      assertEquals(orderedEvents.get(1).getTimeObject().toString(), "2");
	      assertEquals(orderedEvents.get(2).getTimeObject().toString(), "3");
	      assertEquals(orderedEvents.get(3).getTimeObject().toString(), "4");
	      assertEquals(orderedEvents.get(4).getTimeObject().toString(), "5");
	      assertEquals(orderedEvents.get(5).getTimeObject().toString(), "7");
	      assertEquals(orderedEvents.get(6).getTimeObject().toString(), "28");
	      assertEquals(orderedEvents.get(7).getTimeObject().toString(), "29");
	      assertEquals(orderedEvents.get(8).getTimeObject().toString(), "34");
	      assertEquals(orderedEvents.get(9).getTimeObject().toString(), "35");
	      assertEquals(orderedEvents.get(10).getTimeObject().toString(), "37");
	      assertEquals(orderedEvents.get(11).getTimeObject().toString(), "38");
	      assertEquals(orderedEvents.get(12).getTimeObject().toString(), "40");
	      assertEquals(orderedEvents.get(13).getTimeObject().toString(), "41");
	}
	
}

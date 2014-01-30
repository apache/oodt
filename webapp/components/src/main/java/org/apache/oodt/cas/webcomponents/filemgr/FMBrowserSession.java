/**
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

package org.apache.oodt.cas.webcomponents.filemgr;

//JDK imports
import java.util.List;
import java.util.Vector;

//OODT imports
import org.apache.oodt.cas.filemgr.structs.TermQueryCriteria;

//Wicket imports
import org.apache.wicket.Request;
import org.apache.wicket.Session;
import org.apache.wicket.protocol.http.WebSession;

/**
 *
 * A custom session for the FMBrowserApp, holds its 
 * {@link List} of {@link TermQueryCriteria}.
 *
 * @author mattmann
 * @version $Revision$
 *
 */
public class FMBrowserSession extends WebSession {

  private static final long serialVersionUID = 2014426518833800794L;

  private List<TermQueryCriteria> crit;
  
  /**
   * @param request
   */
  public FMBrowserSession(Request request) {
    super(request);
    this.crit = new Vector<TermQueryCriteria>();
  }
  
  public static FMBrowserSession get(){
    return (FMBrowserSession)Session.get();
  }
  
  public final List<TermQueryCriteria> getCriteria(){
    return this.crit;
  }
  
  public final void setCriteria(List<TermQueryCriteria> crit){
    this.crit = crit;
  }

}

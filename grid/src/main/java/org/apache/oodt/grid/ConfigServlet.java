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

package org.apache.oodt.grid;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Controller servlet for making changes to configuration.
 */
public class ConfigServlet extends GridServlet {
  /**
   * Handle updates to a configuration by saving them and directing back to the
   * config page.
   * 
   * @param req
   *          a <code>HttpServletRequest</code> value.
   * @param res
   *          a <code>HttpServletResponse</code> value.
   * @throws ServletException
   *           if an error occurs.
   * @throws IOException
   *           if an error occurs.
   */
  public void doPost(HttpServletRequest req, HttpServletResponse res)
      throws ServletException, IOException {
    Configuration config = getConfiguration(); // Get the singleton
                                               // configuration
    if (!approveAccess(config, req, res))
      return; // Check if the user can access this page

    ConfigBean cb = getConfigBean(req); // Get the bean
    cb.setMessage(""); // Clear out any message
    if (!cb.isAuthentic())
      throw new ServletException(new AuthenticationRequiredException());
    boolean needSave = false; // Assume no changes for now

    String newPass = req.getParameter("password"); // See if she wants to change
                                                   // password
    if (newPass != null && newPass.length() > 0) { // Got a password, and it's
                                                   // not empty?
      config.setPassword(newPass.getBytes()); // Yes, set its bytes
      needSave = true; // We need to save
    }

    boolean https = config.isHTTPSrequired(); // Are we currently using https?
    String httpsParam = req.getParameter("https"); // Get her https param
    boolean newHttps = httpsParam != null && "on".equals(httpsParam); // See if
                                                                      // she
                                                                      // wants
                                                                      // to use
                                                                      // https
                                                                      // or not
    if (https != newHttps) { // Different?
      config.setHTTPSrequired(newHttps); // Yes, set the new state
      needSave = true; // We need to save
    }

    boolean local = config.isLocalhostRequired(); // Are we requiring localhost?
    String localhostParam = req.getParameter("localhost"); // Get her localhost
                                                           // param
    boolean newLocal = localhostParam != null && "on".equals(localhostParam); // See
                                                                              // if
                                                                              // she
                                                                              // wants
                                                                              // to
                                                                              // require
                                                                              // localhost
    if (local != newLocal) { // Different?
      config.setLocalhostRequired(newLocal); // Yes, set the new state
      needSave = true; // We need to save
    }

    String newKey = req.getParameter("newkey"); // See if she's got a new
                                                // property
    if (newKey != null && newKey.length() > 0) { // And make sure it's nonempty
      String newVal = req.getParameter("newval"); // Got one, get its value
      if (newVal == null)
        newVal = ""; // Make sure it's at least an empty string
      config.getProperties().setProperty(newKey, newVal); // Set the new
                                                          // property
      needSave = true; // We need to save
    }

    needSave |= updateProperties(config.getProperties(), // Make any updates to
                                                         // existing property
        req.getParameterMap()); // values, and see note if we need to save
    try {
      needSave |= updateServers(config, req.getParameterMap(), 'd'); // Same
                                                                     // goes for
                                                                     // product
                                                                     // servers
      needSave |= updateServers(config, req.getParameterMap(), 'm'); // And
                                                                     // profile
                                                                     // servers
      needSave |= updateCodeBases(config, req.getParameterMap()); // And for
                                                                  // code bases
    } catch (MalformedURLException ex) { // Make sure code base URLs are OK
      cb.setMessage("Code bases must be valid URLs (" // Not OK?
          + ex.getMessage() + "); no changes made"); // Let user know via a
                                                     // message
      req.getRequestDispatcher("config.jsp").forward(req, res); // And make no
                                                                // changes until
                                                                // she fixes it
      return;
    }

    if (needSave) { // Do we need to save?
      config.save(); // Then do it already!
      cb.setMessage("Changes saved."); // And let the user know
    } else {
      cb.setMessage("No changes made."); // Oh, no changes were made, let user
                                         // know
    }
    req.getRequestDispatcher("config.jsp").forward(req, res); // Back to the
                                                              // config page
  }

  /**
   * Update changes to the code bases.
   * 
   * @param config
   *          a <code>Configuration</code> value.
   * @param params
   *          a <code>Map</code> value.
   * @return True if changes need to be saved.
   * @throws MalformedURLException
   *           if an error occurs.
   */
  private boolean updateCodeBases(Configuration config, Map params)
      throws MalformedURLException {
    boolean needSave = false; // Assume no change
    List codeBases = config.getCodeBases(); // Get the current code bases

    List toRemove = new ArrayList(); // Hold indexes of code bases to remove
    for (Object o : params.entrySet()) { // For each
      // parameter
      Map.Entry entry = (Map.Entry) o; // Get its entry
      String key = (String) entry.getKey(); // And its name
      String value = ((String[]) entry.getValue())[0]; // And its zeroth value
      if (key.startsWith("delcb-") && "on".equals(value)) { // If it's checked
        Integer index = Integer.valueOf(key.substring(6)); // Parse out the index
        toRemove.add(index); // Add it to the list
      }
    }
    if (!toRemove.isEmpty()) { // And if we have any indexes
      Collections.sort(toRemove); // Sort 'em and put 'em in reverse ...
      Collections.reverse(toRemove); // ... order so we can safely remove them
      for (Object aToRemove : toRemove) { // For each index
        // to remove
        int index = (Integer) aToRemove; // Get the index value
        codeBases.remove(index); // And buh-bye.
      }
      needSave = true; // Definitely need to save changes now
    }

    String[] newCBs = (String[]) params.get("newcb"); // Was there a new code
                                                      // base specified?
    if (newCBs != null && newCBs.length == 1) { // And was there exactly one
                                                // value?
      String newCB = newCBs[0]; // Get that exactly one value
      if (newCB != null && newCB.length() > 0) { // Is it nonnull and nonempty?
        URL newURL = new URL(newCB); // Treat is as an URL
        codeBases.add(newURL); // Add it to the list
        needSave = true; // Ad we gotta save
      }
    }
    return needSave;
  }

  /**
   * Update the list of product/profile servers based on request parameters.
   * 
   * @param config
   *          System configuration
   * @param params
   *          Request parameters.
   * @param type
   *          <code>d</code> (data) if product servers, <code>m</code>
   *          (metadata) if profile servers
   * @return True if any changes were made, false if no changes were made
   */
  private boolean updateServers(Configuration config, Map params, char type) {
    List servers = type == 'd' ? config.getProductServers() : config
        .getProfileServers();
    boolean needSave = false; // Assume no changes for now

    List toRemove = new ArrayList(); // Start with empty list of indexes to
                                     // remove
    for (Object o : params.entrySet()) { // Go
      // through
      // each
      // parameter
      Map.Entry entry = (Map.Entry) o; // Get its key/value
      String name = (String) entry.getKey(); // The key is a String
      if (name.startsWith(type + "rm-")) { // Is it an "drm-" or "mrm-"?
        Integer index = Integer.valueOf(name.substring(4)); // Yes, get it sindex
        toRemove.add(index); // Add it to the list
      }
    }

    if (!toRemove.isEmpty()) { // Got any to remove?
      Collections.sort(toRemove); // We need to go through them in reverse ord-
      Collections.reverse(toRemove); // -er, so that removals don't shift
                                     // indexes
      for (Object aToRemove : toRemove) { // For each index
        int index = (Integer) aToRemove; // Get its int value
        servers.remove(index); // and buh-bye
      }
      needSave = true; // Gotta save after all that, whew.
    }

    if (params.containsKey(type + "-newcn")) { // Adding a new server?
      String[] newClasses = (String[]) params.get(type + "-newcn"); // And the
                                                                    // new class
                                                                    // name
      if (newClasses != null && newClasses.length == 1) { // Are present and
                                                          // there's only one of
                                                          // each
        String newClass = newClasses[0]; // Get the new class
        if (newClass != null && newClass.length() > 0) { // And nonempty
          Server server;
          if (type == 'd') // If it's data
            server = new ProductServer(config, newClass); // It's a product
                                                          // server
          else
            // otherwise it's metadata
            server = new ProfileServer(config, newClass); // Which is a profile
                                                          // server
          servers.add(server); // Add it to the set of servers
          needSave = true; // And after all this, we need to save!
        }
      }
    }

    return needSave;
  }

  /**
   * Update properties based on request parameters.
   * 
   * @param props
   *          <code>Properties</code> to update
   * @param params
   *          Request parameters
   * @return True if changes need to be saved, false otherwise
   */
  private boolean updateProperties(Properties props, Map params) {
    boolean needSave = false; // Assume no save for now
    for (Object o : params.entrySet()) { // Go
      // through
      // each
      // request
      // parameter
      Map.Entry entry = (Map.Entry) o; // Get the key/value
      String name = (String) entry.getKey(); // Key is always a string
      String newValue = ((String[]) entry.getValue())[0]; // Value is String[],
      // get the zeroth
      if (name.startsWith("val-")) { // If the param is "val-"
        String key = name.substring(4); // Then find the key
        if (props.containsKey(key)) { // If that key exists
          String value = props.getProperty(key); // Find its value
          if (value == null || !value.equals(newValue)) {// Are they different?
            props.setProperty(key, newValue); // Yes, set the new value
            needSave = true; // And we need to save
          }
        }
      } else if (name.startsWith("del-")) { // If the param is "del-"
        String key = name.substring(4); // Then find the key
        if (props.containsKey(key)) { // If that key exists
          props.remove(key); // Then remove its mapping
          needSave = true; // And we need to save
        }
      }
    }
    return needSave;
  }
}

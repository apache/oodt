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
package org.apache.oodt.cas.protocol.cli.action;

// JDK imports
import java.util.List;
import java.util.regex.Pattern;



// OODT imports
import org.apache.oodt.cas.cli.exception.CmdLineActionException;
import org.apache.oodt.cas.protocol.Protocol;
import org.apache.oodt.cas.protocol.ProtocolFactory;
import org.apache.oodt.cas.protocol.ProtocolFile;
import org.apache.oodt.cas.protocol.verify.ProtocolVerifierFactory;

/**
 * {@link ProtocolAction} for deleting empty files from site.
 * 
 * @author bfoster (Brian Foster)
 */
public class DeleteEmptyDirectoriesCliAction extends ProtocolCliAction {

  private String directoryRegex = ".+";
  private ProtocolVerifierFactory verifierFactory;

  @Override
  public void execute(ActionMessagePrinter printer) throws CmdLineActionException {
    try {
      Protocol protocol = getProtocolManager().getProtocolBySite(
          getSite(), getAuthentication(), verifierFactory.newInstance());
      List<ProtocolFile> files = protocol.ls();
      for (ProtocolFile file : files) {
        if (file.isDir() && Pattern.matches(directoryRegex, file.getName())) {
          try {
            protocol.delete(file);
            printer.println("Success: " + file.getPath());
          } catch (Exception e) {
            printer.println("Failed: " + file.getPath());
          }
        }
      }
    } catch (Exception e) {
      throw new CmdLineActionException("Failed to delete directories", e);
    }
  }

  public void setDirectoryRegex(String directoryRegex) {
    this.directoryRegex = directoryRegex;
  }

  public void setVerifierFactory(ProtocolVerifierFactory verifierFactory) {
    this.verifierFactory = verifierFactory;
  }
}


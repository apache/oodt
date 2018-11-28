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

package org.apache.oodt.filemgrdelete;

import org.apache.oodt.cas.filemgr.structs.Product;
import org.apache.oodt.cas.filemgr.structs.Reference;
import org.apache.oodt.cas.filemgr.system.XmlRpcFileManagerClient;

import java.io.File;
import java.net.URI;
import java.util.List;

/**
 * Created by bugg on 07/03/14.
 */
public class OODTProcesses {

    public boolean deleteProductByName(OODTConfig config, String filename) throws Exception {
        XmlRpcFileManagerClient client = config.getXMLRpcClient();
        Product p = client.getProductByName(filename);
        List<Reference> refs = client.getProductReferences(p);
        if (refs == null) {
            throw new Exception("FileManager returned null References");
        }
        for (Reference ref : refs) {
            if (!client.removeFile(new File(new URI(ref.getDataStoreReference()))
                    .getAbsolutePath())) {
                throw new Exception("Failed to delete file '"
                        + ref.getDataStoreReference() + "'");
            }
        }
        if (client.removeProduct(p)) {
          //  printer.println("Successfully deleted product '"
           //        + p.getProductName() + "'");
            return true;
        } else {
            throw new Exception("Delete product returned false");
        }
    }


}

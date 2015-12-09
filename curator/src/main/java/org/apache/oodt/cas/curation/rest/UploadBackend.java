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
package org.apache.oodt.cas.curation.rest;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.logging.Level;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.io.IOUtils;
import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.apache.cxf.jaxrs.ext.multipart.MultipartBody;
import org.apache.oodt.cas.curation.configuration.Configuration;

/**
 * A RESTful backend allowing for the user to upload files
 * to the CAS-Curator.
 * 
 * @author starchmd
 */
@Path("upload")
public class UploadBackend {
    /**
     * Construct using configuration to determine upload area
     */
    public UploadBackend() {}

    @POST
    @Path("file")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    /**
     * Handles the uploading of the data, by copying all data out of the input stream.
     * @param mbody - body of the multipart post
     * @return - OK response on success
     */
    public Response upload(MultipartBody mbody) {
        try {
        	List<Attachment> attachments = mbody.getAllAttachments();
            for (Attachment attachment : attachments) {
                String filename = attachment.getContentDisposition().getParameter("filename");    
                try {
                    InputStream in = attachment.getDataHandler().getInputStream();
                    OutputStream os = new FileOutputStream(new File(Configuration.getWithReplacement(Configuration.UPLOAD_AREA_CONFIG),filename));
                    IOUtils.copy(in,os);
                    in.close();
                    os.close();
                } catch(IOException e) {
                    throw new IOException("Failed uploading file:",e);
                }
            }
            return Response.ok().build();
        } catch(Exception e) {
            return ExceptionResponseHandler.BuildExceptionResponse(e);
        }
    }
}

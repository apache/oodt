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

import javax.ws.rs.QueryParam;
import org.apache.oodt.cas.curation.configuration.Configuration;
import org.apache.oodt.cas.curation.directory.Directory;
import org.apache.oodt.cas.curation.directory.FileDirectory;
import org.apache.oodt.cas.curation.directory.S3Directory;
import org.apache.oodt.commons.validation.DirectoryValidator;

import com.google.gson.Gson;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

/**
 * A directory backend that returns JSON object representing a directory structure.
 * Will also list available directory backends.
 *  
 * @author starchmd
 */
@Path("directory")
public class DirectoryBackend {
    private Map<String, DirectoryValidator> validators = new HashMap<>();
    Map<String,Directory> types = new HashMap<String,Directory>();
    Gson gson = new Gson();
    /**
     * Construct a directory backend with hard-coded directories
     */
    public DirectoryBackend() {

    }
    @GET
    @Produces("application/json")
    /**
     * Get the types of directory backends.
     */
    public String getDirectoryTypes() {
        //TODO: update this loading code to be user-configured and be fed off of "type"
        if (types.get("files") == null)
            bootstrapValidator("files");
            types.put("files", new FileDirectory(Configuration.getWithReplacement(Configuration.STAGING_AREA_CONFIG),
                validators.get("files")));
        return gson.toJson(types.keySet());
    }

    @GET
    @Produces("application/json")
    @Path("{type}")
    /**
     * Returns the listing of the given directory type
     * @param type - type of directory to list
     */
    public String list(@PathParam("type") String type,
        @QueryParam("s3user")String s3user) throws Exception {
      //TODO: update this loading code to be user-configured and be fed off of "type"
      if (type.equals("s3")) {

        bootstrapValidator(type);
        String s3StagingArea = Configuration.getWithReplacement(Configuration.S3_STAGING_AREA_CONFIG);
        if (s3user != null) {
          if (!s3user.endsWith("/")) {
            s3StagingArea = s3user + "/";
          }
        }
        types.put("s3", new S3Directory(s3StagingArea,
            validators.get(type),
            Configuration.getWithReplacement(Configuration.AWS_BUCKET_CONFIG),
            Configuration.getWithReplacement(Configuration.USE_INSTANCE_CREDENTIALS)));

      }
      else if (types.get("files") == null) {
        bootstrapValidator(type);
        types.put("files",
            new FileDirectory(Configuration.getWithReplacement(Configuration.STAGING_AREA_CONFIG),
                validators.get(type)));
      }
      return gson.toJson(types.get(type).list());
    }

  /**
   * Initialise the validator engine as defined in web.xml.
   */

  private void bootstrapValidator(String type){
        if(validators.get(type)==null) {
            String vclass = type.equals("s3")? Configuration.getWithReplacement(Configuration.S3BACKEND_VALIDATOR):
                Configuration.getWithReplacement(Configuration.DIRECTORYBACKEND_VALIDATOR);
            if (vclass != null && !vclass.equals("")) {
                Class<?> clazz = null;
                try {
                    clazz = Class.forName(vclass);

                    Constructor<?> constructor = clazz.getConstructor();
                    this.validators.put(type, (DirectoryValidator) constructor.newInstance());

                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                } catch (NoSuchMethodException e) {
                    e.printStackTrace();
                } catch (InstantiationException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (InvocationTargetException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
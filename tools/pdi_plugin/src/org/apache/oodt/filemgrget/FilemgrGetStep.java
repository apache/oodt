package org.apache.oodt.filemgrget;

import org.apache.oodt.cas.filemgr.structs.exceptions.CatalogException;
import org.apache.oodt.cas.filemgr.structs.exceptions.ConnectionException;
import org.apache.oodt.cas.filemgr.structs.exceptions.DataTransferException;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.*;
import sun.util.logging.resources.logging_de;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Map;

import static org.pentaho.di.core.row.RowDataUtil.allocateRowData;

/**
* Copyright 2014 OSBI Ltd
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

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

public class FilemgrGetStep extends BaseStep implements StepInterface {

    private OODTConfig oodt = new OODTConfig();
    private OODTProcesses oodtproc = new OODTProcesses();
    private FilemgrGetStepData data;
    private FilemgrGetStepMeta meta;
	/**
	 * The constructor should simply pass on its arguments to the parent class.
	 * 
	 * @param s 				step description
	 * @param stepDataInterface	step data class
	 * @param c					step copy
	 * @param t					transformation description
	 * @param dis				transformation executing
	 */
	public FilemgrGetStep(StepMeta s, StepDataInterface stepDataInterface, int c, TransMeta t, Trans dis) {
		super(s, stepDataInterface, c, t, dis);
	}
	
	/**
	 * This method is called by PDI during transformation startup. 
	 * 
	 * It should initialize required for step execution. 
	 * 
	 * The meta and data implementations passed in can safely be cast
	 * to the step's respective implementations. 
	 * 
	 * It is mandatory that super.init() is called to ensure correct behavior.
	 * 
	 * Typical tasks executed here are establishing the connection to a database,
	 * as wall as obtaining resources, like file handles.
	 * 
	 * @param smi 	step meta interface implementation, containing the step settings
	 * @param sdi	step data interface implementation, used to store runtime information
	 * 
	 * @return true if initialization completed successfully, false if there was an error preventing the step from working. 
	 *  
	 */
	public boolean init(StepMetaInterface smi, StepDataInterface sdi) {
		// Casting to step-specific implementation classes is safe
		meta = (FilemgrGetStepMeta) smi;
		data = (FilemgrGetStepData) sdi;


            logDetailed("loading xmlrpcclient");
        try {
            oodt.loadXMLRpcClient(meta.getServerURLField());
        } catch (MalformedURLException e) {
            logError("Incorrect URL", e);
        } catch (ConnectionException e) {
            logError("There was a problem connecting", e);
        }
        logDetailed("finished loading xmlrpcclient");


        return super.init(meta, data);
	}


    private Object[] buildEmptyRow() {

        return allocateRowData(data.outputRowMeta.size());
    }

	/**
	 * Once the transformation starts executing, the processRow() method is called repeatedly
	 * by PDI for as long as it returns true. To indicate that a step has finished processing rows
	 * this method must call setOutputDone() and return false;
	 *
	 * Steps which process incoming rows typically call getRow() to read a single row from the
	 * input stream, change or add row content, call putRow() to pass the changed row on
	 * and return true. If getRow() returns null, no more rows are expected to come in,
	 * and the processRow() implementation calls setOutputDone() and returns false to
	 * indicate that it is done too.
	 *
	 * Steps which generate rows typically construct a new row Object[] using a call to
	 * RowDataUtil.allocateRowData(numberOfFields), add row content, and call putRow() to
	 * pass the new row on. Above process may happen in a loop to generate multiple rows,
	 * at the end of which processRow() would call setOutputDone() and return false;
	 *
	 * @param smi the step meta interface containing the step settings
	 * @param sdi the step data interface that should be used to store
	 *
	 * @return true to indicate that the function should be called again, false if the step is done
	 */
	public boolean processRow(StepMetaInterface smi, StepDataInterface sdi) throws KettleException {


        if ( first ) {
            first = false;
            data.outputRowMeta = new RowMeta();
            meta.getFields( data.outputRowMeta, getStepname(), null, null, this, repository, metaStore );
        }

        if(meta.getProcessType() == Process.LIST){
            try {
                Map<String, Map<String, String>> products = oodtproc.getAllProducts(oodt, environmentSubstitute(meta.getProductTypeField()));
                for (Map.Entry<String, Map<String,String>> entry : products.entrySet())
                {
                    Object[] row = buildEmptyRow();

                    incrementLinesRead();

                    row[0] = entry.getKey();
                    int i = 1;
                    for(Map.Entry<String,String> innerentry : entry.getValue().entrySet()){
                        row[i] = innerentry.getValue();
                        i++;
                    }

                    putRow(data.outputRowMeta, row);
                }

            } catch (Exception e) {
                logError("Could not get data", e);
            }

        }
        else if(meta.getProcessType() == Process.GET && meta.getSearchType() == Search.ID){

            try {
                String lookup = meta.getLookup();
                if(!Const.isEmpty(lookup)){
                    lookup = environmentSubstitute(lookup);
                }

                Object[] row = buildEmptyRow();
                row[0] = oodtproc.getProductByID(oodt, lookup);
                putRow(data.outputRowMeta, row);

            } catch (CatalogException e) {
                logError("Catalog Exception", e);
            } catch (DataTransferException e) {
                logError("Data Transfer Exception", e);
            } catch (IOException e) {
                logError("IO Exception",e);
            }
        }
        else if(meta.getProcessType() == Process.GET && meta.getSearchType() == Search.NAME){
            try {
                String lookup = meta.getLookup();
                if(!Const.isEmpty(lookup)){
                    lookup = environmentSubstitute(lookup);
                }

                Object[] row = buildEmptyRow();
                row[0] = oodtproc.getProductByName(oodt, lookup);
                putRow(data.outputRowMeta, row);
            } catch (CatalogException e) {
                logError("Catalog Exception", e);
            } catch (DataTransferException e) {
                logError("Data Transfer Exception", e);
            } catch (IOException e) {
                logError("IO Exception", e);
            }
        }


		// log progress if it is time to to so
		if (checkFeedback(getLinesRead())) {
			logBasic("Linenr " + getLinesRead()); // Some basic logging
		}

		// indicate that processRow() should be called again
        setOutputDone();
		return false;
	}

	/**
	 * This method is called by PDI once the step is done processing.
	 *
	 * The dispose() method is the counterpart to init() and should release any resources
	 * acquired for step execution like file handles or database connections.
	 *
	 * The meta and data implementations passed in can safely be cast
	 * to the step's respective implementations.
	 *
	 * It is mandatory that super.dispose() is called to ensure correct behavior.
	 *
	 * @param smi 	step meta interface implementation, containing the step settings
	 * @param sdi	step data interface implementation, used to store runtime information
	 */
	public void dispose(StepMetaInterface smi, StepDataInterface sdi) {

		// Casting to step-specific implementation classes is safe
		FilemgrGetStepMeta meta = (FilemgrGetStepMeta) smi;
		FilemgrGetStepData data = (FilemgrGetStepData) sdi;
		
		super.dispose(meta, data);
	}

}

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

package org.apache.oodt.filemgrget;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.*;
import org.pentaho.di.core.Const;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepDialogInterface;
import org.pentaho.di.ui.core.widget.TextVar;
import org.pentaho.di.ui.trans.step.BaseStepDialog;

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
public class FilemgrGetStepDialog extends BaseStepDialog implements StepDialogInterface {

	/**
	 *	The PKG member is used when looking up internationalized strings.
	 *	The properties file with localized keys is expected to reside in 
	 *	{the package of the class specified}/messages/messages_{locale}.properties   
	 */
	private static Class<?> PKG = FilemgrGetStepMeta.class; // for i18n purposes

	// this is the object the stores the step's settings
	// the dialog reads the settings from it when opening
	// the dialog writes the settings to it when confirmed 
	private FilemgrGetStepMeta meta;

	// text field holding the name of the field to add to the row stream
	//private Text wHelloFieldName;

    // text field holding the name of the field to check the filename against
    private TextVar wFilenameField;
    private Text wServerURLField;
    private TextVar wResultField;
    private Button[] radioButtons2 = new Button[2];
    private Button[] radioButtons = new Button[2];
    private Search selectedsearch;
    private Process selectedprocess;
    /**
	 * The constructor should simply invoke super() and save the incoming meta
	 * object to a local variable, so it can conveniently read and write settings
	 * from/to it.
	 * 
	 * @param parent 	the SWT shell to open the dialog in
	 * @param in		the meta object holding the step's settings
	 * @param transMeta	transformation description
	 * @param sname		the step name
	 */
	public FilemgrGetStepDialog(Shell parent, Object in, TransMeta transMeta, String sname) {
		super(parent, (BaseStepMeta) in, transMeta, sname);
		meta = (FilemgrGetStepMeta) in;
	}

	/**
	 * This method is called by Spoon when the user opens the settings dialog of the step.
	 * It should open the dialog and return only once the dialog has been closed by the user.
	 * 
	 * If the user confirms the dialog, the meta object (passed in the constructor) must
	 * be updated to reflect the new step settings. The changed flag of the meta object must 
	 * reflect whether the step configuration was changed by the dialog.
	 * 
	 * If the user cancels the dialog, the meta object must not be updated, and its changed flag
	 * must remain unaltered.
	 * 
	 * The open() method must return the name of the step after the user has confirmed the dialog,
	 * or null if the user cancelled the dialog.
	 */
	public String open() {

		// store some convenient SWT variables 
		Shell parent = getParent();
		Display display = parent.getDisplay();

		// SWT code for preparing the dialog
		shell = new Shell(parent, SWT.DIALOG_TRIM | SWT.RESIZE | SWT.MIN | SWT.MAX);
		props.setLook(shell);
		setShellImage(shell, meta);
		
		// Save the value of the changed flag on the meta object. If the user cancels
		// the dialog, it will be restored to this saved value.
		// The "changed" variable is inherited from BaseStepDialog
		changed = meta.hasChanged();
		
		// The ModifyListener used on all controls. It will update the meta object to 
		// indicate that changes are being made.
		ModifyListener lsMod = new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				meta.setChanged();
			}
		};
		
		// ------------------------------------------------------- //
		// SWT code for building the actual settings dialog        //
		// ------------------------------------------------------- //
		FormLayout formLayout = new FormLayout();
		formLayout.marginWidth = Const.FORM_MARGIN;
		formLayout.marginHeight = Const.FORM_MARGIN;

		shell.setLayout(formLayout);
		shell.setText(BaseMessages.getString(PKG, "FilemgrGetStep.Name"));

		int middle = props.getMiddlePct();
		int margin = Const.MARGIN;

		// Stepname line
		wlStepname = new Label(shell, SWT.RIGHT);
		wlStepname.setText(BaseMessages.getString(PKG, "System.Label.StepName")); 
		props.setLook(wlStepname);
		fdlStepname = new FormData();
		fdlStepname.left = new FormAttachment(0, 0);
		fdlStepname.right = new FormAttachment(middle, -margin);
		fdlStepname.top = new FormAttachment(0, margin);
		wlStepname.setLayoutData(fdlStepname);
		
		wStepname = new Text(shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
		wStepname.setText(stepname);
		props.setLook(wStepname);
		wStepname.addModifyListener(lsMod);
		fdStepname = new FormData();
		fdStepname.left = new FormAttachment(middle, 0);
		fdStepname.top = new FormAttachment(0, margin);
		fdStepname.right = new FormAttachment(100, 0);
		wStepname.setLayoutData(fdStepname);

        // servername field value
        Label wlServerName = new Label(shell, SWT.RIGHT);
        wlServerName.setText(BaseMessages.getString(PKG, "FilemgrGet.ServerURL.Label"));
        props.setLook(wlServerName);
        FormData fdlServerName = new FormData();
        fdlServerName.left = new FormAttachment(0, 0);
        fdlServerName.right = new FormAttachment(middle, -margin);
        fdlServerName.top = new FormAttachment(wStepname, margin);
        wlServerName.setLayoutData(fdlServerName);

        wServerURLField = new Text(shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
        props.setLook(wServerURLField);
        wServerURLField.addModifyListener(lsMod);
        FormData fdServerName = new FormData();
        fdServerName.left = new FormAttachment(middle, 0);
        fdServerName.right = new FormAttachment(100, 0);
        fdServerName.top = new FormAttachment(wStepname, margin);
        wServerURLField.setLayoutData(fdServerName);

		// product type
		Label wlValName = new Label(shell, SWT.RIGHT);
		wlValName.setText(BaseMessages.getString(PKG, "FilemgrGet.ProductType.Label"));
		props.setLook(wlValName);
		FormData fdlValName = new FormData();
		fdlValName.left = new FormAttachment(0, 0);
		fdlValName.right = new FormAttachment(middle, -margin);
		fdlValName.top = new FormAttachment(wServerURLField, margin);
		wlValName.setLayoutData(fdlValName);

		wFilenameField = new TextVar(transMeta, shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
		props.setLook(wFilenameField);
		wFilenameField.addModifyListener(lsMod);
		FormData fdValName = new FormData();
		fdValName.left = new FormAttachment(middle, 0);
		fdValName.right = new FormAttachment(100, 0);
		fdValName.top = new FormAttachment(wServerURLField, margin);
		wFilenameField.setLayoutData(fdValName);


        // processtype value
        Label wlProcessTypeName = new Label(shell, SWT.RIGHT);
        wlProcessTypeName.setText(BaseMessages.getString(PKG, "FilemgrGet.ProcessType.Label"));
        props.setLook(wlProcessTypeName);
        FormData fdlProcessTypeName = new FormData();
        fdlProcessTypeName.left = new FormAttachment(0, 0);
        fdlProcessTypeName.right = new FormAttachment(middle, -margin);
        fdlProcessTypeName.top = new FormAttachment(wFilenameField, margin);
        wlProcessTypeName.setLayoutData(fdlProcessTypeName);

        Composite composite = new Composite(shell, SWT.NULL);
        composite.setLayout(new RowLayout());
        radioButtons2[0] = new Button(composite, SWT.RADIO);
        radioButtons2[0].setSelection(true);
        radioButtons2[0].setText("List Products");
        radioButtons2[0].pack();
        radioButtons2[1] = new Button(composite, SWT.RADIO);
        radioButtons2[1].setSelection(false);
        radioButtons2[1].setText("Get Product");
        radioButtons2[1].pack();
        props.setLook(radioButtons2[0]);
        props.setLook(radioButtons2[1]);


        //wFilenameField.addModifyListener(lsMod);
        FormData fdProcessTypeName = new FormData();
        fdProcessTypeName.left = new FormAttachment(middle, 0);
        fdProcessTypeName.right = new FormAttachment(100, 0);
        fdProcessTypeName.top = new FormAttachment(wFilenameField, margin);
        composite.setLayoutData(fdProcessTypeName);


        // search type value
        Label wlSearchName = new Label(shell, SWT.RIGHT);
        wlSearchName.setText(BaseMessages.getString(PKG, "FilemgrGet.SearchType.Label"));
        props.setLook(wlSearchName);
        FormData fdlSearchName = new FormData();
        fdlSearchName.left = new FormAttachment(0, 0);
        fdlSearchName.right = new FormAttachment(middle, -margin);
        fdlSearchName.top = new FormAttachment(composite, margin);
        wlSearchName.setLayoutData(fdlSearchName);

        Composite composite1 = new Composite(shell, SWT.NULL);
        composite1.setLayout(new RowLayout());
        radioButtons[0] = new Button(composite1, SWT.RADIO);
        radioButtons[0].setSelection(true);
        radioButtons[0].setText("Name");
        radioButtons[0].pack();
        radioButtons[1] = new Button(composite1, SWT.RADIO);
        radioButtons[1].setSelection(false);
        radioButtons[1].setText("ID");
        radioButtons[1].pack();
        props.setLook(radioButtons[0]);
        props.setLook(radioButtons[1]);
        radioButtons[0].addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                selectedsearch = Search.NAME;
            }
        });
        radioButtons[1].addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                selectedsearch = Search.ID;
            }
        });

        FormData fdSearchName = new FormData();
        fdSearchName.left = new FormAttachment(middle, 0);
        fdSearchName.right = new FormAttachment(100, 0);
        fdSearchName.top = new FormAttachment(composite, margin);
        composite1.setLayoutData(fdSearchName);

        if(meta.getProcessType()==Process.LIST){
            radioButtons[0].setEnabled(false);
            radioButtons[1].setEnabled(false);
        }


        // servername field value
        Label wlResultName = new Label(shell, SWT.RIGHT);
        wlResultName.setText(BaseMessages.getString(PKG, "FilemgrGet.Result.Label"));
        props.setLook(wlResultName);
        FormData fdlResultName = new FormData();
        fdlResultName.left = new FormAttachment(0, 0);
        fdlResultName.right = new FormAttachment(middle, -margin);
        fdlResultName.top = new FormAttachment(composite1, margin);
        wlResultName.setLayoutData(fdlResultName);

        wResultField = new TextVar(transMeta, shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
        props.setLook(wResultField);
        wResultField.addModifyListener(lsMod);
        FormData fdResultName = new FormData();
        fdResultName.left = new FormAttachment(middle, 0);
        fdResultName.right = new FormAttachment(100, 0);
        fdResultName.top = new FormAttachment(composite1, margin);
        wResultField.setLayoutData(fdResultName);
        wResultField.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent e) {
                wResultField.setToolTipText(transMeta.environmentSubstitute(wResultField.getText()));
            }
        });

        if(meta.getProcessType()==Process.LIST){
            wResultField.setEnabled(false);
        }
        // OK and cancel buttons
		wOK = new Button(shell, SWT.PUSH);
		wOK.setText(BaseMessages.getString(PKG, "System.Button.OK")); 
		wCancel = new Button(shell, SWT.PUSH);
		wCancel.setText(BaseMessages.getString(PKG, "System.Button.Cancel")); 

		BaseStepDialog.positionBottomButtons(shell, new Button[] { wOK, wCancel }, margin, wResultField);

		// Add listeners for cancel and OK
		lsCancel = new Listener() {
			public void handleEvent(Event e) {cancel();}
		};
		lsOK = new Listener() {
			public void handleEvent(Event e) {ok();}
		};

		wCancel.addListener(SWT.Selection, lsCancel);
		wOK.addListener(SWT.Selection, lsOK);

        /**
         * Radio button listeners
         */
        radioButtons2[0].addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                selectedprocess = Process.LIST;
                wResultField.setEnabled(false);
                radioButtons[0].setEnabled(false);
                radioButtons[1].setEnabled(false);

            }
        });
        radioButtons2[1].addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                selectedprocess = Process.GET;
                radioButtons[0].setEnabled(true);
                radioButtons[1].setEnabled(true);
                wResultField.setEnabled(true);
            }
        });

		// default listener (for hitting "enter")
		lsDef = new SelectionAdapter() {
			public void widgetDefaultSelected(SelectionEvent e) {ok();}
		};
		wStepname.addSelectionListener(lsDef);
		wFilenameField.addSelectionListener(lsDef);
        wServerURLField.addSelectionListener(lsDef);
        wResultField.addSelectionListener(lsDef);
		// Detect X or ALT-F4 or something that kills this window and cancel the dialog properly
		shell.addShellListener(new ShellAdapter() {
			public void shellClosed(ShellEvent e) {cancel();}
		});
		
		// Set/Restore the dialog size based on last position on screen
		// The setSize() method is inherited from BaseStepDialog
		setSize();

		// populate the dialog with the values from the meta object
		populateDialog();
		
		// restore the changed flag to original value, as the modify listeners fire during dialog population 
		meta.setChanged(changed);

		// open dialog and enter event loop 
		shell.open();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch())
				display.sleep();
		}

		// at this point the dialog has closed, so either ok() or cancel() have been executed
		// The "stepname" variable is inherited from BaseStepDialog
		return stepname;
	}
	
	/**
	 * This helper method puts the step configuration stored in the meta object
	 * and puts it into the dialog controls.
	 */
	private void populateDialog() {
		wStepname.selectAll();
		wFilenameField.setText(meta.getProductTypeField());
        wServerURLField.setText(meta.getServerURLField());
        wResultField.setText(meta.getResultField());
        if(meta.getSearchType() == Search.NAME){
            radioButtons[0].setSelection(true);
            radioButtons[1].setSelection(false);
            selectedsearch = Search.NAME;
        }
        else{
            radioButtons[1].setSelection(true);
            radioButtons[0].setSelection(false);
            selectedsearch = Search.ID;
        }

        if(meta.getProcessType() == Process.LIST){
            radioButtons2[0].setSelection(true);
            radioButtons2[1].setSelection(false);
            selectedprocess= Process.LIST;
        }
        else{
            radioButtons2[1].setSelection(true);
            radioButtons2[0].setSelection(false);
            selectedprocess = Process.GET;
        }

	}

	/**
	 * Called when the user cancels the dialog.  
	 */
	private void cancel() {
		// The "stepname" variable will be the return value for the open() method. 
		// Setting to null to indicate that dialog was cancelled.
		stepname = null;
		// Restoring original "changed" flag on the met aobject
		meta.setChanged(changed);
		// close the SWT dialog window
		dispose();
	}
	
	/**
	 * Called when the user confirms the dialog
	 */
	private void ok() {
		stepname = wStepname.getText(); 
        meta.setProductName(wFilenameField.getText());
        meta.setServerURLField(wServerURLField.getText());
        meta.setResultField(wResultField.getText());
        meta.setProcessType(selectedprocess);
        meta.setSearchType(selectedsearch);
		// close the SWT dialog window
		dispose();
	}
}

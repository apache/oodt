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


package org.apache.oodt.cas.pushpull.util;

//AWT Imports
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

//Swing Imports
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

/**
 * 
 * @author bfoster
 * @version $Revision$
 *
 * <p>Use to evaluate whether your regular expressions are 
 * accepting the correct strings
 * </p>.
 */
public class ExpressionValidator extends JPanel {

	private static final long serialVersionUID = -2840589940304298547L;

	private JLabel exprLabel;
	private JLabel validateLabel;
	private JLabel resultLabel;
	
	private JFormattedTextField exprField;
	private JFormattedTextField validateField;
	private JFormattedTextField resultField;

	public ExpressionValidator() {
		super(new BorderLayout());

		exprLabel = new JLabel("Regular Expression: ");
		validateLabel = new JLabel("Validate String: ");
		resultLabel = new JLabel("Result: ");

		exprField = new JFormattedTextField();
		exprField.setColumns(60);

		validateField = new JFormattedTextField();
		validateField.setColumns(60);

		resultField = new JFormattedTextField();
		resultField.setColumns(60);
		resultField.setEditable(false);
		resultField.setForeground(Color.red);

		exprLabel.setLabelFor(exprField);
		validateLabel.setLabelFor(validateField);
		resultLabel.setLabelFor(resultField);

		JPanel labelPane = new JPanel(new GridLayout(0, 1));
		labelPane.add(exprLabel);
		labelPane.add(validateLabel);
		labelPane.add(resultLabel);

		JPanel fieldPane = new JPanel(new GridLayout(0, 1));
		fieldPane.add(exprField);
		fieldPane.add(validateField);
		fieldPane.add(resultField);

		JPanel buttonPane = new JPanel(new BorderLayout());
		JButton validate = new JButton("Validate");
		validate.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				try {
					boolean value = ((String) validateField.getText()).matches((String) exprField.getText());
					resultField.setText(value + "");
				}catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
		buttonPane.add(validate, BorderLayout.EAST);
		
		setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
		add(labelPane, BorderLayout.WEST);
		add(fieldPane, BorderLayout.CENTER);
		add(buttonPane, BorderLayout.EAST);
	}

	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
			    Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
				JFrame frame = new JFrame("Regular Expression Evaluator");
				frame.setSize(dim.width / 2, dim.height / 2);
			    frame.setLocation(dim.width / 3, dim.height / 3);
				frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
				frame.add(new ExpressionValidator());
				frame.pack();
				frame.setVisible(true);
			}
		});
	}

}

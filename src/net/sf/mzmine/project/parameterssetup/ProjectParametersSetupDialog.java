/*
 * Copyright 2006-2007 The MZmine Development Team
 * 
 * This file is part of MZmine.
 * 
 * MZmine is free software; you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 * 
 * MZmine is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * MZmine; if not, write to the Free Software Foundation, Inc., 51 Franklin St,
 * Fifth Floor, Boston, MA 02110-1301 USA
 */

package net.sf.mzmine.project.parameterssetup;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Hashtable;
import java.util.Iterator;

import javax.swing.BorderFactory;
import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;

import net.sf.mzmine.data.Parameter;
import net.sf.mzmine.data.ParameterType;
import net.sf.mzmine.data.RawDataFile;
import net.sf.mzmine.desktop.Desktop;
import net.sf.mzmine.main.MZmineCore;
import net.sf.mzmine.project.MZmineProject;
import net.sf.mzmine.util.dialogs.ExitCode;

public class ProjectParametersSetupDialog extends JDialog implements
		ActionListener {

	private JPanel panelParameterValues;
	private JScrollPane scrollParameterValues;
	private JTable tableParameterValues;
	private ParameterTableModel tablemodelParameterValues;
	private JPanel panelRemoveParameterButton;
	private JButton buttonAddParameter;
	private JButton buttonRemoveParameter;
	private JButton buttonImportParameters;
	private JPanel panelOKCancelButtons;
	private JButton buttonOK;
	private JButton buttonCancel;

	private ExitCode exitCode = ExitCode.UNKNOWN;

	private RawDataFile[] dataFiles;
	private Hashtable<Parameter, Object[]> parameterValues;

	private Desktop desktop;

	public ProjectParametersSetupDialog() {
		super(MZmineCore.getDesktop().getMainFrame(), true);

		parameterValues = new Hashtable<Parameter, Object[]>();

		this.dataFiles = MZmineCore.getCurrentProject().getDataFiles();
		this.desktop = MZmineCore.getDesktop();

		setTitle("Setup project parameters and values");
		initComponents();

		copyParameterValuesFromRawDataFiles();

		setupTableModel();

		setLocationRelativeTo(desktop.getMainFrame());

	}

	public ExitCode getExitCode() {
		return exitCode;
	}

	public void actionPerformed(ActionEvent actionEvent) {

		Object src = actionEvent.getSource();

		if (src == buttonImportParameters) {
			// Import parameter values from a file
			ProjectParametersImporter importer = new ProjectParametersImporter(
					this);
			importer.importParameters();

		}

		if (src == buttonOK) {

			// Validate parameter values
			if (!validateParameterValues())
				return;

			// Copy parameter values to raw data files
			copyParameterValuesToRawDataFiles();

			exitCode = ExitCode.OK;
			dispose();
		}

		if (src == buttonCancel) {
			exitCode = ExitCode.CANCEL;
			dispose();
		}

		if (src == buttonAddParameter) {

			AddProjectParameterDialog addDialog = new AddProjectParameterDialog(
					this);
			addDialog.setVisible(true);

		}

		if (src == buttonRemoveParameter) {
			int selectedColumn = tableParameterValues.getSelectedColumn();
			Parameter parameter = tablemodelParameterValues
					.getParameter(selectedColumn);
			if (parameter == null) {
				desktop
						.displayErrorMessage("Select a parameter column from the table first.");
				return;
			}

			removeParameter(parameter);

		}

	}

	/**
	 * Adds a new parameter to the table of the dialog (not MZmineProject)
	 * 
	 * @param parameter
	 */
	protected void addParameter(Parameter parameter) {
		// Initialize with default value
		Object[] values = new Object[dataFiles.length];
		for (int dataFileIndex = 0; dataFileIndex < dataFiles.length; dataFileIndex++)
			values[dataFileIndex] = parameter.getDefaultValue();

		// Add this newly created parameter to hashtable and reset table
		parameterValues.put(parameter, values);
		setupTableModel();
	}

	/**
	 * Removes a parameter from the table
	 * 
	 * @param parameter
	 */
	protected void removeParameter(Parameter parameter) {
		parameterValues.remove(parameter);
		setupTableModel();
	}

	/**
	 * Returns parameter by name
	 */
	protected Parameter getParameter(String parameterName) {
		Iterator<Parameter> parameterIterator = parameterValues.keySet()
				.iterator();

		while (parameterIterator.hasNext()) {
			Parameter p = parameterIterator.next();
			if (p.getName().equals(parameterName))
				return p;
		}

		return null;
	}

	/**
	 * Sets value of a parameter in the table (not MZmineProject)
	 * 
	 * @param parameter
	 * @param dataFile
	 * @param value
	 */
	protected void setParameterValue(Parameter parameter, String dataFileName,
			Object value) {
		// Find index for data file
		int dataFileIndex = 0;
		while (dataFileIndex < dataFiles.length) {
			if (dataFiles[dataFileIndex].getFileName().equals(dataFileName))
				break;
			dataFileIndex++;
		}

		// File not found?
		if (dataFileIndex == dataFiles.length)
			return;

		// Set parameter value
		Object[] values = parameterValues.get(parameter);
		values[dataFileIndex] = value;

	}

	private boolean validateParameterValues() {
		// Create new parameters and set values
		for (int columnIndex = 0; columnIndex < parameterValues.keySet().size(); columnIndex++) {
			Parameter parameter = tablemodelParameterValues
					.getParameter(columnIndex + 1);

			if (parameter.getType() == ParameterType.DOUBLE) {
				Double minValue = null;
				Double maxValue = null;
				if (parameter.getMinimumValue() != null)
					minValue = (Double) (parameter.getMinimumValue());
				if (parameter.getMaximumValue() != null)
					maxValue = (Double) (parameter.getMaximumValue());

				for (int dataFileIndex = 0; dataFileIndex < dataFiles.length; dataFileIndex++) {
					Object objValue = tablemodelParameterValues.getValueAt(
							dataFileIndex, columnIndex + 1);
					Double value = null;
					if (objValue instanceof Double)
						value = (Double) objValue;
					if (objValue instanceof String) {
						try {
							value = Double.parseDouble((String) objValue);
						} catch (NumberFormatException ex) {
							desktop
									.displayErrorMessage("Incorrect value ("
											+ (String) objValue
											+ ") for parameter "
											+ parameter.getName()
											+ " in data file "
											+ dataFiles[dataFileIndex]
													.toString() + ".");
							return false;
						}
					}
					if ((minValue != null) && (minValue > value)) {
						desktop.displayErrorMessage("Too small value (" + value
								+ ") for parameter " + parameter.getName()
								+ " in data file "
								+ dataFiles[dataFileIndex].toString() + ".");
						return false;
					}
					if ((maxValue != null) && (maxValue < value)) {
						desktop.displayErrorMessage("Too big value (" + value
								+ ") for parameter " + parameter.getName()
								+ " in data file "
								+ dataFiles[dataFileIndex].toString() + ".");
						return false;
					}
				}
			}
		}

		return true;

	}

	private void copyParameterValuesToRawDataFiles() {

		MZmineProject currentProject = MZmineCore.getCurrentProject();

		// Remove all previous parameters from project
		Parameter[] parameters = currentProject.getParameters();
		for (Parameter parameter : parameters) {
			currentProject.removeParameter(parameter);
		}

		// Add new parameters
		parameters = parameterValues.keySet().toArray(new Parameter[0]);
		for (Parameter parameter : parameters) {
			currentProject.addParameter(parameter);
		}

		// Set values for new parameters
		for (int columnIndex = 0; columnIndex < parameterValues.keySet().size(); columnIndex++) {
			Parameter parameter = tablemodelParameterValues
					.getParameter(columnIndex + 1);

			for (int dataFileIndex = 0; dataFileIndex < dataFiles.length; dataFileIndex++) {
				RawDataFile file = dataFiles[dataFileIndex];

				Object value = tablemodelParameterValues.getValueAt(
						dataFileIndex, columnIndex + 1);
				if (parameter.getType() == ParameterType.DOUBLE) {
					Double doubleValue = null;
					if (value instanceof Double)
						doubleValue = (Double) value;
					if (value instanceof String)
						doubleValue = Double.parseDouble((String) value);
					currentProject.setParameterValue(parameter, file,
							doubleValue);
				}
				if (parameter.getType() == ParameterType.STRING) {
					if (value == null)
						value = "";
					currentProject.setParameterValue(parameter, file,
							(String) value);
				}

			}

		}

	}

	private void copyParameterValuesFromRawDataFiles() {

		MZmineProject currentProject = MZmineCore.getCurrentProject();
		
		for (int dataFileIndex = 0; dataFileIndex < dataFiles.length; dataFileIndex++) {

			RawDataFile file = dataFiles[dataFileIndex];
			Parameter[] parameters = currentProject.getParameters();

			// Loop through all parameters defined for this file
			for (Parameter p : parameters) {

				// Check if this parameter has been seen before?
				Object[] values;
				if (!(parameterValues.containsKey(p))) {
					// No, initialize a new array of values for this parameter
					values = new Object[dataFiles.length];
					for (int i = 0; i < values.length; i++)
						values[i] = p.getDefaultValue();
					parameterValues.put(p, values);
				} else {
					values = parameterValues.get(p);
				}

				// Set value of this parameter for the current raw data file
				values[dataFileIndex] = currentProject.getParameterValue(p,
						file);

			}

		}

	}

	private void setupTableModel() {

		tablemodelParameterValues = new ParameterTableModel(
				dataFiles, parameterValues);
		tableParameterValues.setModel(tablemodelParameterValues);

		for (int columnIndex = 0; columnIndex < (tablemodelParameterValues
				.getColumnCount() - 1); columnIndex++) {
			Parameter parameter = tablemodelParameterValues
					.getParameter(columnIndex + 1);
			if (parameter.getPossibleValues() != null)
				tableParameterValues.getColumnModel()
						.getColumn(columnIndex + 1).setCellEditor(
								new DefaultCellEditor(new JComboBox(parameter
										.getPossibleValues())));
		}

	}

	private void initComponents() {

		panelParameterValues = new JPanel(new BorderLayout());
		scrollParameterValues = new JScrollPane();
		tablemodelParameterValues = new ParameterTableModel(
				new RawDataFile[0], new Hashtable<Parameter, Object[]>());
		tableParameterValues = new JTable(tablemodelParameterValues);
		tableParameterValues.setColumnSelectionAllowed(true);
		tableParameterValues.setRowSelectionAllowed(false);
		scrollParameterValues.setViewportView(tableParameterValues);
		scrollParameterValues.setMinimumSize(new Dimension(100, 100));
		scrollParameterValues.setPreferredSize(new Dimension(100, 100));
		panelRemoveParameterButton = new JPanel(new FlowLayout(FlowLayout.LEFT));
		buttonAddParameter = new JButton("Add new parameter");
		buttonAddParameter.addActionListener(this);
		buttonImportParameters = new JButton("Import parameters and values...");
		buttonImportParameters.addActionListener(this);
		buttonRemoveParameter = new JButton("Remove selected parameter");
		buttonRemoveParameter.addActionListener(this);

		panelRemoveParameterButton.add(buttonAddParameter);
		panelRemoveParameterButton.add(buttonImportParameters);
		panelRemoveParameterButton.add(buttonRemoveParameter);

		panelParameterValues.add(scrollParameterValues, BorderLayout.CENTER);
		panelParameterValues
				.add(panelRemoveParameterButton, BorderLayout.SOUTH);
		panelParameterValues.setBorder(BorderFactory.createEmptyBorder(5, 5, 5,
				5));

		panelOKCancelButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT));

		buttonOK = new JButton("OK");
		buttonOK.addActionListener(this);
		buttonCancel = new JButton("Cancel");
		buttonCancel.addActionListener(this);
		panelOKCancelButtons.add(buttonOK);
		panelOKCancelButtons.add(buttonCancel);

		setLayout(new BorderLayout());

		add(panelParameterValues, BorderLayout.CENTER);
		add(panelOKCancelButtons, BorderLayout.SOUTH);

		pack();
	}

}

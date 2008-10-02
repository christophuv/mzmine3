/*
 * Copyright 2006-2008 The MZmine Development Team
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

package net.sf.mzmine.modules.alignment.fragment;

import java.text.NumberFormat;

import net.sf.mzmine.data.Parameter;
import net.sf.mzmine.data.ParameterType;
import net.sf.mzmine.data.impl.SimpleParameter;
import net.sf.mzmine.data.impl.SimpleParameterSet;
import net.sf.mzmine.main.MZmineCore;

public class FragmentAlignerParameters extends SimpleParameterSet {

	public static final NumberFormat percentFormat = NumberFormat
			.getPercentInstance();

	public static final String RTToleranceTypeAbsolute = "Absolute";
	public static final String RTToleranceTypeRelative = "Relative";

	public static final Object[] RTToleranceTypePossibleValues = {
			RTToleranceTypeAbsolute, RTToleranceTypeRelative };

	public static final Parameter peakListName = new SimpleParameter(
			ParameterType.STRING, "Peaklist name", "Peak list name", null,
			"Aligned peak list", null);

	public static final Parameter MZTolerance = new SimpleParameter(
			ParameterType.DOUBLE, "m/z tolerance",
			"Maximum allowed M/Z difference", "m/z", new Double(0.2), new Double(
					0.0), null, MZmineCore.getMZFormat());

	public static final Parameter MZWeight = new SimpleParameter(
			ParameterType.DOUBLE, "Weight for m/z",
			"Score for perfectly matching m/z values", "", new Double(10.0),
			new Double(0.0), null, NumberFormat.getNumberInstance());

	public static final Parameter RTToleranceType = new SimpleParameter(
			ParameterType.STRING,
			"Retention time tolerance type",
			"Maximum RT difference can be defined either using absolute or relative value",
			RTToleranceTypeAbsolute, RTToleranceTypePossibleValues);

	public static final Parameter RTToleranceValueAbs = new SimpleParameter(
			ParameterType.DOUBLE, "Absolute RT tolerance",
			"Maximum allowed absolute RT difference", null, new Double(15.0),
			new Double(0.0), null, MZmineCore.getRTFormat());

	public static final Parameter RTToleranceValuePercent = new SimpleParameter(
			ParameterType.DOUBLE, "Relative RT tolerance",
			"Maximum allowed relative RT difference", "%", new Double(0.15),
			new Double(0.0), null, percentFormat);

	public static final Parameter RTWeight = new SimpleParameter(
			ParameterType.DOUBLE, "Weight for RT",
			"Score for perfectly matching RT values", "", new Double(10.0),
			new Double(0.0), null, NumberFormat.getNumberInstance());

	public static final Parameter SameIDRequired = new SimpleParameter(
			ParameterType.BOOLEAN,
			"Require same ID",
			"If checked, only rows having same compound identities (or no identities) can be aligned",
			new Boolean(false));

	public static final Parameter SameIDWeight = new SimpleParameter(
			ParameterType.DOUBLE, "Weight for ID",
			"Score for matching compound identities", "", new Double(0.0),
			new Double(0.0), null, NumberFormat.getNumberInstance());

	public static final Parameter MaxFragments = new SimpleParameter(
			ParameterType.INTEGER, "Number of fragments",
			"Maximum number of m/z fragments", "", new Integer(50), new Integer(
					1), null, NumberFormat.getIntegerInstance());

	public FragmentAlignerParameters() {
		super(new Parameter[] { peakListName, MZTolerance, MZWeight,
				RTToleranceType, RTToleranceValueAbs, RTToleranceValuePercent,
				RTWeight, SameIDRequired, SameIDWeight, MaxFragments });
	}

}

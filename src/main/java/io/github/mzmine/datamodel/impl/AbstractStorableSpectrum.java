/*
 * Copyright 2006-2021 The MZmine Development Team
 *
 * This file is part of MZmine.
 *
 * MZmine is free software; you can redistribute it and/or modify it under the terms of the GNU
 * General Public License as published by the Free Software Foundation; either version 2 of the
 * License, or (at your option) any later version.
 *
 * MZmine is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even
 * the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with MZmine; if not,
 * write to the Free Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 */

package io.github.mzmine.datamodel.impl;

import io.github.mzmine.datamodel.Frame;
import io.github.mzmine.datamodel.featuredata.impl.StorageUtils;
import io.github.mzmine.util.MemoryMapStorage;
import java.nio.DoubleBuffer;
import java.util.logging.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * An implementation of MassSpectrum that stores the data points in a MemoryMapStorage.
 */
public abstract class AbstractStorableSpectrum extends AbstractMassSpectrum {

  private static final Logger logger = Logger.getLogger(AbstractStorableSpectrum.class.getName());
  private static final DoubleBuffer EMPTY_BUFFER = DoubleBuffer.wrap(new double[0]);

  protected DoubleBuffer mzValues;
  protected DoubleBuffer intensityValues;

  /**
   * Note: mz and intensity values for a scan shall only be set once and are enforced to be
   * immutable thereafter. These values shall ideally be set during instantiation of the given
   * object. However, if a the object represents an artificially generated object, e.g. a {@link
   * io.github.mzmine.datamodel.Frame} which is generated by merging multiple scans at the same
   * retention time, values can be set at a later stage, but are immutable thereafter.
   *
   * @param storage         If null, mz and intensity values will be stored in ram.
   * @param mzValues        If null, no values will be stored and values can be set at a later stage
   *                        by the implementing class. (e.g. {@link SimpleFrame}.
   * @param intensityValues If null, no values will be stored and values can be set at a later stage
   *                        by the implementing class. (e.g. {@link SimpleFrame}.
   */
  public AbstractStorableSpectrum(@Nullable MemoryMapStorage storage, @Nullable double[] mzValues,
      @Nullable double[] intensityValues) {
    setDataPoints(storage, mzValues, intensityValues);
  }

  protected synchronized void setDataPoints(@Nullable MemoryMapStorage storage,
      @Nullable double[] mzValues,
      @Nullable double[] intensityValues) {

    if (mzValues == null && intensityValues == null) {
      return;
    }

    assert mzValues.length == intensityValues.length;
    // values shall not be reset, but can be set at a later stage
    if(!(this instanceof Frame)) {
      // allow re-generation of frame spectra
      assert this.mzValues == null;
      assert this.intensityValues == null;
    }

    for (int i = 0; i < mzValues.length - 1; i++) {
      if (mzValues[i] > mzValues[i + 1]) {
        throw new IllegalArgumentException("The m/z values must be sorted in ascending order");
      }
    }

    this.mzValues = StorageUtils.storeValuesToDoubleBuffer(storage, mzValues);
    this.intensityValues = StorageUtils.storeValuesToDoubleBuffer(storage, intensityValues);
    updateMzRangeAndTICValues();
  }

  DoubleBuffer getMzValues() {
    if (mzValues == null) {
      return EMPTY_BUFFER;
    } else {
      return mzValues;
    }
  }

  DoubleBuffer getIntensityValues() {
    if (intensityValues == null) {
      return EMPTY_BUFFER;
    } else {
      return intensityValues;
    }
  }

  @Override
  public double[] getMzValues(@NotNull double[] dst) {
    if (mzValues == null) {
      return new double[0];
    }
    if (dst.length < getNumberOfDataPoints()) {
      dst = new double[getNumberOfDataPoints()];
    }
    mzValues.get(0, dst, 0, getNumberOfDataPoints());
    return dst;
  }

  @Override
  public double[] getIntensityValues(@NotNull double[] dst) {
    if (intensityValues == null) {
      return new double[0];
    }

    if (dst.length < getNumberOfDataPoints()) {
      dst = new double[getNumberOfDataPoints()];
    }
    intensityValues.get(0, dst, 0, getNumberOfDataPoints());
    return dst;
  }

}


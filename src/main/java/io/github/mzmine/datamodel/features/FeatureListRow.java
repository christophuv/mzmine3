/*
 * Copyright 2006-2020 The MZmine Development Team
 *
 * This file is part of MZmine.
 *
 * MZmine is free software; you can redistribute it and/or modify it under the terms of the GNU
 * General Public License as published by the Free Software Foundation; either version 2 of the
 * License, or (at your option) any later version.
 *
 * MZmine is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even
 * the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
 * Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with MZmine; if not,
 * write to the Free Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301
 * USA
 */

package io.github.mzmine.datamodel.features;

import io.github.mzmine.datamodel.FeatureIdentity;
import io.github.mzmine.datamodel.FeatureInformation;
import io.github.mzmine.datamodel.FeatureStatus;
import io.github.mzmine.datamodel.IsotopePattern;
import io.github.mzmine.datamodel.RawDataFile;
import io.github.mzmine.datamodel.Scan;
import io.github.mzmine.datamodel.identities.iontype.IonIdentity;
import io.github.mzmine.modules.dataprocessing.id_formulaprediction.ResultFormula;
import io.github.mzmine.modules.dataprocessing.id_lipididentification.lipidutils.MatchedLipid;
import io.github.mzmine.util.spectraldb.entry.SpectralDBFeatureIdentity;
import java.util.List;
import java.util.Objects;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Interface representing feature list row
 */
public interface FeatureListRow {

  /**
   * Return raw data with features on this row
   */
  public ObservableList<RawDataFile> getRawDataFiles();

  /**
   * Returns ID of this row
   */
  public Integer getID();

  /**
   * Returns number of features assigned to this row
   */
  public int getNumberOfFeatures();

  /**
   * Return features assigned to this row
   */
  public ObservableList<Feature> getFeatures();

  /**
   * Returns feature for given raw data file
   */
  @Nullable
  public Feature getFeature(RawDataFile rawData);

  /**
   * Add a feature
   */
  public void addFeature(RawDataFile rawData, Feature feature);

  /**
   * Remove a feature
   */
  public void removeFeature(RawDataFile file);

  /**
   * Has a feature?
   */
  public boolean hasFeature(Feature feature);

  /**
   * Has a feature?
   */
  public boolean hasFeature(RawDataFile rawData);

  /**
   * Returns average M/Z for features on this row
   */
  public Double getAverageMZ();

  /**
   * Sets average mz for this row
   */
  public void setAverageMZ(Double averageMZ);

  /**
   * Returns average RT for features on this row
   */
  public Float getAverageRT();

  /**
   * Sets average rt for this row
   */
  public void setAverageRT(Float averageRT);

  /**
   * Returns average mobility for features on this row
   */
  @Nullable
  Float getAverageMobility();

  Float getAverageCCS();

  /**
   * Returns average height for features on this row
   */
  public Float getAverageHeight();

  /**
   * Returns the charge for feature on this row. If more charges are found 0 is returned
   */
  public Integer getRowCharge();

  /**
   * Returns average area for features on this row
   */
  public Float getAverageArea();

  /**
   * Returns comment for this row
   */
  public String getComment();

  /**
   * Sets comment for this row
   */
  public void setComment(String comment);

  /**
   * Add a new identity candidate (result of identification method)
   *
   * @param identity  New feature identity
   * @param preffered boolean value to define this identity as preferred identity
   */
  public void addFeatureIdentity(FeatureIdentity identity, boolean preffered);

  /**
   * Remove identity candidate
   *
   * @param identity Feature identity
   */
  public void removeFeatureIdentity(FeatureIdentity identity);

  /**
   * Returns all candidates for this feature's identity
   *
   * @return Identity candidates
   */
  public ObservableList<FeatureIdentity> getPeakIdentities();

  /**
   * Returns preferred feature identity among candidates
   *
   * @return Preferred identity
   */
  public FeatureIdentity getPreferredFeatureIdentity();

  /**
   * Sets a preferred feature identity among candidates
   *
   * @param identity Preferred identity
   */
  public void setPreferredFeatureIdentity(FeatureIdentity identity);

  /**
   * Returns FeatureInformation
   *
   * @return
   */

  public FeatureInformation getFeatureInformation();

  /**
   * Adds a new FeatureInformation object.
   * <p>
   * FeatureInformation is used to keep extra information about features in the form of a map
   * <propertyName, propertyValue>
   *
   * @param featureInformation object
   */

  public void setFeatureInformation(FeatureInformation featureInformation);

  /**
   * Returns maximum raw data point intensity among all features in this row
   *
   * @return Maximum intensity
   */
  public Float getMaxDataPointIntensity();

  /**
   * Returns the most intense feature in this row
   */
  public Feature getBestFeature();

  /**
   * Returns the most intense fragmentation scan in this row
   */
  public Scan getMostIntenseFragmentScan();

  /**
   * Returns all fragmentation scans of this row
   */
  @NotNull
  public ObservableList<Scan> getAllFragmentScans();

  /**
   * Returns the most intense isotope pattern in this row. If there are no isotope patterns present
   * in the row, returns null.
   */
  public IsotopePattern getBestIsotopePattern();

  @Nullable
  FeatureList getFeatureList();

  void setFeatureList(@NotNull FeatureList flist);

  default void addSpectralLibraryMatch(SpectralDBFeatureIdentity id) {
    addFeatureIdentity(id, false);
  }

  /**
   * Correlated features grouped
   *
   * @return
   */
  public RowGroup getGroup();

  /**
   * Correlated features grouped
   *
   * @param group
   */
  public void setGroup(RowGroup group);

  /**
   * The list of ion identities
   *
   * @return null or the current list. First element is the "preferred" element
   */
  @Nullable
  List<IonIdentity> getIonIdentities();

  /**
   * Set the list of ion identities with the first element being the preferred
   *
   * @param ions list of ion identities
   */
  void setIonIdentities(@Nullable List<IonIdentity> ions);

  /**
   * Adds the ion identity as the preferred (first element) of the list
   *
   * @param ion the preferred ion identity
   */
  default void addIonIdentity(IonIdentity ion) {
    this.addIonIdentity(ion, true);
  }

  /**
   * Adds the ion as first or last element of the list.
   *
   * @param ion        the ion identity
   * @param markAsBest true: add as first element; false add as last element
   */
  default void addIonIdentity(IonIdentity ion, boolean markAsBest) {
    List<IonIdentity> ionIdentities = getIonIdentities();

    if (ionIdentities == null) {
      ionIdentities = FXCollections.observableArrayList();
      setIonIdentities(ionIdentities);
    }

    // remove first
    if (!ionIdentities.isEmpty()) {
      ionIdentities.remove(ion);
    }

    if (markAsBest) {
      ionIdentities.add(0, ion);
    } else {
      ionIdentities.add(ion);
    }
  }

  /**
   * Clear all ion identities
   */
  default void clearIonIdentites() {
    List<IonIdentity> ionIdentities = getIonIdentities();
    if (ionIdentities != null) {
      ionIdentities.clear();
    }
  }

  /**
   * The first element of {@link #getIonIdentities()}
   *
   * @return the preferred (first) element of all ion identities
   */
  @Nullable
  default IonIdentity getBestIonIdentity() {
    List<IonIdentity> ionIdentities = getIonIdentities();
    return ionIdentities != null && !ionIdentities.isEmpty() ? ionIdentities.get(0) : null;
  }

  /**
   * Set the best ion identity (the first element of the list)
   *
   * @param ion the preferred ion
   */
  default void setBestIonIdentity(@NotNull IonIdentity ion) {
    addIonIdentity(ion, true);
  }

  /**
   * Has at least one ion identity
   */
  default boolean hasIonIdentity() {
    List<IonIdentity> ionIdentities = getIonIdentities();
    return ionIdentities != null && !ionIdentities.isEmpty();
  }

  /**
   * Remove ion identity if available
   *
   * @param ion the ion to remove
   */
  default boolean removeIonIdentity(IonIdentity ion) {
    List<IonIdentity> ionIdentities = getIonIdentities();
    if (ionIdentities != null) {
      return ionIdentities.remove(ion);
    }
    return false;
  }

  /**
   * Returns the group ID
   *
   * @return return the group ID or -1 if not part of a group {@link #getGroup()}
   */
  default int getGroupID() {
    RowGroup g = getGroup();
    return g == null ? -1 : g.groupID;
  }

  List<ResultFormula> getFormulas();

  void setFormulas(List<ResultFormula> formulas);

  /**
   * Checks if MS2 fragmentation data is available
   *
   * @return true if this row has at least 1 MS2 spectrum
   */
  default boolean hasMs2Fragmentation() {
    // should be faster. Best fragmentation loops through all spectra to find best
    return getAllFragmentScans() != null && !getAllFragmentScans().isEmpty();
  }

  /**
   * The intensity summed over all features
   * @return sum of all feature heights
   */
  default double getSumIntensity() {
    return this.getFeatures().stream().filter(Objects::nonNull)
        .filter(f -> f.getFeatureStatus() != FeatureStatus.UNKNOWN).mapToDouble(
        Feature::getHeight).sum();
  }


  /**
   * List of library matches sorted from best (index 0) to last match
   *
   * @return list of library matches or an empty list
   */
  @NotNull
  List<SpectralDBFeatureIdentity> getSpectralLibraryMatches();

  /**
   * Add annotations from lipid search
   *
   * @param matchedLipid the matched lipid
   */
  void addLipidAnnotation(MatchedLipid matchedLipid);
}

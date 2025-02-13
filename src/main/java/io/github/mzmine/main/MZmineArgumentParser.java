/*
 * Copyright (c) 2004-2022 The MZmine Development Team
 *
 * Permission is hereby granted, free of charge, to any person
 * obtaining a copy of this software and associated documentation
 * files (the "Software"), to deal in the Software without
 * restriction, including without limitation the rights to use,
 * copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following
 * conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
 * OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 * HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 * OTHER DEALINGS IN THE SOFTWARE.
 */

package io.github.mzmine.main;

import io.github.mzmine.modules.io.import_rawdata_all.AllSpectralDataImportModule;
import io.github.mzmine.util.files.FileAndPathUtil;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.jetbrains.annotations.Nullable;

/**
 * Parses the command line arguments
 *
 * @author Robin Schmid (https://github.com/robinschmid)
 */
public class MZmineArgumentParser {

  private static final Logger logger = Logger.getLogger(MZmineArgumentParser.class.getName());

  private File batchFile;
  private @Nullable File[] overrideDataFiles;
  private File preferencesFile;
  private File tempDirectory;
  private boolean isKeepRunningAfterBatch = false;
  private boolean loadTdfPseudoProfile = false;
  private boolean loadTsfProfile = false;
  private KeepInMemory isKeepInMemory = null;

  public void parse(String[] args) {
    Options options = new Options();

    // -b  or --batch
    Option batch = new Option("b", "batch", true, "batch mode file");
    batch.setRequired(false);
    options.addOption(batch);

    Option input = new Option("i", "input", true, """
        input data files. Either defined in a .txt text file with one file per line
        or by glob pattern matching. To match all .mzML files in a path: -i "D:\\Data\\*.mzML"
        """);
    input.setRequired(false);
    options.addOption(input);

    Option pref = new Option("p", "pref", true, "preferences file");
    pref.setRequired(false);
    options.addOption(pref);

    Option tmpFolder = new Option("t", "temp", true,
        "Temp directory overrides definition in preferences and JVM");
    tmpFolder.setRequired(false);
    options.addOption(tmpFolder);

    Option keepRunning = new Option("r", "running", false, "keep MZmine running in headless mode");
    keepRunning.setRequired(false);
    options.addOption(keepRunning);

    Option keepInMemory = new Option("m", "memory", true,
        "keep objects (scan data, features, etc) in memory. Options: none, all, features, centroids, raw, masses_features (masses_features for features and centroids)");
    keepInMemory.setRequired(false);
    options.addOption(keepInMemory);

    Option loadTdfPseudoProfile = new Option("tdfpseudoprofile", false,
        "Loads pseudo-profile frame spectra for tdf files instead of centroided spectra.");
    loadTdfPseudoProfile.setRequired(false);
    options.addOption(loadTdfPseudoProfile);

    Option loadTsfProfile = new Option("tsfprofile", false,
        "Loads profile spectra from .tsf data instead of centroid spectra.");
    loadTsfProfile.setRequired(false);
    options.addOption(loadTsfProfile);

    CommandLineParser parser = new BasicParser();
    HelpFormatter formatter = new HelpFormatter();
    CommandLine cmd;

    try {
      cmd = parser.parse(options, args);

      String sbatch = cmd.getOptionValue(batch.getLongOpt());
      if (sbatch != null) {
        logger.info(() -> "Batch file set by command line: " + sbatch);
        batchFile = new File(sbatch);
      }

      String sinput = cmd.getOptionValue(input.getLongOpt());
      if (sinput != null) {
        logger.info(() -> "Input files were set to: " + sinput);
        // search for files
        try {
          overrideDataFiles = FileAndPathUtil.parseFileInputArgument(sinput);
        } catch (IOException e) {
          logger.log(Level.SEVERE,
              "Could not read the list of input data files. Either provide a string \"mypath/*.mzML\" or a text file that contains all files delimited by a new line.");
          throw new RuntimeException(e);
        }
      }

      String spref = cmd.getOptionValue(pref.getLongOpt());
      if (spref != null) {
        logger.info(() -> "Preferences file set by command line: " + spref);
        preferencesFile = new File(spref);
      }

      String stemp = cmd.getOptionValue(tmpFolder.getLongOpt());
      if (stemp != null) {
        logger.info(
            () -> "Temp directory set by command line, will override all other definitions: "
                  + stemp);
        tempDirectory = new File(stemp);
      }

      isKeepRunningAfterBatch = cmd.hasOption(keepRunning.getLongOpt());
      if (isKeepRunningAfterBatch) {

        logger.info(
            () -> "the -r / --running argument was set to keep MZmine alive after batch is finished");
      }

      String keepInData = cmd.getOptionValue(keepInMemory.getLongOpt());
      if (keepInData != null) {
        isKeepInMemory = KeepInMemory.parse(keepInData);
        logger.info(() -> "the -m / --memory argument was set to " + isKeepInMemory.toString()
                          + " to keep objects in RAM (scan data, features, etc) which are otherwise stored in memory mapped ");
      }

      if (cmd.hasOption(loadTdfPseudoProfile.getOpt())) {
        this.loadTdfPseudoProfile = true;
      }
      if (cmd.hasOption(loadTsfProfile.getOpt())) {
        this.loadTsfProfile = true;
      }

    } catch (ParseException e) {
      logger.log(Level.SEVERE, "Wrong command line arguments. " + e.getMessage(), e);
      formatter.printHelp("utility-name", options);
      System.exit(1);
    }
  }

  /**
   * The temp directory overrides all other definitions if set
   *
   * @return the temp directory override (null or a file)
   */
  @Nullable
  public File getTempDirectory() {
    return tempDirectory;
  }

  @Nullable
  public File getPreferencesFile() {
    return preferencesFile;
  }

  @Nullable
  public File getBatchFile() {
    return batchFile;
  }

  /**
   * After batch is finished, keep mzmine running
   *
   * @return true if -r or --running was set as argument
   */
  public boolean isKeepRunningAfterBatch() {
    return isKeepRunningAfterBatch;
  }

  /**
   * Keep all {@link io.github.mzmine.util.MemoryMapStorage} items in RAM (e.g., scans, features,
   * masslists)
   *
   * @return true will keep objects in memory which are usually stored in memory mapped files
   */
  @Nullable
  public KeepInMemory isKeepInMemory() {
    return isKeepInMemory;
  }

  public boolean isLoadTdfPseudoProfile() {
    return loadTdfPseudoProfile;
  }

  public boolean isLoadTsfProfile() {
    return loadTsfProfile;
  }

  /**
   * Defines data files that will be used in headless mode. Those files will replace the input files
   * in the {@link AllSpectralDataImportModule}
   *
   * @return data files if specified as argument else null
   */
  @Nullable
  public File[] getOverrideDataFiles() {
    return overrideDataFiles;
  }
}


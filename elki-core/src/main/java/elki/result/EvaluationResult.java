/*
 * This file is part of ELKI:
 * Environment for Developing KDD-Applications Supported by Index-Structures
 *
 * Copyright (C) 2019
 * ELKI Development Team
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package elki.result;

import java.util.ArrayList;
import java.util.Iterator;

import elki.result.textwriter.TextWriteable;
import elki.result.textwriter.TextWriterStream;

/**
 * Abstract evaluation result.
 *
 * @author Erich Schubert
 * @since 0.7.0
 *
 * @composed - - - EvaluationResult.MeasurementGroup
 */
public class EvaluationResult implements TextWriteable, Iterable<EvaluationResult.MeasurementGroup> {
  /**
   * Result name for aggregating ranking evaluations.
   */
  public static final String RANKING = "Evaluation of Ranking";

  /**
   * Measurements.
   */
  ArrayList<MeasurementGroup> groups = new ArrayList<>();

  /**
   * Header lines.
   */
  ArrayList<String> header = new ArrayList<>();

  /**
   * Constructor.
   */
  public EvaluationResult() {
    super();
  }

  /**
   * Add a new measurement group.
   *
   * @param string Group name
   * @return Measurement group.
   */
  public MeasurementGroup newGroup(String string) {
    MeasurementGroup g = new MeasurementGroup(string);
    groups.add(g);
    return g;
  }

  /**
   * Find or add a new measurement group.
   *
   * @param label Group name
   * @return Measurement group.
   */
  public MeasurementGroup findOrCreateGroup(String label) {
    for(MeasurementGroup g : groups) {
      if(label.equals(g.getName())) {
        return g;
      }
    }
    return newGroup(label);
  }

  @Override
  public void writeToText(TextWriterStream out, String label) {
    for(MeasurementGroup g : groups) {
      out.commentPrintLn(g.getName());
      out.flush();
      for(Measurement m : g) {
        out.inlinePrintNoQuotes(g.getName() + " " +m.name);
        out.inlinePrintNoQuotes(m.val);
        out.flush();
      }
    }
  }

  /**
   * Add a header line to this result.
   *
   * @param line Header line
   */
  public void addHeader(String line) {
    header.add(line);
  }

  /**
   * Get the header lines.
   *
   * @return Header lines
   */
  public Iterable<String> getHeaderLines() {
    return header;
  }

  @Override
  public Iterator<MeasurementGroup> iterator() {
    return groups.iterator();
  }

  /**
   * Number of lines recommended for display.
   *
   * @return Number of lines
   */
  public int numLines() {
    int r = header.size();
    for(MeasurementGroup m : groups) {
      r += 1 + m.measurements.size();
    }
    return r;
  }

  /**
   * Find or create an evaluation result.
   *
   * @param parent Parent result
   * @param name Long name
   *
   * @return Evaluation result
   */
  public static EvaluationResult findOrCreate(Object parent, String name) {
    ArrayList<EvaluationResult> ers = ResultUtil.filterResults(parent, EvaluationResult.class);
    EvaluationResult ev = null;
    for(EvaluationResult e : ers) {
      Metadata m = Metadata.get(e);
      if(m != null && name.equals(m.getLongName())) {
        ev = e;
        break;
      }
    }
    if(ev == null) {
      ev = new EvaluationResult();
      Metadata m = Metadata.of(ev);
      m.setLongName(name);
      Metadata.hierarchyOf(parent).addChild(ev);
    }
    return ev;
  }

  /**
   * A group of evaluation measurements.
   *
   * @author Erich Schubert
   *
   * @composed - - - EvaluationResult.Measurement
   */
  public static class MeasurementGroup implements Iterable<Measurement> {
    /**
     * Group name
     */
    private String groupname;

    /**
     * Measurements in this group.
     */
    private ArrayList<Measurement> measurements = new ArrayList<>();

    /**
     * Constructor.
     *
     * @param gname Group name
     */
    protected MeasurementGroup(String gname) {
      this.groupname = gname;
    }

    /**
     * Get the group name.
     *
     * @return Group name
     */
    public String getName() {
      return groupname;
    }

    /**
     * Add a single measurement.
     *
     * @param name Measurement name
     * @param val Observed value
     * @param min Minimum value
     * @param max Maximum value
     * @param lowerisbetter Flag
     * @return {@code this} (Builder pattern)
     */
    public MeasurementGroup addMeasure(String name, double val, double min, double max, boolean lowerisbetter) {
      measurements.add(new Measurement(name, val, min, max, lowerisbetter));
      return this;
    }

    /**
     * Add a single measurement.
     *
     * @param name Measurement name
     * @param val Observed value
     * @param min Minimum value
     * @param exp Expected value
     * @param lowerisbetter Flag
     * @return {@code this} (Builder pattern)
     */
    public MeasurementGroup addMeasure(String name, double val, double min, double max, double exp, boolean lowerisbetter) {
      measurements.add(new Measurement(name, val, min, max, exp, lowerisbetter));
      return this;
    }

    /**
     * Check if a measurement already exists.
     *
     * @param name Measurement name
     * @return {@code true} if measurement exists
     */
    public boolean hasMeasure(String name) {
      for(Measurement m : measurements) {
        if(m.name.equals(name)) {
          return true;
        }
      }
      return false;
    }

    @Override
    public Iterator<Measurement> iterator() {
      return measurements.iterator();
    }
  }

  /**
   * Class representing a single measurement.
   *
   * @author Erich Schubert
   */
  public static class Measurement {
    /**
     * Measurement name.
     */
    String name;

    /**
     * Observed value, minimum, maximum, expected value.
     */
    double val;

    /**
     * Observed value, minimum, maximum, expected value.
     */
    double min;

    /**
     * Observed value, minimum, maximum, expected value.
     */
    double max;

    /**
     * Observed value, minimum, maximum, expected value.
     */
    double exp;

    /**
     * Indicates low values are better.
     */
    private boolean lowerisbetter;

    /**
     * Constructor.
     *
     * @param name Name
     * @param val Value
     * @param min Minimum
     * @param max Maximum
     * @param lowerisbetter Flag
     */
    protected Measurement(String name, double val, double min, double max, boolean lowerisbetter) {
      this(name, val, min, max, Double.NaN, lowerisbetter);
    }

    /**
     * Constructor.
     *
     * @param name Name
     * @param val Value
     * @param min Minimum
     * @param max Maximum
     * @param exp Expected value
     * @param lowerisbetter Flag
     */
    protected Measurement(String name, double val, double min, double max, double exp, boolean lowerisbetter) {
      super();
      this.name = name;
      this.val = val;
      this.min = min;
      this.max = max;
      this.exp = exp;
      this.lowerisbetter = lowerisbetter;
    }

    /**
     * Get the name of this measurement.
     *
     * @return Measurement name.
     */
    public String getName() {
      return name;
    }

    /**
     * Get the observed value.
     *
     * @return observed value.
     */
    public double getVal() {
      return val;
    }

    /**
     * Get the minimum value.
     *
     * @return Minimum value.
     */
    public double getMin() {
      return min;
    }

    /**
     * Get the maximum value.
     *
     * @return Maximum value.
     */
    public double getMax() {
      return max;
    }

    /**
     * Get the expected value. May be {@code Double.NaN}.
     *
     * @return Expected value.
     */
    public double getExp() {
      return exp;
    }

    /**
     * Return {@code true} if low values are better.
     *
     * @return {@code true} when low values are better.
     */
    public boolean lowerIsBetter() {
      return lowerisbetter;
    }
  }

  /**
   * Flag to indicate that these results should be visualized using a single
   * visualizer.
   *
   * @return Singleton
   */
  public boolean visualizeSingleton() {
    return false;
  }
}

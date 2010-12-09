package de.lmu.ifi.dbs.elki.visualization.gui;

import javax.swing.JFrame;

import de.lmu.ifi.dbs.elki.data.DatabaseObject;
import de.lmu.ifi.dbs.elki.database.Database;
import de.lmu.ifi.dbs.elki.logging.Logging;
import de.lmu.ifi.dbs.elki.normalization.Normalization;
import de.lmu.ifi.dbs.elki.result.HierarchicalResult;
import de.lmu.ifi.dbs.elki.result.ResultHandler;
import de.lmu.ifi.dbs.elki.result.ResultUtil;
import de.lmu.ifi.dbs.elki.utilities.optionhandling.OptionID;
import de.lmu.ifi.dbs.elki.utilities.optionhandling.constraints.GreaterEqualConstraint;
import de.lmu.ifi.dbs.elki.utilities.optionhandling.parameterization.Parameterization;
import de.lmu.ifi.dbs.elki.utilities.optionhandling.parameters.IntParameter;
import de.lmu.ifi.dbs.elki.utilities.optionhandling.parameters.StringParameter;
import de.lmu.ifi.dbs.elki.visualization.gui.overview.OverviewPlot;
import de.lmu.ifi.dbs.elki.visualization.visualizers.VisualizerContext;
import de.lmu.ifi.dbs.elki.visualization.visualizers.VisualizerParameterizer;

/**
 * Handler to process and visualize a Result.
 * 
 * @author Erich Schubert
 * @author Remigius Wojdanowski
 * 
 * @apiviz.composedOf VisualizerParameterizer
 * @apiviz.uses ResultWindow oneway
 */
public class ResultVisualizer<O extends DatabaseObject> implements ResultHandler<O, HierarchicalResult> {
  /**
   * Get a logger for this class.
   */
  protected final static Logging logger = Logging.getLogger(ResultVisualizer.class);

  /**
   * OptionID for {@link #WINDOW_TITLE_PARAM}
   */
  public static final OptionID WINDOW_TITLE_ID = OptionID.getOrCreateOptionID("vis.window.title", "Title to use for visualization window.");

  /**
   * Parameter to specify the window title
   * <p>
   * Key: {@code -vis.window.title}
   * </p>
   * <p>
   * Default value: "ELKI Result Visualization"
   * </p>
   */
  protected final StringParameter WINDOW_TITLE_PARAM = new StringParameter(WINDOW_TITLE_ID, true);

  /**
   * Stores the set title.
   */
  String title;

  /**
   * Default title
   */
  protected final static String DEFAULT_TITLE = "ELKI Result Visualization";

  /**
   * OptionID for {@link #MAXDIM_PARAM}.
   */
  public static final OptionID MAXDIM_ID = OptionID.getOrCreateOptionID("vis.maxdim", "Maximum number of dimensions to display.");

  /**
   * Parameter for the maximum number of dimensions,
   * 
   * <p>
   * Code: -vis.maxdim
   * </p>
   */
  private IntParameter MAXDIM_PARAM = new IntParameter(MAXDIM_ID, new GreaterEqualConstraint(1), OverviewPlot.MAX_DIMENSIONS_DEFAULT);

  /**
   * Stores the maximum number of dimensions to show.
   */
  int maxdim = OverviewPlot.MAX_DIMENSIONS_DEFAULT;

  /**
   * Visualization manager.
   */
  VisualizerParameterizer<O> manager;

  /**
   * Constructor, adhering to
   * {@link de.lmu.ifi.dbs.elki.utilities.optionhandling.Parameterizable}
   * 
   * @param config Parameterization
   */
  public ResultVisualizer(Parameterization config) {
    super();
    config = config.descend(this);
    if(config.grab(WINDOW_TITLE_PARAM)) {
      title = WINDOW_TITLE_PARAM.getValue();
    }
    if(config.grab(MAXDIM_PARAM)) {
      maxdim = MAXDIM_PARAM.getValue();
    }
    manager = new VisualizerParameterizer<O>(config);
  }

  @Override
  public void processResult(final Database<O> db, final HierarchicalResult result) {
    ResultUtil.ensureClusteringResult(db, result);
    ResultUtil.ensureSelectionResult(db, result);

    final VisualizerContext<O> context = manager.newContext(db, result);

    if(title == null) {
      title = VisualizerParameterizer.getTitle(db, result);
    }

    if(title == null) {
      title = DEFAULT_TITLE;
    }

    javax.swing.SwingUtilities.invokeLater(new Runnable() {
      @Override
      public void run() {
        try {
          ResultWindow window = new ResultWindow(title, db, result, maxdim, context);
          window.setVisible(true);
          window.setExtendedState(window.getExtendedState() | JFrame.MAXIMIZED_BOTH);
          window.update();
          window.showOverview();
        }
        catch(Throwable e) {
          logger.exception("Error in starting visualizer window.", e);
        }
      }
    });
  }

  @Override
  public void setNormalization(@SuppressWarnings("unused") Normalization<O> normalization) {
    // TODO: handle normalizations
    logger.warning("Normalizations not yet supported in " + ResultVisualizer.class.getName());
  }
}

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
package elki.projection;

import java.util.Arrays;
import java.util.Random;

import elki.data.DoubleVector;
import elki.data.type.TypeInformation;
import elki.data.type.TypeUtil;
import elki.data.type.VectorFieldTypeInformation;
import elki.database.datastore.DataStoreFactory;
import elki.database.datastore.WritableDataStore;
import elki.database.ids.DBIDArrayIter;
import elki.database.ids.DBIDs;
import elki.database.relation.MaterializedRelation;
import elki.database.relation.Relation;
import elki.logging.Logging;
import elki.logging.progress.FiniteProgress;
import elki.logging.statistics.Duration;
import elki.logging.statistics.LongStatistic;
import elki.math.MathUtil;
import elki.utilities.Alias;
import elki.utilities.documentation.Reference;
import elki.utilities.documentation.Title;
import elki.utilities.exceptions.AbortException;
import elki.utilities.optionhandling.Parameterizer;
import elki.utilities.optionhandling.OptionID;
import elki.utilities.optionhandling.constraints.CommonConstraints;
import elki.utilities.optionhandling.parameterization.Parameterization;
import elki.utilities.optionhandling.parameters.DoubleParameter;
import elki.utilities.optionhandling.parameters.Flag;
import elki.utilities.optionhandling.parameters.IntParameter;
import elki.utilities.optionhandling.parameters.ObjectParameter;
import elki.utilities.optionhandling.parameters.RandomParameter;
import elki.utilities.random.RandomFactory;

/**
 * Uniform Manifold Approximation and Projection (UMAP) is a projection technique dimension reduction.
 * <p>
 * Reference:
 * <p>
 * L. McInnes, J. Healy, J. Melville<br>
 * UMAP: Uniform Manifold Approximation and Projection for Dimension Reduction<br>
 * arXiv e-prints arXiv:1802.03426
 *
 * @author Erich Schubert
 * @author Alexander Kojen
 * @since X.X.X
 *
 * @composed - - - ???
 *
 * @param <O> Object type
 */
@Title("UMAP")
@Reference(authors = "L. McInnes, J. Healy, J. Melville", //
        title = "UMAP: Uniform Manifold Approximation and Projection for Dimension Reduction", //
        booktitle = "arXiv preprint arXiv:1802.03426", //
        url = "https://arxiv.org/abs/1802.03426", //
        bibkey = "DBLP:journals/corr/abs-1802-03426")
@Alias({ "UMAP", "umap" })
public class UMAP extends AbstractProjectionAlgorithm<Relation<DoubleVector>> {
  public UMAP(boolean keep) {
		super(keep);
		// TODO Auto-generated constructor stub
	}

/**
   * Class logger.
   */
  private static final Logging LOG = Logging.getLogger(UMAP.class);

  /**
   * input hyper-parameter for the k nearest neighbors
  */
  protected int k;

@Override
public TypeInformation[] getInputTypeRestriction() {
	// TODO Auto-generated method stub
	return null;
}

@Override
protected Logging getLogger() {
	// TODO Auto-generated method stub
	return null;
}


}

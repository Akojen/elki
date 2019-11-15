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
package elki.database.query.rknn;

import elki.database.ids.DBIDRef;
import elki.database.ids.DoubleDBIDList;
import elki.database.query.LinearScanQuery;
import elki.database.relation.Relation;

/**
 * Find nearest neighbors by querying with the original object.
 *
 * @author Erich Schubert
 *
 * @param <O> relation object type
 */
public class WrappedRKNNDBIDByLookup<O> implements RKNNSearcher<DBIDRef> {
  /**
   * Data relation.
   */
  private Relation<? extends O> relation;

  /**
   * Inner kNN searcher.
   */
  private RKNNSearcher<O> inner;

  /**
   * Constructor.
   *
   * @param relation data relation
   * @param inner inner kNN searcher
   */
  private WrappedRKNNDBIDByLookup(Relation<? extends O> relation, RKNNSearcher<O> inner) {
    super();
    this.relation = relation;
    this.inner = inner;
  }

  @Override
  public DoubleDBIDList getRKNN(DBIDRef id, int k) {
    return inner.getRKNN(relation.get(id), k);
  }

  /**
   * Linear scan searcher.
   * 
   * @author Erich Schubert
   *
   * @param <O> relation object type
   */
  public static class Linear<O> extends WrappedRKNNDBIDByLookup<O> implements LinearScanQuery {
    /**
     * Constructor.
     *
     * @param relation data relation
     * @param inner inner kNN searcher
     */
    private Linear(Relation<? extends O> relation, RKNNSearcher<O> inner) {
      super(relation, inner);
    }
  }

  /**
   * Wrap a query, if not {@code null}.
   *
   * @param <O> inner data type
   * @param relation Relation to get object from
   * @param inner inner query
   * @return wrapped query, or {@code null} if inner was {@code null}
   */
  public static <O> RKNNSearcher<DBIDRef> wrap(Relation<? extends O> relation, RKNNSearcher<O> inner) {
    return inner == null ? null : inner instanceof LinearScanQuery //
        ? new Linear<O>(relation, inner) : new WrappedRKNNDBIDByLookup<O>(relation, inner);
  }
}

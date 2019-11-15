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
package elki.index.tree.spatial.rstarvariants.deliclu;

import java.util.ArrayList;
import java.util.List;

import elki.data.NumberVector;
import elki.database.ids.*;
import elki.database.query.PrioritySearcher;
import elki.database.query.distance.DistanceQuery;
import elki.database.query.distance.SpatialDistanceQuery;
import elki.database.query.knn.KNNSearcher;
import elki.database.query.range.RangeSearcher;
import elki.database.relation.Relation;
import elki.index.DistancePriorityIndex;
import elki.index.DynamicIndex;
import elki.index.tree.IndexTreePath;
import elki.index.tree.spatial.rstarvariants.RTreeSettings;
import elki.index.tree.spatial.rstarvariants.query.RStarTreeUtil;
import elki.logging.Logging;
import elki.persistent.PageFile;
import elki.utilities.exceptions.AbortException;

/**
 * The common use of the DeLiClu tree: indexing number vectors.
 * 
 * @author Erich Schubert
 * @since 0.4.0
 * 
 * @param <O> Object type
 */
public class DeLiCluTreeIndex<O extends NumberVector> extends DeLiCluTree implements DistancePriorityIndex<O>, DynamicIndex {
  /**
   * The relation we index.
   */
  private Relation<O> relation;

  /**
   * Constructor.
   * 
   * @param relation Relation to index
   * @param pagefile Page file
   * @param settings Tree settings
   */
  public DeLiCluTreeIndex(Relation<O> relation, PageFile<DeLiCluNode> pagefile, RTreeSettings settings) {
    super(pagefile, settings);
    this.relation = relation;
  }

  /**
   * The appropriate logger for this index.
   */
  private static final Logging LOG = Logging.getLogger(DeLiCluTreeIndex.class);

  /**
   * Creates a new leaf entry representing the specified data object.
   * 
   * @param id Object id
   */
  protected DeLiCluLeafEntry createNewLeafEntry(DBID id) {
    return new DeLiCluLeafEntry(id, relation.get(id));
  }

  /**
   * Marks the specified object as handled and returns the path of node ids from
   * the root to the objects's parent.
   * 
   * @param id the objects id to be marked as handled
   * @param obj the object to be marked as handled
   * @return the path of node ids from the root to the objects's parent
   */
  public synchronized IndexTreePath<DeLiCluEntry> setHandled(DBID id, O obj) {
    if(LOG.isDebugging()) {
      LOG.debugFine("setHandled " + id + ", " + obj + "\n");
    }

    // find the leaf node containing o
    IndexTreePath<DeLiCluEntry> pathToObject = findPathToObject(getRootPath(), obj, id);

    if(pathToObject == null) {
      throw new AbortException("Object not found in setHandled.");
    }

    // set o handled
    DeLiCluEntry entry = pathToObject.getEntry();
    entry.setHasHandled(true);
    entry.setHasUnhandled(false);

    for(IndexTreePath<DeLiCluEntry> path = pathToObject; path.getParentPath() != null; path = path.getParentPath()) {
      DeLiCluEntry parentEntry = path.getParentPath().getEntry();
      DeLiCluNode node = getNode(parentEntry);
      boolean hasHandled = false;
      boolean hasUnhandled = false;
      for(int i = 0; i < node.getNumEntries(); i++) {
        final DeLiCluEntry nodeEntry = node.getEntry(i);
        hasHandled = hasHandled || nodeEntry.hasHandled();
        hasUnhandled = hasUnhandled || nodeEntry.hasUnhandled();
      }
      parentEntry.setHasUnhandled(hasUnhandled);
      parentEntry.setHasHandled(hasHandled);
    }

    return pathToObject;
  }

  @Override
  public void initialize() {
    super.initialize();
    insertAll(relation.getDBIDs());
  }

  /**
   * Inserts the specified real vector object into this index.
   * 
   * @param id the object id that was inserted
   */
  @Override
  public final void insert(DBIDRef id) {
    insertLeaf(createNewLeafEntry(DBIDUtil.deref(id)));
  }

  /**
   * Inserts the specified objects into this index. If a bulk load mode is
   * implemented, the objects are inserted in one bulk.
   * 
   * @param ids the objects to be inserted
   */
  @Override
  public final void insertAll(DBIDs ids) {
    if(ids.isEmpty() || (ids.size() == 1)) {
      return;
    }

    // Make an example leaf
    if(canBulkLoad()) {
      List<DeLiCluEntry> leafs = new ArrayList<>(ids.size());
      for(DBIDIter iter = ids.iter(); iter.valid(); iter.advance()) {
        leafs.add(createNewLeafEntry(DBIDUtil.deref(iter)));
      }
      bulkLoad(leafs);
    }
    else {
      for(DBIDIter iter = ids.iter(); iter.valid(); iter.advance()) {
        insert(iter);
      }
    }

    doExtraIntegrityChecks();
  }

  /**
   * Deletes the specified object from this index.
   * 
   * @return true if this index did contain the object with the specified id,
   *         false otherwise
   */
  @Override
  public final boolean delete(DBIDRef id) {
    // find the leaf node containing o
    O obj = relation.get(id);
    IndexTreePath<DeLiCluEntry> deletionPath = findPathToObject(getRootPath(), obj, id);
    if(deletionPath == null) {
      return false;
    }
    deletePath(deletionPath);
    return true;
  }

  @Override
  public void deleteAll(DBIDs ids) {
    for(DBIDIter iter = ids.iter(); iter.valid(); iter.advance()) {
      delete(DBIDUtil.deref(iter));
    }
  }

  @Override
  public KNNSearcher<O> kNNByObject(DistanceQuery<O> distanceQuery, int maxk, int flags) {
    // Can we support this distance function - spatial distances only!
    return distanceQuery.getRelation() == relation && distanceQuery instanceof SpatialDistanceQuery ? //
        RStarTreeUtil.getKNNQuery(this, (SpatialDistanceQuery<O>) distanceQuery, maxk, flags) : null;
  }

  @Override
  public RangeSearcher<O> rangeByObject(DistanceQuery<O> distanceQuery, double maxradius, int flags) {
    // Can we support this distance function - spatial distances only!
    return distanceQuery.getRelation() == relation && distanceQuery instanceof SpatialDistanceQuery ? //
        RStarTreeUtil.getRangeQuery(this, (SpatialDistanceQuery<O>) distanceQuery, maxradius, flags) : null;
  }

  @Override
  public PrioritySearcher<O> priorityByObject(DistanceQuery<O> distanceQuery, double maxradius, int flags) {
    // Can we support this distance function - spatial distances only!
    return distanceQuery.getRelation() == relation && distanceQuery instanceof SpatialDistanceQuery ? //
        RStarTreeUtil.getDistancePrioritySearcher(this, (SpatialDistanceQuery<O>) distanceQuery, maxradius, flags) : null;
  }

  @Override
  public String getLongName() {
    return "DeLiClu-Tree";
  }

  @Override
  public String getShortName() {
    return "deliclutree";
  }

  @Override
  protected Logging getLogger() {
    return LOG;
  }
}

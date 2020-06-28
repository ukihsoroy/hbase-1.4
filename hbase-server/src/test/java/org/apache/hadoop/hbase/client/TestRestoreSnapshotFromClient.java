/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.hadoop.hbase.client;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.apache.hadoop.fs.Path;
import org.apache.hadoop.hbase.CategoryBasedTimeout;
import org.apache.hadoop.hbase.HBaseTestingUtility;
import org.apache.hadoop.hbase.HColumnDescriptor;
import org.apache.hadoop.hbase.HConstants;
import org.apache.hadoop.hbase.HRegionInfo;
import org.apache.hadoop.hbase.HTableDescriptor;
import org.apache.hadoop.hbase.MetaTableAccessor;
import org.apache.hadoop.hbase.coprocessor.BaseRegionObserver;
import org.apache.hadoop.hbase.coprocessor.ObserverContext;
import org.apache.hadoop.hbase.coprocessor.RegionCoprocessorEnvironment;
import org.apache.hadoop.hbase.regionserver.InternalScanner;
import org.apache.hadoop.hbase.regionserver.ScanType;
import org.apache.hadoop.hbase.regionserver.Store;
import org.apache.hadoop.hbase.testclassification.LargeTests;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.Waiter;
import org.apache.hadoop.hbase.master.AssignmentManager;
import org.apache.hadoop.hbase.master.MasterFileSystem;
import org.apache.hadoop.hbase.master.RegionState;
import org.apache.hadoop.hbase.master.snapshot.SnapshotManager;
import org.apache.hadoop.hbase.regionserver.NoSuchColumnFamilyException;
import org.apache.hadoop.hbase.snapshot.CorruptedSnapshotException;
import org.apache.hadoop.hbase.snapshot.SnapshotDoesNotExistException;
import org.apache.hadoop.hbase.snapshot.SnapshotTestingUtils;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.hadoop.hbase.util.FSUtils;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.rules.TestRule;

/**
 * Test restore snapshots from the client
 */
@Category(LargeTests.class)
public class TestRestoreSnapshotFromClient {
  @Rule public final TestRule timeout = CategoryBasedTimeout.builder()
      .withTimeout(this.getClass())
      .withLookingForStuckThread(true)
      .build();

  protected final static HBaseTestingUtility TEST_UTIL = new HBaseTestingUtility();

  protected final byte[] FAMILY = Bytes.toBytes("cf");

  protected byte[] emptySnapshot;
  protected byte[] snapshotName0;
  protected byte[] snapshotName1;
  protected byte[] snapshotName2;
  protected int snapshot0Rows;
  protected int snapshot1Rows;
  protected TableName tableName;
  protected Admin admin;

  @BeforeClass
  public static void setUpBeforeClass() throws Exception {
    TEST_UTIL.getConfiguration().setBoolean(SnapshotManager.HBASE_SNAPSHOT_ENABLED, true);
    TEST_UTIL.getConfiguration().setBoolean("hbase.online.schema.update.enable", true);
    TEST_UTIL.getConfiguration().setInt("hbase.hstore.compactionThreshold", 10);
    TEST_UTIL.getConfiguration().setInt("hbase.regionserver.msginterval", 100);
    TEST_UTIL.getConfiguration().setInt("hbase.client.pause", 250);
    TEST_UTIL.getConfiguration().setInt(HConstants.HBASE_CLIENT_RETRIES_NUMBER, 6);
    TEST_UTIL.getConfiguration().setBoolean("hbase.master.enabletable.roundrobin", true);
    // Setting bigger value to avoid catalog janitor execution (parent region cleanup)
    TEST_UTIL.getConfiguration().setLong("hbase.catalogjanitor.interval", 1800000);
    TEST_UTIL.startMiniCluster(3);
  }

  @AfterClass
  public static void tearDownAfterClass() throws Exception {
    TEST_UTIL.shutdownMiniCluster();
  }

  /**
   * Initialize the tests with a table filled with some data
   * and two snapshots (snapshotName0, snapshotName1) of different states.
   * The tableName, snapshotNames and the number of rows in the snapshot are initialized.
   */
  @Before
  public void setup() throws Exception {
    this.admin = TEST_UTIL.getHBaseAdmin();

    long tid = System.currentTimeMillis();
    tableName =
        TableName.valueOf("testtb-" + tid);
    emptySnapshot = Bytes.toBytes("emptySnaptb-" + tid);
    snapshotName0 = Bytes.toBytes("snaptb0-" + tid);
    snapshotName1 = Bytes.toBytes("snaptb1-" + tid);
    snapshotName2 = Bytes.toBytes("snaptb2-" + tid);

    // create Table and disable it
    SnapshotTestingUtils.createTable(TEST_UTIL, tableName, getNumReplicas(), FAMILY);
    admin.disableTable(tableName);

    // take an empty snapshot
    admin.snapshot(emptySnapshot, tableName);

    // enable table and insert data
    admin.enableTable(tableName);
    SnapshotTestingUtils.loadData(TEST_UTIL, tableName, 500, FAMILY);
    try (Table table = TEST_UTIL.getConnection().getTable(tableName)) {
      snapshot0Rows = TEST_UTIL.countRows(table);
    }
    admin.disableTable(tableName);

    // take a snapshot
    admin.snapshot(snapshotName0, tableName);

    // enable table and insert more data
    admin.enableTable(tableName);
    SnapshotTestingUtils.loadData(TEST_UTIL, tableName, 500, FAMILY);
    try (Table table = TEST_UTIL.getConnection().getTable(tableName)) {
      snapshot1Rows = TEST_UTIL.countRows(table);
    }
  }

  @After
  public void tearDown() throws Exception {
    TEST_UTIL.deleteTable(tableName);
    SnapshotTestingUtils.deleteAllSnapshots(TEST_UTIL.getHBaseAdmin());
    SnapshotTestingUtils.deleteArchiveDirectory(TEST_UTIL);
  }

  @Test
  public void testRestoreSnapshot() throws IOException {
    SnapshotTestingUtils.verifyRowCount(TEST_UTIL, tableName, snapshot1Rows);
    admin.disableTable(tableName);
    admin.snapshot(snapshotName1, tableName);
    // Restore from snapshot-0
    admin.restoreSnapshot(snapshotName0);
    admin.enableTable(tableName);
    SnapshotTestingUtils.verifyRowCount(TEST_UTIL, tableName, snapshot0Rows);
    SnapshotTestingUtils.verifyReplicasCameOnline(tableName, admin, getNumReplicas());

    // Restore from emptySnapshot
    admin.disableTable(tableName);
    admin.restoreSnapshot(emptySnapshot);
    admin.enableTable(tableName);
    SnapshotTestingUtils.verifyRowCount(TEST_UTIL, tableName, 0);
    SnapshotTestingUtils.verifyReplicasCameOnline(tableName, admin, getNumReplicas());

    // Restore from snapshot-1
    admin.disableTable(tableName);
    admin.restoreSnapshot(snapshotName1);
    admin.enableTable(tableName);
    SnapshotTestingUtils.verifyRowCount(TEST_UTIL, tableName, snapshot1Rows);
    SnapshotTestingUtils.verifyReplicasCameOnline(tableName, admin, getNumReplicas());

    // Restore from snapshot-1
    TEST_UTIL.deleteTable(tableName);
    admin.restoreSnapshot(snapshotName1);
    SnapshotTestingUtils.verifyRowCount(TEST_UTIL, tableName, snapshot1Rows);
    SnapshotTestingUtils.verifyReplicasCameOnline(tableName, admin, getNumReplicas());
  }

  @Test(timeout = 300000)
  public void testRestoreSnapshotAfterSplit() throws Exception {
    Admin admin = null;
    try {
      admin = TEST_UTIL.getHBaseAdmin();
      final int regionReplication = admin.getTableDescriptor(tableName).getRegionReplication();
      // Region count before split
      final int primaryRegionCountBeforeSplit = MetaTableAccessor
          .getTableRegions(TEST_UTIL.getZooKeeperWatcher(), TEST_UTIL.getConnection(), tableName)
          .size() / regionReplication;

      admin.split(tableName, "m".getBytes());
      final AssignmentManager am = TEST_UTIL.getHBaseCluster().getMaster().getAssignmentManager();
      // Wait for replica region to become online
      TEST_UTIL.waitFor(60000, 500, new Waiter.Predicate<IOException>() {
        @Override
        public boolean evaluate() throws IOException {
          return am.getRegionStates().getRegionByStateOfTable(tableName).get(RegionState.State.OPEN)
              .size() == ((primaryRegionCountBeforeSplit + 1) * regionReplication);
        }
      });

      int regionCountAfterSplit = MetaTableAccessor
          .getTableRegions(TEST_UTIL.getZooKeeperWatcher(), TEST_UTIL.getConnection(), tableName)
          .size() / regionReplication;
      // regionCountAfterSplit will contain parent region, so primaryregionCountBeforeSplit + 2
      assertEquals(primaryRegionCountBeforeSplit + 2, regionCountAfterSplit);

      String snapshotName = "testRestoreSnapshotAfterSplit-snap";
      // Create snapshot after table split
      admin.snapshot(snapshotName, tableName);
      assertEquals(1,
        admin.listTableSnapshots("testtb-.*", "testRestoreSnapshotAfterSplit-snap*").size());
      // Restore snapshot
      admin.disableTable(tableName);
      admin.restoreSnapshot(snapshotName);

      int regionCountAfterRestoreSnapshot = MetaTableAccessor
          .getTableRegions(TEST_UTIL.getZooKeeperWatcher(), TEST_UTIL.getConnection(), tableName)
          .size();
      assertEquals(primaryRegionCountBeforeSplit + 2, regionCountAfterRestoreSnapshot);

      // Enable the table
      admin.enableTable(tableName);
      assertEquals((primaryRegionCountBeforeSplit + 1) * regionReplication,
        am.getRegionStates().getRegionByStateOfTable(tableName).get(RegionState.State.OPEN).size());

    } finally {
      if (admin != null) {
        try {
          admin.deleteTableSnapshots("testtb-.*", "testRestoreSnapshotAfterSplit-snap*");
        } catch (SnapshotDoesNotExistException ignore) {
        }
        admin.close();
      }
    }
  }

  protected int getNumReplicas() {
    return 1;
  }

  @Test
  public void testRestoreSchemaChange() throws Exception {
    byte[] TEST_FAMILY2 = Bytes.toBytes("cf2");

    HTable table = new HTable(TEST_UTIL.getConfiguration(), tableName);

    // Add one column family and put some data in it
    admin.disableTable(tableName);
    admin.addColumn(tableName, new HColumnDescriptor(TEST_FAMILY2));
    admin.enableTable(tableName);
    assertEquals(2, table.getTableDescriptor().getFamilies().size());
    HTableDescriptor htd = admin.getTableDescriptor(tableName);
    assertEquals(2, htd.getFamilies().size());
    SnapshotTestingUtils.loadData(TEST_UTIL, tableName, 500, TEST_FAMILY2);
    long snapshot2Rows = snapshot1Rows + 500;
    assertEquals(snapshot2Rows, TEST_UTIL.countRows(table));
    assertEquals(500, TEST_UTIL.countRows(table, TEST_FAMILY2));
    Set<String> fsFamilies = getFamiliesFromFS(tableName);
    assertEquals(2, fsFamilies.size());

    // Take a snapshot
    admin.disableTable(tableName);
    admin.snapshot(snapshotName2, tableName);

    // Restore the snapshot (without the cf)
    admin.restoreSnapshot(snapshotName0);
    admin.enableTable(tableName);
    assertEquals(1, table.getTableDescriptor().getFamilies().size());
    try {
      TEST_UTIL.countRows(table, TEST_FAMILY2);
      fail("family '" + Bytes.toString(TEST_FAMILY2) + "' should not exists");
    } catch (NoSuchColumnFamilyException e) {
      // expected
    }
    assertEquals(snapshot0Rows, TEST_UTIL.countRows(table));
    htd = admin.getTableDescriptor(tableName);
    assertEquals(1, htd.getFamilies().size());
    fsFamilies = getFamiliesFromFS(tableName);
    assertEquals(1, fsFamilies.size());

    // Restore back the snapshot (with the cf)
    admin.disableTable(tableName);
    admin.restoreSnapshot(snapshotName2);
    admin.enableTable(tableName);
    htd = admin.getTableDescriptor(tableName);
    assertEquals(2, htd.getFamilies().size());
    assertEquals(2, table.getTableDescriptor().getFamilies().size());
    assertEquals(500, TEST_UTIL.countRows(table, TEST_FAMILY2));
    assertEquals(snapshot2Rows, TEST_UTIL.countRows(table));
    fsFamilies = getFamiliesFromFS(tableName);
    assertEquals(2, fsFamilies.size());
    table.close();
  }

  @Test
  public void testCloneSnapshotOfCloned() throws IOException, InterruptedException {
    TableName clonedTableName =
        TableName.valueOf("clonedtb-" + System.currentTimeMillis());
    admin.cloneSnapshot(snapshotName0, clonedTableName);
    SnapshotTestingUtils.verifyRowCount(TEST_UTIL, clonedTableName, snapshot0Rows);
    SnapshotTestingUtils.verifyReplicasCameOnline(clonedTableName, admin, getNumReplicas());
    admin.disableTable(clonedTableName);
    admin.snapshot(snapshotName2, clonedTableName);
    TEST_UTIL.deleteTable(clonedTableName);
    waitCleanerRun();

    admin.cloneSnapshot(snapshotName2, clonedTableName);
    SnapshotTestingUtils.verifyRowCount(TEST_UTIL, clonedTableName, snapshot0Rows);
    SnapshotTestingUtils.verifyReplicasCameOnline(clonedTableName, admin, getNumReplicas());
    TEST_UTIL.deleteTable(clonedTableName);
  }

  @Test
  public void testCloneAndRestoreSnapshot() throws IOException, InterruptedException {
    TEST_UTIL.deleteTable(tableName);
    waitCleanerRun();

    admin.cloneSnapshot(snapshotName0, tableName);
    SnapshotTestingUtils.verifyRowCount(TEST_UTIL, tableName, snapshot0Rows);
    SnapshotTestingUtils.verifyReplicasCameOnline(tableName, admin, getNumReplicas());
    waitCleanerRun();

    admin.disableTable(tableName);
    admin.restoreSnapshot(snapshotName0);
    admin.enableTable(tableName);
    SnapshotTestingUtils.verifyRowCount(TEST_UTIL, tableName, snapshot0Rows);
    SnapshotTestingUtils.verifyReplicasCameOnline(tableName, admin, getNumReplicas());
  }

  @Test
  public void testCorruptedSnapshot() throws IOException, InterruptedException {
    SnapshotTestingUtils.corruptSnapshot(TEST_UTIL, Bytes.toString(snapshotName0));
    TableName cloneName = TableName.valueOf("corruptedClone-" + System.currentTimeMillis());
    try {
      admin.cloneSnapshot(snapshotName0, cloneName);
      fail("Expected CorruptedSnapshotException, got succeeded cloneSnapshot()");
    } catch (CorruptedSnapshotException e) {
      // Got the expected corruption exception.
      // check for no references of the cloned table.
      assertFalse(admin.tableExists(cloneName));
    } catch (Exception e) {
      fail("Expected CorruptedSnapshotException got: " + e);
    }
  }

  @Test
  public void testRestoreSnapshotAfterSplittingRegions() throws IOException, InterruptedException {
    // HBASE-20008: Add a coprocessor to delay compactions of the daughter regions. To reproduce
    // the NullPointerException, we need to delay compactions of the daughter regions after
    // splitting region.
    HTableDescriptor tableDescriptor = admin.getTableDescriptor(tableName);
    tableDescriptor.addCoprocessor(DelayCompactionObserver.class.getName());
    admin.disableTable(tableName);
    admin.modifyTable(tableName, tableDescriptor);
    admin.enableTable(tableName);

    List<HRegionInfo> regionInfos = admin.getTableRegions(tableName);
    RegionReplicaUtil.removeNonDefaultRegions(regionInfos);

    // Split the first region
    splitRegion(regionInfos.get(0));

    // Take a snapshot
    admin.snapshot(snapshotName1, tableName);

    // Restore the snapshot
    admin.disableTable(tableName);
    admin.restoreSnapshot(snapshotName1);
    admin.enableTable(tableName);

    SnapshotTestingUtils.verifyRowCount(TEST_UTIL, tableName, snapshot1Rows);
  }

  public static class DelayCompactionObserver extends BaseRegionObserver {
    @Override
    public InternalScanner preCompact(ObserverContext<RegionCoprocessorEnvironment> e,
        final Store store, final InternalScanner scanner, final ScanType scanType)
        throws IOException {

      try {
        // Delay 5 seconds.
        TimeUnit.SECONDS.sleep(5);
      } catch (InterruptedException ex) {
        throw new InterruptedIOException(ex.getMessage());
      }

      return scanner;
    }
  }

  // ==========================================================================
  //  Helpers
  // ==========================================================================
  private void waitCleanerRun() throws InterruptedException {
    TEST_UTIL.getMiniHBaseCluster().getMaster().getHFileCleaner().choreForTesting();
  }

  private Set<String> getFamiliesFromFS(final TableName tableName) throws IOException {
    MasterFileSystem mfs = TEST_UTIL.getMiniHBaseCluster().getMaster().getMasterFileSystem();
    Set<String> families = new HashSet<String>();
    Path tableDir = FSUtils.getTableDir(mfs.getRootDir(), tableName);
    for (Path regionDir: FSUtils.getRegionDirs(mfs.getFileSystem(), tableDir)) {
      for (Path familyDir: FSUtils.getFamilyDirs(mfs.getFileSystem(), regionDir)) {
        families.add(familyDir.getName());
      }
    }
    return families;
  }

  protected void splitRegion(final HRegionInfo regionInfo) throws IOException {
    byte[][] splitPoints = Bytes.split(regionInfo.getStartKey(), regionInfo.getEndKey(), 1);
    admin.split(regionInfo.getTable(), splitPoints[1]);
  }
}

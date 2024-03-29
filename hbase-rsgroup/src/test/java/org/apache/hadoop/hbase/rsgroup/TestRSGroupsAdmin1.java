/**
 * Copyright The Apache Software Foundation
 *
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
package org.apache.hadoop.hbase.rsgroup;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.hbase.HColumnDescriptor;
import org.apache.hadoop.hbase.HTableDescriptor;
import org.apache.hadoop.hbase.NamespaceDescriptor;
import org.apache.hadoop.hbase.ServerName;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.Waiter;
import org.apache.hadoop.hbase.constraint.ConstraintException;
import org.apache.hadoop.hbase.master.ServerManager;
import org.apache.hadoop.hbase.master.TableNamespaceManager;
import org.apache.hadoop.hbase.master.snapshot.SnapshotManager;
import org.apache.hadoop.hbase.net.Address;
import org.apache.hadoop.hbase.quotas.QuotaUtil;
import org.apache.hadoop.hbase.testclassification.MediumTests;
import org.apache.hadoop.hbase.util.Bytes;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.google.common.collect.Sets;

@Category({MediumTests.class})
public class TestRSGroupsAdmin1 extends TestRSGroupsBase {
  protected static final Log LOG = LogFactory.getLog(TestRSGroupsAdmin1.class);

  @BeforeClass
  public static void setUp() throws Exception {
    setUpTestBeforeClass();
  }

  @AfterClass
  public static void tearDown() throws Exception {
    tearDownAfterClass();
  }

  @Before
  public void beforeMethod() throws Exception {
    setUpBeforeMethod();
  }

  @After
  public void afterMethod() throws Exception {
    tearDownAfterMethod();
  }

  @Test
  public void testValidGroupNames() throws IOException {
    String[] badNames = {"foo*","foo@","-"};
    String[] goodNames = {"foo_123"};

    for(String entry: badNames) {
      try {
        rsGroupAdmin.addRSGroup(entry);
        fail("Expected a constraint exception for: "+entry);
      } catch(ConstraintException ex) {
        //expected
      }
    }

    for(String entry: goodNames) {
      rsGroupAdmin.addRSGroup(entry);
    }
  }

  @Test
  public void testBogusArgs() throws Exception {
    assertNull(rsGroupAdmin.getRSGroupInfoOfTable(TableName.valueOf("nonexistent")));
    assertNull(rsGroupAdmin.getRSGroupOfServer(Address.fromParts("bogus",123)));
    assertNull(rsGroupAdmin.getRSGroupInfo("bogus"));

    try {
      rsGroupAdmin.removeRSGroup("bogus");
      fail("Expected removing bogus group to fail");
    } catch(ConstraintException ex) {
      //expected
    }

    try {
      rsGroupAdmin.moveTables(Sets.newHashSet(TableName.valueOf("bogustable")), "bogus");
      fail("Expected move with bogus group to fail");
    } catch(ConstraintException ex) {
      //expected
    }

    try {
      rsGroupAdmin.moveServers(Sets.newHashSet(Address.fromParts("bogus",123)), "bogus");
      fail("Expected move with bogus group to fail");
    } catch(ConstraintException ex) {
      //expected
    }

    try {
      rsGroupAdmin.balanceRSGroup("bogus");
      fail("Expected move with bogus group to fail");
    } catch(ConstraintException ex) {
      //expected
    }
  }

  @Test
  public void testNamespaceConstraint() throws Exception {
    String nsName = tablePrefix+"_foo";
    String groupName = tablePrefix+"_foo";
    LOG.info("testNamespaceConstraint");
    rsGroupAdmin.addRSGroup(groupName);
    assertTrue(observer.preAddRSGroupCalled);
    assertTrue(observer.postAddRSGroupCalled);
    admin.createNamespace(NamespaceDescriptor.create(nsName)
        .addConfiguration(RSGroupInfo.NAMESPACE_DESC_PROP_GROUP, groupName)
        .build());
    //test removing a referenced group
    try {
      rsGroupAdmin.removeRSGroup(groupName);
      fail("Expected a constraint exception");
    } catch (IOException ex) {
    }
    //test modify group
    //changing with the same name is fine
    admin.modifyNamespace(
        NamespaceDescriptor.create(nsName)
          .addConfiguration(RSGroupInfo.NAMESPACE_DESC_PROP_GROUP, groupName)
          .build());
    String anotherGroup = tablePrefix+"_anotherGroup";
    rsGroupAdmin.addRSGroup(anotherGroup);
    //test add non-existent group
    admin.deleteNamespace(nsName);
    rsGroupAdmin.removeRSGroup(groupName);
    assertTrue(observer.preRemoveRSGroupCalled);
    assertTrue(observer.postRemoveRSGroupCalled);
    try {
      admin.createNamespace(NamespaceDescriptor.create(nsName)
          .addConfiguration(RSGroupInfo.NAMESPACE_DESC_PROP_GROUP, "foo")
          .build());
      fail("Expected a constraint exception");
    } catch (IOException ex) {
    }
  }

  @Test
  public void testGroupInfoMultiAccessing() throws Exception {
    RSGroupInfoManager manager = RSGroupAdminEndpoint.getGroupInfoManager();
    final RSGroupInfo defaultGroup = manager.getRSGroup("default");
    // getRSGroup updates default group's server list
    // this process must not affect other threads iterating the list
    Iterator<Address> it = defaultGroup.getServers().iterator();
    manager.getRSGroup("default");
    it.next();
  }

  @Test
  public void testFailRemoveGroup() throws IOException, InterruptedException {
    LOG.info("testFailRemoveGroup");

    int initNumGroups = rsGroupAdmin.listRSGroups().size();
    addGroup(rsGroupAdmin, "bar", 3);
    TableName tableName = TableName.valueOf(tablePrefix+"_my_table");
    TEST_UTIL.createTable(tableName, Bytes.toBytes("f"));
    rsGroupAdmin.moveTables(Sets.newHashSet(tableName), "bar");
    RSGroupInfo barGroup = rsGroupAdmin.getRSGroupInfo("bar");
    //group is not empty therefore it should fail
    try {
      rsGroupAdmin.removeRSGroup(barGroup.getName());
      fail("Expected remove group to fail");
    } catch(IOException e) {
    }
    //group cannot lose all it's servers therefore it should fail
    try {
      rsGroupAdmin.moveServers(barGroup.getServers(), RSGroupInfo.DEFAULT_GROUP);
      fail("Expected move servers to fail");
    } catch(IOException e) {
    }

    rsGroupAdmin.moveTables(barGroup.getTables(), RSGroupInfo.DEFAULT_GROUP);
    try {
      rsGroupAdmin.removeRSGroup(barGroup.getName());
      fail("Expected move servers to fail");
    } catch(IOException e) {
    }

    rsGroupAdmin.moveServers(barGroup.getServers(), RSGroupInfo.DEFAULT_GROUP);
    rsGroupAdmin.removeRSGroup(barGroup.getName());

    Assert.assertEquals(initNumGroups, rsGroupAdmin.listRSGroups().size());
  }

  @Test
  public void testMultiTableMove() throws Exception {
    LOG.info("testMultiTableMove");

    final TableName tableNameA = TableName.valueOf(tablePrefix + "_testMultiTableMoveA");
    final TableName tableNameB = TableName.valueOf(tablePrefix + "_testMultiTableMoveB");
    final byte[] familyNameBytes = Bytes.toBytes("f");
    String newGroupName = getGroupName("testMultiTableMove");
    final RSGroupInfo newGroup = addGroup(rsGroupAdmin, newGroupName, 1);

    TEST_UTIL.createTable(tableNameA, familyNameBytes);
    TEST_UTIL.createTable(tableNameB, familyNameBytes);
    TEST_UTIL.waitFor(WAIT_TIMEOUT, new Waiter.Predicate<Exception>() {
      @Override
      public boolean evaluate() throws Exception {
        List<String> regionsA = getTableRegionMap().get(tableNameA);
        if (regionsA == null) {
          return false;
        }
        List<String> regionsB = getTableRegionMap().get(tableNameB);
        if (regionsB == null) {
          return false;
        }
        return getTableRegionMap().get(tableNameA).size() >= 1
                && getTableRegionMap().get(tableNameB).size() >= 1;
      }
    });

    RSGroupInfo tableGrpA = rsGroupAdmin.getRSGroupInfoOfTable(tableNameA);
    assertTrue(tableGrpA.getName().equals(RSGroupInfo.DEFAULT_GROUP));

    RSGroupInfo tableGrpB = rsGroupAdmin.getRSGroupInfoOfTable(tableNameB);
    assertTrue(tableGrpB.getName().equals(RSGroupInfo.DEFAULT_GROUP));
    //change table's group
    LOG.info("Moving table [" + tableNameA + "," + tableNameB + "] to " + newGroup.getName());
    rsGroupAdmin.moveTables(Sets.newHashSet(tableNameA, tableNameB), newGroup.getName());

    //verify group change
    Assert.assertEquals(newGroup.getName(),
            rsGroupAdmin.getRSGroupInfoOfTable(tableNameA).getName());

    Assert.assertEquals(newGroup.getName(),
            rsGroupAdmin.getRSGroupInfoOfTable(tableNameB).getName());

    //verify tables' not exist in old group
    Set<TableName> DefaultTables =
        rsGroupAdmin.getRSGroupInfo(RSGroupInfo.DEFAULT_GROUP).getTables();
    assertFalse(DefaultTables.contains(tableNameA));
    assertFalse(DefaultTables.contains(tableNameB));

    //verify tables' exist in new group
    Set<TableName> newGroupTables = rsGroupAdmin.getRSGroupInfo(newGroupName).getTables();
    assertTrue(newGroupTables.contains(tableNameA));
    assertTrue(newGroupTables.contains(tableNameB));
  }

  @Test
  public void testTableMoveTruncateAndDrop() throws Exception {
    LOG.info("testTableMove");

    final TableName tableName = TableName.valueOf(tablePrefix + "_testTableMoveAndDrop");
    final byte[] familyNameBytes = Bytes.toBytes("f");
    String newGroupName = getGroupName("testTableMove");
    final RSGroupInfo newGroup = addGroup(rsGroupAdmin, newGroupName, 2);

    TEST_UTIL.createMultiRegionTable(tableName, familyNameBytes, 5);
    TEST_UTIL.waitFor(WAIT_TIMEOUT, new Waiter.Predicate<Exception>() {
      @Override
      public boolean evaluate() throws Exception {
        List<String> regions = getTableRegionMap().get(tableName);
        if (regions == null)
          return false;
        return getTableRegionMap().get(tableName).size() >= 5;
      }
    });

    RSGroupInfo tableGrp = rsGroupAdmin.getRSGroupInfoOfTable(tableName);
    assertTrue(tableGrp.getName().equals(RSGroupInfo.DEFAULT_GROUP));

    //change table's group
    LOG.info("Moving table "+tableName+" to "+newGroup.getName());
    rsGroupAdmin.moveTables(Sets.newHashSet(tableName), newGroup.getName());

    //verify group change
    Assert.assertEquals(newGroup.getName(),
        rsGroupAdmin.getRSGroupInfoOfTable(tableName).getName());

    TEST_UTIL.waitFor(WAIT_TIMEOUT, new Waiter.Predicate<Exception>() {
      @Override
      public boolean evaluate() throws Exception {
        Map<ServerName, List<String>> serverMap = getTableServerRegionMap().get(tableName);
        int count = 0;
        if (serverMap != null) {
          for (ServerName rs : serverMap.keySet()) {
            if (newGroup.containsServer(rs.getAddress())) {
              count += serverMap.get(rs).size();
            }
          }
        }
        return count == 5;
      }
    });

    //test truncate
    admin.disableTable(tableName);
    admin.truncateTable(tableName, true);
    Assert.assertEquals(1, rsGroupAdmin.getRSGroupInfo(newGroup.getName()).getTables().size());
    Assert.assertEquals(tableName, rsGroupAdmin.getRSGroupInfo(
        newGroup.getName()).getTables().first());

    //verify removed table is removed from group
    TEST_UTIL.deleteTable(tableName);
    Assert.assertEquals(0, rsGroupAdmin.getRSGroupInfo(newGroup.getName()).getTables().size());

    assertTrue(observer.preMoveTablesCalled);
    assertTrue(observer.postMoveTablesCalled);
  }

  @Test
  public void testDisabledTableMove() throws Exception {
    final TableName tableName = TableName.valueOf(tablePrefix + "_testDisabledTableMove");
    final byte[] familyNameBytes = Bytes.toBytes("f");
    String newGroupName = getGroupName("testDisabledTableMove");
    final RSGroupInfo newGroup = addGroup(rsGroupAdmin, newGroupName, 2);

    TEST_UTIL.createMultiRegionTable(tableName, familyNameBytes, 5);
    TEST_UTIL.waitFor(WAIT_TIMEOUT, new Waiter.Predicate<Exception>() {
      @Override
      public boolean evaluate() throws Exception {
        List<String> regions = getTableRegionMap().get(tableName);
        if (regions == null) {
          return false;
        }
        return getTableRegionMap().get(tableName).size() >= 5;
      }
    });

    RSGroupInfo tableGrp = rsGroupAdmin.getRSGroupInfoOfTable(tableName);
    assertTrue(tableGrp.getName().equals(RSGroupInfo.DEFAULT_GROUP));

    //test disable table
    admin.disableTable(tableName);

    //change table's group
    LOG.info("Moving table "+ tableName + " to " + newGroup.getName());
    rsGroupAdmin.moveTables(Sets.newHashSet(tableName), newGroup.getName());

    //verify group change
    Assert.assertEquals(newGroup.getName(),
        rsGroupAdmin.getRSGroupInfoOfTable(tableName).getName());
  }

  @Test
  public void testRSGroupListDoesNotContainFailedTableCreation() throws Exception {
    toggleQuotaCheckAndRestartMiniCluster(true);
    String nsp = "np1";
    NamespaceDescriptor nspDesc =
        NamespaceDescriptor.create(nsp).addConfiguration(TableNamespaceManager.KEY_MAX_REGIONS, "5")
            .addConfiguration(TableNamespaceManager.KEY_MAX_TABLES, "2").build();
    admin.createNamespace(nspDesc);
    assertEquals(3, admin.listNamespaceDescriptors().length);
    HColumnDescriptor fam1 = new HColumnDescriptor("fam1");
    HTableDescriptor tableDescOne =
        new HTableDescriptor(TableName.valueOf(nsp + TableName.NAMESPACE_DELIM + "table1"));
    tableDescOne.addFamily(fam1);
    admin.createTable(tableDescOne);

    HTableDescriptor tableDescTwo =
        new HTableDescriptor(TableName.valueOf(nsp + TableName.NAMESPACE_DELIM + "table2"));
    tableDescTwo.addFamily(fam1);
    boolean constraintViolated = false;

    try {
      admin.createTable(tableDescTwo, Bytes.toBytes("AAA"), Bytes.toBytes("ZZZ"),
          6);
      Assert.fail("Creation table should fail because of quota violation.");
    } catch (Exception exp) {
      assertTrue(exp instanceof IOException);
      constraintViolated = true;
    } finally {
      assertTrue("Constraint not violated for table " + tableDescTwo.getTableName(),
          constraintViolated);
    }
    List<RSGroupInfo> rsGroupInfoList = rsGroupAdmin.listRSGroups();
    boolean foundTable2 = false;
    boolean foundTable1 = false;
    for (int i = 0; i < rsGroupInfoList.size(); i++){
      if(rsGroupInfoList.get(i).getTables().contains(tableDescTwo.getTableName())){
        foundTable2 = true;
      }
      if(rsGroupInfoList.get(i).getTables().contains(tableDescOne.getTableName())){
        foundTable1 = true;
      }
    }
    assertFalse("Found table2 in rsgroup list.", foundTable2);
    assertTrue("Did not find table1 in rsgroup list", foundTable1);

    TEST_UTIL.deleteTable(tableDescOne.getTableName());
    admin.deleteNamespace(nspDesc.getName());
    toggleQuotaCheckAndRestartMiniCluster(false);

  }

  private void toggleQuotaCheckAndRestartMiniCluster(boolean enable) throws Exception {
    TEST_UTIL.shutdownMiniCluster();
    TEST_UTIL.getConfiguration().setBoolean(QuotaUtil.QUOTA_CONF_KEY, enable);
    TEST_UTIL.startMiniCluster(NUM_SLAVES_BASE - 1);
    TEST_UTIL.getConfiguration().setInt(
        ServerManager.WAIT_ON_REGIONSERVERS_MINTOSTART,
        NUM_SLAVES_BASE - 1);
    TEST_UTIL.getConfiguration().setBoolean(SnapshotManager.HBASE_SNAPSHOT_ENABLED, true);
    initialize();
  }

}

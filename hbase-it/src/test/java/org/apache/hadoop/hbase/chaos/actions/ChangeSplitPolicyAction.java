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
package org.apache.hadoop.hbase.chaos.actions;

import java.util.Random;

import org.apache.hadoop.hbase.HBaseTestingUtility;
import org.apache.hadoop.hbase.HTableDescriptor;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Admin;
import org.apache.hadoop.hbase.regionserver.ConstantSizeRegionSplitPolicy;
import org.apache.hadoop.hbase.regionserver.DisabledRegionSplitPolicy;
import org.apache.hadoop.hbase.regionserver.IncreasingToUpperBoundRegionSplitPolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ChangeSplitPolicyAction extends Action {
  private static final Logger LOG = LoggerFactory.getLogger(ChangeSplitPolicyAction.class);
  private final TableName tableName;
  private final String[] possiblePolicies;
  private final Random random;

  public ChangeSplitPolicyAction(TableName tableName) {
    this.tableName = tableName;
    possiblePolicies = new String[] {
        IncreasingToUpperBoundRegionSplitPolicy.class.getName(),
        ConstantSizeRegionSplitPolicy.class.getName(),
        DisabledRegionSplitPolicy.class.getName()
    };
    this.random = new Random();
  }


  @Override
  public void perform() throws Exception {
    HBaseTestingUtility util = context.getHBaseIntegrationTestingUtility();
    Admin admin = util.getHBaseAdmin();

    LOG.info("Performing action: Change split policy of table " + tableName);
    HTableDescriptor tableDescriptor = admin.getTableDescriptor(tableName);
    String chosenPolicy = possiblePolicies[random.nextInt(possiblePolicies.length)];
    tableDescriptor.setRegionSplitPolicyClassName(chosenPolicy);
    LOG.info("Changing "  + tableName + " split policy to " + chosenPolicy);
    admin.modifyTable(tableName, tableDescriptor);
  }
}

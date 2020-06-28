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
package org.apache.hadoop.hbase.hbtop.mode;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import java.util.List;
import org.apache.hadoop.hbase.hbtop.Record;
import org.apache.hadoop.hbase.hbtop.TestUtils;
import org.apache.hadoop.hbase.hbtop.field.Field;
import org.apache.hadoop.hbase.testclassification.SmallTests;
import org.junit.experimental.categories.Category;

@Category(SmallTests.class)
public class TestRegionServerMode extends TestModeBase {

  @Override
  protected Mode getMode() {
    return Mode.REGION_SERVER;
  }

  @Override
  protected void assertRecords(List<Record> records) {
    TestUtils.assertRecordsInRegionServerMode(records);
  }

  @Override
  protected void assertDrillDown(Record currentRecord, DrillDownInfo drillDownInfo) {
    assertThat(drillDownInfo.getNextMode(), is(Mode.REGION));
    assertThat(drillDownInfo.getInitialFilters().size(), is(1));

    switch (currentRecord.get(Field.REGION_SERVER).asString()) {
      case "host1:1000":
        assertThat(drillDownInfo.getInitialFilters().get(0).toString(), is("RS==host1:1000"));
        break;

      case "host2:1001":
        assertThat(drillDownInfo.getInitialFilters().get(0).toString(), is("RS==host2:1001"));
        break;

      default:
        fail();
    }
  }
}

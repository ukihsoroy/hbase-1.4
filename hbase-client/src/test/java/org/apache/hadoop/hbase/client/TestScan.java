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
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.util.Arrays;
import java.util.Set;

import org.apache.hadoop.hbase.HConstants;
import org.apache.hadoop.hbase.filter.FilterList;
import org.apache.hadoop.hbase.protobuf.ProtobufUtil;
import org.apache.hadoop.hbase.protobuf.generated.ClientProtos;
import org.apache.hadoop.hbase.security.visibility.Authorizations;
import org.apache.hadoop.hbase.testclassification.SmallTests;
import org.apache.hadoop.hbase.util.Bytes;
import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;

// TODO: cover more test cases
@Category(SmallTests.class)
public class TestScan {
  @Test
  public void testAttributesSerialization() throws IOException {
    Scan scan = new Scan();
    scan.setAttribute("attribute1", Bytes.toBytes("value1"));
    scan.setAttribute("attribute2", Bytes.toBytes("value2"));
    scan.setAttribute("attribute3", Bytes.toBytes("value3"));

    ClientProtos.Scan scanProto = ProtobufUtil.toScan(scan);

    Scan scan2 = ProtobufUtil.toScan(scanProto);

    Assert.assertNull(scan2.getAttribute("absent"));
    Assert.assertTrue(Arrays.equals(Bytes.toBytes("value1"), scan2.getAttribute("attribute1")));
    Assert.assertTrue(Arrays.equals(Bytes.toBytes("value2"), scan2.getAttribute("attribute2")));
    Assert.assertTrue(Arrays.equals(Bytes.toBytes("value3"), scan2.getAttribute("attribute3")));
    Assert.assertEquals(3, scan2.getAttributesMap().size());
  }

  @Test
  public void testGetToScan() throws IOException {
    Get get = new Get(Bytes.toBytes(1));
    get.setCacheBlocks(true).setConsistency(Consistency.TIMELINE).setFilter(new FilterList())
        .setId("get").setIsolationLevel(IsolationLevel.READ_COMMITTED)
        .setLoadColumnFamiliesOnDemand(false).setMaxResultsPerColumnFamily(1000)
        .setMaxVersions(9999).setRowOffsetPerColumnFamily(5).setTimeRange(0, 13)
        .setAttribute("att_v0", Bytes.toBytes("att_v0"))
        .setColumnFamilyTimeRange(Bytes.toBytes("cf"), 0, 123);
    Scan scan = new Scan(get);
    assertEquals(get.getCacheBlocks(), scan.getCacheBlocks());
    assertEquals(get.getConsistency(), scan.getConsistency());
    assertEquals(get.getFilter(), scan.getFilter());
    assertEquals(get.getId(), scan.getId());
    assertEquals(get.getIsolationLevel(), scan.getIsolationLevel());
    assertEquals(get.getLoadColumnFamiliesOnDemandValue(),
        scan.getLoadColumnFamiliesOnDemandValue());
    assertEquals(get.getMaxResultsPerColumnFamily(), scan.getMaxResultsPerColumnFamily());
    assertEquals(get.getMaxVersions(), scan.getMaxVersions());
    assertEquals(get.getRowOffsetPerColumnFamily(), scan.getRowOffsetPerColumnFamily());
    assertEquals(get.getTimeRange().getMin(), scan.getTimeRange().getMin());
    assertEquals(get.getTimeRange().getMax(), scan.getTimeRange().getMax());
    assertTrue(Bytes.equals(get.getAttribute("att_v0"), scan.getAttribute("att_v0")));
    assertEquals(get.getColumnFamilyTimeRange().get(Bytes.toBytes("cf")).getMin(),
      scan.getColumnFamilyTimeRange().get(Bytes.toBytes("cf")).getMin());
    assertEquals(get.getColumnFamilyTimeRange().get(Bytes.toBytes("cf")).getMax(),
      scan.getColumnFamilyTimeRange().get(Bytes.toBytes("cf")).getMax());
  }

  @Test
  public void testScanAttributes() {
    Scan scan = new Scan();
    Assert.assertTrue(scan.getAttributesMap().isEmpty());
    Assert.assertNull(scan.getAttribute("absent"));

    scan.setAttribute("absent", null);
    Assert.assertTrue(scan.getAttributesMap().isEmpty());
    Assert.assertNull(scan.getAttribute("absent"));

    // adding attribute
    scan.setAttribute("attribute1", Bytes.toBytes("value1"));
    Assert.assertTrue(Arrays.equals(Bytes.toBytes("value1"), scan.getAttribute("attribute1")));
    Assert.assertEquals(1, scan.getAttributesMap().size());
    Assert.assertTrue(Arrays.equals(Bytes.toBytes("value1"),
        scan.getAttributesMap().get("attribute1")));

    // overriding attribute value
    scan.setAttribute("attribute1", Bytes.toBytes("value12"));
    Assert.assertTrue(Arrays.equals(Bytes.toBytes("value12"), scan.getAttribute("attribute1")));
    Assert.assertEquals(1, scan.getAttributesMap().size());
    Assert.assertTrue(Arrays.equals(Bytes.toBytes("value12"),
        scan.getAttributesMap().get("attribute1")));

    // adding another attribute
    scan.setAttribute("attribute2", Bytes.toBytes("value2"));
    Assert.assertTrue(Arrays.equals(Bytes.toBytes("value2"), scan.getAttribute("attribute2")));
    Assert.assertEquals(2, scan.getAttributesMap().size());
    Assert.assertTrue(Arrays.equals(Bytes.toBytes("value2"),
        scan.getAttributesMap().get("attribute2")));

    // removing attribute
    scan.setAttribute("attribute2", null);
    Assert.assertNull(scan.getAttribute("attribute2"));
    Assert.assertEquals(1, scan.getAttributesMap().size());
    Assert.assertNull(scan.getAttributesMap().get("attribute2"));

    // removing non-existed attribute
    scan.setAttribute("attribute2", null);
    Assert.assertNull(scan.getAttribute("attribute2"));
    Assert.assertEquals(1, scan.getAttributesMap().size());
    Assert.assertNull(scan.getAttributesMap().get("attribute2"));

    // removing another attribute
    scan.setAttribute("attribute1", null);
    Assert.assertNull(scan.getAttribute("attribute1"));
    Assert.assertTrue(scan.getAttributesMap().isEmpty());
    Assert.assertNull(scan.getAttributesMap().get("attribute1"));
  }

  @Test
  public void testNullQualifier() {
    Scan scan = new Scan();
    byte[] family = Bytes.toBytes("family");
    scan.addColumn(family, null);
    Set<byte[]> qualifiers = scan.getFamilyMap().get(family);
    Assert.assertEquals(1, qualifiers.size());
  }

  @Test
  public void testSetAuthorizations() {
    Scan scan = new Scan();
    try {
      scan.setAuthorizations(new Authorizations("\u002b|\u0029"));
      scan.setAuthorizations(new Authorizations("A", "B", "0123", "A0", "1A1", "_a"));
      scan.setAuthorizations(new Authorizations("A|B"));
      scan.setAuthorizations(new Authorizations("A&B"));
      scan.setAuthorizations(new Authorizations("!B"));
      scan.setAuthorizations(new Authorizations("A", "(A)"));
      scan.setAuthorizations(new Authorizations("A", "{A"));
      scan.setAuthorizations(new Authorizations(" "));
      scan.setAuthorizations(new Authorizations(":B"));
      scan.setAuthorizations(new Authorizations("-B"));
      scan.setAuthorizations(new Authorizations(".B"));
      scan.setAuthorizations(new Authorizations("/B"));
    } catch (IllegalArgumentException e) {
      fail("should not throw exception");
    }
  }

  @Test
  public void testSetStartRowAndSetStopRow() {
    Scan scan = new Scan();
    scan.setStartRow(null);
    scan.setStartRow(new byte[1]);
    scan.setStartRow(new byte[HConstants.MAX_ROW_LENGTH]);
    try {
      scan.setStartRow(new byte[HConstants.MAX_ROW_LENGTH+1]);
      fail("should've thrown exception");
    } catch (IllegalArgumentException iae) {
    } catch (Exception e) {
      fail("expected IllegalArgumentException to be thrown");
    }

    scan.setStopRow(null);
    scan.setStopRow(new byte[1]);
    scan.setStopRow(new byte[HConstants.MAX_ROW_LENGTH]);
    try {
      scan.setStopRow(new byte[HConstants.MAX_ROW_LENGTH+1]);
      fail("should've thrown exception");
    } catch (IllegalArgumentException iae) {
    } catch (Exception e) {
      fail("expected IllegalArgumentException to be thrown");
    }
  }
}

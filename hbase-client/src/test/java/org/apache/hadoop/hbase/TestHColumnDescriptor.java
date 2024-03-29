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
package org.apache.hadoop.hbase;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.apache.hadoop.hbase.exceptions.DeserializationException;
import org.apache.hadoop.hbase.io.compress.Compression;
import org.apache.hadoop.hbase.io.compress.Compression.Algorithm;
import org.apache.hadoop.hbase.io.encoding.DataBlockEncoding;
import org.apache.hadoop.hbase.regionserver.BloomType;
import org.apache.hadoop.hbase.testclassification.SmallTests;
import org.apache.hadoop.hbase.util.BuilderStyleTest;
import org.apache.hadoop.hbase.util.Bytes;
import org.junit.Test;
import org.junit.experimental.categories.Category;

/**
 * Tests the HColumnDescriptor with appropriate arguments
 */
@Category(SmallTests.class)
public class TestHColumnDescriptor {
  @Test
  public void testPb() throws DeserializationException {
    HColumnDescriptor hcd = new HColumnDescriptor(
      HTableDescriptor.META_TABLEDESC.getColumnFamilies()[0]);
    final int v = 123;
    hcd.setBlocksize(v);
    hcd.setTimeToLive(v);
    hcd.setBlockCacheEnabled(!HColumnDescriptor.DEFAULT_BLOCKCACHE);
    hcd.setValue("a", "b");
    hcd.setMaxVersions(v);
    assertEquals(v, hcd.getMaxVersions());
    hcd.setMinVersions(v);
    assertEquals(v, hcd.getMinVersions());
    hcd.setKeepDeletedCells(KeepDeletedCells.TRUE);
    hcd.setInMemory(!HColumnDescriptor.DEFAULT_IN_MEMORY);
    boolean inmemory = hcd.isInMemory();
    // Valid values for replication scope are 0 or 1.
    hcd.setScope(0);
    hcd.setDataBlockEncoding(DataBlockEncoding.FAST_DIFF);
    hcd.setBloomFilterType(BloomType.ROW);
    hcd.setCompressionType(Algorithm.SNAPPY);
    hcd.setDFSReplication((short) v);

    byte [] bytes = hcd.toByteArray();
    HColumnDescriptor deserializedHcd = HColumnDescriptor.parseFrom(bytes);
    assertTrue(hcd.equals(deserializedHcd));
    assertEquals(v, hcd.getBlocksize());
    assertEquals(v, hcd.getTimeToLive());
    assertEquals(hcd.getValue("a"), deserializedHcd.getValue("a"));
    assertEquals(hcd.getMaxVersions(), deserializedHcd.getMaxVersions());
    assertEquals(hcd.getMinVersions(), deserializedHcd.getMinVersions());
    assertEquals(hcd.getKeepDeletedCells(), deserializedHcd.getKeepDeletedCells());
    assertEquals(inmemory, deserializedHcd.isInMemory());
    assertEquals(hcd.getScope(), deserializedHcd.getScope());
    assertTrue(deserializedHcd.getCompressionType().equals(Compression.Algorithm.SNAPPY));
    assertTrue(deserializedHcd.getDataBlockEncoding().equals(DataBlockEncoding.FAST_DIFF));
    assertTrue(deserializedHcd.getBloomFilterType().equals(BloomType.ROW));
    assertEquals(v, deserializedHcd.getDFSReplication());
  }

  @Test
  /** Tests HColumnDescriptor with empty familyName*/
  public void testHColumnDescriptorShouldThrowIAEWhenFamiliyNameEmpty()
      throws Exception {
    try {
      new HColumnDescriptor(Bytes.toBytes(""));
      fail("Did not throw");
    } catch (IllegalArgumentException e) {
      assertEquals("Family name can not be empty", e.getLocalizedMessage());
    }
  }

  /**
   * Test that we add and remove strings from configuration properly.
   */
  @Test
  public void testAddGetRemoveConfiguration() throws Exception {
    HColumnDescriptor desc = new HColumnDescriptor("foo");
    String key = "Some";
    String value = "value";
    desc.setConfiguration(key, value);
    assertEquals(value, desc.getConfigurationValue(key));
    desc.removeConfiguration(key);
    assertEquals(null, desc.getConfigurationValue(key));
  }

  @Test
  public void testClassMethodsAreBuilderStyle() {
    /* HColumnDescriptor should have a builder style setup where setXXX/addXXX methods
     * can be chainable together:
     * . For example:
     * HColumnDescriptor hcd
     *   = new HColumnDescriptor()
     *     .setFoo(foo)
     *     .setBar(bar)
     *     .setBuz(buz)
     *
     * This test ensures that all methods starting with "set" returns the declaring object
     */

    BuilderStyleTest.assertClassesAreBuilderStyle(HColumnDescriptor.class);
  }

  @Test(expected=IllegalArgumentException.class)
  public void testInvalidReplicationScope() {
    HColumnDescriptor column = new HColumnDescriptor("f1");
    column.setScope(5);
  }
}

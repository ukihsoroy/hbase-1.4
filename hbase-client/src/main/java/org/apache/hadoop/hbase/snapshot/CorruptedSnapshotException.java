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
package org.apache.hadoop.hbase.snapshot;

import org.apache.hadoop.hbase.classification.InterfaceAudience;
import org.apache.hadoop.hbase.classification.InterfaceStability;
import org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.SnapshotDescription;


/**
 * Exception thrown when the found snapshot info from the filesystem is not valid.
 */
@SuppressWarnings("serial")
@InterfaceAudience.Public
@InterfaceStability.Evolving
public class CorruptedSnapshotException extends HBaseSnapshotException {
  /**
   * Snapshot was corrupt for some reason.
   *
   * @param message message describing the exception
   * @param e the actual cause of the exception
   */
  public CorruptedSnapshotException(String message, Exception e) {
    super(message, e);
  }

  /**
   * Snapshot was corrupt for some reason.
   *
   * @param message full description of the failure
   * @param snapshotDescription snapshot that was expected
   * @deprecated since 1.3.0, will be removed in 3.0.0
   */
  @Deprecated
  public CorruptedSnapshotException(String message, SnapshotDescription snapshotDescription) {
    super(message, snapshotDescription);
  }

  /**
   * Snapshot was corrupt for some reason.
   *
   * @param message message describing the exception
   */
  public CorruptedSnapshotException(String message) {
    super(message, (SnapshotDescription)null);
  }
}

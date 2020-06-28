/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.hadoop.hbase;

import org.apache.hadoop.hbase.classification.InterfaceAudience;
import org.apache.hadoop.hbase.classification.InterfaceStability;
import org.apache.hadoop.hbase.TableName;

/**
 * Thrown when a table exists but should not.
 */
@InterfaceAudience.Public
@InterfaceStability.Stable
public class TableExistsException extends DoNotRetryIOException {
  private static final long serialVersionUID = (1L << 7) - 1L;

  public TableExistsException() {
    super();
  }

  /**
   * @param tableName the name of the table that should not exist
   */
  public TableExistsException(String tableName) {
    super(tableName);
  }

  /**
   * @param tableName the name of the table that should not exist
   */
  public TableExistsException(TableName tableName) {
    this(tableName.getNameAsString());
  }
}
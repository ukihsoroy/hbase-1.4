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

import org.apache.hadoop.hbase.classification.InterfaceAudience;
import org.apache.hadoop.hbase.classification.InterfaceStability;

/**
 * Failed to find {@code .tableinfo} file under the table directory.
 */
@InterfaceAudience.Public
@InterfaceStability.Evolving
@SuppressWarnings("serial")
public class TableInfoMissingException extends HBaseIOException {
  /**
   * Failed to find {@code .tableinfo} file under the table directory.
   */
  public TableInfoMissingException() {
    super();
  }

  /**
   * @param message the message for this exception
   */
  public TableInfoMissingException(String message) {
    super(message);
  }

  /**
   * @param message the message for this exception
   * @param throwable the {@link Throwable} to use for this exception
   */
  public TableInfoMissingException(String message, Throwable throwable) {
    super(message, throwable);
  }

  /**
   * @param throwable the {@link Throwable} to use for this exception
   */
  public TableInfoMissingException(Throwable throwable) {
    super(throwable);
  }
}

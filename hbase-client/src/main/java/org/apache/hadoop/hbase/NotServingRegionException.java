/**
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
package org.apache.hadoop.hbase;

import java.io.IOException;

import org.apache.hadoop.hbase.classification.InterfaceAudience;
import org.apache.hadoop.hbase.classification.InterfaceStability;
import org.apache.hadoop.hbase.util.Bytes;

/**
 * Thrown by a region server if it is sent a request for a region it is not serving.
 */
@InterfaceAudience.Public
@InterfaceStability.Stable
public class NotServingRegionException extends IOException {
  private static final long serialVersionUID = (1L << 17) - 1L;

  public NotServingRegionException() {
    super();
  }

  /**
   * @param message the message for this exception
   */
  public NotServingRegionException(String message) {
    super(message);
  }

  /**
   * @param message the message for this exception
   */
  public NotServingRegionException(final byte[] message) {
    super(Bytes.toString(message));
  }
}
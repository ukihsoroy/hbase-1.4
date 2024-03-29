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
package org.apache.hadoop.hbase.hbtop.screen.top;

import static org.apache.hadoop.hbase.hbtop.Record.entry;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.argThat;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.apache.hadoop.hbase.hbtop.Record;
import org.apache.hadoop.hbase.hbtop.field.Field;
import org.apache.hadoop.hbase.hbtop.field.FieldInfo;
import org.apache.hadoop.hbase.hbtop.terminal.TerminalSize;
import org.apache.hadoop.hbase.testclassification.SmallTests;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatcher;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@Category(SmallTests.class)
@RunWith(MockitoJUnitRunner.class)
public class TestTopScreenPresenter {

  private static final List<FieldInfo> TEST_FIELD_INFOS = Arrays.asList(
    new FieldInfo(Field.REGION, 10, true),
    new FieldInfo(Field.REQUEST_COUNT_PER_SECOND, 10, true),
    new FieldInfo(Field.LOCALITY, 10, true)
  );

  private static final List<Record> TEST_RECORDS = Arrays.asList(
    Record.ofEntries(
      entry(Field.REGION, "region1"),
      entry(Field.REQUEST_COUNT_PER_SECOND, 1L),
      entry(Field.LOCALITY, 0.3f)),
    Record.ofEntries(
      entry(Field.REGION, "region2"),
      entry(Field.REQUEST_COUNT_PER_SECOND, 2L),
      entry(Field.LOCALITY, 0.2f)),
    Record.ofEntries(
      entry(Field.REGION, "region3"),
      entry(Field.REQUEST_COUNT_PER_SECOND, 3L),
      entry(Field.LOCALITY, 0.1f))
  );

  private static final Summary TEST_SUMMARY = new Summary(
    "00:00:01", "3.0.0-SNAPSHOT", "01234567-89ab-cdef-0123-456789abcdef",
    3, 2, 1, 6, 1, 3.0, 300);

  @Mock
  private TopScreenView topScreenView;

  @Mock
  private TopScreenModel topScreenModel;

  private TopScreenPresenter topScreenPresenter;

  @Before
  public void setup() {
    when(topScreenView.getTerminalSize()).thenReturn(new TerminalSize(100, 100));
    when(topScreenView.getPageSize()).thenReturn(100);

    when(topScreenModel.getFieldInfos()).thenReturn(TEST_FIELD_INFOS);

    List<Field> fields = new ArrayList<>();
    for (FieldInfo fieldInfo : TEST_FIELD_INFOS) {
      fields.add(fieldInfo.getField());
    }
    when(topScreenModel.getFields()).thenReturn(fields);

    when(topScreenModel.getRecords()).thenReturn(TEST_RECORDS);
    when(topScreenModel.getSummary()).thenReturn(TEST_SUMMARY);

    topScreenPresenter = new TopScreenPresenter(topScreenView, 3000, topScreenModel);
  }

  @Test
  public void testRefresh() {
    topScreenPresenter.init();
    topScreenPresenter.refresh(true);

    verify(topScreenView).showTopScreen(
      argThat(new ArgumentMatcher<Summary>() {
        @Override
        public boolean matches(Object argument) {
          return assertSummary((Summary) argument);
        }
      }), argThat(new ArgumentMatcher<List<Header>>() {
        @Override
        @SuppressWarnings("unchecked")
        public boolean matches(Object argument) {
          return assertHeaders((List<Header>) argument);
        }
      }), argThat(new ArgumentMatcher<List<Record>>() {
        @Override
        @SuppressWarnings("unchecked")
        public boolean matches(Object argument) {
          return assertRecords((List<Record>) argument);
        }
      }), argThat(new ArgumentMatcher<Record>() {
        @Override
        public boolean matches(Object argument) {
          return assertSelectedRecord((Record) argument, 0);
        }
      }));
  }

  @Test
  public void testVerticalScrolling() {
    topScreenPresenter.init();
    topScreenPresenter.refresh(true);

    topScreenPresenter.arrowDown();
    topScreenPresenter.arrowDown();
    topScreenPresenter.arrowDown();

    topScreenPresenter.arrowDown();
    topScreenPresenter.arrowDown();
    topScreenPresenter.arrowDown();

    topScreenPresenter.arrowUp();
    topScreenPresenter.arrowUp();
    topScreenPresenter.arrowUp();

    topScreenPresenter.pageDown();
    topScreenPresenter.pageDown();

    topScreenPresenter.pageUp();
    topScreenPresenter.pageUp();

    InOrder inOrder = inOrder(topScreenView);
    verifyVerticalScrolling(inOrder, 0);

    verifyVerticalScrolling(inOrder, 1);
    verifyVerticalScrolling(inOrder, 2);
    verifyVerticalScrolling(inOrder, 2);

    verifyVerticalScrolling(inOrder, 1);
    verifyVerticalScrolling(inOrder, 0);
    verifyVerticalScrolling(inOrder, 0);

    verifyVerticalScrolling(inOrder, 2);
    verifyVerticalScrolling(inOrder, 2);

    verifyVerticalScrolling(inOrder, 0);
    verifyVerticalScrolling(inOrder, 0);
  }

  @SuppressWarnings("unchecked")
  private void verifyVerticalScrolling(InOrder inOrder, final int expectedSelectedRecodeIndex) {
    inOrder.verify(topScreenView).showTopScreen(any(Summary.class), any(List.class),
      any(List.class), argThat(new ArgumentMatcher<Record>() {
        @Override
        public boolean matches(Object argument) {
          return assertSelectedRecord((Record) argument, expectedSelectedRecodeIndex);
        }
      })
    );
  }

  @Test
  public void testHorizontalScrolling() {
    topScreenPresenter.init();
    topScreenPresenter.refresh(true);

    topScreenPresenter.arrowRight();
    topScreenPresenter.arrowRight();
    topScreenPresenter.arrowRight();

    topScreenPresenter.arrowLeft();
    topScreenPresenter.arrowLeft();
    topScreenPresenter.arrowLeft();

    topScreenPresenter.end();
    topScreenPresenter.end();

    topScreenPresenter.home();
    topScreenPresenter.home();

    InOrder inOrder = inOrder(topScreenView);
    verifyHorizontalScrolling(inOrder, 3);

    verifyHorizontalScrolling(inOrder, 2);
    verifyHorizontalScrolling(inOrder, 1);
    verifyHorizontalScrolling(inOrder, 1);

    verifyHorizontalScrolling(inOrder, 2);
    verifyHorizontalScrolling(inOrder, 3);
    verifyHorizontalScrolling(inOrder, 3);

    verifyHorizontalScrolling(inOrder, 1);
    verifyHorizontalScrolling(inOrder, 1);

    verifyHorizontalScrolling(inOrder, 3);
    verifyHorizontalScrolling(inOrder, 3);
  }

  @SuppressWarnings("unchecked")
  private void verifyHorizontalScrolling(InOrder inOrder, final int expectedHeaderCount) {
    inOrder.verify(topScreenView).showTopScreen(any(Summary.class),
      argThat(new ArgumentMatcher<List<Header>>() {
        @Override
        public boolean matches(Object argument) {
          List<Header> headers = (List<Header>) argument;
          return headers.size() == expectedHeaderCount;
        }
      }), any(List.class), any(Record.class));
  }

  private boolean assertSummary(Summary actual) {
    return actual.getCurrentTime().equals(TEST_SUMMARY.getCurrentTime())
      && actual.getVersion().equals(TEST_SUMMARY.getVersion())
      && actual.getClusterId().equals(TEST_SUMMARY.getClusterId())
      && actual.getServers() == TEST_SUMMARY.getServers()
      && actual.getLiveServers() == TEST_SUMMARY.getLiveServers()
      && actual.getDeadServers() == TEST_SUMMARY.getDeadServers()
      && actual.getRegionCount() == TEST_SUMMARY.getRegionCount()
      && actual.getRitCount() == TEST_SUMMARY.getRitCount()
      && actual.getAverageLoad() == TEST_SUMMARY.getAverageLoad()
      && actual.getAggregateRequestPerSecond() == TEST_SUMMARY.getAggregateRequestPerSecond();
  }

  private boolean assertHeaders(List<Header> actual) {
    List<Header> expected = new ArrayList<>();
    for (FieldInfo fieldInfo : TEST_FIELD_INFOS) {
      expected.add(new Header(fieldInfo.getField(), fieldInfo.getDefaultLength()));
    }

    if (actual.size() != expected.size()) {
      return false;
    }

    for (int i = 0; i < actual.size(); i++) {
      if (actual.get(i).getField() != expected.get(i).getField()) {
        return false;
      }
      if (actual.get(i).getLength() != expected.get(i).getLength()) {
        return false;
      }
    }

    return true;
  }

  private boolean assertRecords(List<Record> actual) {
    if (actual.size() != TEST_RECORDS.size()) {
      return false;
    }

    for (int i = 0; i < actual.size(); i++) {
      if (!assertRecord(actual.get(i), TEST_RECORDS.get(i))) {
        return false;
      }
    }

    return true;
  }

  private boolean assertSelectedRecord(Record actual, int expectedSelectedRecodeIndex) {
    return assertRecord(actual, TEST_RECORDS.get(expectedSelectedRecodeIndex));
  }

  private boolean assertRecord(Record actual, Record expected) {
    return actual.get(Field.REGION).equals(expected.get(Field.REGION)) && actual
      .get(Field.REQUEST_COUNT_PER_SECOND).equals(expected.get(Field.REQUEST_COUNT_PER_SECOND))
      && actual.get(Field.LOCALITY).equals(expected.get(Field.LOCALITY));
  }
}

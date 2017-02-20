/*
 * SonarQube
 * Copyright (C) 2009-2017 SonarSource SA
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.sonar.server.component.ws;

import java.util.Collections;
import java.util.List;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.sonar.server.component.ws.FilterParser.Criterion;
import org.sonar.server.measure.index.ProjectMeasuresQuery;
import org.sonar.server.measure.index.ProjectMeasuresQuery.MetricCriterion;
import org.sonar.server.tester.UserSessionRule;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptySet;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.sonar.server.component.ws.FilterParser.Operator;
import static org.sonar.server.component.ws.FilterParser.Operator.EQ;
import static org.sonar.server.component.ws.FilterParser.Operator.GT;
import static org.sonar.server.component.ws.FilterParser.Operator.IN;
import static org.sonar.server.component.ws.FilterParser.Operator.LTE;
import static org.sonar.server.component.ws.ProjectMeasuresQueryFactory.newProjectMeasuresQuery;
import static org.sonar.server.computation.task.projectanalysis.measure.Measure.Level.OK;

public class ProjectMeasuresQueryFactoryTest {

  @Rule
  public ExpectedException expectedException = ExpectedException.none();
  @Rule
  public UserSessionRule userSession = UserSessionRule.standalone();

  @Test
  public void create_query() throws Exception {
    List<Criterion> criteria = asList(
      Criterion.builder().setKey("ncloc").setOperator(GT).setValue("10").build(),
      Criterion.builder().setKey("coverage").setOperator(LTE).setValue("80").build());

    ProjectMeasuresQuery underTest = newProjectMeasuresQuery(criteria, emptySet());

    assertThat(underTest.getMetricCriteria())
      .extracting(MetricCriterion::getMetricKey, MetricCriterion::getOperator, MetricCriterion::getValue)
      .containsOnly(
        tuple("ncloc", GT, 10d),
        tuple("coverage", Operator.LTE, 80d));
  }

  @Test
  public void fail_when_no_value() throws Exception {
    expectedException.expect(IllegalArgumentException.class);
    expectedException.expectMessage("Value cannot be null for 'ncloc'");

    newProjectMeasuresQuery(singletonList(Criterion.builder().setKey("ncloc").setOperator(GT).setValue(null).build()),
      emptySet());
  }

  @Test
  public void fail_when_not_double() throws Exception {
    expectedException.expect(IllegalArgumentException.class);
    expectedException.expectMessage("Value 'ten' is not a number");

    newProjectMeasuresQuery(singletonList(Criterion.builder().setKey("ncloc").setOperator(GT).setValue("ten").build()),
      emptySet());
  }

  @Test
  public void fail_when_no_operator() throws Exception {
    expectedException.expect(IllegalArgumentException.class);
    expectedException.expectMessage("Operator cannot be null for 'ncloc'");

    newProjectMeasuresQuery(singletonList(Criterion.builder().setKey("ncloc").setOperator(null).setValue("ten").build()),
      emptySet());
  }

  @Test
  public void create_query_on_quality_gate() throws Exception {
    ProjectMeasuresQuery query = newProjectMeasuresQuery(singletonList(Criterion.builder().setKey("alert_status").setOperator(EQ).setValue("OK").build()),
      emptySet());

    assertThat(query.getQualityGateStatus().get().name()).isEqualTo(OK.name());
  }

  @Test
  public void fail_to_create_query_on_quality_gate_when_operator_is_not_equal() throws Exception {
    expectedException.expect(IllegalArgumentException.class);
    expectedException.expectMessage("Only equals operator is available for quality gate criteria");

    newProjectMeasuresQuery(singletonList(Criterion.builder().setKey("alert_status").setOperator(GT).setValue("OK").build()), emptySet());
  }

  @Test
  public void fail_to_create_query_on_quality_gate_when_value_is_incorrect() throws Exception {
    expectedException.expect(IllegalArgumentException.class);
    expectedException.expectMessage("Unknown quality gate status : 'unknown'");

    newProjectMeasuresQuery(singletonList(Criterion.builder().setKey("alert_status").setOperator(EQ).setValue("unknown").build()), emptySet());
  }

  @Test
  public void create_query_on_language_using_in_operator() throws Exception {
    ProjectMeasuresQuery query = newProjectMeasuresQuery(
      singletonList(Criterion.builder().setKey("language").setOperator(IN).setValues(asList("java", "js")).build()),
      emptySet());

    assertThat(query.getLanguages().get()).containsOnly("java", "js");
  }

  @Test
  public void create_query_on_language_using_equals_operator() throws Exception {
    ProjectMeasuresQuery query = newProjectMeasuresQuery(
      singletonList(Criterion.builder().setKey("language").setOperator(EQ).setValue("java").build()),
      emptySet());

    assertThat(query.getLanguages().get()).containsOnly("java");
  }

  @Test
  public void fail_to_create_query_on_language_using_in_operator_and_value() throws Exception {
    expectedException.expect(IllegalArgumentException.class);
    expectedException.expectMessage("Language should be set either by using 'language = java' or 'language IN (java, js)");

    newProjectMeasuresQuery(singletonList(Criterion.builder().setKey("language").setOperator(IN).setValue("java").build()),
      emptySet());
  }

  @Test
  public void fail_to_create_query_on_language_using_eq_operator_and_values() throws Exception {
    expectedException.expect(IllegalArgumentException.class);
    expectedException.expectMessage("Language should be set either by using 'language = java' or 'language IN (java, js)");

    newProjectMeasuresQuery(singletonList(Criterion.builder().setKey("language").setOperator(EQ).setValues(asList("java")).build()),
      emptySet());
  }

  @Test
  public void do_not_filter_on_projectUuids_if_criteria_non_empty_and_projectUuid_is_null() {
    ProjectMeasuresQuery query = newProjectMeasuresQuery(singletonList(Criterion.builder().setKey("ncloc").setOperator(EQ).setValue("10").build()),
      null);

    assertThat(query.getProjectUuids()).isEmpty();
  }

  @Test
  public void filter_on_projectUuids_if_projectUuid_is_empty_and_criteria_non_empty() throws Exception {
    ProjectMeasuresQuery query = newProjectMeasuresQuery(singletonList(Criterion.builder().setKey("ncloc").setOperator(GT).setValue("10").build()),
      emptySet());

    assertThat(query.getProjectUuids()).isPresent();
  }

  @Test
  public void filter_on_projectUuids_if_projectUuid_is_non_empty_and_criteria_non_empty() throws Exception {
    ProjectMeasuresQuery query = newProjectMeasuresQuery(singletonList(Criterion.builder().setKey("ncloc").setOperator(GT).setValue("10").build()),
      Collections.singleton("foo"));

    assertThat(query.getProjectUuids()).isPresent();
  }

  @Test
  public void filter_on_projectUuids_if_projectUuid_is_empty_and_criteria_is_empty() throws Exception {
    ProjectMeasuresQuery query = newProjectMeasuresQuery(emptyList(), emptySet());

    assertThat(query.getProjectUuids()).isPresent();
  }

  @Test
  public void filter_on_projectUuids_if_projectUuid_is_non_empty_and_criteria_empty() throws Exception {
    ProjectMeasuresQuery query = newProjectMeasuresQuery(emptyList(), Collections.singleton("foo"));

    assertThat(query.getProjectUuids()).isPresent();
  }

  @Test
  public void convert_metric_to_lower_case() throws Exception {
    ProjectMeasuresQuery query = newProjectMeasuresQuery(asList(
      Criterion.builder().setKey("NCLOC").setOperator(GT).setValue("10").build(),
      Criterion.builder().setKey("coVERage").setOperator(LTE).setValue("80").build()),
      emptySet());

    assertThat(query.getMetricCriteria())
      .extracting(MetricCriterion::getMetricKey, MetricCriterion::getOperator, MetricCriterion::getValue)
      .containsOnly(
        tuple("ncloc", GT, 10d),
        tuple("coverage", Operator.LTE, 80d));
  }

  @Test
  public void accept_empty_query() throws Exception {
    ProjectMeasuresQuery result = newProjectMeasuresQuery(emptyList(), emptySet());

    assertThat(result.getMetricCriteria()).isEmpty();
  }

}

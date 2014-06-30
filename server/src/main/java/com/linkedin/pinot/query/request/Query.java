package com.linkedin.pinot.query.request;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.joda.time.Interval;
import org.json.JSONArray;
import org.json.JSONObject;

import com.linkedin.pinot.index.query.FilterQuery;
import com.linkedin.pinot.query.aggregation.AggregationFunction;
import com.linkedin.pinot.query.aggregation.AggregationFunctionFactory;
import com.linkedin.pinot.query.utils.TimeUtils;


public class Query implements Serializable {
  private QueryType _queryType;
  private String _sourceName;
  private String _resourceName;
  private String _tableName;
  private Interval _timeInterval;
  private Duration _timeGranularity;
  private FilterQuery _filterQuery;
  private JSONArray _aggregationJsonArray;
  private GroupBy _groupBy;
  private Selection _selections;

  public QueryType getQueryType() {
    return _queryType;
  }

  public void setQueryType(QueryType queryType) {
    _queryType = queryType;
  }

  public Duration getTimeGranularity() {
    return _timeGranularity;
  }

  public void setTimeGranularity(Duration timeGranularity) {
    _timeGranularity = timeGranularity;
  }

  public String getSourceName() {
    return _sourceName;
  }

  public void setSourceName(String sourceName) {
    this._sourceName = sourceName;
    int indexOfDot = sourceName.indexOf(".");
    if (indexOfDot > 0) {
      _resourceName = sourceName.substring(0, indexOfDot);
      _tableName = sourceName.substring(indexOfDot + 1, sourceName.length());
    } else {
      _resourceName = sourceName;
      _tableName = null;
    }
  }

  public String getResourceName() {
    return _resourceName;
  }

  public void setResourceName(String resourceName) {
    this._resourceName = resourceName;
  }

  public String getTableName() {
    return _tableName;
  }

  public void setTableName(String tableName) {
    this._tableName = tableName;
  }

  public FilterQuery getFilterQuery() {
    return _filterQuery;
  }

  public void setFilterQuery(FilterQuery filterQuery) {
    this._filterQuery = filterQuery;
  }

  public JSONArray getAggregationJSONArray() {
    return _aggregationJsonArray;
  }

  public void setAggregations(JSONArray aggregationJsonArray) {
    this._aggregationJsonArray = aggregationJsonArray;
  }

  public List<AggregationFunction> getAggregationFunction() {
    List<AggregationFunction> aggregationFunctions = new ArrayList<AggregationFunction>();
    for (int i = 0; i < _aggregationJsonArray.length(); ++i) {
      aggregationFunctions.add(AggregationFunctionFactory.get(_aggregationJsonArray.getJSONObject(i)));
    }
    return aggregationFunctions;
  }

  public GroupBy getGroupBy() {
    return _groupBy;
  }

  public void setGroupBy(GroupBy groupBy) {
    this._groupBy = groupBy;
  }

  public Interval getTimeInterval() {
    return _timeInterval;
  }

  public void setTimeInterval(Interval timeInterval) {
    this._timeInterval = timeInterval;
  }

  public Selection getSelections() {
    return _selections;
  }

  public void setSelections(Selection selections) {
    _selections = selections;
  }

  public static Query fromJson(JSONObject jsonQuery) {
    Query query = new Query();
    query.setQueryType(QueryType.valueOf(jsonQuery.getString("queryType")));
    query.setAggregations(jsonQuery.getJSONArray("aggregations"));
    query.setSourceName(jsonQuery.getString("source"));
    query.setFilterQuery(FilterQuery.fromJson(jsonQuery.getJSONObject("filters")));
    query.setGroupBy(GroupBy.fromJson(jsonQuery.getJSONObject("groupBy")));
    query.setSelections(Selection.fromJson(jsonQuery.getJSONObject("selections")));
    query.setTimeInterval(getIntervalFromJson(jsonQuery.getJSONObject("timeInterval")));
    query.setTimeGranularity(getTimeGranularityFromJson(jsonQuery.getString("timeGranularity")));
    return query;
  }

  public static Interval getIntervalFromJson(JSONObject jsonObject) {
    try {
      DateTime start = new DateTime(jsonObject.getString("startTime"));
      DateTime end = new DateTime(jsonObject.getString("endTime"));
      return new Interval(start, end);
    } catch (Exception e) {
      return new Interval(0, System.currentTimeMillis());
    }
  }

  public static Duration getTimeGranularityFromJson(String timeGranularity) {
    long timeInMilisecond = TimeUtils.toMillis(timeGranularity);
    return new Duration(timeInMilisecond);
  }

}

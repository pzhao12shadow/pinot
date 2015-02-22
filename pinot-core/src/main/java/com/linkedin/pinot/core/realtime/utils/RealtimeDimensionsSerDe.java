package com.linkedin.pinot.core.realtime.utils;

import java.nio.IntBuffer;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.linkedin.pinot.common.data.Schema;
import com.linkedin.pinot.core.data.GenericRow;
import com.linkedin.pinot.core.realtime.impl.dictionary.MutableDictionaryReader;


public class RealtimeDimensionsSerDe {

  private final List<String> dimensionsList;
  private final Schema dataSchema;
  private final Map<String, MutableDictionaryReader> dictionaryMap;

  public RealtimeDimensionsSerDe(List<String> dimensionName, Schema schema,
      Map<String, MutableDictionaryReader> dictionary) {
    this.dimensionsList = dimensionName;
    this.dataSchema = schema;
    this.dictionaryMap = dictionary;
  }

  public IntBuffer serializeToIntBuffer(GenericRow row) {
    List<Integer> rowConvertedToDictionaryId = new LinkedList<Integer>();
    List<Integer> columnOffsets = new LinkedList<Integer>();
    int pointer = 0;

    for (int i = 0; i < dataSchema.getDimensionNames().size(); i++) {
      columnOffsets.add(pointer);

      if (dataSchema.getFieldSpecFor(dataSchema.getDimensionNames().get(i)).isSingleValueField()) {
        rowConvertedToDictionaryId.add(dictionaryMap.get(dataSchema.getDimensionNames().get(i)).indexOf(
            row.getValue(dataSchema.getDimensionNames().get(i))));
        pointer += 1;
      } else {
        Object[] multivalues = (Object[]) row.getValue(dataSchema.getDimensionNames().get(i));
        Arrays.sort(multivalues);
        for (Object multivalue : multivalues) {
          rowConvertedToDictionaryId.add(dictionaryMap.get(dataSchema.getDimensionNames().get(i)).indexOf(multivalue));
        }

        pointer += multivalues.length;
      }
      if (i == dataSchema.getDimensionNames().size() - 1) {
        columnOffsets.add(pointer);
      }
    }

    IntBuffer buff = IntBuffer.allocate(columnOffsets.size() + rowConvertedToDictionaryId.size());
    for (Integer offset : columnOffsets) {
      buff.put(offset + columnOffsets.size());
    }

    for (Integer dicId : rowConvertedToDictionaryId) {
      buff.put(dicId);
    }

    return buff;
  }

  public int[] deSerializeAndReturnDicIdsFor(String column, IntBuffer buffer) {
    int ret[] = null;
    int dimIndex = dataSchema.getDimensionNames().indexOf(column);
    int start = buffer.get(dimIndex);
    int end = buffer.get((dimIndex + 1));

    ret = new int[end - start];

    int counter = 0;
    for (int i = start; i < end; i++) {
      ret[counter] = buffer.get(i);
      counter++;
    }
    return ret;
  }

  public GenericRow deSerialize(IntBuffer buffer) {
    GenericRow row = new GenericRow();
    Map<String, Object> rowValues = new HashMap<String, Object>();

    for (String dimension : dataSchema.getDimensionNames()) {
      int[] ret = deSerializeAndReturnDicIdsFor(dimension, buffer);

      if (dataSchema.getFieldSpecFor(dimension).isSingleValueField()) {
        rowValues.put(dimension, dictionaryMap.get(dimension).get(ret[0]));
      } else {
        Object[] mV = new Object[ret.length];
        for (int i = 0; i < ret.length; i++) {
          mV[i] = dictionaryMap.get(dimension).get(ret[i]);
        }
        rowValues.put(dimension, mV);
      }
    }

    row.init(rowValues);

    return row;
  }
}

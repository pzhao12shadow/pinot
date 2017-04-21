package com.linkedin.thirdeye.api;

import org.testng.Assert;
import org.testng.annotations.Test;

public class DimensionMapTest {

  @Test
  public void testCreate() {
    DimensionMap dimensionOrg = new DimensionMap();
    dimensionOrg.put("K1", "V1");
    dimensionOrg.put("K2", "V2");

    String jsonString = dimensionOrg.toString();

    DimensionMap dimensionClone1 = new DimensionMap(jsonString);
    Assert.assertEquals(dimensionClone1, dimensionOrg);

    DimensionMap dimensionClone2 = new DimensionMap("{K1=V1,K2=V2}");
    Assert.assertEquals(dimensionClone2, dimensionOrg);
  }

  @Test
  public void testComparison() {
    DimensionMap d1;
    DimensionMap d2;

    // Case 1: a={}, b={"K"="V"} --> a="", b="KV" --> a < b
    d1 = new DimensionMap();
    d2 = new DimensionMap();
    d2.put("K", "V");
    Assert.assertEquals(d1.compareTo(d2) < 0, "".compareTo("KV") < 0);
    Assert.assertEquals(d2.compareTo(d1) > 0, "KV".compareTo("") > 0);

    // Case 2: a={"K"="V"}, b={"K"="V"} --> a="KV", b="KV" --> a = b
    d1 = new DimensionMap();
    d2 = new DimensionMap();
    d1.put("K", "V");
    d2.put("K", "V");
    Assert.assertEquals(d1.compareTo(d2) == 0, "KV".compareTo("KV") == 0);
    Assert.assertEquals(d2.compareTo(d1) == 0, "KV".compareTo("KV") == 0);

    // Case 3: a={"K2"="V1"}, b={"K1"="V1","K2"="V2"} --> a="K2V1", b="K1V1K2V2" --> a > b
    d1 = new DimensionMap();
    d2 = new DimensionMap();
    d1.put("K2", "V1");
    d2.put("K1", "V1");
    d2.put("K2", "V2");
    Assert.assertEquals(d1.compareTo(d2) > 0, "K2V1".compareTo("K1V1K2V2") > 0);
    Assert.assertEquals(d2.compareTo(d1) < 0, "K1V1K2V2".compareTo("K2V1") < 0);

    // Case 4: a={"K1"="V1"}, b={"K1"="V1","K2"="V2"} --> a="K1V1", b="K1V1K2V2" --> a < b
    d1 = new DimensionMap();
    d2 = new DimensionMap();
    d1.put("K1", "V1");
    d2.put("K1", "V1");
    d2.put("K2", "V2");
    Assert.assertEquals(d1.compareTo(d2) < 0, "K1V1".compareTo("K1V1K2V2") < 0);
    Assert.assertEquals(d2.compareTo(d1) > 0, "K1V1K2V2".compareTo("K1V1") > 0);
  }

  @Test
  public void testEqualsOrIsChild() {
    DimensionMap d1;
    DimensionMap d2;

    // Case 1: a={}, b={"K"="V"} --> a is parent of b
    d1 = new DimensionMap();
    d2 = new DimensionMap();
    d2.put("K", "V");
    Assert.assertFalse(d1.equalsOrChildOf(d2));
    Assert.assertTrue(d2.equalsOrChildOf(d1));

    // Case 2: a={"K"="V"}, b={"K"="V"} --> a and b are sibling or self
    d1 = new DimensionMap();
    d2 = new DimensionMap();
    d1.put("K", "V1");
    d2.put("K", "V2");
    Assert.assertFalse(d1.equalsOrChildOf(d2));
    Assert.assertFalse(d2.equalsOrChildOf(d1));

    // Case 3: a={"K2"="V1"}, b={"K1"="V1","K2"="V2"} --> a and b does not have any relation
    d1 = new DimensionMap();
    d2 = new DimensionMap();
    d1.put("K2", "V1");
    d2.put("K1", "V1");
    d2.put("K2", "V2");
    Assert.assertFalse(d1.equalsOrChildOf(d2));
    Assert.assertFalse(d2.equalsOrChildOf(d1));

    // Case 4: a=null, b={"K"="V"} --> a is universal parent
    d2 = new DimensionMap();
    Assert.assertTrue(d2.equalsOrChildOf(null));
  }
}
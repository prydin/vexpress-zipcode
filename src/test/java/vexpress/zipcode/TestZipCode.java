package vexpress.zipcode;

import org.junit.jupiter.api.Test;
import org.springframework.util.Assert;

public class TestZipCode {
  @Test
  public void testLatLongDistance() {
    final double d = ZipCodeController.distance(0, 0, 1, 0);
    Assert.isTrue(d > 52.1385 && d < 52.1386, String.format("Distance is incorrect: %f", d));
  }
}

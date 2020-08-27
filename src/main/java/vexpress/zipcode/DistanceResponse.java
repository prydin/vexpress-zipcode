package vexpress.zipcode;

public class DistanceResponse {
  private final double distance;

  private final String unit;

  public DistanceResponse(final double distance, final String unit) {
    this.distance = distance;
    this.unit = unit;
  }

  public double getDistance() {
    return distance;
  }

  public String getUnit() {
    return unit;
  }
}

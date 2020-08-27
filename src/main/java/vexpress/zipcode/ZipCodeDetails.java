package vexpress.zipcode;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "zipcodes")
public class ZipCodeDetails {
  public ZipCodeDetails() {}

  @Column(name = "locality")
  private String locality;

  @Id
  @Column(name = "zip")
  private int zip;

  @Column(name = "state")
  private String state;

  @Column(name = "lat")
  private double lat;

  @Column(name = "long")
  private double lon;

  public int getZip() {
    return zip;
  }

  public void setZip(final int zip) {
    this.zip = zip;
  }

  public String getState() {
    return state;
  }

  public void setState(final String state) {
    this.state = state;
  }

  public ZipCodeDetails(final String locality, final double lat, final double lon) {
    this.locality = locality;
    this.lat = lat;
    this.lon = lon;
  }

  public String getLocality() {
    return locality;
  }

  public double getLat() {
    return lat;
  }

  public double getLon() {
    return lon;
  }
}

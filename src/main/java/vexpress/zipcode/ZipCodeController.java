package vexpress.zipcode;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ZipCodeController {
  @Autowired ZipCodeRepository zipCodeRepository;

  @GetMapping(
      path = "/zipcode/{zipCode}",
      produces = {MediaType.APPLICATION_JSON_VALUE})
  public ZipCodeDetails lookupZipcode(@PathVariable("zipCode") final int zipCode)
      throws ZipNotFoundException {
    final ZipCodeDetails z = innerLookupZip(zipCode);
    if (z == null) {
      throw new ZipNotFoundException(Integer.toString(zipCode));
    }
    return z;
  }

  @GetMapping(
      path = "/distance",
      produces = {MediaType.APPLICATION_JSON_VALUE})
  public DistanceResponse zipcodeDistance(
      @RequestParam("fromZip") final int startZip, @RequestParam("toZip") final int endZip)
      throws ZipNotFoundException {
    final ZipCodeDetails start = lookupZipcode(startZip);
    final ZipCodeDetails end = lookupZipcode(endZip);
    return new DistanceResponse(
        distance(start.getLat(), start.getLon(), end.getLat(), end.getLon()), "miles");
  }

  private ZipCodeDetails innerLookupZip(final int zipCode) {
    final List<ZipCodeDetails> z = zipCodeRepository.findByZip(zipCode);
    return z.size() == 1 ? z.get(0) : null;
  }

  static double distance(
      @RequestParam final double lat1,
      @RequestParam final double lon1,
      @RequestParam final double lat2,
      @RequestParam final double lon2) {
    final double x1 = Math.toRadians(lat1);
    final double y1 = Math.toRadians(lon1);
    final double x2 = Math.toRadians(lat2);
    final double y2 = Math.toRadians(lon2);
    double theta =
        Math.acos(Math.sin(x1) * Math.sin(x2) + Math.cos(x1) * Math.cos(x2) * Math.cos(y1 - y2));
    theta = Math.toDegrees(theta);
    return (60 * theta) / 1.15078;
  }
}

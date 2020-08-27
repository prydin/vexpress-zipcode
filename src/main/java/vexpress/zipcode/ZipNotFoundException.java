package vexpress.zipcode;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class ZipNotFoundException extends Exception {
  public ZipNotFoundException(final String message) {
    super(message);
  }
}

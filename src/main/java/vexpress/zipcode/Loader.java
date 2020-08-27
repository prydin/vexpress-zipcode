package vexpress.zipcode;

import java.io.BufferedReader;
import java.io.EOFException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.StringTokenizer;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

public class Loader {
  private final Connection conn;

  public Loader(final String url, final String user, final String password)
      throws ClassNotFoundException, SQLException {
    Class.forName("org.postgresql.Driver");
    conn = DriverManager.getConnection(url, user, password);
  }

  public void load(final String filename) throws IOException, SQLException {
    final PreparedStatement stmt =
        conn.prepareStatement(
            "INSERT INTO zipcodes(zip, locality, state, lat, long) VALUES(?, ?, ?, ?, ?) ON CONFLICT(zip) DO NOTHING");
    try (final BufferedReader r = new BufferedReader(new FileReader(filename))) {
      String line;
      if (r.readLine() == null) { // Skip first line
        throw new EOFException(filename);
      }
      while ((line = r.readLine()) != null) {
        final StringTokenizer st = new StringTokenizer(line, ",");
        final int zipCode = Integer.parseInt(unquote(st.nextToken()));
        final double lat = Double.parseDouble(unquote(st.nextToken()));
        final double lon = Double.parseDouble(unquote(st.nextToken()));
        final String name = unquote(st.nextToken());
        final String state = unquote(st.nextToken());
        stmt.setInt(1, zipCode);
        stmt.setString(2, name);
        stmt.setString(3, state);
        stmt.setDouble(4, lat);
        stmt.setDouble(5, lon);
        stmt.executeUpdate();
      }
    }
  }

  private static String unquote(String s) {
    if (s.startsWith("\"")) {
      s = s.substring(1);
    }
    if (s.endsWith("\"")) {
      s = s.substring(0, s.length() - 1);
    }
    return s;
  }

  public static void main(final String[] args) throws Exception {
    final Options options = new Options();

    final Option url = new Option("U", "url", true, "database URL");
    final Option user = new Option("u", "user", true, "database user");
    final Option password = new Option("p", "password", true, "database password");
    final Option file = new Option("f", "file", true, "input filename");

    url.setRequired(true);
    user.setRequired(false);
    password.setRequired(false);
    file.setRequired(true);

    options.addOption(url);
    options.addOption(user);
    options.addOption(password);
    options.addOption(file);

    final CommandLineParser parser = new DefaultParser();
    final HelpFormatter formatter = new HelpFormatter();
    CommandLine cmd = null;

    try {
      cmd = parser.parse(options, args);
    } catch (final ParseException e) {
      System.out.println(e.getMessage());
      formatter.printHelp("Loader", options);
      System.exit(1);
    }
    final Loader l =
        new Loader(
            cmd.getOptionValue("url"), cmd.getOptionValue("user"), cmd.getOptionValue("password"));
    l.load(cmd.getOptionValue("file"));
  }
}

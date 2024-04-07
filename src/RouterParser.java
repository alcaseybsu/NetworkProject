import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class RouterParser {

  public static List<String> NeighborFinder(
    String startHeader,
    String endHeader,
    String searchString
  ) {
    String filePath = "config.txt";
    boolean withinHeaders = false;

    try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
      String line;
      List<String> neighborList = new ArrayList<>();
      while ((line = br.readLine()) != null) { // While the line is not null
        if (line.equals(startHeader)) { // If line being read starts the defined search area
          withinHeaders = true;
        } else if (line.equals(endHeader)) { // If line being read ends the defined search area
          withinHeaders = false;
        } else if (withinHeaders && !line.isEmpty()) { // If the line is within the search area and the line is not empty
          // Split the line by :
          String[] parts = line.split(":");
          // Check if there are two parts
          if (parts.length == 2) {
            // Check if either element matches the searchString
            if (parts[0].trim().equals(searchString.trim())) {
              // Return the second machine listed on that line
              neighborList.add(parts[1].trim());
            }
            if (parts[1].trim().equals(searchString.trim())) {
              // Return the first machine listed on that line
              neighborList.add(parts[0].trim());
            }
          }
        }
      }
      return neighborList;
    } catch (IOException e) {
      e.printStackTrace();
    }

    // Return null if no match is found
    return null;
  }

  //------------------------------------------------------------------------------------------------------------

  public static String NeighborInfo(
    String startHeader,
    String endHeader,
    String searchString
  ) {
    String filePath = "config.txt";
    boolean withinHeaders = false;

    try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
      String line;

      while ((line = br.readLine()) != null) {
        if (line.equals(startHeader)) {
          withinHeaders = true;
        } else if (line.equals(endHeader)) {
          withinHeaders = false;
        } else if (withinHeaders && !line.isEmpty()) {
          // Split the line by ,
          String[] parts = line.split(",");

          // Check if there are three parts
          if (parts.length == 3) {
            // Check if the first part of the line matches the searchString / Look up on file by name
            if (
              searchString != null &&
              parts[0].trim().equals(searchString.trim())
            ) {
              // Return IP and Port numbers of machine given in searchString
              return parts[1].trim() + "," + parts[2].trim();
            }
          } else {
            // Exceptions
            //System.out.println("Invalid line: " + line);
          }
        }
      }
    } catch (IOException e) {
      e.printStackTrace();
    }

    // Returning null for no matches
    return null;
  }
}

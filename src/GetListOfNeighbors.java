import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class GetListOfNeighbors {
    public static List<String> GetSpecifiedList(String device, String name) {
        // Will return a list of either directly connected subnets or router neighbors
        // Given the name of the router in question and what neighbor list is being requested

        String filePath = "config.txt";
        String startOfList = "";
        String endOfList = "";

        // Determine the section of the config file to read based on the device type
        if (device.equalsIgnoreCase("Router")) {
            startOfList = "#Routers to Routers";
            endOfList = "#End Router to Router List";
        } else if (device.equalsIgnoreCase("Subnet")) {
            startOfList = "#Routers to subnets";
            endOfList = "#End Routers to Subnets List";
        } else {
            System.out.println("Invalid input, a device can only have connections to Routers or Subnets");
            return new ArrayList<>(); // Return an empty list to avoid further processing
        }

        List<String> neighbors = new ArrayList<>();
        boolean withinHeaders = false;

        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.equals(startOfList)) {
                    withinHeaders = true;
                } else if (line.equals(endOfList)) {
                    withinHeaders = false;
                } else if (withinHeaders && !line.isEmpty()) {
                    // Process lines within the relevant section
                    String[] parts = line.split(":");
                    if (parts.length == 2 && (parts[0].trim().equalsIgnoreCase(name) || parts[1].trim().equalsIgnoreCase(name))) {
                        // If the line describes a neighbor of the specified router or subnet, add it to the list
                        neighbors.add(parts[0].trim().equalsIgnoreCase(name) ? parts[1].trim() : parts[0].trim());
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return neighbors.isEmpty() ? List.of("Device with given name has no neighbors!") : neighbors;
    }
}

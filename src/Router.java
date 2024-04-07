import java.io.*;
import java.net.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/*  Update all routers & print table as updates are received
    ---------------------------------------------------------------------------------------
    Add distance vector received to the routers' initial distance vector.
    If matching entries, compare distance vectors of their entries,
    use the next hop of the shorter distance vector.
  
    Subnet, Cost, nextHop
    - N1,0,R1
    - N2,0,R1
*/

public class Router {

  public String name;
  private InetAddress Address;
  private final int Port;
  private List<DVTableEntry> distanceVectorTable;
  private List<String> RouterNeighborData;

  //----------------------------------Router Constructor----------------------------------
  public Router(String name) {
    this.Port = 0;
    this.name = name;
  }

  public Router(String name, String IpAddress, int Port)
    throws UnknownHostException {
    this.name = name;
    this.Address = InetAddress.getByName(IpAddress);
    this.Port = Port;
    this.distanceVectorTable = new ArrayList<>();
    this.RouterNeighborData = new ArrayList<>();
  }

  //---------------------------------Getters and Setters-----------------------------------
  public String getName() {
    return this.name;
  }

  public InetAddress getAddress() {
    return this.Address;
  }

  public void setAddress(InetAddress Address) {
    this.Address = Address;
  }

  public int getPort() {
    return this.Port;
  }

  //------------------Synchronize to avoid multithreading issues------------------------------
  public synchronized List<DVTableEntry> getDistanceVectorTable() {
    return distanceVectorTable;
  }

  public synchronized void setDistanceVectorTable(
    List<DVTableEntry> distanceVectorTable
  ) {
    this.distanceVectorTable = distanceVectorTable;
  }

  //--------------------------------------Main--------------------------------------------------
  public static void main(String[] args) {
    if (args.length < 1) {
      System.out.println("\nUsage: java Router <routerName>");
      System.out.println("Please start your router.\n");
      
      return;
    }

    String name = args[0]; // Router name is provided as command-line argument.

    // Attempt to look up router's IP and port from the config file
    String routerData = RouterParser.NeighborInfo(
      "#Routers",
      "#End of router list",
      name
    );
    if (routerData == null) {
      System.out.println("Router Name Does Not Exist In Configuration File.");
      System.exit(1);
    }

    String[] parts = routerData.split(",");
    if (parts.length < 2) {
      System.out.println(
        "Configuration Error: IP address or port missing for router " + name
      );
      System.exit(1);
    }
    String ipAddress = parts[0];
    int port = Integer.parseInt(parts[1]);

    // Proceed with starting the router using the configuration obtained from the file
    try {
      GenerateRouter(name, ipAddress, port);
    } catch (Exception e) {
      System.err.println("Failed to start the router: " + e.getMessage());
      e.printStackTrace();
    }
  }

  //----------------------------Router Generation-----------------------------------------------
  public static void GenerateRouter(String name, String IpAddress, int Port)
    throws IOException {
    try {
      Router router = new Router(name, IpAddress, Port);
      router.start(name);
    } catch (UnknownHostException e) {
      System.err.println("Router setup error: " + e.getMessage());
    } catch (SocketException e) {
      e.printStackTrace();
    }
  }

  //------------------------------------Start--------------------------------------------------
  public void start(String name) throws IOException {
    LaunchRouter(name);

    // Thread for receiving DV tables
    Thread receiveThread = new Thread(() -> {
      try {
        ReceiveDV();
      } catch (IOException e) {
        e.printStackTrace();
      }
    });

    // Start the receive thread
    receiveThread.start();

    // Prompt for manual updates
    try (
    Scanner scanner = new Scanner(System.in)) {
      while (true) {
        System.out.println("Press 's' to share DV table or 'q' to quit:");
        String input = scanner.nextLine();
        if ("s".equalsIgnoreCase(input)) {
          shareDVTable();
        } else if ("q".equalsIgnoreCase(input)) {          
          break;
        }
      }
    }
  }

  // Router launcher to initialize the DV table and share it initially
  private void LaunchRouter(String name) throws IOException {
    // Find Subnet and Router Neighbors
    findNeighbors(name);

    // Generate & Print Initial DV
    GenerateInitialDistanceVector(name);
    printDistanceVectorTable();

    // Share Initial DV Table
    shareDVTable();
  }

  //-----------------------------Initial Distance Vector----------------------------------
  public void GenerateInitialDistanceVector(String routerName) {
    List<String> connectedSubnets = GetListOfNeighbors.GetSpecifiedList(
      "Subnet",
      routerName
    );

    if (connectedSubnets.contains("Device with given name has no neighbors!")) {
      System.out.println(
        "Cannot generate initial distance vector. Given name has no neighbors!"
      );
      System.exit(0);
    }
    List<DVTableEntry> initialDistanceVectorTable = new ArrayList<>();

    for (String subnet : connectedSubnets) {
      initialDistanceVectorTable.add(new DVTableEntry(subnet, 0, routerName));
    }
    setDistanceVectorTable(initialDistanceVectorTable);
  }

  public String GetTime() {
    DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
    LocalDateTime now = LocalDateTime.now();
    return (dtf.format(now));
  }

  private void shareDVTable() throws IOException {
    DatagramSocket socket = new DatagramSocket();
    byte[] data = DistanceVectorToByteArray(getDistanceVectorTable());
    for (String neighbor : RouterNeighborData) { 
      String[] parts = neighbor.split(",");
      InetAddress address = InetAddress.getByName(parts[1]);
      int port = Integer.parseInt(parts[2]);
      DatagramPacket packet = new DatagramPacket(
        data,
        data.length,
        address,
        port
      );
      socket.send(packet);
    }
    socket.close();
  }

  //----------------------------Router Receive DV Table------------------------------------

  // Compare the received distance vector with the router's own distance vector table

  public void ReceiveDV() throws IOException {
    try (DatagramSocket socket = new DatagramSocket(this.Port)) {
      byte[] buffer = new byte[1024];
      while (true) {
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
        socket.receive(packet);
        System.out.println("Distance Vector has been received!");
        List<DVTableEntry> receivedDistanceVector = ByteArrayToDistanceVectorTableEntry(
          packet.getData()
        );
        CompareAndUpdateDistanceVector(receivedDistanceVector);
      }
    } catch (IOException | ClassNotFoundException e) {
      e.printStackTrace();
    }
  }

  private void CompareAndUpdateDistanceVector(
    List<DVTableEntry> receivedDistanceVector
  ) {
    int entryUpdates = 0;
    int tableAdditions = 0;
    for (DVTableEntry receivedEntry : receivedDistanceVector) {
      boolean found = false;
      for (DVTableEntry ownEntry : this.distanceVectorTable) {
        if (ownEntry.getDestination().equals(receivedEntry.getDestination())) {
          found = true;
          if (receivedEntry.getDistance() < ownEntry.getDistance()) {
            ownEntry.setDistance(receivedEntry.getDistance());
            ownEntry.setNextHop(receivedEntry.getNextHop());
            entryUpdates++;
          }
          break;
        }
      }
      if (!found) {
        this.distanceVectorTable.add(
            new DVTableEntry(
              receivedEntry.getDestination(),
              receivedEntry.getDistance() + 1,
              receivedEntry.getNextHop()
            )
          );
        tableAdditions++;
      }
    }
    System.out.println("Number of entries updated: " + entryUpdates + ".");
    System.out.println("Number of new entries added: " + tableAdditions + ".");
    printDistanceVectorTable();
  }

  public void printDistanceVectorTable() {
    System.out.println(
      GetTime() + "\nDistance Vector Table:\n+--------+--------+--------+"
    );
    System.out.printf("| %-6s | %-6s | %-6s |\n", "Dest", "Dist", "Next");
    System.out.println("+--------+--------+--------+");
    for (DVTableEntry entry : distanceVectorTable) {
      System.out.printf(
        "| %-6s | %-6d | %-6s |\n",
        entry.getDestination(),
        entry.getDistance(),
        entry.getNextHop()
      );
      System.out.println("+--------+--------+--------+");
    }
    System.out.println();
  }

  private byte[] DistanceVectorToByteArray(List<DVTableEntry> distanceVector)
    throws IOException {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    ObjectOutputStream oos = new ObjectOutputStream(baos);
    oos.writeObject(distanceVector);
    oos.flush();
    return baos.toByteArray();
  }

  @SuppressWarnings("unchecked")
  private List<DVTableEntry> ByteArrayToDistanceVectorTableEntry(byte[] data)
    throws IOException, ClassNotFoundException {
    ByteArrayInputStream bais = new ByteArrayInputStream(data);
    ObjectInputStream ois = new ObjectInputStream(bais);
    Object obj = ois.readObject();
    if (obj instanceof List<?>) {
      List<?> list = (List<?>) obj;
      return (List<DVTableEntry>) list;
    } else {
      throw new ClassNotFoundException("Object is not a list");
    }
  }

  private void findNeighbors(String name) throws IOException {
    // Finds and stores subnet and router neighbors for initial sharing and updates
    String startHeaderSubnet = "#Routers to subnets";
    String endHeaderSubnet = "#End Routers to Subnets List";
    List<String> subnetNeighbors = RouterParser.NeighborFinder(
      startHeaderSubnet,
      endHeaderSubnet,
      name
    );
    System.out.println("Subnet neighbors for " + name + ": " + subnetNeighbors);

    String startHeaderRouter = "#Routers to Routers";
    String endHeaderRouter = "#End Router to Router List";
    List<String> routerNeighbors = RouterParser.NeighborFinder(
      startHeaderRouter,
      endHeaderRouter,
      name
    );

    for (String neighborName : routerNeighbors) {
      String routerData = RouterParser.NeighborInfo(
        "#Routers",
        "#End of router list",
        neighborName
      );
      if (routerData != null) {
        String[] parts = routerData.split(",");
        String address = parts[0];
        int portNum = Integer.parseInt(parts[1]);
        RouterNeighborData.add(neighborName + "," + address + "," + portNum);
      }
    }
  }
}

# Distance Vector Routing Simulation Application

## Overview

This application simulates a network of routers using the Distance Vector routing protocol. Each simulated router maintains a table known as the Distance Vector (DV) table, which tracks the best-known distances to various subnets and the next hop router for those paths.

## Key Features

- **Dynamic Routing Table Updates:** Routers exchange DV tables with their neighbors, updating their own tables based on received information to ensure the shortest paths are always known.
- **Manual Control Over Updates:** Users can prompt routers to share their DV tables at any time, providing a hands-on experience with the routing update process.
- **Real-Time Feedback:** The application offers immediate visual feedback on DV table changes, enhancing understanding of the routing dynamics.

## How It Works

1. **Initialization:** Each router instance is started with a unique identifier and configuration, including IP address and port number. The configuration determines its neighbors within the network.
2. **Distance Vector Sharing:** Routers periodically share their DV tables with immediate neighbors. This sharing can also be triggered manually by the user, simulating real-time adjustments in the network.
3. **Table Updates:** Upon receiving a DV table from a neighbor, a router updates its own table to reflect any shorter paths discovered, adhering to the distance vector algorithm principles.
4. **User Interaction:** Through a simple console interface, users can command routers to share their DV tables or exit the simulation, providing interactive learning of distance vector routing.

## Usage

Running the application is straightforward. Launch a router instance via the command line, providing the router's name as an argument. The application will then prompt you for actions, such as sharing the DV table by entering "s" or entering "q" for quitting the simulation.

## Team Members:

Cyarina Amatya  
Leah Casey  
Adrianna Hatcher  
Mason Scott  

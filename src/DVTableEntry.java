//------------------------------Distance Vector Table Format------------------------------------
import java.io.Serializable;    

public class DVTableEntry implements Serializable {
        private String destination;
        private int distance;
        private String nextHop;

        public DVTableEntry(String destination, int distance, String nextHop) {
            this.destination = destination;
            this.distance = distance;
            this.nextHop = nextHop;
        }

        // Getters and setters
        public String getDestination() {
            return destination;
        }

        public void setDestination(String destination) {
            this.destination = destination;
        }

        public int getDistance() {
            return distance;
        }

        public void setDistance(int distance) {
            this.distance = distance;
        }

        public String getNextHop() {
            return nextHop;
        }

        public void setNextHop(String nextHop) {
            this.nextHop = nextHop;
        }
    }

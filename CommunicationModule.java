package Backend;
import java.util.Random;

public class CommunicationModule {
    private double range;
    private double lossProbability;
    private final Random random;

    // Constructor
    public CommunicationModule(double range, double lossProbability) {
        this.range = range;
        this.lossProbability = lossProbability;
        this.random = new Random();
    }
    // Default Constructor
    public CommunicationModule()
    {
        this(100.0, 0.2);
    }

    // Setters
    public void setRange(double range) {
        this.range = range;
    }
    public void setLossProbability(double lossProbability) {
        this.lossProbability = lossProbability;
    }

    // Getters
    public double getRange() {
        return range;
    }
    public double getLossProbability() {
        return lossProbability;
    }

    public boolean inRange(Drone a, Drone b) {
        double dist = a.getPosition().subtract(b.getPosition()).length();
        return dist <= range;
    }

    public boolean messageSuccess() {
        return random.nextDouble() > lossProbability;
    }

    public boolean canCommunicate(Drone a, Drone b) {
        if (!inRange(a, b)) {
            return false;
        }
        return messageSuccess();
    }

    public Drone[] getConnectedNeighbors(Drone[] drones, int index) {
        // Count how many neighbors first
        int count = 0;
        for (int i = 0; i < drones.length; i++) {
            if (i != index && canCommunicate(drones[index], drones[i])) {
                count++;
            }
        }
        // Create neighbor list
        Drone[] neighbors = new Drone[count];
        int k = 0;
        for (int i = 0; i < drones.length; i++) {
            if (i != index && canCommunicate(drones[index], drones[i])) {
                neighbors[k++] = drones[i];
            }
        }
        return neighbors;
    }
}

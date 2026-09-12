package Backend;

public class FormationManager {
    private double kPos;
    private double kVel;
    private final double neighborRange;

    // Constructor
    public FormationManager(double kPos, double kVel, double neighborRange) {
        this.kPos = kPos;
        this.kVel = kVel;
        this.neighborRange = neighborRange;
    }
    // Default Constructor
    public FormationManager() {
        this(0.4, 0.2, 80.0); // Default values from MainCheck
    }

    // setters
    public void setGains(double kPos, double kVel) {
        this.kPos = kPos;
        this.kVel = kVel;
    }
    // getters
    public double getKPos() {
        return kPos;
    }
    public double getKVel() {
        return kVel;
    }
    public double getNeighborRange() {
        return neighborRange;
    }

    public Vector3D computeFormationForce(Drone[] drones, int i) {
        Drone d_i = drones[i];
        Vector3D F = new Vector3D(0, 0, 0); // initialize force sum
        for (int j = 0; j < drones.length; j++) {
            if (j == i)
                continue;
            Drone d_j = drones[j];
            // Distance check
            double dist = d_i.getPosition().subtract(d_j.getPosition()).length();
            if (dist <= neighborRange) {
                // Position difference Pi - Pj
                Vector3D posDiff = d_i.getPosition().subtract(d_j.getPosition());
                // Velocity difference Vi - Vj
                Vector3D velDiff = d_i.getVelocity().subtract(d_j.getVelocity());
                // Add: - [ kPos*(Pi-Pj) + kVel*(Vi-Vj) ]
                Vector3D term = posDiff.multiply(kPos)
                        .add(velDiff.multiply(kVel));

                F = F.add(term.multiply(-1)); // negative sign according to formula
            }
        }
        return F;
    }
}

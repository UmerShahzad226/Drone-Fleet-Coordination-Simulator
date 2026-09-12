package Backend;

public class CollisionAvoidance {
    private double kRepulsion;
    private double safeDistance;

    // Constructor
    public CollisionAvoidance(double kRepulsion, double safeDistance) {
        this.kRepulsion = kRepulsion;
        this.safeDistance = safeDistance;
    }

    // Default Constructor
    public CollisionAvoidance() {
        this(5000.0, 30.0);
    }

    // Setters
    public void setParameters(double kRepulsion, double safeDistance) {
        this.kRepulsion = kRepulsion;
        this.safeDistance = safeDistance;
    }

    // Getters
    public double getKRepulsion() {
        return kRepulsion;
    }

    public double getSafeDistance() {
        return safeDistance;
    }

    public Vector3D computeRepulsiveForce(Drone[] drones, int i) {
        Drone d_i = drones[i];
        Vector3D totalForce = new Vector3D(0, 0, 0);
        for (int j = 0; j < drones.length; j++) {
            if (i == j)
                continue;
            Drone d_j = drones[j];
            Vector3D diff = d_i.getPosition().subtract(d_j.getPosition());
            double dist = diff.length();

            if (dist < safeDistance && dist > 0) {
                double factor = kRepulsion / (dist * dist * dist);
                Vector3D force = diff.multiply(factor);
                totalForce = totalForce.add(force);
            }
        }
        return totalForce;
    }
}

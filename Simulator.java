package Backend;

import java.util.ArrayList;

public class Simulator {
    private final Drone[] drones;
    private final Environment environment;
    private final FormationManager formationManager;
    private final CollisionAvoidance collisionAvoidance;
    private final CommunicationModule communicationModule;
    private final Logger logger;
    private final double dt; // time step
    private final int totalSteps; // number of simulation steps
    private double minCollisionDist;
    private ArrayList<Vector3D> dronePositions;
    private int collisionCount; // Added collision counter

    public Simulator(int droneCount) {
        this.drones = new Drone[droneCount];
        for (int i = 0; i < droneCount; i++) {
            drones[i] = new Drone(); // Make sure Drone has a default constructor
        }
        this.environment = new Environment();
        this.formationManager = new FormationManager();
        this.collisionAvoidance = new CollisionAvoidance();
        this.communicationModule = new CommunicationModule();
        this.logger = new Logger();
        this.dt = 0.1;
        this.totalSteps = 0;
        this.minCollisionDist = 20.0;
        this.dronePositions = new ArrayList<>();
        this.collisionCount = 0; // Initialize collision count
        updatePositionsList();
    }

    // Constructor
    public Simulator(Drone[] drones, Environment env, FormationManager fm, CollisionAvoidance ca,
            CommunicationModule comm, Logger logger, double dt, int totalSteps, double minCollisionDist) {
        this.drones = drones;
        this.environment = env;
        this.formationManager = fm;
        this.collisionAvoidance = ca;
        this.communicationModule = comm;
        this.logger = logger;
        this.dt = dt;
        this.totalSteps = totalSteps;
        this.minCollisionDist = minCollisionDist;
        this.dronePositions = new ArrayList<>();
        this.collisionCount = 0; // Initialize collision count
        updatePositionsList();
    }

    // Refactored Simulator for GUI Integration
    public Simulator(Drone[] drones, Environment env, FormationManager fm, CollisionAvoidance ca,
            CommunicationModule comm, Logger logger) {
        this.drones = drones;
        this.environment = env;
        this.formationManager = fm;
        this.collisionAvoidance = ca;
        this.communicationModule = comm;
        this.logger = logger;
        this.dt = 0.1; // Default time step
        this.totalSteps = 0; // Infinite/Controlled by GUI
        this.minCollisionDist = 20.0;
        this.dronePositions = new ArrayList<>();
        this.collisionCount = 0; // Initialize collision count
        updatePositionsList();
    }

    public void fly() {
        for (int step = 0; step < totalSteps; step++) {
            step();
        }
        logger.writeSummary(drones, minCollisionDist); // Write simulation summary
    }

    // Step function for GUI
    public void step() {
        for (int i = 0; i < drones.length; i++) {
            Vector3D fForm = formationManager.computeFormationForce(drones, i);
            drones[i].setFormationForce(fForm);
        }

        for (int i = 0; i < drones.length; i++) {
            Vector3D fRep = collisionAvoidance.computeRepulsiveForce(drones, i);
            drones[i].setRepulsiveForce(fRep);
        }

        for (int i = 0; i < drones.length; i++) {
            for (int j = i + 1; j < drones.length; j++) {
                logger.registerCommAttempt();
                if (communicationModule.canCommunicate(drones[i], drones[j])) {
                    logger.registerCommSuccess();
                }
            }
        }

        for (int i = 0; i < drones.length; i++) {
            Drone d = drones[i];
            Vector3D target = d.getTarget();
            Vector3D thrust = d.getController().computeThrust(d, target, d.getMass());
            d.updatePhysics(dt, target);

            logger.logDroneState(i, d, thrust);
        }

        for (Drone drone : drones) {
            environment.applyBounds(drone);
        }

        detectCollisions();
        updatePositionsList();
    }

    private void updatePositionsList() {
        dronePositions.clear();
        for (Drone d : drones) {
            dronePositions.add(d.getPosition());
        }
    }

    public void update() {
        step();
    }

    private void detectCollisions() {
        for (int i = 0; i < drones.length; i++) {
            for (int j = i + 1; j < drones.length; j++) {
                double dist = drones[i].getPosition().subtract(drones[j].getPosition()).length();
                if (dist < minCollisionDist) {
                    // collisionCount++; // Increment collision counter
                    // logger.registerCollision();
                }
            }
        }
    }

    public ArrayList<Vector3D> getDronePositions() {
        return dronePositions;
    }

    public Drone[] getDrones() {
        return drones;
    }

    // Get collision count
    public int getCollisionCount() {
        return collisionCount;
    }

    // Reset collision count (optional, for restart functionality)
    public void resetCollisionCount() {
        this.collisionCount = 0;
    }

    public void setMinCollisionDist(double dist) {
        this.minCollisionDist = dist;
    }

    public double getMinCollisionDist() {
        return minCollisionDist;
    }
}
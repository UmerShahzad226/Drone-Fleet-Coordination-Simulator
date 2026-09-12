
package Backend;

public class MainCheck {

    public static void main(String[] args) {
        String configPath = "initial_input.txt";
        java.io.File f = new java.io.File(configPath);
        Drone[] drones;

        if (f.exists() && f.isFile()) {
            System.out.println("Loading drones from " + configPath);
            drones = ConfigLoader.loadDrones(configPath);
        } else {
            System.out.println("File not found, creating default drones.");
            drones = ConfigLoader.createDrones(5);
        }

        Vector3D targetPoint = new Vector3D(150, 150, 50);
        Environment environment = new Environment(300, 300, 200);
        FormationManager formationManager = new FormationManager(0.4, 0.2, 80);
        CollisionAvoidance collisionAvoidance = new CollisionAvoidance(5000, 30);
        CommunicationModule communicationModule = new CommunicationModule(100, 0.2);
        Logger logger = new Logger("positions.csv", "metrics.txt");
        Simulator simulator = new Simulator(drones, environment, formationManager, collisionAvoidance,
                communicationModule, logger, 0.1, 1000, 5);

        System.out.println("Simulation started...");
        System.out.println("Simulation started...");
        for (Drone d : drones) {
            d.setTarget(targetPoint);
        }
        simulator.fly();
        System.out.println("Simulation completed!");
        System.out.println("Output written to positions.csv and metrics.txt");
        logger.close();
    }
}

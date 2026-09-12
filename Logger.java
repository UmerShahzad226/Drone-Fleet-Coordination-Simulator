package Backend;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

public class Logger {
    private PrintWriter positionWriter;
    private PrintWriter metricsWriter;
    private int collisionCount = 0;
    private int messageAttempts = 0;
    private int messageSuccess = 0;

    public Logger() {
        String postionWriter = "";
        String metricsWriter = "";
    }

    // Constructor
    public Logger(String positionFile, String metricsFile) {
        try {
            positionWriter = new PrintWriter(new FileWriter(positionFile));
            metricsWriter = new PrintWriter(new FileWriter(metricsFile));
            // CSV header
            positionWriter.println("DroneID,px,py,pz,vx,vy,vz,thrustZ");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void logDroneState(int droneID, Drone d, Vector3D thrust) {
        Vector3D p = d.getPosition();
        Vector3D v = d.getVelocity();
        positionWriter.println(droneID + "," +
                p.x + "," + p.y + "," + p.z + "," +
                v.x + "," + v.y + "," + v.z + "," +
                thrust.z);
    }

    public void registerCollision() {
        collisionCount++;
    }

    public void registerCommAttempt() {
        messageAttempts++;
    }

    public void registerCommSuccess() {
        messageSuccess++;
    }

    public void writeSummary(Drone[] drones, double minSafeDist) {
        double totalDist = 0;
        int pairs = 0;
        for (int i = 0; i < drones.length; i++) {
            for (int j = i + 1; j < drones.length; j++) {
                double d = drones[i].getPosition()
                        .subtract(drones[j].getPosition())
                        .length();
                totalDist += d;
                pairs++;
            }
        }

        double avgSpacing = (pairs > 0) ? totalDist / pairs : 0;
        double commRate = (messageAttempts > 0) ? (double) messageSuccess / messageAttempts : 0;

        metricsWriter.println("=== SIMULATION METRICS ===");
        metricsWriter.println("Average Spacing: " + avgSpacing);
        metricsWriter.println("Collision Count: " + collisionCount);
        metricsWriter.println("Communication Success Rate: " + commRate);

        metricsWriter.flush();
        // metricsWriter.close();
        positionWriter.flush();
        // positionWriter.close();
    }

    public void close() {
        if (metricsWriter != null)
            metricsWriter.close();
        if (positionWriter != null)
            positionWriter.close();
    }
}

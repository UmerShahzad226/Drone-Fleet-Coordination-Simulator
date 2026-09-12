package Backend;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ConfigLoader {

    public static Drone[] loadDrones(String filePath) {
        List<Drone> droneList = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty() || line.startsWith("#"))
                    continue;
                String[] parts = line.split(",");
                if (parts.length >= 5) {
                    double x = Double.parseDouble(parts[0].trim());
                    double y = Double.parseDouble(parts[1].trim());
                    double z = Double.parseDouble(parts[2].trim());
                    double mass = Double.parseDouble(parts[3].trim());
                    double drag = Double.parseDouble(parts[4].trim());

                    droneList.add(new Drone(new Vector3D(x, y, z), mass, drag));
                }
            }
        } catch (IOException e) {
            System.err.println("Error loading config: " + e.getMessage());
            return createDefaultDrones();
        }
        return droneList.toArray(new Drone[0]);
    }

    private static Drone[] createDefaultDrones() {
        return createDrones(5);
    }

    public static Drone[] createDrones(int count) {
        Drone[] drones = new Drone[count];
        int rows = (int) Math.ceil(Math.sqrt(count));
        int cols = (int) Math.ceil((double) count / rows);

        for (int i = 0; i < count; i++) {
            int r = i / cols;
            int c = i % cols;
            // Arrange in a grid for better visibility
            drones[i] = new Drone(new Vector3D(100 + c * 80, 100 + r * 80, 50), 1.5, 0.1);
        }
        return drones;
    }
}

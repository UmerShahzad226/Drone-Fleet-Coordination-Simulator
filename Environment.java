package Backend;

public class Environment {
    private final double width;
    private final double height;
    private final double depth;
    
    public Environment() {
        this.width = 440;   // default width
        this.height = 440;  // default height
        this.depth = 500;    // default depth
    }

    // Constructor
    public Environment(double width, double height, double depth) {
        this.width = width;
        this.height = height;
        this.depth = depth;
    }

    // Getters
    public double getWidth() { return width; }
    public double getHeight() { return height; }
    public double getDepth() { return depth; }

    public void applyBounds(Drone drone) {
        Vector3D pos = drone.getPosition();
        Vector3D vel = drone.getVelocity();

        boolean corrected = false;
        // X-axis bounds
        if (pos.x < 0) {
            pos.x = 0;
            vel.x = -vel.x * 0.5;   // reflect with damping
            corrected = true;
        }
        else if (pos.x > width) {
            pos.x = width;
            vel.x = -vel.x * 0.5;
            corrected = true;
        }
        // Y-axis bounds
        if (pos.y < 0) {
            pos.y = 0;
            vel.y = -vel.y * 0.5;
            corrected = true;
        }
        else if (pos.y > height) {
            pos.y = height;
            vel.y = -vel.y * 0.5;
            corrected = true;
        }
        // Z-axis bounds
        if (pos.z < 0) {
            pos.z = 0;
            vel.z = -vel.z * 0.5;
            corrected = true;
        }
        else if (pos.z > depth) {
            pos.z = depth;
            vel.z = -vel.z * 0.5;
            corrected = true;
        }

        if (corrected) {
            drone.setPosition(pos);
            drone.setVelocity(vel);
        }
    }
}

package Backend;

public class Drone {
    private Vector3D position;
    private Vector3D velocity;
    private Vector3D acceleration;
    private Vector3D rotationangles;
    private Vector3D angularVelocity;
    private final double mass;
    private final double drag;
    private final Vector3D gravity = new Vector3D(0, 0, -9.81);
    private Vector3D thrustForce;
    private Vector3D formationForce;
    private Vector3D repulsiveForce;
    private final Controller controller;

    // --- Target ---
    private Vector3D target;

 // --- Default Constructor ---
    public Drone() {
        this.position = new Vector3D(0, 0, 0);       // Start at origin
        this.velocity = new Vector3D(0, 0, 0);
        this.acceleration = new Vector3D(0, 0, 0);
        this.rotationangles = new Vector3D(0, 0, 0);
        this.angularVelocity = new Vector3D(0, 0, 0);
        this.mass = 1.0;                             // Default mass
        this.drag = 0.1;                             // Default drag coefficient
        this.controller = new Controller();
        this.thrustForce = new Vector3D(0, 0, 0);
        this.formationForce = new Vector3D(0, 0, 0);
        this.repulsiveForce = new Vector3D(0, 0, 0);
        this.target = new Vector3D(400, 400, 200);   // Default target
    }

    // --- Constructor ---
    public Drone(Vector3D startPos, double mass, double drag) {
        this.position = startPos;
        this.velocity = new Vector3D(0, 0, 0);
        this.acceleration = new Vector3D(0, 0, 0);
        this.rotationangles = new Vector3D(0, 0, 0);
        this.angularVelocity = new Vector3D(0, 0, 0);
        this.mass = mass;
        this.drag = drag;
        this.controller = new Controller();
        this.thrustForce = new Vector3D(0, 0, 0);
        this.formationForce = new Vector3D(0, 0, 0);
        this.repulsiveForce = new Vector3D(0, 0, 0);
        this.target = new Vector3D(400, 400, 200); // Default target
    }

    // SETTERS
    public void setPosition(Vector3D p) {
        this.position = p;
    }

    public void setVelocity(Vector3D v) {
        this.velocity = v;
    }

    public void setRotationangles(Vector3D r) {
        this.rotationangles = r;
    }

    public void setAngularVelocity(Vector3D av) {
        this.angularVelocity = av;
    }

    // GETTERS
    public Vector3D getPosition() {
        return position;
    }

    public Vector3D getVelocity() {
        return velocity;
    }

    public Vector3D getAcceleration() {
        return acceleration;
    }

    public Vector3D getRotationangles() {
        return rotationangles;
    }

    public Vector3D getAngularVelocity() {
        return angularVelocity;
    }

    public double getMass() {
        return mass;
    }

    public double getDrag() {
        return drag;
    }

    public Controller getController() {
        return controller;
    }

    public void updatePhysics(double dt, Vector3D target) {
        // 1. Compute thrust force from controller (PD controller)
        thrustForce = controller.computeThrust(this, target, mass);
        // 2. Aerodynamic drag force: F = -k * v
        Vector3D aeroDrag = velocity.multiply(-drag);
        // 3. Sum of forces:
        // m*a = mg + T + Fdrag + Frep + Fform
        Vector3D totalForce = gravity.multiply(mass).add(thrustForce)
                .add(aeroDrag).add(repulsiveForce).add(formationForce);

        // 4. Acceleration a = F/m
        acceleration = totalForce.multiply(1.0 / mass);

        // 5. Update translational motion:
        velocity = velocity.add(acceleration.multiply(dt));
        position = position.add(velocity.multiply(dt));

        // 6. Update rotational motion
        angularVelocity = angularVelocity.add(
                controller.computeTorque(rotationangles, angularVelocity).multiply(dt));
        rotationangles = rotationangles.add(angularVelocity.multiply(dt));
    }

    public void setFormationForce(Vector3D f) {
        this.formationForce = f;
    }

    public void setRepulsiveForce(Vector3D f) {
        this.repulsiveForce = f;
    }

    public void setTarget(Vector3D t) {
        this.target = t;
    }

    public Vector3D getTarget() {
        return target;
    }
}

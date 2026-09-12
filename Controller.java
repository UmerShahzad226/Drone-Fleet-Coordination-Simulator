package Backend;
public class Controller {
    private double kp_pos = 2.0;
    private double kv_vel = 1.0;
    private double kp_rot = 1.5;
    private double kv_rot = 0.8;
    private final Vector3D gravity = new Vector3D(0, 0, -9.81);

    // SETTER
    public void setPositionGains(double kp, double kv) {
        this.kp_pos = kp;
        this.kv_vel = kv;
    }
    public void setRotationGains(double kp, double kv) {
        this.kp_rot = kp;
        this.kv_rot = kv;
    }

    public Vector3D computeThrust(Drone d, Vector3D target, double mass) {

        // 1. Position error: e = p_target - p
        Vector3D position = target.subtract(d.getPosition());

        // 2. Velocity error: e_dot = v_target(0) - v
        Vector3D velocity = d.getVelocity().multiply(-1);

        // 3. Desired acceleration:
        //     a = kp * e + kv * e_dot + g
        Vector3D desiredAcc = position.multiply(kp_pos).add(velocity.multiply(kv_vel)).add(gravity);

        // 4. Convert acceleration to force: F = m * a
        return desiredAcc.multiply(mass);
    }

    public Vector3D computeTorque(Vector3D rotation, Vector3D angularVelocity) {
        Vector3D desiredRot = new Vector3D(0, 0, 0);
        Vector3D rotational = desiredRot.subtract(rotation);
        Vector3D rotVelocity = angularVelocity.multiply(-1);
        // t = kr(rt-r) + kw(wt-w)

        return rotational.multiply(kp_rot).add(rotVelocity.multiply(kv_rot));
    }
}


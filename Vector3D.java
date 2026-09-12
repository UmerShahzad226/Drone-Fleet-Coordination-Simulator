package Backend;

public class Vector3D {
    public double x;
    public double y;
    public double z;

    public Vector3D(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public Vector3D add(Vector3D v) {
        return new Vector3D(this.x + v.x, this.y + v.y, this.z + v.z);
    }

    public Vector3D subtract(Vector3D v) {
        return new Vector3D(this.x - v.x, this.y - v.y, this.z - v.z);
    }

    public Vector3D multiply(double k) {
        return new Vector3D(this.x * k, this.y * k, this.z * k);
    }

    public double length() {
        return Math.sqrt(x * x + y * y + z * z);
    }

    public Vector3D normalize() {
        double len = length();
        if (len == 0) return new Vector3D(0, 0, 0);
        return new Vector3D(x / len, y / len, z / len);
    }

    public String toString() {
        return "(" + x + ", " + y + ", " + z + ")";
    }
}


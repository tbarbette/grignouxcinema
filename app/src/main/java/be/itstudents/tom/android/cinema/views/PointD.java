package be.itstudents.tom.android.cinema.views;

public class PointD {
    public double x = 0d;
    public double y = 0d;

    public PointD(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public PointD() {

    }

    public static PointD center(PointD p1, PointD p2) {
        PointD c = new PointD((p1.x + p2.x) / 2d, (p1.y + p2.y) / 2d);
        return c;
    }

    @Override
    public String toString() {
        return "(" + x + " " + y + ")";
    }
}

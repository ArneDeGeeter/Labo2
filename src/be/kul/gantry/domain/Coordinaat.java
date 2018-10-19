package be.kul.gantry.domain;

import java.util.Objects;

public class Coordinaat implements  Comparable{
    public int x;
    public int y;

    public Coordinaat(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    @Override
    public String toString() {
        return "Coordinaat{" +
                "x=" + x +
                ", y=" + y +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Coordinaat)) return false;
        Coordinaat that = (Coordinaat) o;
        return x == that.x &&
                y == that.y;
    }

    @Override
    public int hashCode() {

        return Objects.hash(x, y);
    }

    @Override
    public int compareTo(Object o) {
        if (this == o) return 0;
        if (!(o instanceof Coordinaat)) throw new ClassCastException();
        Coordinaat that = (Coordinaat) o;
        return x*x+y*y-that.x*that.x-that.y*that.y;
    }
}

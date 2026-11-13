package model;

public class Edge {
    private int x1, y1, x2, y2;

    public Edge(Point2D a, Point2D b) {
        this.x1 = a.getX();
        this.y1 = a.getY();
        this.x2 = b.getX();
        this.y2 = b.getY();
    }

    public boolean isHorizontal() {
        return y1 == y2;
    }

    public void orientate() {
        if (y1 > y2) {
            int tempX = x1;
            int tempY = y1;
            x1 = x2;
            y1 = y2;
            x2 = tempX;
            y2 = tempY;
        }
    }

    public boolean isIntersection(int y) {
        return y >= y1 && y < y2;
    }

    public int getIntersection(int y) {
        float k = (float) (x2 - x1) / (y2 - y1);
        return Math.round(x1 + (y - y1) * k);
    }

    public int getY1() { return y1; }
    public int getY2() { return y2; }
}
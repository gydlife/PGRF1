package clip;

import model.Point2D;

public class CohenSutherland {

    private static final int INSIDE = 0; // 0000
    private static final int LEFT = 1;   // 0001
    private static final int RIGHT = 2;  // 0010
    private static final int BOTTOM = 4; // 0100
    private static final int TOP = 8;    // 1000

    private int xMin, yMin, xMax, yMax;

    public CohenSutherland(int xMin, int yMin, int xMax, int yMax) {
        setWindow(xMin, yMin, xMax, yMax);
    }

    public void setWindow(int xMin, int yMin, int xMax, int yMax) {
        this.xMin = Math.min(xMin, xMax);
        this.yMin = Math.min(yMin, yMax);
        this.xMax = Math.max(xMin, xMax);
        this.yMax = Math.max(yMin, yMax);
    }

    private int computeCode(int x, int y) {
        int code = INSIDE;
        if (x < xMin)      code |= LEFT;
        else if (x > xMax) code |= RIGHT;
        if (y < yMin)      code |= BOTTOM;
        else if (y > yMax) code |= TOP;
        return code;
    }


    public Point2D[] clip(Point2D p1, Point2D p2) {
        int x1 = p1.getX(), y1 = p1.getY();
        int x2 = p2.getX(), y2 = p2.getY();

        int code1 = computeCode(x1, y1);
        int code2 = computeCode(x2, y2);
        boolean accept = false;

        while (true) {
            if ((code1 | code2) == 0) {
                accept = true;
                break;
            } else if ((code1 & code2) != 0) {
                break;
            } else {

                int x = 0, y = 0;
                int outCode = (code1 != 0) ? code1 : code2;

                if ((outCode & TOP) != 0) {
                    x = x1 + (x2 - x1) * (yMax - y1) / (y2 - y1);
                    y = yMax;
                } else if ((outCode & BOTTOM) != 0) {
                    x = x1 + (x2 - x1) * (yMin - y1) / (y2 - y1);
                    y = yMin;
                } else if ((outCode & RIGHT) != 0) {
                    y = y1 + (y2 - y1) * (xMax - x1) / (x2 - x1);
                    x = xMax;
                } else if ((outCode & LEFT) != 0) {
                    y = y1 + (y2 - y1) * (xMin - x1) / (x2 - x1);
                    x = xMin;
                }

                if (outCode == code1) {
                    x1 = x; y1 = y;
                    code1 = computeCode(x1, y1);
                } else {
                    x2 = x; y2 = y;
                    code2 = computeCode(x2, y2);
                }
            }
        }

        if (accept) {
            return new Point2D[] { new Point2D(x1, y1), new Point2D(x2, y2) };
        }
        return null;
    }

    public int getXMin() { return xMin; }
    public int getYMin() { return yMin; }
    public int getXMax() { return xMax; }
    public int getYMax() { return yMax; }
}
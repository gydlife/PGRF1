package clip;

import model.Point2D;
import model.Polygon;

import java.util.List;

public class SutherlandHodgman {


    public static Polygon clip(Polygon subjectPolygon, Polygon clipPolygon) {
        if (subjectPolygon == null || subjectPolygon.getSize() < 3 ||
                clipPolygon == null || clipPolygon.getSize() < 3) {
            return new Polygon();
        }

        Polygon outputPolygon = subjectPolygon;


        for (int i = 0; i < clipPolygon.getSize(); i++) {
            Point2D cp1 = clipPolygon.getPoint(i);
            Point2D cp2 = clipPolygon.getPoint((i + 1) % clipPolygon.getSize());

            Polygon inputPolygon = outputPolygon;
            outputPolygon = new Polygon();

            if (inputPolygon.getSize() == 0) break;

            Point2D s = inputPolygon.getPoint(inputPolygon.getSize() - 1);

            for (int j = 0; j < inputPolygon.getSize(); j++) {
                Point2D p = inputPolygon.getPoint(j);

                if (isInside(p, cp1, cp2)) {
                    if (!isInside(s, cp1, cp2)) {
                        Point2D inter = computeIntersection(s, p, cp1, cp2);
                        if (inter != null) outputPolygon.addPoint(inter);
                    }
                    outputPolygon.addPoint(p);
                } else if (isInside(s, cp1, cp2)) {
                    Point2D inter = computeIntersection(s, p, cp1, cp2);
                    if (inter != null) outputPolygon.addPoint(inter);
                }
                s = p;
            }
        }

        return outputPolygon;
    }

    private static boolean isInside(Point2D p, Point2D cp1, Point2D cp2) {
        return (cp2.getX() - cp1.getX()) * (p.getY() - cp1.getY()) -
                (cp2.getY() - cp1.getY()) * (p.getX() - cp1.getX()) >= 0;
    }

    private static Point2D computeIntersection(Point2D s, Point2D p, Point2D cp1, Point2D cp2) {
        double x1 = s.getX(), y1 = s.getY();
        double x2 = p.getX(), y2 = p.getY();
        double x3 = cp1.getX(), y3 = cp1.getY();
        double x4 = cp2.getX(), y4 = cp2.getY();

        double denom = (x1 - x2) * (y3 - y4) - (y1 - y2) * (x3 - x4);
        if (Math.abs(denom) < 1e-10) return null;

        double nx = ((x1 * y2 - y1 * x2) * (x3 - x4) - (x1 - x2) * (x3 * y4 - y3 * x4)) / denom;
        double ny = ((x1 * y2 - y1 * x2) * (y3 - y4) - (y1 - y2) * (x3 * y4 - y3 * x4)) / denom;

        return new Point2D((int) Math.round(nx), (int) Math.round(ny));
    }
}
package fill;

import model.Edge;
import model.Point2D;
import model.Polygon;
import raster.Raster;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ScanLineFill implements Filler {
    private int fillColor;
    private Polygon polygon;
    private Raster raster;

    public ScanLineFill(Raster raster) {
        this.raster = raster;
        this.fillColor = 0x00FF00;
    }

    @Override
    public void fill() {
        if (polygon == null || polygon.getSize() < 3) return;

        List<Edge> edges = new ArrayList<>();
        int yMin = Integer.MAX_VALUE;
        int yMax = Integer.MIN_VALUE;


        for (int i = 0; i < polygon.getSize(); i++) {
            Point2D pointA = polygon.getPoint(i);
            Point2D pointB = polygon.getPoint((i + 1) % polygon.getSize());

            Edge edge = new Edge(pointA, pointB);
            if (!edge.isHorizontal()) {
                edge.orientate();
                edges.add(edge);

                if (edge.getY1() < yMin) yMin = edge.getY1();
                if (edge.getY2() > yMax) yMax = edge.getY2();
            }
        }

        if (edges.isEmpty()) return;

        for (int y = yMin; y <= yMax; y++) {
            List<Integer> intersections = new ArrayList<>();

            for (Edge edge : edges) {
                if (edge.isIntersection(y)) {
                    intersections.add(edge.getIntersection(y));
                }
            }

            Collections.sort(intersections);

            for (int i = 0; i < intersections.size() - 1; i += 2) {
                int xStart = intersections.get(i);
                int xEnd = intersections.get(i + 1);

                for (int x = xStart; x <= xEnd; x++) {
                    raster.setPixel(x, y, fillColor);
                }
            }
        }
    }

    @Override
    public void setFillColor(int color) {
        this.fillColor = color;
    }

    public void setPolygon(Polygon polygon) {
        this.polygon = polygon;
    }
}
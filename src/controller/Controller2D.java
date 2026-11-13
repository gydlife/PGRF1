package controller;

import clip.CohenSutherland;
import clip.SutherlandHodgman;
import fill.ScanLineFill;
import fill.SeedFill;
import model.Point2D;
import model.Polygon;
import rasterize.LineRasterizer;
import rasterize.LineRasterizerGraphics;
import rasterize.PolygonRasterizer;
import view.Panel;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

public class Controller2D {
    private final Panel panel;

    private LineRasterizer lineRasterizer;
    private PolygonRasterizer polygonRasterizer;
    private SeedFill seedFiller;
    private ScanLineFill scanLineFiller;

    private CohenSutherland cohenSutherland;
    private Polygon clippingWindowPolygon;

    private enum Mode { DRAW, CLIP_LINE, CLIP_POLYGON }
    private Mode currentMode = Mode.DRAW;

    private Polygon currentPolygon;
    private final List<Polygon> finishedPolygons = new ArrayList<>();

    private Point2D lineStart, lineEnd;
    private Point2D windowStart, windowEnd;
    private Point2D polygonPreviewPoint;
    private final List<Point2D> linePoints = new ArrayList<>();

    private boolean shiftPressed = false;

    public Controller2D(Panel panel) {
        this.panel = panel;

        lineRasterizer = new LineRasterizerGraphics(panel.getRaster());
        polygonRasterizer = new PolygonRasterizer(lineRasterizer);
        seedFiller = new SeedFill(panel.getRaster());
        scanLineFiller = new ScanLineFill(panel.getRaster());


        cohenSutherland = new CohenSutherland(150, 150, 450, 350);

        currentPolygon = new Polygon();

        initListeners();
        drawScene();
    }

    private void initListeners() {
        panel.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON1) {
                    if (currentMode == Mode.CLIP_LINE) {

                        windowStart = new Point2D(e.getX(), e.getY());
                        windowEnd = windowStart;
                    } else {

                        lineStart = new Point2D(e.getX(), e.getY());
                        lineEnd = getAdjustedEndPoint(lineStart, e.getX(), e.getY(), shiftPressed);
                    }
                    drawScene();
                } else if (e.getButton() == MouseEvent.BUTTON2) {

                    seedFiller.setSeed(new Point2D(e.getX(), e.getY()));
                    seedFiller.fill();
                    panel.repaint();
                } else if (e.getButton() == MouseEvent.BUTTON3) {

                    Point2D point = new Point2D(e.getX(), e.getY());
                    currentPolygon.addPoint(point);
                    polygonPreviewPoint = null;
                    drawScene();
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON1) {
                    if (currentMode == Mode.CLIP_LINE && windowStart != null) {

                        int x1 = windowStart.getX();
                        int y1 = windowStart.getY();
                        int x2 = e.getX();
                        int y2 = e.getY();
                        cohenSutherland.setWindow(x1, y1, x2, y2);
                        windowStart = null;
                        windowEnd = null;
                    } else if (lineStart != null) {
                        lineEnd = getAdjustedEndPoint(lineStart, e.getX(), e.getY(), shiftPressed);
                        linePoints.add(lineStart);
                        linePoints.add(lineEnd);
                        lineStart = null;
                        lineEnd = null;
                    }
                    drawScene();
                }
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    finishPolygon();
                }
            }
        });

        panel.addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                if (currentMode == Mode.CLIP_LINE && windowStart != null) {
                    windowEnd = new Point2D(e.getX(), e.getY());
                    drawScene();
                } else if (lineStart != null) {
                    lineEnd = getAdjustedEndPoint(lineStart, e.getX(), e.getY(), shiftPressed);
                    drawScene();
                }
            }

            @Override
            public void mouseMoved(MouseEvent e) {
                if (currentPolygon.getSize() > 0) {
                    polygonPreviewPoint = new Point2D(e.getX(), e.getY());
                    drawScene();
                }
            }
        });

        panel.setFocusable(true);
        panel.requestFocusInWindow();

        panel.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_SHIFT) {
                    shiftPressed = true;
                } else if (e.getKeyCode() == KeyEvent.VK_C) {

                    linePoints.clear();
                    currentPolygon.clear();
                    finishedPolygons.clear();
                    clippingWindowPolygon = null;
                    windowStart = null;
                    windowEnd = null;
                    lineStart = null;
                    lineEnd = null;
                    polygonPreviewPoint = null;
                    drawScene();
                } else if (e.getKeyCode() == KeyEvent.VK_1) {
                    currentMode = Mode.DRAW;
                    System.out.println("Mode: Normal Drawing");
                } else if (e.getKeyCode() == KeyEvent.VK_J) {

                    currentMode = Mode.CLIP_LINE;
                    System.out.println("Mode: Cohen-Sutherland (J)");
                    drawScene();
                } else if (e.getKeyCode() == KeyEvent.VK_G) {

                    currentMode = Mode.CLIP_POLYGON;
                    System.out.println("Mode: Sutherland-Hodgman (G)");
                    drawScene();
                } else if (e.getKeyCode() == KeyEvent.VK_ENTER || e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    finishPolygon();
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_SHIFT) {
                    shiftPressed = false;
                }
            }
        });
    }

    private Point2D getAdjustedEndPoint(Point2D start, int x2, int y2, boolean isShift) {
        if (!isShift) return new Point2D(x2, y2);
        double dx = x2 - start.getX();
        double dy = y2 - start.getY();
        double length = Math.sqrt(dx * dx + dy * dy);
        double angle = Math.atan2(dy, dx);
        double snapAngle = Math.round(angle / (Math.PI / 4)) * (Math.PI / 4);
        int snappedX = (int) Math.round(start.getX() + length * Math.cos(snapAngle));
        int snappedY = (int) Math.round(start.getY() + length * Math.sin(snapAngle));
        return new Point2D(snappedX, snappedY);
    }

    private void finishPolygon() {
        if (currentPolygon.getSize() >= 3) {
            if (currentMode == Mode.CLIP_POLYGON && clippingWindowPolygon == null) {

                clippingWindowPolygon = currentPolygon;
                System.out.println("Clipping polygon set!");
            } else if (currentMode == Mode.CLIP_POLYGON && clippingWindowPolygon != null) {

                Polygon clipped = SutherlandHodgman.clip(currentPolygon, clippingWindowPolygon);
                finishedPolygons.add(clipped);
            } else {
                finishedPolygons.add(currentPolygon);
            }
            currentPolygon = new Polygon();
            polygonPreviewPoint = null;
            drawScene();
        } else if (currentPolygon.getSize() > 0) {
            currentPolygon.clear();
            polygonPreviewPoint = null;
            drawScene();
        }
    }

    private void drawScene() {
        panel.getRaster().clear();


        for (Polygon poly : finishedPolygons) {
            scanLineFiller.setPolygon(poly);
            scanLineFiller.fill();
        }

        for (int i = 0; i + 1 < linePoints.size(); i += 2) {
            Point2D p1 = linePoints.get(i);
            Point2D p2 = linePoints.get(i + 1);
            if (currentMode == Mode.CLIP_LINE) {
                Point2D[] clipped = cohenSutherland.clip(p1, p2);
                if (clipped != null) {
                    lineRasterizer.rasterize(clipped[0].getX(), clipped[0].getY(), clipped[1].getX(), clipped[1].getY());
                }
            } else {
                lineRasterizer.rasterize(p1.getX(), p1.getY(), p2.getX(), p2.getY());
            }
        }

        if (lineStart != null && lineEnd != null) {
            lineRasterizer.rasterize(lineStart.getX(), lineStart.getY(), lineEnd.getX(), lineEnd.getY());
        }

        for (Polygon poly : finishedPolygons) {
            polygonRasterizer.rasterize(poly);
        }
        polygonRasterizer.rasterize(currentPolygon);

        if (currentMode == Mode.CLIP_LINE) {
            int x1, y1, x2, y2;
            if (windowStart != null && windowEnd != null) {
                x1 = Math.min(windowStart.getX(), windowEnd.getX());
                y1 = Math.min(windowStart.getY(), windowEnd.getY());
                x2 = Math.max(windowStart.getX(), windowEnd.getX());
                y2 = Math.max(windowStart.getY(), windowEnd.getY());
            } else {
                x1 = cohenSutherland.getXMin();
                y1 = cohenSutherland.getYMin();
                x2 = cohenSutherland.getXMax();
                y2 = cohenSutherland.getYMax();
            }
            lineRasterizer.rasterize(x1, y1, x2, y1);
            lineRasterizer.rasterize(x2, y1, x2, y2);
            lineRasterizer.rasterize(x2, y2, x1, y2);
            lineRasterizer.rasterize(x1, y2, x1, y1);
        }

        if (currentMode == Mode.CLIP_POLYGON && clippingWindowPolygon != null) {
            polygonRasterizer.rasterize(clippingWindowPolygon);
        }

        if (polygonPreviewPoint != null && currentPolygon.getSize() > 0) {
            Point2D first = currentPolygon.getPoint(0);
            Point2D last = currentPolygon.getPoint(currentPolygon.getSize() - 1);
            lineRasterizer.rasterize(first.getX(), first.getY(), polygonPreviewPoint.getX(), polygonPreviewPoint.getY());
            if (currentPolygon.getSize() > 1) {
                lineRasterizer.rasterize(last.getX(), last.getY(), polygonPreviewPoint.getX(), polygonPreviewPoint.getY());
            }
        }

        panel.repaint();
    }
}
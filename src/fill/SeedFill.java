package fill;

import model.Point2D;
import raster.Raster;

public class SeedFill implements Filler {
    private Raster raster;
    private Point2D seed;
    private int backgroundColor, fillColor;

    public SeedFill(Raster raster) {
        this.raster = raster;
        this.fillColor = 0x00FF00;
    }

    @Override
    public void fill() {
        if (seed == null) return;
        fillRecursive(seed.getX(), seed.getY());
    }

    private void fillRecursive(int x, int y) {
        if (x < 0 || x >= raster.getWidth() || y < 0 || y >= raster.getHeight())
            return;

        int currentPixel = raster.getPixel(x, y);
        if (currentPixel != backgroundColor)
            return;

        raster.setPixel(x, y, fillColor);

        fillRecursive(x + 1, y);
        fillRecursive(x - 1, y);
        fillRecursive(x, y + 1);
        fillRecursive(x, y - 1);
    }

    public void setSeed(Point2D seed) {
        this.seed = seed;
        if (seed.getX() >= 0 && seed.getX() < raster.getWidth() &&
                seed.getY() >= 0 && seed.getY() < raster.getHeight()) {
            this.backgroundColor = raster.getPixel(seed.getX(), seed.getY());
        }
    }

    @Override
    public void setFillColor(int fillColor) {
        this.fillColor = fillColor;
    }
}
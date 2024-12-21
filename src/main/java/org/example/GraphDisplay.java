package org.example;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Ellipse2D;
import java.util.ArrayList;
import java.util.List;

public class GraphDisplay extends JPanel {

    private static final int WIDTH = 800;
    private static final int HEIGHT = 600;
    private double minX = -10;
    private double maxX = 10;
    private double minY = -10;
    private double maxY = 10;

    private double initialMinX, initialMaxX, initialMinY, initialMaxY;
    private Point startPoint = null;
    private Rectangle zoomRect = null;

    private List<Point> points = new ArrayList<>();
    private Point hoverPoint = null;

    public GraphDisplay() {
        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        initialMinX = minX;
        initialMaxX = maxX;
        initialMinY = minY;
        initialMaxY = maxY;

        // Mouse listeners for hovering and zooming
        addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                checkHover(e.getPoint());
                repaint();
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                if (startPoint != null) {
                    int x = Math.min(startPoint.x, e.getX());
                    int y = Math.min(startPoint.y, e.getY());
                    int width = Math.abs(startPoint.x - e.getX());
                    int height = Math.abs(startPoint.y - e.getY());
                    zoomRect = new Rectangle(x, y, width, height);
                    repaint();
                }
            }
        });

        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (SwingUtilities.isLeftMouseButton(e)) {
                    startPoint = e.getPoint();
                    zoomRect = null;
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (SwingUtilities.isLeftMouseButton(e) && zoomRect != null) {
                    applyZoom(zoomRect);
                    startPoint = null;
                    zoomRect = null;
                    repaint();
                } else if (SwingUtilities.isRightMouseButton(e)) {
                    resetZoom();
                    repaint();
                }
            }
        });

        // Generate function points
        generatePoints();
    }

    private void generatePoints() {
        double step = 0.1;
        for (double x = minX; x <= maxX; x += step) {
            double y = Math.sin(x);  // Example function y = sin(x)
            int screenX = (int) ((x - minX) / (maxX - minX) * WIDTH);
            int screenY = HEIGHT - (int) ((y - minY) / (maxY - minY) * HEIGHT);
            points.add(new Point(screenX, screenY));
        }
    }

    private void checkHover(Point mousePoint) {
        hoverPoint = null;
        for (Point p : points) {
            if (mousePoint.distance(p) < 6) {  // Tolerance for hover detection
                hoverPoint = p;
                break;
            }
        }
    }

    private void applyZoom(Rectangle zoomRect) {
        double x1 = minX + zoomRect.x * (maxX - minX) / WIDTH;
        double x2 = minX + (zoomRect.x + zoomRect.width) * (maxX - minX) / WIDTH;
        double y1 = maxY - (zoomRect.y + zoomRect.height) * (maxY - minY) / HEIGHT;
        double y2 = maxY - zoomRect.y * (maxY - minY) / HEIGHT;

        minX = Math.min(x1, x2);
        maxX = Math.max(x1, x2);
        minY = Math.min(y1, y2);
        maxY = Math.max(y1, y2);

        points.clear();
        generatePoints();
    }

    private void resetZoom() {
        minX = initialMinX;
        maxX = initialMaxX;
        minY = initialMinY;
        maxY = initialMaxY;

        points.clear();
        generatePoints();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;

        // Smoothing
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Draw axes
        drawAxes(g2d);

        // Draw function line with custom dashed pattern
        float[] dashPattern = {10, 5, 2, 5, 10};
        g2d.setColor(Color.BLUE);
        g2d.setStroke(new BasicStroke(2, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, dashPattern, 0));
        for (int i = 0; i < points.size() - 1; i++) {
            Point p1 = points.get(i);
            Point p2 = points.get(i + 1);
            g2d.drawLine(p1.x, p1.y, p2.x, p2.y);
        }

        // Draw green function points (markers)
        g2d.setColor(Color.GREEN);
        for (Point p : points) {
            g2d.fill(new Ellipse2D.Double(p.x - 5.5, p.y - 5.5, 11, 11));
        }

        // Draw zoom rectangle if active
        if (zoomRect != null) {
            g2d.setColor(Color.GRAY);
            g2d.setStroke(new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[]{5}, 0));
            g2d.draw(zoomRect);
        }

        // Show coordinates if hovering over a point
        if (hoverPoint != null) {
            g2d.setColor(Color.BLACK);
            double x = minX + hoverPoint.x * (maxX - minX) / WIDTH;
            double y = maxY - hoverPoint.y * (maxY - minY) / HEIGHT;
            g2d.drawString(String.format("(%.2f, %.2f)", x, y), hoverPoint.x + 10, hoverPoint.y - 10);
        }
    }

    private void drawAxes(Graphics2D g2d) {
        g2d.setColor(Color.BLACK);

        // Draw axes lines
        g2d.drawLine(0, HEIGHT / 2, WIDTH, HEIGHT / 2);
        g2d.drawLine(WIDTH / 2, 0, WIDTH / 2, HEIGHT);

        // Draw ticks on axes
        double xStep = (maxX - minX) / 100;
        double yStep = (maxY - minY) / 100;

        for (int i = 0; i <= 100; i++) {
            int xTick = (int) (i * WIDTH / 100.0);
            int yTick = (int) (i * HEIGHT / 100.0);

            if (i % 5 == 0) {
                g2d.drawLine(xTick, HEIGHT / 2 - 10, xTick, HEIGHT / 2 + 10);
                g2d.drawLine(WIDTH / 2 - 10, yTick, WIDTH / 2 + 10, yTick);
            } else {
                g2d.drawLine(xTick, HEIGHT / 2 - 5, xTick, HEIGHT / 2 + 5);
                g2d.drawLine(WIDTH / 2 - 5, yTick, WIDTH / 2 + 5, yTick);
            }
        }
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("Graph Display with Zoom and Hover");
        GraphDisplay graphDisplay = new GraphDisplay();

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.add(graphDisplay);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}

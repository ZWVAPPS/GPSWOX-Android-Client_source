package com.gpswox.android.utils;
//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.Rect;
import android.view.MotionEvent;

import org.osmdroid.DefaultResourceProxyImpl;
import org.osmdroid.ResourceProxy;
import org.osmdroid.bonuspack.overlays.OverlayWithIW;
import org.osmdroid.bonuspack.overlays.Polyline;
import org.osmdroid.util.BoundingBoxE6;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.util.GeometryMath;
import org.osmdroid.views.MapView;
import org.osmdroid.views.Projection;

import java.util.ArrayList;
import java.util.List;

public class FilledPolyline extends OverlayWithIW {
    private int[][] mOriginalPoints;
    protected boolean mGeodesic;
    private final Path mPath;
    protected Paint mPaint, mFillPaint;
    private ArrayList<Point> mPoints;
    private int mPointsPrecomputed;
    private final Rect mLineBounds;
    private final Point mTempPoint1;
    private final Point mTempPoint2;
    protected Polyline.OnClickListener mOnClickListener;

    public FilledPolyline(Context ctx) {
        this((ResourceProxy)(new DefaultResourceProxyImpl(ctx)));
    }

    public FilledPolyline(ResourceProxy resourceProxy) {
        super(resourceProxy);
        this.mPath = new Path();
        this.mPaint = new Paint();
        this.mFillPaint = new Paint();
        this.mLineBounds = new Rect();
        this.mTempPoint1 = new Point();
        this.mTempPoint2 = new Point();
        this.mPaint.setColor(-16777216);
        this.mPaint.setStrokeWidth(10.0F);
        this.mPaint.setStyle(Paint.Style.STROKE);
        this.mPaint.setAntiAlias(true);
        this.mFillPaint.setStyle(Paint.Style.FILL);
        this.clearPath();
        this.mOriginalPoints = new int[0][2];
        this.mGeodesic = false;
    }

    protected void clearPath() {
        this.mPoints = new ArrayList();
        this.mPointsPrecomputed = 0;
    }

    protected void addPoint(GeoPoint aPoint) {
        this.addPoint(aPoint.getLatitudeE6(), aPoint.getLongitudeE6());
    }

    protected void addPoint(int aLatitudeE6, int aLongitudeE6) {
        this.mPoints.add(new Point(aLatitudeE6, aLongitudeE6));
    }

    public List<GeoPoint> getPoints() {
        ArrayList result = new ArrayList(this.mOriginalPoints.length);

        for(int i = 0; i < this.mOriginalPoints.length; ++i) {
            GeoPoint gp = new GeoPoint(this.mOriginalPoints[i][0], this.mOriginalPoints[i][1]);
            result.add(gp);
        }

        return result;
    }

    public int getNumberOfPoints() {
        return this.mOriginalPoints.length;
    }

    public int getColor() {
        return this.mPaint.getColor();
    }

    public float getWidth() {
        return this.mPaint.getStrokeWidth();
    }

    public Paint getPaint() {
        return this.mPaint;
    }

    public boolean isVisible() {
        return this.isEnabled();
    }

    public boolean isGeodesic() {
        return this.mGeodesic;
    }

    public void setColor(int color) {
        this.mPaint.setColor(color);
    }
    public void setFillColor(int color) {
        this.mFillPaint.setColor(color);
    }

    public void setWidth(float width) {
        this.mPaint.setStrokeWidth(width);
    }

    public void setVisible(boolean visible) {
        this.setEnabled(visible);
    }

    public void setOnClickListener(Polyline.OnClickListener listener) {
        this.mOnClickListener = listener;
    }

    protected void addGreatCircle(GeoPoint startPoint, GeoPoint endPoint, int numberOfPoints) {
        double lat1 = startPoint.getLatitude() * 0.01745329238474369D;
        double lon1 = startPoint.getLongitude() * 0.01745329238474369D;
        double lat2 = endPoint.getLatitude() * 0.01745329238474369D;
        double lon2 = endPoint.getLongitude() * 0.01745329238474369D;
        double d = 2.0D * Math.asin(Math.sqrt(Math.pow(Math.sin((lat1 - lat2) / 2.0D), 2.0D) + Math.cos(lat1) * Math.cos(lat2) * Math.pow(Math.sin((lon1 - lon2) / 2.0D), 2.0D)));
        double bearing = Math.atan2(Math.sin(lon1 - lon2) * Math.cos(lat2), Math.cos(lat1) * Math.sin(lat2) - Math.sin(lat1) * Math.cos(lat2) * Math.cos(lon1 - lon2)) / -0.01745329238474369D;
        if(bearing < 0.0D) {
            double var10000 = 360.0D + bearing;
        }

        for(int i = 1; i <= numberOfPoints; ++i) {
            double f = 1.0D * (double)i / (double)(numberOfPoints + 1);
            double A = Math.sin((1.0D - f) * d) / Math.sin(d);
            double B = Math.sin(f * d) / Math.sin(d);
            double x = A * Math.cos(lat1) * Math.cos(lon1) + B * Math.cos(lat2) * Math.cos(lon2);
            double y = A * Math.cos(lat1) * Math.sin(lon1) + B * Math.cos(lat2) * Math.sin(lon2);
            double z = A * Math.sin(lat1) + B * Math.sin(lat2);
            double latN = Math.atan2(z, Math.sqrt(Math.pow(x, 2.0D) + Math.pow(y, 2.0D)));
            double lonN = Math.atan2(y, x);
            this.addPoint((int)(latN * 57.295780181884766D * 1000000.0D), (int)(lonN * 57.295780181884766D * 1000000.0D));
        }

    }

    public void setPoints(List<GeoPoint> points) {
        this.clearPath();
        int size = points.size();
        this.mOriginalPoints = new int[size][2];

        for(int i = 0; i < size; ++i) {
            GeoPoint p = (GeoPoint)points.get(i);
            this.mOriginalPoints[i][0] = p.getLatitudeE6();
            this.mOriginalPoints[i][1] = p.getLongitudeE6();
            if(!this.mGeodesic) {
                this.addPoint(p);
            } else {
                if(i > 0) {
                    GeoPoint prev = (GeoPoint)points.get(i - 1);
                    int greatCircleLength = prev.distanceTo(p);
                    int numberOfPoints = greatCircleLength / 100000;
                    this.addGreatCircle(prev, p, numberOfPoints);
                }

                this.addPoint(p);
            }
        }

    }

    public void setGeodesic(boolean geodesic) {
        this.mGeodesic = geodesic;
    }

    protected void precomputePoints(Projection pj) {
        for(int size = this.mPoints.size(); this.mPointsPrecomputed < size; ++this.mPointsPrecomputed) {
            Point pt = (Point)this.mPoints.get(this.mPointsPrecomputed);
            pj.toProjectedPixels(pt.x, pt.y, pt);
        }

    }

    protected void draw(Canvas canvas, MapView mapView, boolean shadow) {
        if(!shadow) {
            int size = this.mPoints.size();
            if(size >= 2) {
                Projection pj = mapView.getProjection();
                this.precomputePoints(pj);
                Point screenPoint0 = null;
                BoundingBoxE6 boundingBox = pj.getBoundingBox();
                Point topLeft = pj.toProjectedPixels(boundingBox.getLatNorthE6(), boundingBox.getLonWestE6(), (Point)null);
                Point bottomRight = pj.toProjectedPixels(boundingBox.getLatSouthE6(), boundingBox.getLonEastE6(), (Point)null);
                Rect clipBounds = new Rect(topLeft.x, topLeft.y, bottomRight.x, bottomRight.y);
                if(mapView.getMapOrientation() != 0.0F) {
                    GeometryMath.getBoundingBoxForRotatatedRectangle(clipBounds, mapView.getMapOrientation(), clipBounds);
                }

                this.mPath.rewind();
                Point projectedPoint0 = (Point)this.mPoints.get(size - 1);
                this.mLineBounds.set(projectedPoint0.x, projectedPoint0.y, projectedPoint0.x, projectedPoint0.y);

                for(int i = size - 2; i >= 0; --i) {
                    Point projectedPoint1 = (Point)this.mPoints.get(i);
                    this.mLineBounds.union(projectedPoint1.x, projectedPoint1.y);
                    if(!Rect.intersects(clipBounds, this.mLineBounds)) {
                        projectedPoint0 = projectedPoint1;
                        screenPoint0 = null;
                    } else {
                        if(screenPoint0 == null) {
                            screenPoint0 = pj.toPixelsFromProjected(projectedPoint0, this.mTempPoint1);
                            this.mPath.moveTo((float)screenPoint0.x, (float)screenPoint0.y);
                        }

                        Point screenPoint1 = pj.toPixelsFromProjected(projectedPoint1, this.mTempPoint2);
                        if(Math.abs(screenPoint1.x - screenPoint0.x) + Math.abs(screenPoint1.y - screenPoint0.y) > 1) {
                            this.mPath.lineTo((float)screenPoint1.x, (float)screenPoint1.y);
                            projectedPoint0 = projectedPoint1;
                            screenPoint0.x = screenPoint1.x;
                            screenPoint0.y = screenPoint1.y;
                            this.mLineBounds.set(projectedPoint1.x, projectedPoint1.y, projectedPoint1.x, projectedPoint1.y);
                        }
                    }
                }

                canvas.drawPath(this.mPath, this.mFillPaint);
                canvas.drawPath(this.mPath, this.mPaint);
            }
        }
    }

    public boolean isCloseTo(GeoPoint point, double tolerance, MapView mapView) {
        Projection pj = mapView.getProjection();
        this.precomputePoints(pj);
        Point p = pj.toPixels(point, (Point)null);
        int i = 0;

        boolean found;
        for(found = false; i < this.mPointsPrecomputed - 1 && !found; ++i) {
            Point projectedPoint1 = (Point)this.mPoints.get(i);
            if(i == 0) {
                pj.toPixelsFromProjected(projectedPoint1, this.mTempPoint1);
            } else {
                this.mTempPoint1.set(this.mTempPoint2.x, this.mTempPoint2.y);
            }

            Point projectedPoint2 = (Point)this.mPoints.get(i + 1);
            pj.toPixelsFromProjected(projectedPoint2, this.mTempPoint2);
            found = this.linePointDist(this.mTempPoint1, this.mTempPoint2, p, true) <= tolerance;
        }

        return found;
    }

    private double dot(Point A, Point B, Point C) {
        double AB_X = (double)(B.x - A.x);
        double AB_Y = (double)(B.y - A.y);
        double BC_X = (double)(C.x - B.x);
        double BC_Y = (double)(C.y - B.y);
        double dot = AB_X * BC_X + AB_Y * BC_Y;
        return dot;
    }

    private double cross(Point A, Point B, Point C) {
        double AB_X = (double)(B.x - A.x);
        double AB_Y = (double)(B.y - A.y);
        double AC_X = (double)(C.x - A.x);
        double AC_Y = (double)(C.y - A.y);
        double cross = AB_X * AC_Y - AB_Y * AC_X;
        return cross;
    }

    private double distance(Point A, Point B) {
        double dX = (double)(A.x - B.x);
        double dY = (double)(A.y - B.y);
        return Math.sqrt(dX * dX + dY * dY);
    }

    private double linePointDist(Point A, Point B, Point C, boolean isSegment) {
        double dAB = this.distance(A, B);
        if(dAB == 0.0D) {
            return this.distance(A, C);
        } else {
            double dist = this.cross(A, B, C) / dAB;
            if(isSegment) {
                double dot1 = this.dot(A, B, C);
                if(dot1 > 0.0D) {
                    return this.distance(B, C);
                }

                double dot2 = this.dot(B, A, C);
                if(dot2 > 0.0D) {
                    return this.distance(A, C);
                }
            }

            return Math.abs(dist);
        }
    }

    public void showInfoWindow(GeoPoint position) {
        if(this.mInfoWindow != null) {
            this.mInfoWindow.open(this, position, 0, 0);
        }
    }

    public boolean onSingleTapConfirmed(MotionEvent event, MapView mapView) {
        /*Projection pj = mapView.getProjection();
        GeoPoint eventPos = (GeoPoint)pj.fromPixels((int)event.getX(), (int)event.getY());
        double tolerance = (double)this.mPaint.getStrokeWidth();
        boolean touched = this.isCloseTo(eventPos, tolerance, mapView);
        return touched?(this.mOnClickListener == null?this.onClickDefault(this, mapView, eventPos):this.mOnClickListener.onClick(this, mapView, eventPos)):touched;*/

        return false;
    }

    protected boolean onClickDefault(Polyline polyline, MapView mapView, GeoPoint eventPos) {
        polyline.showInfoWindow(eventPos);
        return true;
    }

    public interface OnClickListener {
        boolean onClick(Polyline var1, MapView var2, GeoPoint var3);
    }
}

package ch.hgdev.toposuite.calculation;

import java.util.Date;

import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;
import ch.hgdev.toposuite.App;
import ch.hgdev.toposuite.R;
import ch.hgdev.toposuite.SharedResources;
import ch.hgdev.toposuite.calculation.activities.linecircleintersection.LineCircleIntersectionActivity;
import ch.hgdev.toposuite.points.Point;
import ch.hgdev.toposuite.utils.DisplayUtils;
import ch.hgdev.toposuite.utils.Logger;
import ch.hgdev.toposuite.utils.MathUtils;

import com.google.common.base.Preconditions;

public class LineCircleIntersection extends Calculation {
    private static final String LINE_CIRCLE_INTERSECTION   = "Line-Circle intersection: ";

    private static final String LINE_POINT_ONE_NUMBER      = "line_point_one_number";
    private static final String LINE_POINT_TWO_NUMBER      = "line_point_two_number";
    private static final String LINE_DISPLACEMENT          = "line_displacement";
    private static final String LINE_GISEMENT              = "line_gisement";
    private static final String LINE_DISTANCE              = "line_distance";
    private static final String CIRCLE_POINT_CENTER_NUMBER = "circle_point_center_number";
    private static final String CIRCLE_RADIUS              = "circle_radius";

    private Point               p1L;
    private Point               p2L;
    private double              displacementL;
    private double              gisementL;
    private double              distanceL;

    private Point               centerC;
    private double              radiusC;

    /**
     * Point on the first intersection.
     */
    private Point               firstIntersection;
    /**
     * Point on the second intersection (if relevant).
     */
    private Point               secondIntersection;

    public LineCircleIntersection(long id, Date lastModification) {
        super(id,
                CalculationType.LINECIRCINTERSEC,
                App.getContext().getString(R.string.title_activity_line_circle_intersection),
                lastModification,
                true);
    }

    public LineCircleIntersection(Point _p1L, Point _p2L, double _displacementL, double _gisement,
            double _distance, Point _centerC, double _radiusC,
            boolean hasDAO) throws IllegalArgumentException {
        super(CalculationType.LINECIRCINTERSEC,
                App.getContext().getString(R.string.title_activity_line_circle_intersection),
                hasDAO);

        this.initAttributes(_p1L, _p2L, _displacementL, _gisement, _distance, _centerC, _radiusC);

        if (hasDAO) {
            SharedResources.getCalculationsHistory().add(0, this);
        }
    }

    public LineCircleIntersection(Point _p1L, Point _p2L, double _displacementL, double _distance,
            Point _centerC, double _radiusC) {
        this(_p1L, _p2L, _displacementL, 0.0, _distance, _centerC, _radiusC, false);
    }

    public LineCircleIntersection(Point _p1L, double _displacementL, double _gisement,
            double _distance,
            Point _centerC, double _radiusC) {
        this(_p1L, null, _displacementL, _gisement, _distance, _centerC, _radiusC, false);
    }

    public LineCircleIntersection(Point _p1L, Point _p2L, double _displacementL, Point _centerC,
            double _radiusC) {
        this(_p1L, _p2L, _displacementL, 0.0, _centerC, _radiusC);
    }

    public LineCircleIntersection() {
        super(CalculationType.LINECIRCINTERSEC,
                App.getContext().getString(R.string.title_activity_line_circle_intersection),
                true);

        this.p1L = new Point(0, 0.0, 0.0, 0.0, false, false);
        this.p2L = new Point(0, 0.0, 0.0, 0.0, false, false);
        this.displacementL = 0.0;
        this.gisementL = 0.0;
        this.distanceL = 0.0;

        this.centerC = new Point(0, 0.0, 0.0, 0.0, false, false);
        this.radiusC = 0.0;

        this.firstIntersection = new Point(0, 0.0, 0.0, 0.0, false, false);
        this.secondIntersection = new Point(0, 0.0, 0.0, 0.0, false, false);

        SharedResources.getCalculationsHistory().add(0, this);
    }

    /**
     * Initialize class attributes.
     * 
     * @param _p1L
     *            First point on the line. Must NOT be null.
     * @param _p2L
     *            Second point on the line. May be null.
     * @param _displacementL
     *            Displacement.
     * @param _gisement
     *            Gisement. Not used of _p2L is not null.
     * @param _centerC
     *            Center of the circle.
     * @param _radiusC
     *            Radius of the circle.
     * @throws IllegalArgumentException
     */
    public void initAttributes(Point _p1L, Point _p2L, double _displacementL, double _gisement,
            double _distance, Point _centerC, double _radiusC) throws IllegalArgumentException {
        Preconditions.checkNotNull(_p1L, "The first point must not be null");

        this.p1L = _p1L;
        if (_p2L == null) {
            this.p2L = new Point(
                    0,
                    MathUtils.pointLanceEast(_p1L.getEast(), _gisement, 100),
                    MathUtils.pointLanceNorth(_p1L.getNorth(), _gisement, 100.0),
                    0.0,
                    false);
        } else {
            this.p2L = _p2L;
        }
        this.displacementL = _displacementL;
        this.gisementL = _gisement;
        if (!MathUtils.isZero(_distance)) {
            this.distanceL = _distance;
            double gis = new Gisement(this.p1L, this.p2L, false).getGisement();
            this.p1L.setEast(
                    MathUtils.pointLanceEast(this.p1L.getEast(), gis, _distance));
            this.p1L.setNorth(
                    MathUtils.pointLanceNorth(this.p1L.getNorth(), gis, _distance));
            gis += 100;
            this.p2L.setEast(
                    MathUtils.pointLanceEast(this.p2L.getEast(), gis, 100));
            this.p2L.setNorth(
                    MathUtils.pointLanceNorth(this.p2L.getNorth(), gis, 100));
        }

        this.centerC = _centerC;
        this.radiusC = _radiusC;

        this.firstIntersection = new Point(0, 0.0, 0.0, 0.0, false, false);
        this.secondIntersection = new Point(0, 0.0, 0.0, 0.0, false, false);
    }

    @Override
    public void compute() {
        Point p1LClone = this.p1L.clone();
        Point p2LClone = this.p2L.clone();

        if (!MathUtils.isZero(this.displacementL)) {
            double displGis = new Gisement(p1LClone, p2LClone, false).getGisement();
            displGis += MathUtils.isNegative(this.displacementL) ? -100 : 100;
            p1LClone.setEast(MathUtils.pointLanceEast(
                    p1LClone.getEast(), displGis, Math.abs(this.displacementL)));
            p1LClone.setNorth(MathUtils.pointLanceNorth(
                    p1LClone.getNorth(), displGis, Math.abs(this.displacementL)));
            p2LClone.setEast(MathUtils.pointLanceEast(
                    p2LClone.getEast(), displGis, Math.abs(this.displacementL)));
            p2LClone.setNorth(MathUtils.pointLanceNorth(
                    p2LClone.getNorth(), displGis, Math.abs(this.displacementL)));
        }

        double alpha = new Gisement(p1LClone, p2LClone, false).getGisement() -
                new Gisement(p1LClone, this.centerC, false).getGisement();

        double minRadius = MathUtils.euclideanDistance(
                p1LClone, this.centerC) * Math.sin(MathUtils.gradToRad(alpha));
        double proj = minRadius / this.radiusC;
        double beta = 0.0;

        // check that the circle crosses the line
        if (MathUtils.isPositive((-proj * proj) + 1)) {
            beta = MathUtils.radToGrad(Math.atan(proj / Math.sqrt((-proj * proj) + 1)));
        } else {
            Log.w(Logger.TOPOSUITE_CALCULATION_IMPOSSIBLE,
                    LINE_CIRCLE_INTERSECTION
                            + "No line-circle crossing. The radius should be longer than "
                            + DisplayUtils.toString(minRadius)
                            + " (" + DisplayUtils.toString(this.radiusC) + " given).");
            this.setZeros();
            return;
        }

        double stPtIntersecGis1 = new Gisement(p1LClone, p2LClone, false).getGisement();
        double stPtIntersecGis2 = stPtIntersecGis1;
        double distAP1, distAP2;

        // center of the circle on first point of the line
        if (MathUtils.equals(this.centerC.getEast(), p1LClone.getEast())
                && MathUtils.equals(this.centerC.getNorth(), p1LClone.getNorth())) {
            distAP1 = this.radiusC;
            distAP2 = this.radiusC;
            stPtIntersecGis2 = stPtIntersecGis2 - 200;

            // center of the circle on the second point of the line
        } else if (MathUtils.equals(this.centerC.getEast(), p2LClone.getEast())
                && MathUtils.equals(this.centerC.getNorth(), p2LClone.getNorth())) {
            distAP1 = MathUtils.euclideanDistance(p1LClone, this.centerC) + this.radiusC;
            distAP2 = MathUtils.euclideanDistance(p2LClone, p1LClone) - this.radiusC;

            // center of the circle aligned with the two points of the line
        } else if (MathUtils.isZero(Math.sin(MathUtils.gradToRad(alpha)))) {
            double dist = MathUtils.euclideanDistance(p1LClone, this.centerC);
            distAP1 = dist + this.radiusC;
            distAP2 = dist - this.radiusC;

            // center of the circle elsewhere
        } else {
            distAP1 = (this.radiusC * Math.sin(MathUtils.gradToRad(200 - alpha - beta)))
                    / Math.sin(MathUtils.gradToRad(alpha));
            distAP2 = distAP1 + ((this.radiusC * Math.sin(MathUtils.gradToRad((2 * beta) - 200)))
                    / Math.sin(MathUtils.gradToRad(200 - beta)));
        }

        this.firstIntersection.setEast(
                MathUtils.pointLanceEast(p1LClone.getEast(), stPtIntersecGis1, distAP1));
        this.firstIntersection.setNorth(
                MathUtils.pointLanceNorth(p1LClone.getNorth(), stPtIntersecGis1, distAP1));
        this.secondIntersection.setEast(
                MathUtils.pointLanceEast(p1LClone.getEast(), stPtIntersecGis2, distAP2));
        this.secondIntersection.setNorth(
                MathUtils.pointLanceNorth(p1LClone.getNorth(), stPtIntersecGis2, distAP2));

        this.updateLastModification();
        this.notifyUpdate(this);
    }

    /**
     * Set resulting points coordinate to 0.0. This usually indicates that there
     * was an error.
     */
    private void setZeros() {
        this.firstIntersection.setEast(0.0);
        this.firstIntersection.setNorth(0.0);
        this.secondIntersection.setEast(0.0);
        this.secondIntersection.setNorth(0.0);
    }

    @Override
    public String exportToJSON() throws JSONException {
        JSONObject json = new JSONObject();

        json.put(LINE_POINT_ONE_NUMBER, this.p1L.getNumber());
        json.put(LINE_POINT_TWO_NUMBER, this.p2L.getNumber());
        json.put(LINE_DISPLACEMENT, this.displacementL);
        json.put(LINE_GISEMENT, this.gisementL);
        json.put(LINE_DISTANCE, this.distanceL);
        json.put(CIRCLE_POINT_CENTER_NUMBER, this.centerC.getNumber());
        json.put(CIRCLE_RADIUS, this.radiusC);

        return json.toString();
    }

    @Override
    public void importFromJSON(String jsonInputArgs) throws JSONException {
        JSONObject json = new JSONObject(jsonInputArgs);

        Point p1L = SharedResources.getSetOfPoints().find(
                json.getInt(LINE_POINT_ONE_NUMBER));
        Point p2L = SharedResources.getSetOfPoints().find(
                json.getInt(LINE_POINT_TWO_NUMBER));
        double displacement = json.getDouble(LINE_DISPLACEMENT);
        double gisement = json.getDouble(LINE_GISEMENT);
        double distance = json.getDouble(LINE_DISTANCE);

        Point centerC = SharedResources.getSetOfPoints().find(
                json.getInt(CIRCLE_POINT_CENTER_NUMBER));
        double radiusC = json.getDouble(CIRCLE_RADIUS);

        this.initAttributes(p1L, p2L, displacement, gisement, distance, centerC, radiusC);
    }

    @Override
    public Class<?> getActivityClass() {
        return LineCircleIntersectionActivity.class;
    }

    public Point getP1L() {
        return this.p1L;
    }

    public Point getP2L() {
        return this.p2L;
    }

    public double getDisplacementL() {
        return this.displacementL;
    }

    public double getGisementL() {
        return this.gisementL;
    }

    public double getDistanceL() {
        return this.distanceL;
    }

    public Point getCenterC() {
        return this.centerC;
    }

    public double getRadiusC() {
        return this.radiusC;
    }

    public Point getFirstIntersection() {
        return this.firstIntersection;
    }

    public Point getSecondIntersection() {
        return this.secondIntersection;
    }
}
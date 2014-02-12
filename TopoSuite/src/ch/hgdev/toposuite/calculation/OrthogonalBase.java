package ch.hgdev.toposuite.calculation;

import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;
import ch.hgdev.toposuite.SharedResources;
import ch.hgdev.toposuite.points.Point;
import ch.hgdev.toposuite.utils.Logger;
import ch.hgdev.toposuite.utils.MathUtils;

public class OrthogonalBase {
    public static final String ORIGIN            = "origin";
    public static final String EXTREMITY         = "extremity";
    public static final String MEASURED_DISTANCE = "measured_distance";

    private Point              origin;
    private Point              extremity;
    private double             calculatedDistance;
    private double             measuredDistance;
    private double             scaleFactor;

    public OrthogonalBase(Point _origin, Point _extremity, double _measuredDistance) {
        this.origin = _origin;
        this.extremity = _extremity;
        this.measuredDistance = _measuredDistance;

        this.updateCalcDistAndScaleFactor();
    }

    public OrthogonalBase(Point _origin, Point _extremity) {
        this(_origin, _extremity, 0.0);
    }

    public OrthogonalBase() {
        // empty constructor
    }

    private void updateCalcDistAndScaleFactor() {
        if ((this.origin == null) || (this.extremity == null)) {
            return;
        }

        this.calculatedDistance = MathUtils.euclideanDistance(
                this.origin, this.extremity);

        if (!MathUtils.isZero(this.measuredDistance)) {
            this.scaleFactor = this.calculatedDistance / this.measuredDistance;
        } else {
            this.scaleFactor = 0.0;
        }
    }

    public JSONObject toJSONObject() {
        JSONObject jo = new JSONObject();

        try {
            if (this.origin != null) {
                jo.put(OrthogonalBase.ORIGIN, this.origin.getNumber());
            }

            if (this.extremity != null) {
                jo.put(OrthogonalBase.EXTREMITY, this.extremity.getNumber());
            }

            jo.put(OrthogonalBase.MEASURED_DISTANCE, this.measuredDistance);
        } catch (JSONException e) {
            Log.e(Logger.TOPOSUITE_PARSE_ERROR, e.getMessage());
        }

        return jo;
    }

    public static OrthogonalBase getOrthogonalBaseFromJSON(String json) {
        OrthogonalBase ob = null;

        try {
            JSONObject jo = new JSONObject(json);

            Point origin = SharedResources.getSetOfPoints().find(
                    jo.getInt(OrthogonalBase.ORIGIN));
            Point extremity = SharedResources.getSetOfPoints().find(
                    jo.getInt(OrthogonalBase.EXTREMITY));
            double measureDist = jo.getDouble(OrthogonalBase.MEASURED_DISTANCE);

            ob = new OrthogonalBase(origin, extremity, measureDist);
        } catch (JSONException e) {
            Log.e(Logger.TOPOSUITE_PARSE_ERROR, e.getMessage());
        }

        return ob;
    }

    public Point getOrigin() {
        return this.origin;
    }

    public void setOrigin(Point _origin) {
        this.origin = _origin;
        this.updateCalcDistAndScaleFactor();
    }

    public Point getExtemity() {
        return this.extremity;
    }

    public void setExtemity(Point _extemity) {
        this.extremity = _extemity;
        this.updateCalcDistAndScaleFactor();
    }

    public double getCalculatedDistance() {
        return this.calculatedDistance;
    }

    public double getMeasuredDistance() {
        return this.measuredDistance;
    }

    public void setMeasuredDistance(double measuredDistance) {
        this.measuredDistance = measuredDistance;
        this.updateCalcDistAndScaleFactor();
    }

    public double getScaleFactor() {
        return this.scaleFactor;
    }
}
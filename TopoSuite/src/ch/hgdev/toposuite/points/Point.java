package ch.hgdev.toposuite.points;

import java.util.ArrayList;

import android.content.Context;
import ch.hgdev.toposuite.R;
import ch.hgdev.toposuite.dao.PointsDataSource;
import ch.hgdev.toposuite.dao.interfaces.DAO;
import ch.hgdev.toposuite.dao.interfaces.DAOUpdater;
import ch.hgdev.toposuite.utils.DisplayUtils;
import ch.hgdev.toposuite.utils.MathUtils;

import com.google.common.base.Preconditions;

/**
 * A point is defined by a number, its distance to the east and the north and
 * its altitude.
 * 
 * @author HGdev
 * 
 */
public class Point implements DAOUpdater {

    private final int            number;
    private double               east;
    private double               north;
    private double               altitude;
    private final boolean        basePoint;

    /**
     * List of DAO linked.
     */
    private final ArrayList<DAO> daoList;

    /**
     * A point is characterized by its number, distance to the east and north
     * and its altitude.
     * 
     * @param number
     *            Point number.
     * @param east
     *            Point distance to the east.
     * @param north
     *            Point distance to the north.
     * @param altitude
     *            Point altitude.
     * @param basePoint
     *            Determine if this point is a base point. A base point is a
     *            point that has been added as is and NOT computed.
     */
    public Point(int number, double east, double north, double altitude, boolean basePoint) {
        Preconditions.checkArgument(number >= 0, "A point number must be a positive integer: %s",
                number);

        this.number = number;
        this.east = east;
        this.north = north;
        this.altitude = altitude;
        this.basePoint = basePoint;

        this.daoList = new ArrayList<DAO>();
        this.registerDAO(PointsDataSource.getInstance());
    }

    /**
     * See {@link Point#Point(int, double, double, double, boolean)}.
     */
    public Point(int number, double east, double north, double altitude, boolean basePoint,
            boolean hasDAO) {
        this.number = number;
        this.east = east;
        this.north = north;
        this.altitude = altitude;
        this.basePoint = basePoint;

        this.daoList = new ArrayList<DAO>();

        if (hasDAO) {
            this.registerDAO(PointsDataSource.getInstance());
        }
    }

    @Override
    public boolean equals(Object o) {
        if (o == null) {
            return false;
        }
        if ((o.getClass() != this.getClass())) {
            return false;
        }

        Point point = (Point) o;
        if (this.getNumber() == point.getNumber()) {
            if (!MathUtils.equals(this.getEast(), point.getEast())) {
                return false;
            }
            if (!MathUtils.equals(this.getNorth(), point.getNorth())) {
                return false;
            }
            if (!MathUtils.equals(this.getAltitude(), point.getAltitude())) {
                return false;
            }
            return true;
        }
        return false;
    }

    public int getNumber() {
        return this.number;
    }

    public double getEast() {
        return this.east;
    }

    public void setEast(double _east) {
        this.east = _east;
        this.notifyUpdate(this);
    }

    public double getNorth() {
        return this.north;
    }

    public void setNorth(double _north) {
        this.north = _north;
        this.notifyUpdate(this);
    }

    public double getAltitude() {
        return this.altitude;
    }

    public void setAltitude(double _altitude) {
        this.altitude = _altitude;
        this.notifyUpdate(this);
    }

    public boolean isBasePoint() {
        return this.basePoint;
    }

    public String getBasePointAsString(Context context) {
        return this.basePoint ? context.getString(R.string.point_provided) : context
                .getString(R.string.point_computed);
    }

    @Override
    public String toString() {
        // the 0 number is used to put an empty item into the spinner
        if (this.number == 0) {
            return "";
        }
        return DisplayUtils.toString(this.number);
    }

    @Override
    public void registerDAO(DAO dao) {
        this.daoList.add(dao);
    }

    @Override
    public void removeDAO(DAO dao) {
        this.daoList.remove(dao);
    }

    @Override
    public void notifyUpdate(Object obj) {
        for (DAO dao : this.daoList) {
            dao.update(obj);
        }
    }
}
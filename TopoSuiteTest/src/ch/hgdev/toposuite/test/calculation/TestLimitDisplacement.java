package ch.hgdev.toposuite.test.calculation;

import java.text.DecimalFormat;

import junit.framework.Assert;
import junit.framework.TestCase;
import ch.hgdev.toposuite.calculation.LimitDisplacement;
import ch.hgdev.toposuite.points.Point;

public class TestLimitDisplacement extends TestCase {
    private DecimalFormat df4;

    @Override
    protected void setUp() throws Exception {
        this.df4 = new DecimalFormat("#.####");
        super.setUp();
    }

    public void testLimitDisplacement1() {
        Point ptA = new Point(1, 96321.1527, 71470.5391, 0.0, true, false);
        Point ptB = new Point(2, 96331.2818, 71467.6509, 0.0, true, false);
        Point ptC = new Point(3, 96334.9793, 71477.2001, 0.0, true, false);
        Point ptD = new Point(4, 96328.0009, 71480.5567, 0.0, true, false);
        double surface = 25.0;

        LimitDisplacement ld = new LimitDisplacement(ptA, ptB, ptC, ptD,
                surface, 5, 6, false);
        ld.compute();

        Assert.assertEquals("96320.0074",
                this.df4.format(ld.getNewPointX().getEast()));
        Assert.assertEquals("71468.8637",
                this.df4.format(ld.getNewPointX().getNorth()));

        Assert.assertEquals("96323.2745",
                this.df4.format(ld.getNewPointY().getEast()));
        Assert.assertEquals("71469.9341",
                this.df4.format(ld.getNewPointY().getNorth()));
    }

    public void testLimitDisplacement2() {
        Assert.assertTrue(true);
    }
}
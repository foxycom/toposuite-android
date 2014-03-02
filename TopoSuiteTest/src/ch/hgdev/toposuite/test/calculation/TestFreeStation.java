package ch.hgdev.toposuite.test.calculation;

import java.math.RoundingMode;
import java.text.DecimalFormat;

import junit.framework.Assert;
import junit.framework.TestCase;
import ch.hgdev.toposuite.calculation.FreeStation;
import ch.hgdev.toposuite.calculation.Measure;
import ch.hgdev.toposuite.points.Point;

public class TestFreeStation extends TestCase {
    private DecimalFormat df3;
    private DecimalFormat df2;
    private DecimalFormat df1;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        this.df3 = new DecimalFormat("#.###");
        this.df3.setRoundingMode(RoundingMode.HALF_UP);

        this.df2 = new DecimalFormat("#.##");
        this.df2.setRoundingMode(RoundingMode.HALF_UP);

        this.df1 = new DecimalFormat("#.#");
        this.df1.setRoundingMode(RoundingMode.HALF_UP);
    }

    public void testFreeStation1() {
        Point p1 = new Point(1, 542430.11, 151989.66, 0.0, true, false);
        Point p2 = new Point(2, 542610.79, 151979.94, 0.0, true, false);
        Point p3 = new Point(3, 542624.36, 151873.24, 0.0, true, false);
        Point p4 = new Point(4, 542495.94, 151847.05, 0.0, true, false);

        Measure m1 = new Measure(p1, 271.234, 100, 162.154);
        Measure m2 = new Measure(p2, 356.627, 100, 125.149);
        Measure m3 = new Measure(p3, 21.493, 100, 80.431);
        Measure m4 = new Measure(p4, 188.014, 100, 55.128);

        FreeStation fs = new FreeStation(42, false);
        fs.getMeasures().add(m1);
        fs.getMeasures().add(m2);
        fs.getMeasures().add(m3);
        fs.getMeasures().add(m4);
        fs.compute();

        Assert.assertEquals("542543.93", this.df2.format(
                fs.getStationResult().getEast()));
        Assert.assertEquals("151874.16", this.df2.format(
                fs.getStationResult().getNorth()));

        Assert.assertEquals("2.6", this.df1.format(fs.getResults().get(0).getvE()));
        Assert.assertEquals("0.9", this.df1.format(fs.getResults().get(0).getvN()));
        Assert.assertEquals("2.8", this.df1.format(fs.getResults().get(0).getfS()));

        Assert.assertEquals("-1.8", this.df1.format(fs.getResults().get(1).getvE()));
        Assert.assertEquals("1.7", this.df1.format(fs.getResults().get(1).getvN()));
        Assert.assertEquals("2.5", this.df1.format(fs.getResults().get(1).getfS()));

        Assert.assertEquals("-0.5", this.df1.format(fs.getResults().get(2).getvE()));
        Assert.assertEquals("-1.4", this.df1.format(fs.getResults().get(2).getvN()));
        Assert.assertEquals("1.4", this.df1.format(fs.getResults().get(2).getfS()));

        Assert.assertEquals("-0.3", this.df1.format(fs.getResults().get(3).getvE()));
        Assert.assertEquals("-1.3", this.df1.format(fs.getResults().get(3).getvN()));
        Assert.assertEquals("1.3", this.df1.format(fs.getResults().get(3).getfS()));
    }

    public void testFreeStation2() {
        Point p6 = new Point(6, 622.475, 210.990, 100.400, true, false);
        Point p7 = new Point(7, 636.236, 145.773, 99.964, true, false);
        Point p8 = new Point(8, 635.417, 177.289, 99.144, true, false);
        Point p9 = new Point(9, 595.012, 210.991, 100.068, true, false);
        Point p10 = new Point(10, 598.055, 218.982, 100.189, true, false);

        Measure m1 = new Measure(p6, 10.562, 99.124, 25.030, 1.570);
        Measure m2 = new Measure(p7, 102.070, 100.068, 65.200, 1.620);
        Measure m3 = new Measure(p8, 75.852, 101.162, 42.070, 1.740);
        Measure m4 = new Measure(p9, 312.411, 99.724, 12.070, 1.600);
        Measure m5 = new Measure(p10, 333.020, 98.180, 19.080, 2.000);

        FreeStation fs = new FreeStation(42, 1.6, false);
        fs.getMeasures().add(m1);
        fs.getMeasures().add(m2);
        fs.getMeasures().add(m3);
        fs.getMeasures().add(m4);
        fs.getMeasures().add(m5);
        fs.compute();

        Assert.assertEquals("600.009", this.df3.format(
                fs.getStationResult().getEast()));
        Assert.assertEquals("199.995", this.df3.format(
                fs.getStationResult().getNorth()));
        Assert.assertEquals("100.026", this.df3.format(
                fs.getStationResult().getAltitude()));

        Assert.assertEquals("0.03", this.df2.format(fs.getResults().get(0).getvA()));
        Assert.assertEquals("-2.76", this.df2.format(fs.getResults().get(1).getvA()));
        Assert.assertEquals("-2.59", this.df2.format(fs.getResults().get(2).getvA()));
        Assert.assertEquals("1.01", this.df2.format(fs.getResults().get(3).getvA()));
        Assert.assertEquals("-1.78", this.df2.format(fs.getResults().get(4).getvA()));

        Assert.assertEquals("0.1", this.df1.format(fs.getsE()));
        Assert.assertEquals("0.1", this.df1.format(fs.getsN()));
        Assert.assertEquals("0.7", this.df1.format(fs.getsA()));

        Assert.assertEquals("60.443", this.df3.format(fs.getUnknownOrientation()));
    }
}
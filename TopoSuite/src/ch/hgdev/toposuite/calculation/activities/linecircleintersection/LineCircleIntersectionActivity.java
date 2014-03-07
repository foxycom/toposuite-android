package ch.hgdev.toposuite.calculation.activities.linecircleintersection;

import java.util.ArrayList;
import java.util.List;

import android.os.Bundle;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import ch.hgdev.toposuite.App;
import ch.hgdev.toposuite.R;
import ch.hgdev.toposuite.SharedResources;
import ch.hgdev.toposuite.TopoSuiteActivity;
import ch.hgdev.toposuite.calculation.LineCircleIntersection;
import ch.hgdev.toposuite.calculation.activities.MergePointsDialog;
import ch.hgdev.toposuite.dao.PointsDataSource;
import ch.hgdev.toposuite.history.HistoryActivity;
import ch.hgdev.toposuite.points.Point;
import ch.hgdev.toposuite.utils.DisplayUtils;
import ch.hgdev.toposuite.utils.MathUtils;

public class LineCircleIntersectionActivity extends TopoSuiteActivity implements
        MergePointsDialog.MergePointsDialogListener {

    // line
    private Spinner                             point1Spinner;
    private Spinner                             point2Spinner;
    private TextView                            point1TextView;
    private TextView                            point2TextView;
    private TextView                            distP1TexView;
    private EditText                            gisementEditText;
    private EditText                            displacementEditText;
    private EditText                            distP1EditText;
    private LinearLayout                        point2SpinnerLayout;
    private LinearLayout                        point2Layout;
    private LinearLayout                        gisementLayout;
    private RadioButton                         modeGisementRadio;
    private CheckBox                            perpendicularCheckBox;
    private int                                 point1SelectedPosition;
    private int                                 point2SelectedPosition;
    private boolean                             isLinePerpendicular;
    private LineCircleIntersectionActivity.Mode mode;

    // circle
    private Spinner                             centerCSpinner;
    private int                                 centerCSelectedPosition;
    private Point                               centerCPoint;
    private TextView                            centerCTextView;
    private double                              radiusC;
    private EditText                            radiusCEditText;
    private Spinner                             byPointSpinner;
    private int                                 byPointSelectedPosition;
    private Point                               byPoint;
    private TextView                            byPointTextView;

    // results
    private TextView                            intersectionOneTextView;
    private EditText                            intersectionOneEditText;
    private Point                               intersectionOne;
    private TextView                            intersectionTwoTextView;
    private EditText                            intersectionTwoEditText;
    private Point                               intersectionTwo;

    private ArrayAdapter<Point>                 adapter;
    private LineCircleIntersection              lineCircleIntersection;

    /**
     * Position of the calculation in the calculations list. Only used when open
     * from the history.
     */
    private int                                 position;

    private enum Mode {
        LINE,
        GISEMENT;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_line_circle_intersection);

        this.isLinePerpendicular = false;

        this.mode = LineCircleIntersectionActivity.Mode.LINE;

        this.point1SelectedPosition = 0;
        this.point2SelectedPosition = 0;

        this.position = -1;

        this.mapViews();
        this.initViews();
    }

    @Override
    public void onResume() {
        super.onResume();

        List<Point> points = new ArrayList<Point>();
        points.add(new Point(0, 0.0, 0.0, 0.0, false, false));
        points.addAll(SharedResources.getSetOfPoints());

        this.adapter = new ArrayAdapter<Point>(this, R.layout.spinner_list_item, points);
        this.point1Spinner.setAdapter(this.adapter);
        this.point2Spinner.setAdapter(this.adapter);
        this.centerCSpinner.setAdapter(this.adapter);
        this.byPointSpinner.setAdapter(this.adapter);

        Bundle bundle = this.getIntent().getExtras();
        if ((bundle != null)) {
            this.position = bundle.getInt(HistoryActivity.CALCULATION_POSITION);
            this.lineCircleIntersection = (LineCircleIntersection) SharedResources
                    .getCalculationsHistory().get(this.position);

            this.point1SelectedPosition = this.adapter.getPosition(
                    this.lineCircleIntersection.getP1L());
            this.point2SelectedPosition = this.adapter.getPosition(
                    this.lineCircleIntersection.getP2L());
            double distance = this.lineCircleIntersection.getDistanceL();
            if (MathUtils.isPositive(distance)) {
                this.isLinePerpendicular = true;
                this.distP1EditText.setText(DisplayUtils.toString(distance));
            } else {
                this.isLinePerpendicular = false;
                this.distP1EditText.setText("");
            }
            this.distP1TexView.setEnabled(this.isLinePerpendicular);
            this.distP1EditText.setEnabled(this.isLinePerpendicular);

            double displacement = this.lineCircleIntersection.getDisplacementL();
            if (!MathUtils.isZero(displacement)) {
                this.displacementEditText.setText(
                        DisplayUtils.toString(displacement));
            }
            double gisement = this.lineCircleIntersection.getGisementL();
            if (MathUtils.isPositive(gisement)) {
                this.modeGisementRadio.setChecked(true);
                this.setModeGisement();
                this.gisementEditText.setText(
                        DisplayUtils.toString(gisement));
            } else {
                this.modeGisementRadio.setChecked(false);
                this.setModeLine();
                this.gisementEditText.setText("");
            }

            this.centerCSelectedPosition = this.adapter.getPosition(
                    this.lineCircleIntersection.getCenterC());
            this.radiusCEditText.setText(
                    DisplayUtils.toString(this.lineCircleIntersection.getRadiusC()));
        }

        this.point1Spinner.setSelection(this.point1SelectedPosition);
        this.point2Spinner.setSelection(this.point2SelectedPosition);
        this.modeGisementRadio.callOnClick();

        this.centerCSpinner.setSelection(this.centerCSelectedPosition);
        this.byPointSpinner.setSelection(this.byPointSelectedPosition);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        this.getMenuInflater().inflate(R.menu.line_circle_intersection, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    protected String getActivityTitle() {
        return this.getString(R.string.title_activity_line_circle_intersection);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
        case R.id.run_calculation_button:
            if (this.checkInputs()) {
                this.runCalculations();
                this.updateResults();
            } else {
                Toast errorToast = Toast.makeText(this, this.getText(R.string.error_fill_data),
                        Toast.LENGTH_SHORT);
                errorToast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
                errorToast.show();
            }
            return true;
        case R.id.save_points:
            if ((this.intersectionOne == null) || (this.intersectionTwo == null)) {
                Toast.makeText(this, R.string.error_no_points_to_save,
                        Toast.LENGTH_SHORT).show();
                return true;
            }

            if ((this.intersectionOneEditText.length() < 1)
                    && (this.intersectionTwoEditText.length() < 1)) {
                Toast.makeText(this, R.string.error_no_points_saved,
                        Toast.LENGTH_SHORT).show();
                return true;
            }

            // save first point
            if (this.intersectionOneEditText.length() > 0) {
                this.intersectionOne.setNumber(
                        Integer.parseInt(this.intersectionOneEditText.getText().toString()));
                if (SharedResources.getSetOfPoints().find(
                        this.intersectionOne.getNumber()) == null) {
                    SharedResources.getSetOfPoints().add(this.intersectionOne);
                    this.intersectionOne.registerDAO(PointsDataSource.getInstance());

                    Toast.makeText(this, R.string.point_add_success, Toast.LENGTH_LONG)
                            .show();
                } else {
                    // this point already exists
                    MergePointsDialog dialog = new MergePointsDialog();

                    Bundle args = new Bundle();
                    args.putInt(
                            MergePointsDialog.POINT_NUMBER,
                            this.intersectionOne.getNumber());

                    args.putDouble(MergePointsDialog.NEW_EAST,
                            this.intersectionOne.getEast());
                    args.putDouble(MergePointsDialog.NEW_NORTH,
                            this.intersectionOne.getNorth());
                    args.putDouble(MergePointsDialog.NEW_ALTITUDE,
                            this.intersectionOne.getAltitude());

                    dialog.setArguments(args);
                    dialog.show(this.getFragmentManager(), "MergePointsDialogFragment");
                }
            } else {
                Toast.makeText(this, R.string.point_one_not_saved,
                        Toast.LENGTH_SHORT).show();
            }

            // save second point
            if (this.intersectionTwoEditText.length() > 0) {
                this.intersectionTwo.setNumber(
                        Integer.parseInt(this.intersectionTwoEditText.getText().toString()));

                if (SharedResources.getSetOfPoints().find(
                        this.intersectionTwo.getNumber()) == null) {
                    SharedResources.getSetOfPoints().add(this.intersectionTwo);
                    this.intersectionTwo.registerDAO(PointsDataSource.getInstance());

                    Toast.makeText(this, R.string.point_add_success, Toast.LENGTH_LONG)
                            .show();
                } else {
                    // this point already exists
                    MergePointsDialog dialog = new MergePointsDialog();

                    Bundle args = new Bundle();
                    args.putInt(
                            MergePointsDialog.POINT_NUMBER,
                            this.intersectionTwo.getNumber());

                    args.putDouble(MergePointsDialog.NEW_EAST,
                            this.intersectionTwo.getEast());
                    args.putDouble(MergePointsDialog.NEW_NORTH,
                            this.intersectionTwo.getNorth());
                    args.putDouble(MergePointsDialog.NEW_ALTITUDE,
                            this.intersectionTwo.getAltitude());

                    dialog.setArguments(args);
                    dialog.show(this.getFragmentManager(), "MergePointsDialogFragment");
                }
            } else {
                Toast.makeText(this, R.string.point_two_not_saved,
                        Toast.LENGTH_SHORT).show();
            }
            return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Map views to their respective attributes.
     */
    private void mapViews() {
        // line
        this.point1TextView = (TextView) this.findViewById(R.id.point_1);
        this.point2TextView = (TextView) this.findViewById(R.id.point_2);
        this.distP1TexView = (TextView) this.findViewById(R.id.dist_p1_label);
        this.perpendicularCheckBox = (CheckBox) this.findViewById(R.id.is_l_perpendicular);
        this.gisementEditText = (EditText) this.findViewById(R.id.gisement);
        this.displacementEditText = (EditText) this.findViewById(R.id.displacement);
        this.distP1EditText = (EditText) this.findViewById(R.id.dist_p1);
        this.point2SpinnerLayout = (LinearLayout) this.findViewById(R.id.point2_spinner_layout);
        this.point2Layout = (LinearLayout) this.findViewById(R.id.point2_layout);
        this.gisementLayout = (LinearLayout) this.findViewById(R.id.gisement_layout);
        this.modeGisementRadio = (RadioButton) this.findViewById(R.id.mode_gisement);
        this.point1Spinner = (Spinner) this.findViewById(R.id.point_1_spinner);
        this.point2Spinner = (Spinner) this.findViewById(R.id.point_2_spinner);

        // circle
        this.centerCSpinner = (Spinner) this.findViewById(R.id.center_spinner);
        this.centerCTextView = (TextView) this.findViewById(R.id.center_textview);
        this.radiusCEditText = (EditText) this.findViewById(R.id.radius);
        this.byPointSpinner = (Spinner) this.findViewById(R.id.by_point_spinner);
        this.byPointTextView = (TextView) this.findViewById(R.id.by_point_textview);

        // results
        this.intersectionOneTextView = (TextView) this.findViewById(R.id.intersection_one);
        this.intersectionTwoTextView = (TextView) this.findViewById(R.id.intersection_two);
        this.intersectionOneEditText = (EditText) this.findViewById(R.id.intersection_one_number);
        this.intersectionTwoEditText = (EditText) this.findViewById(R.id.intersection_two_number);
    }

    /**
     * Init views.
     */
    private void initViews() {
        // line
        if (this.perpendicularCheckBox.isChecked()) {
            this.distP1EditText.setEnabled(true);
            this.distP1TexView.setEnabled(true);
        }
        this.distP1EditText.setInputType(App.INPUTTYPE_TYPE_NUMBER_COORDINATE);
        this.displacementEditText.setInputType(App.INPUTTYPE_TYPE_NUMBER_COORDINATE);
        this.gisementEditText.setInputType(App.INPUTTYPE_TYPE_NUMBER_COORDINATE);
        this.point1Spinner.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                LineCircleIntersectionActivity.this.point1SelectedPosition = pos;

                Point pt = (Point) LineCircleIntersectionActivity.this.point1Spinner
                        .getItemAtPosition(pos);
                if (pt.getNumber() > 0) {
                    LineCircleIntersectionActivity.this.point1TextView.setText(DisplayUtils
                            .formatPoint(LineCircleIntersectionActivity.this, pt));
                } else {
                    LineCircleIntersectionActivity.this.point1TextView.setText("");
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // actually nothing
            }
        });
        this.point2Spinner.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                LineCircleIntersectionActivity.this.point2SelectedPosition = pos;

                Point pt = (Point) LineCircleIntersectionActivity.this.point2Spinner
                        .getItemAtPosition(pos);
                if (pt.getNumber() > 0) {
                    LineCircleIntersectionActivity.this.point2TextView.setText(DisplayUtils
                            .formatPoint(LineCircleIntersectionActivity.this, pt));
                } else {
                    LineCircleIntersectionActivity.this.point2TextView.setText("");
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // actually nothing
            }
        });

        // circle
        this.centerCSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                LineCircleIntersectionActivity.this.centerCSelectedPosition = pos;

                LineCircleIntersectionActivity.this.centerCPoint = (Point) LineCircleIntersectionActivity.this.centerCSpinner
                        .getItemAtPosition(pos);
                if (LineCircleIntersectionActivity.this.centerCPoint.getNumber() > 0) {
                    LineCircleIntersectionActivity.this.centerCTextView.setText(DisplayUtils
                            .formatPoint(LineCircleIntersectionActivity.this,
                                    LineCircleIntersectionActivity.this.centerCPoint));
                } else {
                    LineCircleIntersectionActivity.this.centerCTextView.setText("");
                }
                LineCircleIntersectionActivity.this.fillRadiusC();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // actually nothing
            }
        });
        this.byPointSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                LineCircleIntersectionActivity.this.byPointSelectedPosition = pos;

                LineCircleIntersectionActivity.this.byPoint = (Point) LineCircleIntersectionActivity.this.byPointSpinner
                        .getItemAtPosition(pos);
                if (LineCircleIntersectionActivity.this.byPoint.getNumber() > 0) {
                    LineCircleIntersectionActivity.this.byPointTextView.setText(DisplayUtils
                            .formatPoint(LineCircleIntersectionActivity.this,
                                    LineCircleIntersectionActivity.this.byPoint));
                } else {
                    LineCircleIntersectionActivity.this.byPointTextView.setText("");
                }
                LineCircleIntersectionActivity.this.fillRadiusC();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // actually nothing
            }
        });
        this.radiusCEditText.setInputType(App.INPUTTYPE_TYPE_NUMBER_COORDINATE);

        // results
        this.intersectionOneEditText.setInputType(App.INPUTTYPE_TYPE_NUMBER_COORDINATE);
        this.intersectionTwoEditText.setInputType(App.INPUTTYPE_TYPE_NUMBER_COORDINATE);
    }

    /**
     * Fill radius for the circle with the distance between the center of the
     * first circle and the selected point from the spinner.
     */
    private void fillRadiusC() {
        if ((this.centerCSelectedPosition > 0) && (this.byPointSelectedPosition > 0)) {
            this.radiusCEditText.setText(DisplayUtils.toString(
                    MathUtils.euclideanDistance(this.centerCPoint, this.byPoint)));
            this.radiusCEditText.setEnabled(false);
        } else {
            this.radiusCEditText.setEnabled(true);
        }
    }

    /**
     * Handle click on the radio button.
     * 
     * @param view
     *            The radio button.
     */
    public void onRadioButtonClicked(View view) {
        boolean checked = ((RadioButton) view).isChecked();

        switch (view.getId()) {
        case R.id.mode_gisement:
            if (checked) {
                this.setModeGisement();
                break;
            }
        case R.id.mode_line:
            if (checked) {
                this.setModeLine();
                break;
            }
        }
    }

    /**
     * Set mode to gisement and adapt views accordingly.
     */
    private void setModeGisement() {
        this.point2SpinnerLayout.setVisibility(View.GONE);
        if (this.point2Layout != null) {
            this.point2Layout.setVisibility(View.GONE);
        }
        this.gisementLayout.setVisibility(View.VISIBLE);
        this.mode = LineCircleIntersectionActivity.Mode.GISEMENT;
    }

    /**
     * Set mode to line and adapt views accordingly.
     */
    private void setModeLine() {
        this.point2SpinnerLayout.setVisibility(View.VISIBLE);
        if (this.point2Layout != null) {
            this.point2Layout.setVisibility(View.VISIBLE);
        }
        this.gisementLayout.setVisibility(View.GONE);
        this.mode = LineCircleIntersectionActivity.Mode.LINE;
    }

    /**
     * Handle click on checkbox.
     * 
     * @param view
     */
    public void onCheckboxClicked(View view) {
        boolean checked = ((CheckBox) view).isChecked();

        this.distP1TexView.setEnabled(checked);
        this.distP1EditText.setEnabled(checked);
        this.isLinePerpendicular = checked;
        if (!checked) {
            this.distP1EditText.setText("");
        }
    }

    /**
     * Check that inputs are OK so the calculation can be run safely.
     * 
     * @return True if OK, false otherwise.
     */
    private boolean checkInputs() {
        if (this.point1SelectedPosition < 1) {
            return false;
        }
        if (this.mode == Mode.LINE) {
            if (this.point2SelectedPosition < 1) {
                return false;
            }
            if (this.point1SelectedPosition == this.point2SelectedPosition) {
                return false;
            }
        } else {
            if (this.gisementEditText.length() < 1) {
                return false;
            }
        }

        if (this.centerCSelectedPosition < 1) {
            return false;
        }
        if (this.radiusCEditText.length() < 1) {
            return false;
        }
        return true;
    }

    /**
     * Do the actual calculation and update the results.
     */
    private void runCalculations() {
        if (this.lineCircleIntersection == null) {
            this.lineCircleIntersection = new LineCircleIntersection();
        }

        Point p1 = this.adapter.getItem(this.point1SelectedPosition);
        Point p2 = null;
        double gisement = 0.0;
        if (this.mode == Mode.GISEMENT) {
            gisement = Double.parseDouble(
                    this.gisementEditText.getText().toString());
        } else {
            p2 = this.adapter.getItem(this.point2SelectedPosition);
        }
        double distP1 = 0.0;
        if ((this.distP1EditText.length() > 0) && this.isLinePerpendicular) {
            distP1 = Double.parseDouble(
                    this.displacementEditText.getText().toString());
        }
        double displacement = 0.0;
        if (this.displacementEditText.length() > 0) {
            displacement = Double.parseDouble(
                    this.displacementEditText.getText().toString());
        }

        this.centerCPoint = (Point) this.centerCSpinner
                .getItemAtPosition(this.centerCSelectedPosition);
        this.radiusC = Double.parseDouble(this.radiusCEditText.getText().toString());

        this.lineCircleIntersection.initAttributes(p1, p2, displacement, gisement, distP1,
                this.centerCPoint, this.radiusC);

        this.lineCircleIntersection.compute();

        this.intersectionOne = this.lineCircleIntersection.getFirstIntersection();
        this.intersectionTwo = this.lineCircleIntersection.getSecondIntersection();

    }

    /**
     * Update the results view.
     */
    private void updateResults() {
        this.intersectionOneTextView.setText(
                DisplayUtils.formatPoint(this, this.intersectionOne));
        this.intersectionTwoTextView.setText(
                DisplayUtils.formatPoint(this, this.intersectionTwo));
    }

    @Override
    public void onMergePointsDialogSuccess(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onMergePointsDialogError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

}

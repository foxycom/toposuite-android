package ch.hgdev.toposuite.calculation.activities.abriss;

import java.util.ArrayList;

import android.os.Bundle;
import android.view.Menu;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import ch.hgdev.toposuite.R;
import ch.hgdev.toposuite.SharedResources;
import ch.hgdev.toposuite.TopoSuiteActivity;
import ch.hgdev.toposuite.calculation.Abriss;
import ch.hgdev.toposuite.calculation.Measure;
import ch.hgdev.toposuite.points.Point;
import ch.hgdev.toposuite.utils.DisplayUtils;

public class AbrissResultsActivity extends TopoSuiteActivity {
    private ListView                    resultsListView;

    private TextView                    stationNumberTextView;
    private TextView                    unknOrientTextView;
    private TextView                    meanErrorTextView;

    private Abriss                      abriss;
    private ArrayAdapter<Abriss.Result> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_abriss_results);

        this.resultsListView = (ListView) this.findViewById(R.id.results_list);
        this.stationNumberTextView = (TextView) this.findViewById(R.id.station_number);
        this.unknOrientTextView = (TextView) this.findViewById(R.id.unknown_orientation);
        this.meanErrorTextView = (TextView) this.findViewById(R.id.mean_error);

        Bundle bundle = this.getIntent().getExtras();
        Point station = SharedResources.getSetOfPoints().find(
                bundle.getInt(AbrissActivity.STATION_NUMBER_LABEL));
        ArrayList<Measure> orientationsList = bundle.getParcelableArrayList(
                AbrissActivity.ORIENTATIONS_LABEL);

        this.abriss = new Abriss(station);
        this.abriss.getMeasures().addAll(orientationsList);
        this.abriss.compute();

        this.displayResults();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        this.getMenuInflater().inflate(R.menu.abriss_results, menu);
        return super.onCreateOptionsMenu(menu);
    }

    public void displayResults() {
        StringBuilder builder = new StringBuilder(
                this.abriss.getStation().getNumber());
        builder.append(" (");
        builder.append(DisplayUtils.formatPoint(this, this.abriss.getStation()));
        builder.append(")");

        this.stationNumberTextView.setText(builder.toString());

        this.adapter = new ArrayListOfResultsAdapter(this, R.layout.abriss_results_list_item,
                this.abriss.getResults());
        this.resultsListView.setAdapter(this.adapter);

        this.unknOrientTextView.setText(DisplayUtils.toString(this.abriss.getMean()));
        this.meanErrorTextView.setText(DisplayUtils.toString(this.abriss.getMSE()));
    }
}
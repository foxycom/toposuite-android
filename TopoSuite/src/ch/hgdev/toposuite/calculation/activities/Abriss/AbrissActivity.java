package ch.hgdev.toposuite.calculation.activities.Abriss;

import ch.hgdev.toposuite.R;
import ch.hgdev.toposuite.R.layout;
import ch.hgdev.toposuite.R.menu;
import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;

public class AbrissActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_abriss);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.abriss, menu);
        return true;
    }

}

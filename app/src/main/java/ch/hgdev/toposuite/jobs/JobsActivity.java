package ch.hgdev.toposuite.jobs;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.google.common.base.Joiner;
import com.google.common.io.Files;

import org.json.JSONException;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import ch.hgdev.toposuite.App;
import ch.hgdev.toposuite.R;
import ch.hgdev.toposuite.SharedResources;
import ch.hgdev.toposuite.TopoSuiteActivity;
import ch.hgdev.toposuite.dao.CalculationsDataSource;
import ch.hgdev.toposuite.dao.PointsDataSource;
import ch.hgdev.toposuite.utils.AppUtils;
import ch.hgdev.toposuite.utils.Logger;
import ch.hgdev.toposuite.utils.ViewUtils;

public class JobsActivity extends TopoSuiteActivity implements ExportDialog.ExportDialogListener,
        ImportDialog.ImportDialogListener, ActivityCompat.OnRequestPermissionsResultCallback {

    private ListView jobsListView;
    private ArrayListOfJobFilesAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_jobs);

        this.jobsListView = (ListView) this.findViewById(R.id.apm_list_of_jobs);
        this.registerForContextMenu(this.jobsListView);
    }

    @Override
    protected void onResume() {
        super.onResume();
        this.drawList();
    }

    @Override
    protected String getActivityTitle() {
        return this.getString(R.string.title_activity_jobs);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        this.getMenuInflater().inflate(R.menu.jobs, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = this.getMenuInflater();
        inflater.inflate(R.menu.jobs_list_row_context_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case R.id.export_job:
                if (AppUtils.isPermissionGranted(this, AppUtils.Permission.WRITE_EXTERNAL_STORAGE)) {
                    this.exportJob();
                } else {
                    AppUtils.requestPermission(this, AppUtils.Permission.WRITE_EXTERNAL_STORAGE,
                            String.format(this.getString(R.string.need_storage_access), AppUtils.getAppName()));
                }
                return true;
            case R.id.clear_job:
                this.clearJobs();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        switch (item.getItemId()) {
            case R.id.import_job:
                int position = (int) info.id;
                if (AppUtils.isPermissionGranted(JobsActivity.this, AppUtils.Permission.READ_EXTERNAL_STORAGE)) {
                    JobsActivity.this.importJob(position);
                } else {
                    AppUtils.requestPermission(JobsActivity.this, AppUtils.Permission.READ_EXTERNAL_STORAGE,
                            String.format(JobsActivity.this.getString(R.string.need_storage_access), AppUtils.getAppName()));
                    if (AppUtils.isPermissionGranted(JobsActivity.this, AppUtils.Permission.READ_EXTERNAL_STORAGE)) {
                        JobsActivity.this.importJob(position);
                    }
                }
            default:
                return super.onContextItemSelected(item);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (AppUtils.Permission.valueOf(requestCode)) {
            case READ_EXTERNAL_STORAGE:
                if (!AppUtils.isPermissionGranted(this, AppUtils.Permission.READ_EXTERNAL_STORAGE)) {
                    ViewUtils.showToast(this, this.getString(R.string.error_impossible_to_import));
                }
                break;
            case WRITE_EXTERNAL_STORAGE:
                if (AppUtils.isPermissionGranted(this, AppUtils.Permission.WRITE_EXTERNAL_STORAGE)) {
                    this.exportJob();
                } else {
                    ViewUtils.showToast(this, this.getString(R.string.error_impossible_to_export));
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    @Override
    public void onExportDialogSuccess(String message) {
        this.drawList();
        ViewUtils.showToast(this, message);
    }

    @Override
    public void onExportDialogError(String message) {
        ViewUtils.showToast(this, message);
    }

    @Override
    public void onImportDialogSuccess(String message) {
        ViewUtils.showToast(this, message);
    }

    @Override
    public void onImportDialogError(String message) {
        ViewUtils.showToast(this, message);
    }

    public void onJobClicked(int position) {

    }

    /**
     * Draw the main table containing all the points.
     */
    private void drawList() {
        String[] filenameList = new File(App.publicDataDirectory).list(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String filename) {
                return Files.getFileExtension(filename).equalsIgnoreCase(Job.EXTENSION);
            }
        });
        Arrays.sort(filenameList);

        ArrayList<File> files = new ArrayList<File>();
        for (String filename : filenameList) {
            files.add(new File(App.publicDataDirectory, filename));
        }

        this.adapter = new ArrayListOfJobFilesAdapter(this, R.layout.jobs_list_item, files);
        this.jobsListView.setAdapter(this.adapter);
    }

    private void importJob(int pos) {
        File f = this.adapter.getItem(pos);

        try {
            List<String> lines = Files.readLines(f, Charset.defaultCharset());
            // remove previous points and calculations from the SQLite DB
            PointsDataSource.getInstance().truncate();
            CalculationsDataSource.getInstance().truncate();

            // clean in-memory residues
            SharedResources.getSetOfPoints().clear();
            SharedResources.getCalculationsHistory().clear();

            String json = Joiner.on('\n').join(lines);
            Job.loadJobFromJSON(json);
        } catch (IOException e) {
            Logger.log(Logger.ErrLabel.IO_ERROR, e.getMessage());
            ViewUtils.showToast(this, e.getMessage());
            return;
        } catch (JSONException | ParseException e) {
            Logger.log(Logger.ErrLabel.PARSE_ERROR, e.getMessage());
            ViewUtils.showToast(this, e.getMessage());
            return;
        }

        ViewUtils.showToast(this, this.getString(R.string.success_import_job_dialog));
    }

    private void exportJob() {
        ExportDialog dialog = new ExportDialog();
        dialog.show(this.getFragmentManager(), "ExportDialogFragment");
    }

    private void clearJobs() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.delete_job)
                .setMessage(R.string.loose_job)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton(R.string.delete,
                        new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // remove previous points and calculations from the SQLite DB
                                PointsDataSource.getInstance().truncate();
                                CalculationsDataSource.getInstance().truncate();

                                // clean in-memory residues
                                SharedResources.getSetOfPoints().clear();
                                SharedResources.getCalculationsHistory().clear();
                            }
                        })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // do nothing
                    }
                });
        builder.create().show();
    }
}

/*
 * Copyright (C) 2014 University of Washington
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.opendatakit.tables.tasks;

import android.os.AsyncTask;
import org.opendatakit.builder.CsvUtil;
import org.opendatakit.builder.CsvUtilSupervisor;
import org.opendatakit.database.service.UserDbInterface;
import org.opendatakit.exception.ServicesAvailabilityException;
import org.opendatakit.listener.ImportListener;
import org.opendatakit.logging.WebLogger;
import org.opendatakit.tables.activities.AbsBaseActivity;
import org.opendatakit.tables.application.Tables;
import org.opendatakit.tables.utils.ImportExportDialog;

/**
 * A task that imports csv files
 */
public class ImportTask extends AsyncTask<ImportRequest, Integer, Boolean>
    implements ImportListener {

  // Used for logging
  private static final String TAG = ImportTask.class.getSimpleName();

  // the app name
  private final String appName;
  // the dialog "Importing row xx..." progress dialog with the spinner
  private ImportExportDialog progressDialogFragment;
  // a task that needs to be passed to progressDialogFragment so it can update the progress
  // dialog's message
  private AbsBaseActivity context;

  // booleans used to keep track of whether there was an error
  public boolean caughtDuplicateTableException = false;
  public boolean problemImportingKVSEntries = false;

  /**
   * Constructor that stores off it's three arguments. Used by ImportCSVActivity
   *
   * @param progressDialogFragment the (already opened) progress dialog
   * @param appName                the app name
   * @param context                the context that we need to give the progress dialog
   */
  public ImportTask(ImportExportDialog progressDialogFragment, String appName,
      AbsBaseActivity context) {
    this.progressDialogFragment = progressDialogFragment;
    this.appName = appName;
    this.context = context;
  }

  /**
   * tells services to import the csv file in the background
   *
   * @param importRequests which request to tell services to execute
   * @return whether successful or not
   */
  @Override
  protected Boolean doInBackground(ImportRequest... importRequests) {
    ImportRequest request = importRequests[0];
    CsvUtil cu = new CsvUtil(new CsvUtilSupervisor() {
      @Override
      public UserDbInterface getDatabase() {
        return Tables.getInstance().getDatabase();
      }
    }, appName);
    try {
      return cu.importSeparable(this, request.getTableId(), request.getFileQualifier(),
          request.getCreateTable());
    } catch (ServicesAvailabilityException e) {
      WebLogger.getLogger(appName).printStackTrace(e);
      WebLogger.getLogger(appName).e(TAG, "Unable to access database");
      return false;
    }
  }

  /**
   * called when the import is complete, records the result in probleImportingKVSEntries
   *
   * @param outcome whether the import was successful or not
   */
  @Override
  public void importComplete(boolean outcome) {
    problemImportingKVSEntries = !outcome;
  }

  /**
   * Updates the open progress dialog with the new status
   * just passes along the request to ImportExportDialog
   *
   * @param progressString the string to set in the window, like "Importing row 10"
   */
  @Override
  public void updateProgressDetail(String progressString) {
    progressDialogFragment.updateProgressDialogStatusString(context, progressString);
  }

  /**
   * does nothing, but called when there's new progress
   *
   * @param progress unknown
   */
  protected void onProgressUpdate(Integer... progress) {
    // do nothing.
  }

  /**
   * Called when the csv import is done.
   * Dismisses the progress dialog fragment, and displays an alert dialog with either a success
   * message, or one of the three failure messages.
   */
  protected void onPostExecute(Boolean result) {
    progressDialogFragment.dismiss();
    if (result) {
      ImportExportDialog.newInstance(ImportExportDialog.CSVIMPORT_SUCCESS_DIALOG, context);
    } else {
      if (caughtDuplicateTableException) {
        ImportExportDialog.newInstance(ImportExportDialog.CSVIMPORT_FAIL_DUPLICATE_TABLE, context);
      } else if (problemImportingKVSEntries) {
        ImportExportDialog
            .newInstance(ImportExportDialog.CSVEXPORT_SUCCESS_SECONDARY_KVS_ENTRIES_FAIL_DIALOG,
                context);
      } else {
        ImportExportDialog.newInstance(ImportExportDialog.CSVIMPORT_FAIL_DIALOG, context);
      }
    }
  }
}
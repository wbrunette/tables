/*
 * Copyright (C) 2012 University of Washington
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
package org.opendatakit.tables.activities;

import org.opendatakit.activities.BaseActivity;
import org.opendatakit.utilities.ODKFileUtils;
import org.opendatakit.tables.R;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Toast;

/**
 * An abstract parent class for import/export activities.
 *
 * @author sudar.sam@gmail.com
 * @author unknown
 */
public abstract class AbstractImportExportActivity extends AbsBaseActivity {

	/** dialog IDs */
	public static final int CSVEXPORT_SUCCESS_DIALOG = 1;
	public static final int CSVIMPORT_SUCCESS_DIALOG = 2;
	public static final int EXPORT_IN_PROGRESS_DIALOG = 3;
	public static final int IMPORT_IN_PROGRESS_DIALOG = 4;
	public static final int CSVIMPORT_FAIL_DIALOG = 5;
	public static final int CSVEXPORT_FAIL_DIALOG = 6;
	// This is intended to say that "your csv exported successfully, but there
	// was a problem with the key value store setting mapping.
	public static final int
	  CSVEXPORT_SUCCESS_SECONDARY_KVS_ENTRIES_FAIL_DIALOG = 7;
	public static final int
	  CSVIMPORT_FAIL_DUPLICATE_TABLE = 8;
	protected static final int
	  CSVIMPORT_SUCCESS_SECONDARY_KVS_ENTRIES_FAIL_DIALOG = 9;

	private Dialog activeDialog = null;

	@Override
	protected Dialog onCreateDialog(int id) {
		Dialog d = null;
		switch(id) {
		case CSVEXPORT_SUCCESS_DIALOG:
			d = getDialog(getString(R.string.export_success));
			break;
		case CSVIMPORT_SUCCESS_DIALOG:
			d = getDialog(getString(R.string.import_success));
			break;
		case EXPORT_IN_PROGRESS_DIALOG:
			ProgressDialog epd = new ProgressDialog(this);
			epd.setMessage(getString(R.string.export_in_progress));
			d = epd;
			break;
		case IMPORT_IN_PROGRESS_DIALOG:
			ProgressDialog ipd = new ProgressDialog(this);
			ipd.setMessage(getString(R.string.import_in_progress));
			d = ipd;
			break;
		case CSVIMPORT_FAIL_DIALOG:
			d = getDialog(getString(R.string.import_failure));
			break;
		case CSVEXPORT_FAIL_DIALOG:
			d = getDialog(getString(R.string.export_failure));
			break;
		case CSVEXPORT_SUCCESS_SECONDARY_KVS_ENTRIES_FAIL_DIALOG:
			d = getDialog(getString(R.string.export_partial_success));
			break;
		case CSVIMPORT_FAIL_DUPLICATE_TABLE:
			d = getDialog(getString(R.string.import_failure_existing_table));
			break;
		case CSVIMPORT_SUCCESS_SECONDARY_KVS_ENTRIES_FAIL_DIALOG:
		  d = getDialog(getString(R.string.import_partial_success));
			break;
		default:
			throw new IllegalArgumentException();
		}
		activeDialog = d;
		d.setOnDismissListener(new DialogInterface.OnDismissListener() {
			@Override public void onDismiss(DialogInterface dialog) {
				activeDialog = null;
			}
		});
		return d;
	}

	 public void updateProgressDialogStatusString(final String status) {
		 this.runOnUiThread(new Runnable() {
			 @Override public void run() {
				 Dialog d = activeDialog;
				 if ( d != null ) {
					 if ( d instanceof ProgressDialog ) {
						 ProgressDialog epd = (ProgressDialog) d;
						 epd.setMessage(status);
					 }
				 }
			 }
		 });
	 }

    protected class PickFileButtonListener implements OnClickListener {
      String appName;
    	String title;

    	public PickFileButtonListener(String appName, String title) {
    	   this.appName = appName;
    		this.title = title;
    	}

        @Override
        public void onClick(View v) {
            Intent intent = new Intent("org.openintents.action.PICK_FILE");
            intent.setData(Uri.parse("file://" + ODKFileUtils.getAssetsCsvFolder(appName)));
            intent.putExtra("org.openintents.extra.TITLE", title);
            try {
              startActivityForResult(intent, 1);
            } catch ( ActivityNotFoundException e ) {
              e.printStackTrace();
              Toast.makeText(AbstractImportExportActivity.this, getString(R.string.file_picker_not_found), Toast.LENGTH_LONG).show();
            }
        }
    }

	/**
	 * Creates a simple alert dialog.
	 * @param message the dialog's message
	 * @return the dialog
	 */
	private AlertDialog getDialog(String message) {
		AlertDialog.Builder adBuilder = new AlertDialog.Builder(this);
		adBuilder = adBuilder.setMessage(message);
		adBuilder = adBuilder.setNeutralButton(getString(R.string.ok),
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int id) {
						dialog.cancel();
					}
		});
		AlertDialog d = adBuilder.create();
		return d;
	}

}

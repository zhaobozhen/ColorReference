/**
 * Copyright 2016-2017 By_syk
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.by_syk.mdcolor;

import android.app.ActionBar;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.by_syk.lib.toast.GlobalToast;
import com.by_syk.mdcolor.fragment.HelpDialog;
import com.by_syk.mdcolor.util.C;
import com.by_syk.mdcolor.util.adapter.GradesAdapter;
import com.by_syk.mdcolor.util.Palette;

/**
 * Created by By_syk on 2016-04-01.
 */
public class DetailsActivity extends BaseActivity {
    private ListView lvGrades;

    private GradesAdapter gradesAdapter = null;
    private Palette palette = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        /*// Translucent navigation bar
        // android:fitsSystemWindows="true"
        if (!getResources().getBoolean(R.bool.is_land)) {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION,
                    WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
        }*/

        setContentView(R.layout.activity_details);

        init();

        // Tell users how to copy color values.
        // Just once.
        copyToast();
    }

    private void init() {
        Intent intent = getIntent();
        palette = (Palette) intent.getSerializableExtra("palette");

        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(palette.getName());
        }

        lvGrades = findViewById(R.id.lv_grades);

        gradesAdapter = new GradesAdapter(this, palette);
        lvGrades.setAdapter(gradesAdapter);

        lvGrades.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                copy2Clipboard(palette.getColorStr(position));

                // Do not show again.
                sp.save(C.SP_TOAST_COPY, false);
            }
        });
    }

    private void copyToast() {
        if (sp.getBoolean(C.SP_TOAST_COPY, true)) {
            GlobalToast.showToast(this, R.string.toast_tap_card_to_copy);

            //sp.save(C.SP_TOAST_COPY, false);
        }
    }

    private void copy2Clipboard(String text) {
        if (text == null) {
            return;
        }

        ClipboardManager clipboardManager = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
        clipboardManager.setPrimaryClip(ClipData.newPlainText(null, text));

        GlobalToast.showToast(DetailsActivity.this, getString(R.string.toast_copied, text));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_details, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            case R.id.menu_help:
                (new HelpDialog()).show(getFragmentManager(), "helpDialog");
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}

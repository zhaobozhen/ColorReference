/*
  Copyright 2016-2017 By_syk

  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at

      http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
 */

package com.by_syk.mdcolor;

import androidx.core.app.NotificationCompat;
import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewStub;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.Switch;

import com.by_syk.lib.toast.GlobalToast;
import com.by_syk.mdcolor.fragment.AboutDialog;
import com.by_syk.mdcolor.util.C;
import com.by_syk.mdcolor.util.Palette;
import com.by_syk.mdcolor.util.TransitionHelper;
import com.by_syk.mdcolor.util.adapter.MainAdapter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MainActivity extends BaseActivity {
    private ListView lvColors;
    private Switch switchBoard;
    private View viewControlBar;
    private View viewUIBoard;
    private ImageButton fabLucky;

    private MainAdapter mainAdapter = null;

    private NotificationManager notificationManager;

    private static Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        init();

        (new LoadColorsTask()).execute();

        TransitionHelper.getInstance().onActivityCreate(this);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        Log.d(C.LOG_TAG, "onPostCreate");

        super.onPostCreate(savedInstanceState);

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                fabLucky.setVisibility(View.VISIBLE);
                fabLucky.setAnimation(AnimationUtils
                        .loadAnimation(MainActivity.this, R.anim.fab_bottom_in));
            }
        }, 134);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        cancelNotification();
    }

    private void init() {
        viewControlBar = findViewById(R.id.view_control_bar);
        switchBoard = findViewById(R.id.switch_board);
        fabLucky = findViewById(R.id.fab_lucky);

        // Sets the data behind the ListView.
        mainAdapter = new MainAdapter(this, sp.getInt(C.SP_THEME_COLOR, -1));
        lvColors = findViewById(R.id.lv_colors);
        lvColors.setAdapter(mainAdapter);

        //lvColors.setChoiceMode(ListView.CHOICE_MODE_SINGLE);

        lvColors.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (position == sp.getInt(C.SP_THEME_COLOR, -1)) {
                    gotoDetails(position);
                    return;
                }

                // Tell users how to view details.
                // Just once.
                viewDetailsToast();

                sp.put(C.SP_THEME_COLOR, position).put(C.SP_WITH_DARK_AB,
                        mainAdapter.getItem(position).isLightThemeWithDarkABSuggested())
                        .save();

                changeTheme();
            }
        });

        RadioGroup radioGroup = findViewById(R.id.rg_themes);
        radioGroup.check(switchRadioButtonOrderAndId(sp.getInt(C.SP_THEME_STYLE)));
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                int theme_style = switchRadioButtonOrderAndId(checkedId);
                if (theme_style == -1) {
                    return;
                }

                sp.save(C.SP_THEME_STYLE, theme_style);

                changeTheme();
            }
        });

        initControlAndUIBoard();

        initFab();
    }

    private void initControlAndUIBoard() {
        // As default, off if in portrait, and on if in landscape.
        if (sp.getBoolean(C.SP_UI_BOARD, getResources().getBoolean(R.bool.is_land))) {
            viewUIBoard = ((ViewStub) findViewById(R.id.vs_ui_board)).inflate();
            switchBoard.setChecked(true);
        }

        switchBoard.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                sp.save(C.SP_UI_BOARD, isChecked);

                if (isChecked) { // Show the UI board.
                    if (viewUIBoard == null) {
                        viewUIBoard = ((ViewStub) findViewById(R.id.vs_ui_board)).inflate();
                    } else {
                        viewUIBoard.setVisibility(View.VISIBLE);
                    }

                    if (!getResources().getBoolean(R.bool.is_land)) {
                        viewControlBar.startAnimation(AnimationUtils.loadAnimation(MainActivity.this,
                                R.anim.translate));
                    }
                    viewUIBoard.startAnimation(AnimationUtils.loadAnimation(MainActivity.this,
                            R.anim.fade_in));
                } else { // Hide the UI board.
                    viewUIBoard.setVisibility(View.GONE);
                }
            }
        });

        viewControlBar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //switchBoard.clearFocus();

                switchBoard.performClick();
            }
        });
        /*viewControlBar.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switchBoard.requestFocusFromTouch();
                return false;
            }
        });*/
    }

    private void initFab() {
        /*fabLucky.setImageResource(System.currentTimeMillis() % 2 == 0
                ? R.drawable.ic_fab_random : R.drawable.ic_fab_random2);
        fabLucky.setImageResource(sharedPreferences.getInt(C.SP_THEME_STYLE, 0) == 0
                ? R.drawable.ic_fab_random : R.drawable.ic_fab_random2);*/

        fabLucky.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Choose a random number except for current one.
                Random random = new Random();
                int lucky_one;
                do {
                    lucky_one = random.nextInt(mainAdapter.getCount());
                } while (lucky_one == mainAdapter.getChecked());

                sp.put(C.SP_THEME_COLOR, lucky_one)
                        .put(C.SP_WITH_DARK_AB, mainAdapter.getItem(lucky_one)
                                .isLightThemeWithDarkABSuggested())
                        .save();

                GlobalToast.showToast(MainActivity.this, getString(R.string.toast_lucky_color,
                        mainAdapter.getItem(lucky_one).getName()));

                changeTheme();
            }
        });
        fabLucky.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                GlobalToast.showToast(MainActivity.this, R.string.menu_lucky);

                return true;
            }
        });

        //fabLucky.setVisibility(View.VISIBLE);
        //fabLucky.setAnimation(AnimationUtils.loadAnimation(this, R.anim.fab_bottom_in));
    }

    /*private void showTips() {
        if (sp.getBoolean(C.SP_BOARD, true) && sp.getInt(C.SP_THEME_COLOR, -1) >= 0) {
            viewTip = ((ViewStub) findViewById(R.id.vs_tip)).inflate();

            viewTip.findViewById(R.id.bt_hide_tip).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    viewTip.setVisibility(View.GONE);

                    sp.save(C.SP_BOARD, false);
                }
            });

            viewTip.startAnimation(AnimationUtils.loadAnimation(this, R.anim.alpha_in));
        }
    }*/

    private void gotoDetails(int which) {
        // Do not show toast again.
        sp.save(C.SP_TOAST_DETAILS, false);

        Intent intent = new Intent(this, DetailsActivity.class);
        intent.putExtra("palette", mainAdapter.getItem(which));

        startActivity(intent);
    }

    private void viewDetailsToast() {
        if (sp.getBoolean(C.SP_TOAST_DETAILS, true)) {
            GlobalToast.showToast(this, R.string.toast_tap_again_details);

            //sp.save(C.SP_TOAST_DETAILS, false);
        }
    }

    private int switchRadioButtonOrderAndId(int i) {
        switch (i) {
            case 0:
                return R.id.rb_dark_theme;
            case 1:
                return R.id.rb_light_theme;
            case R.id.rb_dark_theme:
                return 0;
            case R.id.rb_light_theme:
                return 1;
            //case R.id.rb_light_with_dark_theme:
            //    return 2;
            default:
                return -1;
        }
    }

    @SuppressLint("StaticFieldLeak")
    private class LoadColorsTask extends AsyncTask<String, Integer, List<Palette>> {
        @Override
        protected List<Palette> doInBackground(String... params) {
            return loadColors();
        }

        @Override
        protected void onPostExecute(List<Palette> dataList) {
            super.onPostExecute(dataList);

            mainAdapter.notifyRefresh(dataList);
            //mainAdapter.notifyDataSetChanged();

            //lvColors.smoothScrollToPosition(sharedPreferences.getInt(C.SP_THEME_COLOR, 0));
            lvColors.setSelection(sp.getInt(C.SP_THEME_COLOR));
        }
    }

    private List<Palette> loadColors() {
        List<Palette> dataList = new ArrayList<>();

        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(getResources()
                .openRawResource(R.raw.palette)));

        Palette palette;
        String tempStr;
        try {
            while ((tempStr = bufferedReader.readLine()) != null) {
                if (tempStr.startsWith("# ")) {
                    continue;
                }

                palette = new Palette();
                boolean is_ok = palette.setAll(tempStr);
                if (!is_ok) {
                    continue;
                }

                dataList.add(palette);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return dataList;
    }

    private void changeTheme() {
        // Shine!!!
        //recreate();

        TransitionHelper.getInstance().onRestartActivity(this);
        startActivity(new Intent(this, MainActivity.class));
        finish();
        overridePendingTransition(0, 0);
    }

    private void showNotification() {
        TypedValue typedValue = new TypedValue();
        getTheme().resolveAttribute(R.attr.colorPrimary, typedValue, true);
        int primaryColor = typedValue.data;

        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            String CHANNEL_ID = "noti_channel";
            CharSequence name = "通知栏";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel mChannel = new NotificationChannel(CHANNEL_ID, name, importance);
            mChannel.enableLights(true);
            mChannel.setLightColor(Color.RED);
            mChannel.setShowBadge(false);
            manager.createNotificationChannel(mChannel);
        }

        Notification notification = new NotificationCompat.Builder(this, "noti_channel")
                .setContentTitle(getString(R.string.notify_content))
                .setSmallIcon(R.drawable.ic_notify_pantone)
                //.setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher))
                .setColor(primaryColor)
                .setPriority(Notification.PRIORITY_LOW)
                .setAutoCancel(true)
                .setFullScreenIntent(PendingIntent.getActivity(this, 0, new Intent(), 0), false)
                .build();

        if (notificationManager == null) {
            notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        }
        notificationManager.notify(1, notification);
    }

    private void cancelNotification() {
        if (notificationManager != null) {
            notificationManager.cancel(1);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_notification:
                showNotification();
                return true;
            case R.id.menu_reset: {
                sp.delete(C.SP_THEME_COLOR);
                sp.delete(C.SP_THEME_STYLE);

                GlobalToast.showToast(this, R.string.toast_reset);

                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        /*// Instead of this:
                        // ((RadioGroup) findViewById(R.id.rg_themes)).check(R.id.rb_dark_theme);

                        ((RadioGroup) findViewById(R.id.rg_themes)).clearCheck();
                        ((RadioButton) findViewById(R.id.rb_dark_theme)).setChecked(true);*/

                        changeTheme();
                    }
                }, 400);

                return true;
            }
            case R.id.menu_about:
                (new AboutDialog()).show(getFragmentManager(), "aboutDialog");
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}

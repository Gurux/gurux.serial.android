//
// --------------------------------------------------------------------------
//  Gurux Ltd
//
//
//
// Filename:        $HeadURL$
//
// Version:         $Revision$,
//                  $Date$
//                  $Author$
//
// Copyright (c) Gurux Ltd
//
//---------------------------------------------------------------------------
//
//  DESCRIPTION
//
// This file is a part of Gurux Device Framework.
//
// Gurux Device Framework is Open Source software; you can redistribute it
// and/or modify it under the terms of the GNU General Public License
// as published by the Free Software Foundation; version 2 of the License.
// Gurux Device Framework is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
// See the GNU General Public License for more details.
//
// More information of Gurux products: http://www.gurux.org
//
// This code is licensed under the GNU General Public License v2.
// Full text may be retrieved at http://www.gnu.org/licenses/gpl-2.0.txt
//---------------------------------------------------------------------------

package gurux.serial;

import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.preference.PreferenceManager;

import gurux.common.IGXMediaListener;
import gurux.common.MediaStateEventArgs;
import gurux.common.PropertyChangedEventArgs;
import gurux.common.ReceiveEventArgs;
import gurux.common.TraceEventArgs;
import gurux.serial.properties.PropertiesViewModel;

/**
 * Serial port properties activity.
 */
public class GXPropertiesActivity extends AppCompatActivity implements IGXMediaListener {

    private GXSerial mSerial;
    SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_properties);
        String settings = getIntent().getStringExtra("mediaSettings");
        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        mSerial = new GXSerial(this);
        mSerial.setSettings(settings);
        mSerial.addListener(this);
        PropertiesViewModel mediaViewModel =
                new ViewModelProvider(this).get(PropertiesViewModel.class);
        mediaViewModel.setMedia(mSerial);
        Fragment childFragment = mediaViewModel.getMedia().properties();

        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.settings, childFragment)
                    .commit();
        }
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public void onError(Object sender, RuntimeException ex) {

    }

    @Override
    public void onReceived(Object sender, ReceiveEventArgs e) {

    }

    @Override
    public void onMediaStateChange(Object sender, MediaStateEventArgs e) {

    }

    @Override
    public void onTrace(Object sender, TraceEventArgs e) {

    }

    @Override
    public void onPropertyChanged(Object sender, PropertyChangedEventArgs e) {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("mediaSettings", mSerial.getSettings());
        editor.apply();
    }

    @Override
    public void onDestroy() {
        if (mSerial != null) {
            mSerial.removeListener(this);
        }
        super.onDestroy();
    }
}

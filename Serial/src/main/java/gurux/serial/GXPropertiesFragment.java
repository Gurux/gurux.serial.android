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

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import androidx.fragment.app.Fragment;
import gurux.serial.R;

/**
 * Serial port settings fragment.
 */
public class GXPropertiesFragment extends Fragment {
    GXPropertiesBase base;
    Button mShowInfo;

    public GXPropertiesFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_properties, container, false);
        base = new GXPropertiesBase((ListView) view.findViewById(R.id.properties), getActivity());
        //Show serial port info.
        mShowInfo = view.findViewById(R.id.showInfo);
        mShowInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    GXPort port = base.getSerial().getPort();
                    String info = "";
                    if (port != null) {
                        info = port.getInfo();
                    }
                    new AlertDialog.Builder(getContext())
                            .setTitle("Info")
                            .setMessage(info)
                            .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    //Do nothing.
                                }
                            })
                            .setIcon(android.R.drawable.ic_dialog_info)
                            .show();
                } catch (Exception ex) {
                }
            }
        });
        return view;
    }

    @Override
    public void onDestroy() {
        if (base != null) {
            base.close();
        }
        super.onDestroy();
    }
}

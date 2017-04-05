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
import android.content.Context;
import android.content.DialogInterface;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import gurux.common.IGXMediaListener;
import gurux.common.MediaStateEventArgs;
import gurux.common.PropertyChangedEventArgs;
import gurux.common.ReceiveEventArgs;
import gurux.common.TraceEventArgs;
import gurux.io.BaudRate;
import gurux.io.Parity;
import gurux.io.StopBits;
import gurux.serial.enums.Chipset;
import gurux.serial.java.android.R;

/**
 * Serial port base settings for activity and fragment.
 */
class GXPropertiesBase implements IGXSerialListener, IGXMediaListener {
    Context activity;
    ListView listView;
    private final List<String> rows = new ArrayList<String>();
    private static GXSerial serial;

    public GXPropertiesBase(final ListView lv, final Context c) {
        activity = c;
        listView = lv;
        if (serial.getPort() == null) {
            GXPort[] ports = serial.getPorts();
            if (ports.length != 0) {
                serial.setPort(ports[0]);
            }
        }
        serial.addListener(this);
        rows.add(getPort());
        rows.add(getBaudRate());
        rows.add(getDataBits());
        rows.add(getParity());
        rows.add(getStopBits());
        rows.add(getChipset());
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(activity,
                android.R.layout.simple_list_item_1, rows);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                switch (position) {
                    case 0:
                        updatePort();
                        break;
                    case 1:
                        updateBaudRate();
                        break;
                    case 2:
                        updateDataBits();
                        break;
                    case 3:
                        updateParity();
                        break;
                    case 4:
                        updateStopBits();
                        break;
                    case 5:
                        updateChipset();
                        break;
                    default:
                        //Do nothing.
                }
            }
        });
    }

    public void close() {
        serial.removeListener(this);
    }

    public static GXSerial getSerial() {
        return serial;
    }

    public static void setSerial(GXSerial value) {
        serial = value;
    }

    private String getPort() {
        return activity.getResources().getString(R.string.port) + "\r\n" + String.valueOf(serial.getPort());
    }

    private String getBaudRate() {
        return activity.getResources().getString(R.string.baudRate) + "\r\n" + serial.getBaudRate().getValue();
    }

    private String getDataBits() {
        return activity.getResources().getString(R.string.dataBits) + "\r\n" + serial.getDataBits();
    }

    private String getParity() {
        String value = "";
        switch (serial.getParity()) {
            case NONE:
                value = "None";
                break;
            case ODD:
                value = "Odd";
                break;
            case EVEN:
                value = "Even";
                break;
            case MARK:
                value = "Mark";
                break;
            case SPACE:
                value = "Space";
                break;
        }
        return activity.getResources().getString(R.string.parity) + "\r\n" + value;
    }

    private String getStopBits() {
        String value = "1";
        if (serial.getStopBits() == StopBits.TWO) {
            value = "2";
        }
        return activity.getResources().getString(R.string.stopBits) + "\r\n" + value;
    }

    private String getChipset() {
        return activity.getResources().getString(R.string.chipset) + "\r\n" + serial.getChipset();
    }

    /**
     * Update serial port.
     */
    private void updatePort() {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        final GXPort[] values = serial.getPorts();
        String[] tmp = new String[values.length];
        GXPort actual = serial.getPort();
        int selected = -1;
        int pos = 0;
        for (GXPort it : values) {
            tmp[pos] = it.toString();
            //Find selected item.
            if (actual != null && actual.getPort().equals(it.getPort())) {
                selected = pos;
            }
            ++pos;
        }
        if (values.length != 0) {
            builder.setTitle(R.string.port)
                    .setSingleChoiceItems(tmp, selected, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            serial.setPort(values[which]);
                            rows.set(0, getPort());
                            rows.set(5, getChipset());
                            ((BaseAdapter) listView.getAdapter()).notifyDataSetChanged();
                            dialog.dismiss();
                        }
                    }).show();
        } else {
            serial.setPort(null);
            rows.set(0, getPort());
            rows.set(5, getChipset());
            ((BaseAdapter) listView.getAdapter()).notifyDataSetChanged();
        }
    }

    /**
     * Update baud rate.
     */
    private void updateBaudRate() {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        final int tmp[] = GXSerial.getAvailableBaudRates(serial.getPort());
        String[] values = new String[tmp.length];
        int actual = serial.getBaudRate().getValue();
        int selected = -1;

        int pos = 0;
        for (int it : tmp) {
            values[pos] = String.valueOf(it);
            //Get selected item.
            if (actual == it) {
                selected = pos;
            }
            ++pos;
        }
        builder.setTitle(R.string.baudRate)
                .setSingleChoiceItems(values, selected, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        serial.setBaudRate(BaudRate.forValue(tmp[which]));
                        rows.set(1, getBaudRate());
                        ((BaseAdapter) listView.getAdapter()).notifyDataSetChanged();
                        dialog.dismiss();
                    }
                }).show();
    }

    /**
     * Update data bits.
     */
    private void updateDataBits() {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        final int tmp[] = new int[]{6, 7, 8};
        String[] values = new String[tmp.length];
        int actual = serial.getDataBits();
        int selected = -1;
        int pos = 0;
        for (int it : tmp) {
            values[pos] = String.valueOf(it);
            //Get selected item.
            if (actual == it) {
                selected = pos;
            }
            ++pos;
        }
        builder.setTitle(R.string.dataBits)
                .setSingleChoiceItems(values, selected, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        serial.setDataBits(tmp[which]);
                        rows.set(2, getDataBits());
                        ((BaseAdapter) listView.getAdapter()).notifyDataSetChanged();
                        dialog.dismiss();
                    }
                }).show();
    }

    /**
     * Update parity.
     */
    private void updateParity() {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        String[] values = new String[]{"None", "Odd", "Even", "Mark", "Space"};
        builder.setTitle(R.string.parity)
                .setSingleChoiceItems(values, serial.getParity().ordinal(), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        serial.setParity(Parity.values()[which]);
                        rows.set(3, getParity());
                        ((BaseAdapter) listView.getAdapter()).notifyDataSetChanged();
                        dialog.dismiss();
                    }
                }).show();
    }

    /**
     * Update stop bits.
     */
    private void updateStopBits() {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        String[] values = new String[]{"1", "2"};
        int actual = 0;
        if (serial.getStopBits() == StopBits.TWO) {
            actual = 1;
        }
        builder.setTitle(R.string.stopBits)
                .setSingleChoiceItems(values, actual, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        StopBits tmp;
                        if (which == 0) {
                            tmp = StopBits.ONE;
                        } else {
                            tmp = StopBits.TWO;
                        }
                        serial.setStopBits(tmp);
                        rows.set(4, getStopBits());
                        ((BaseAdapter) listView.getAdapter()).notifyDataSetChanged();
                        dialog.dismiss();
                    }
                }).show();
    }

    /**
     * Update chipset.
     */
    private void updateChipset() {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        final Chipset[] tmp = (Chipset[]) EnumSet.allOf(Chipset.class).toArray(new Chipset[0]);
        String[] values = new String[tmp.length - 1];
        int selected = serial.getChipset().ordinal() - 1;
        int pos = 0;
        for (Chipset it : tmp) {
            if (it != Chipset.NONE) {
                values[pos] = String.valueOf(it);
                ++pos;
            }
        }
        builder.setTitle(R.string.chipset)
                .setSingleChoiceItems(values, selected, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        serial.setChipset(tmp[which + 1]);
                        rows.set(5, getChipset());
                        ((BaseAdapter) listView.getAdapter()).notifyDataSetChanged();
                        dialog.dismiss();
                    }
                }).show();
    }

    /*
     * Select new port if port is not selected.
     */
    @Override
    public void onPortAdded(final GXPort port) {
        if (serial.getPort() == null) {
            serial.setPort(port);
            updatePort();
        }
    }

    /*
     * If removed port is selected.
     */
    @Override
    public void onPortRemoved(final GXPort port, final int index) {
        if (serial.getPort() == port) {
            updatePort();
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

    }
}

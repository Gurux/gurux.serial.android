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

package gurux.serial.properties;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import gurux.io.BaudRate;
import gurux.io.Parity;
import gurux.io.StopBits;
import gurux.serial.GXPort;
import gurux.serial.GXSerial;
import gurux.serial.IGXSerialListener;
import gurux.serial.R;
import gurux.serial.databinding.FragmentPropertiesBinding;

public class PropertiesFragment extends Fragment implements IGXSerialListener {

    private GXSerial mSerial;
    private FragmentPropertiesBinding binding;
    private ListView listView;

    private final List<String> rows = new ArrayList<>();

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        PropertiesViewModel propertiesViewModel =
                new ViewModelProvider(requireActivity()).get(PropertiesViewModel.class);

        binding = FragmentPropertiesBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        mSerial = (GXSerial) propertiesViewModel.getMedia();
        mSerial.addListener(this);
        if (mSerial != null) {
            listView = binding.properties;
            if (mSerial.getPort() == null) {
                GXPort[] ports = mSerial.getPorts();
                if (ports.length != 0) {
                    mSerial.setPort(ports[0]);
                }
            }
            if (mSerial.getPort() != null) {
                binding.infoBtn.setOnClickListener(v -> {
                    try {
                        GXPort port = mSerial.getPort();
                        String info = "";
                        if (port != null) {
                            info = port.getInfo();
                        }
                        new AlertDialog.Builder(getActivity())
                                .setTitle("Info")
                                .setMessage(info)
                                .setPositiveButton(android.R.string.ok, (dialog, which) -> {
                                    //Do nothing.
                                })
                                .setIcon(android.R.drawable.ic_dialog_info)
                                .show();
                    } catch (Exception ex) {
                        Log.e("Serial", Objects.requireNonNull(ex.getMessage()));
                        Toast.makeText(getContext(), ex.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                binding.infoBtn.setVisibility(View.GONE);
            }
            rows.add(getPort());
            rows.add(getBaudRate());
            rows.add(getDataBits());
            rows.add(getParity());
            rows.add(getStopBits());
            rows.add(getChipset());
            ArrayAdapter<String> adapter = new ArrayAdapter<>(container.getContext(),
                    android.R.layout.simple_list_item_1, rows);
            listView.setAdapter(adapter);
            listView.setOnItemClickListener((parent, view, position, id) -> {
                if (mSerial.getPorts().length != 0) {
                    //User can't change the settings when the connection is open.
                    if (!mSerial.isOpen()) {
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
                                //Chipset can't be changed.
                                break;
                            default:
                                //Do nothing.
                        }
                    } else {
                        Toast.makeText(getActivity(), R.string.connectionEstablished, Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(getActivity(), R.string.invalidSerialports, Toast.LENGTH_SHORT).show();
                }
            });
        }
        return root;
    }

    private String getPort() {
        return getString(R.string.port) + System.lineSeparator() + String.valueOf(mSerial.getPort());
    }

    private String getBaudRate() {
        return getString(R.string.baudRate) + System.lineSeparator() + mSerial.getBaudRate().getValue();
    }

    private String getDataBits() {
        return getString(R.string.dataBits) + System.lineSeparator() + mSerial.getDataBits();
    }

    private String getParity() {
        String value = "";
        switch (mSerial.getParity()) {
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
        return getString(R.string.parity) + System.lineSeparator() + value;
    }

    private String getStopBits() {
        String value = "1";
        if (mSerial.getStopBits() == StopBits.TWO) {
            value = "2";
        }
        return getString(R.string.stopBits) + System.lineSeparator() + value;
    }

    private String getChipset() {
        return getString(R.string.chipset) + System.lineSeparator() + mSerial.getChipset();
    }

    /**
     * Update serial port.
     */
    private void updatePort() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        final GXPort[] values = mSerial.getPorts();
        String[] tmp = new String[values.length];
        GXPort actual = mSerial.getPort();
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
                    .setSingleChoiceItems(tmp, selected, (dialog, which) -> {
                        mSerial.setPort(values[which]);
                        rows.set(0, getPort());
                        rows.set(5, getChipset());
                        ((BaseAdapter) listView.getAdapter()).notifyDataSetChanged();
                        dialog.dismiss();
                    })
                    .setNegativeButton(android.R.string.cancel, (dialog, which) -> dialog.cancel())
                    .show();
        } else {
            mSerial.setPort(null);
            rows.set(0, getPort());
            rows.set(5, getChipset());
            ((BaseAdapter) listView.getAdapter()).notifyDataSetChanged();
        }
    }

    /**
     * Update baud rate.
     */
    private void updateBaudRate() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        final int tmp[] = GXSerial.getAvailableBaudRates(mSerial.getPort());
        String[] values = new String[tmp.length];
        int actual = mSerial.getBaudRate().getValue();
        builder.setTitle(R.string.baudRate)
                .setSingleChoiceItems(values, actual, (dialog, which) -> {
                    mSerial.setBaudRate(BaudRate.forValue(tmp[which]));
                    rows.set(1, getBaudRate());
                    ((BaseAdapter) listView.getAdapter()).notifyDataSetChanged();
                    dialog.dismiss();
                })
                .setNegativeButton(android.R.string.cancel, (dialog, which) -> dialog.cancel())
                .show();
    }

    /**
     * Update data bits.
     */
    private void updateDataBits() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        final int tmp[] = new int[]{6, 7, 8};
        String[] values = new String[tmp.length];
        int actual = mSerial.getDataBits();
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
                .setSingleChoiceItems(values, selected, (dialog, which) -> {
                    mSerial.setDataBits(tmp[which]);
                    rows.set(2, getDataBits());
                    ((BaseAdapter) listView.getAdapter()).notifyDataSetChanged();
                    dialog.dismiss();
                })
                .setNegativeButton(android.R.string.cancel, (dialog, which) -> dialog.cancel())
                .show();
    }

    /**
     * Update parity.
     */
    private void updateParity() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        String[] values = new String[]{"None", "Odd", "Even", "Mark", "Space"};
        builder.setTitle(R.string.parity)
                .setSingleChoiceItems(values, mSerial.getParity().ordinal(), (dialog, which) -> {
                    mSerial.setParity(Parity.values()[which]);
                    rows.set(3, getParity());
                    ((BaseAdapter) listView.getAdapter()).notifyDataSetChanged();
                    dialog.dismiss();
                })
                .setNegativeButton(android.R.string.cancel, (dialog, which) -> dialog.cancel())
                .show();
    }

    /**
     * Update stop bits.
     */
    private void updateStopBits() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        String[] values = new String[]{"1", "2"};
        int actual = 0;
        if (mSerial.getStopBits() == StopBits.TWO) {
            actual = 1;
        }
        builder.setTitle(R.string.stopBits)
                .setSingleChoiceItems(values, actual, (dialog, which) -> {
                    StopBits tmp;
                    if (which == 0) {
                        tmp = StopBits.ONE;
                    } else {
                        tmp = StopBits.TWO;
                    }
                    mSerial.setStopBits(tmp);
                    rows.set(4, getStopBits());
                    ((BaseAdapter) listView.getAdapter()).notifyDataSetChanged();
                    dialog.dismiss();
                })
                .setNegativeButton(android.R.string.cancel, (dialog, which) -> dialog.cancel())
                .show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mSerial.removeListener(this);
        binding = null;
    }

    /**
     * Select new port if serial port is not selected.
     */
    @Override
    public void onPortAdded(GXPort port) {
        if (mSerial.getPort() == null) {
            mSerial.setPort(port);
            rows.set(0, getPort());
            rows.set(5, getChipset());
            //Update UI.
            ((BaseAdapter) listView.getAdapter()).notifyDataSetChanged();
            binding.infoBtn.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Reset port if it's removed.
     */
    @Override
    public void onPortRemoved(GXPort port, int index) {
        if (mSerial.getPort() == port) {
            mSerial.setPort(null);
            rows.set(0, getPort());
            rows.set(5, getChipset());
            //Update UI.
            ((BaseAdapter) listView.getAdapter()).notifyDataSetChanged();
            binding.infoBtn.setVisibility(View.GONE);
        }
    }
}
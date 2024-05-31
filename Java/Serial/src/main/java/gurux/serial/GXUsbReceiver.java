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

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * Handle USB register events.
 */
final class GXUsbReceiver extends BroadcastReceiver {
    GXSerial mSerial;

    /**
     * Construcot.
     *
     * @param serial owner.
     */
    public GXUsbReceiver(final GXSerial serial) {
        mSerial = serial;
    }


    @Override
    public void onReceive(final Context context, final Intent intent) {
        String action = intent.getAction();
        try {
            if ("gurux.serial".equals(action)) {
                ArrayList<UsbDevice> newPorts = new ArrayList<>();
                synchronized (this) {
                    UsbDevice device = (UsbDevice) intent
                            .getParcelableExtra(UsbManager.EXTRA_DEVICE);
                    if (device != null && intent.getBooleanExtra(
                            UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                        mSerial.addPort(null, device, true);
                    } else {
                        //Add new ports where user has added permissions.
                        UsbManager manager = (UsbManager) context.getSystemService(Context.USB_SERVICE);
                        List<GXPort> ports = GXSerial.mPorts;
                        if (ports == null) {
                            GXSerial.mPorts = new ArrayList<>();
                        }
                            for (UsbDevice usbDevice : manager.getDeviceList().values()) {
                            boolean found = false;
                                for (GXPort port : ports) {
                                    if (port.getPort().equals(usbDevice.getDeviceName())) {
                                        found = true;
                                        break;
                                    }
                            }
                            if (!found) {
                                newPorts.add(usbDevice);
                            }
                        }
                    }
                    for (UsbDevice it : newPorts) {
                        mSerial.addPort(null, it, true);
                    }
                }
            } else if (UsbManager.ACTION_USB_DEVICE_DETACHED.equals(action)) {
                Log.i("gurux.serial", "USB removed.");
                synchronized (this) {
                    UsbDevice device = (UsbDevice) intent
                            .getParcelableExtra(UsbManager.EXTRA_DEVICE);
                    mSerial.removePort(device);
                }
            } else if (UsbManager.ACTION_USB_DEVICE_ATTACHED.equals(action)) {
                Log.i("gurux.serial", "USB added.");
                synchronized (this) {
                    UsbDevice device = (UsbDevice) intent
                            .getParcelableExtra(UsbManager.EXTRA_DEVICE);
                    mSerial.addPort(null, device, true);
                }
            }
        }
        catch(Exception ex)
        {
            Log.e("gurux.serial", ex.getMessage());
        }
    }
}

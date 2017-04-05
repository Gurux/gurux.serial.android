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

import android.hardware.usb.UsbConstants;
import android.hardware.usb.UsbDeviceConnection;

import java.io.IOException;

import gurux.serial.enums.Chipset;

/**
 * Profilic settings.
 */
class GXProfilic extends GXChipset {

    public Chipset getChipset() {
        return Chipset.PROFILIC;
    }

    private static final int USB_RECIP_INTERFACE = 0x01;
    private static final int PROLIFIC_CTRL_OUT_REQTYPE = UsbConstants.USB_DIR_OUT
            | UsbConstants.USB_TYPE_CLASS | USB_RECIP_INTERFACE;
    private static final int SET_LINE_CODING = 0x20;
    private static final int GET_LINE_CODING = 0x21;
    private static final int CONTROL_DTR = 0x01;
    private static final int CONTROL_RTS = 0x02;
    private static final int SET_CONTROL_REQUEST = 0x22;
    private static final int BREAK_REQUEST = 0x23;
    private static final int PROLIFIC_VENDOR_WRITE_REQUEST = 0x01;
    private static final int PROLIFIC_VENDOR_OUT_REQTYPE = UsbConstants.USB_DIR_OUT
            | UsbConstants.USB_TYPE_VENDOR;

    public static boolean isUsing(final String stringManufacturer, int vendor, int product) {
        /**Aten UC-232*/
        if ((vendor == 0x557 && product == 0x2008) ||
                /*Prolific BF-810*/
                (vendor == 1659 && product == 8963)) {
            return true;
        }
        return false;
    }

    public boolean open(GXSerial serial, UsbDeviceConnection connection, byte[] rawDescriptors) throws IOException {
        byte[] lineRequestData = new byte[7];
        int baudRate = serial.getBaudRate().getValue();
        lineRequestData[0] = (byte) (baudRate & 0xff);
        lineRequestData[1] = (byte) ((baudRate >> 8) & 0xff);
        lineRequestData[2] = (byte) ((baudRate >> 16) & 0xff);
        lineRequestData[3] = (byte) ((baudRate >> 24) & 0xff);
        lineRequestData[4] = (byte) serial.getStopBits().ordinal();
        lineRequestData[5] = (byte) serial.getParity().ordinal();
        lineRequestData[6] = (byte) serial.getDataBits();
        int ret = connection.controlTransfer(PROLIFIC_CTRL_OUT_REQTYPE, SET_LINE_CODING, 0, 0,
                lineRequestData, lineRequestData.length, serial.getWriteTimeout());
        if (ret != lineRequestData.length) {
            return false;
        }

        ret = connection.controlTransfer(PROLIFIC_CTRL_OUT_REQTYPE, BREAK_REQUEST, 0, 0, null, 0, serial.getWriteTimeout());
        if (ret < 0) {
            return false;
        }
        //Set flow control.
        ret = connection.controlTransfer(64, 1, 0, 0, null, 0, serial.getWriteTimeout());
        if (ret < 0) {
            return false;
        }

        ret = connection.controlTransfer(64, 1, 1, 0, null, 0, serial.getWriteTimeout());
        if (ret < 0) {
            return false;
        }
        ret = connection.controlTransfer(64, 1, 2, 68, null, 0, serial.getWriteTimeout());
        if (ret < 0) {
            return false;
        }
        //Enable DTR and rts.
        int tmp = CONTROL_DTR | CONTROL_RTS;
        int result = connection.controlTransfer(PROLIFIC_CTRL_OUT_REQTYPE, SET_CONTROL_REQUEST,
                tmp,
                0, null, 0, serial.getWriteTimeout());
        if (result != 0) {
            throw new IOException("setRTS failed.");
        }
        connection.controlTransfer(PROLIFIC_VENDOR_OUT_REQTYPE, PROLIFIC_VENDOR_WRITE_REQUEST, 0x0404,
                0, null, 0, 1000);
        connection.controlTransfer(PROLIFIC_VENDOR_OUT_REQTYPE, PROLIFIC_VENDOR_WRITE_REQUEST, 0x0404,
                1, null, 0, 1000);
        connection.controlTransfer(PROLIFIC_VENDOR_OUT_REQTYPE, PROLIFIC_VENDOR_WRITE_REQUEST, 0,
                1, null, 0, 1000);
        connection.controlTransfer(PROLIFIC_VENDOR_OUT_REQTYPE, PROLIFIC_VENDOR_WRITE_REQUEST, 1,
                0, null, 0, 1000);
        if (rawDescriptors[7] == 0x40) {
            //If chipset is HX.
            connection.controlTransfer(PROLIFIC_VENDOR_OUT_REQTYPE, PROLIFIC_VENDOR_WRITE_REQUEST, 2,
                    0x44, null, 0, 1000);
        } else {
            connection.controlTransfer(PROLIFIC_VENDOR_OUT_REQTYPE, PROLIFIC_VENDOR_WRITE_REQUEST, 2,
                    0x24, null, 0, 1000);
        }
        return true;
    }
}

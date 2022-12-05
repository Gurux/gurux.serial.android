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

import gurux.io.StopBits;
import gurux.serial.enums.Chipset;

/**
 * Ch 34x chipset settings.
 */
class GXCh34x extends GXChipset {

    public Chipset getChipset() {
        return Chipset.CH34X;
    }

    final static int OUT_REQTYPE = 0x41;
    final static int IN_REQTYPE = UsbConstants.USB_TYPE_VENDOR | UsbConstants.USB_DIR_IN;

    public static boolean isUsing(final String stringManufacturer, final int vendor, final int product) {
        /**QinHeng Electronics*/
        if ((vendor == 0x1a86)) {
            return true;
        }
        return false;
    }

    private void setBaudRate(UsbDeviceConnection connection, int baudRate) throws IOException {
        int a, b;
        switch (baudRate) {
            case 2400:
                a = 0xd901;
                b = 0x0038;
                break;
            case 4800:
                a = 0x6402;
                b = 0x001f;
                break;
            case 9600:
                a = 0xb202;
                b = 0x0013;
                break;
            case 19200:
                a = 0xd902;
                b = 0x000d;
                break;
            case 38400:
                a = 0x6403;
                b = 0x000a;
                break;
            case 115200:
                a = 0xcc03;
                b = 0x0008;
                break;
            default:
                throw new IOException("Invalid baud rate: " + baudRate);
        }
        int ret = connection.controlTransfer(64, 0x9a, 0x1312, a, null, 0, 1000);
        if (ret < 0) {
            throw new IOException("Failed to set baud rate. #1");
        }
        ret = connection.controlTransfer(64, 0x9a, 0x0f2c, b, null, 0, 1000);
        if (ret < 0) {
            throw new IOException("Failed to set baud rate. #2");
        }
    }

    private void setConfig(GXSerial serial, UsbDeviceConnection connection) throws IOException {
        int value1 = 0, value2 = 0;
        switch (serial.getParity()) {
            case NONE:
                value1 = 0;
                break;
            case ODD:
                value1 = 8;
                break;
            case EVEN:
                value1 = 24;
                break;
            case MARK:
                value1 = 40;
                break;
            case SPACE:
                value1 = 56;
                break;
            default: {
                throw new IOException("Invalid parity.");
            }
        }
        if (serial.getStopBits() == StopBits.TWO) {
            value1 = (value1 | 4);
        }
        switch (serial.getDataBits()) {
            case 5:
                break;
            case 6:
                value1 |= 1;
                break;
            case 7:
                value1 |= 2;
                break;
            case 8:
                value1 |= 3;
                break;
            default:
                throw new IOException("Invalid data bits value.");
        }
        value1 = (value1 | 192);
        value1 = (156 | value1 << 8) & 0xFF;
        value2 = 0x88;
        switch (serial.getBaudRate()) {
           /*
            case BAUD_RATE_50: {
                value2 |= 0;
                value2 |= 22 << 8;
                break;
            }
            case BAUD_RATE_75: {
                value2 |= 0;
                value2 |= 100 << 8;
                break;
            }
            case BAUD_RATE_110: {
                value2 |= 0;
                value2 |= 150 << 8;
                break;
            }
            case BAUD_RATE_135: {
                value2 |= 0;
                value2 |= 169 << 8;
                break;
            }
            case BAUD_RATE_150: {
                value2 |= 0;
                value2 |= 178 << 8;
                break;
            }
            */
            case BAUD_RATE_300: {
                value2 |= 0;
                value2 |= 217 << 8;
                break;
            }
            case BAUD_RATE_600: {
                value2 |= 1;
                value2 |= 100 << 8;
                break;
            }
            case BAUD_RATE_1200: {
                value2 |= 1;
                value2 |= 178 << 8;
                break;
            }
            /*
            case BAUD_RATE_1800: {
                value2 |= 1;
                value2 |= 204 << 8;
                break;
            }
            */
            case BAUD_RATE_2400: {
                value2 |= 1;
                value2 |= 217 << 8;
                break;
            }
            case BAUD_RATE_4800: {
                value2 |= 2;
                value2 |= 100 << 8;
                break;
            }
            case BAUD_RATE_9600: {
                value2 |= 2;
                value2 |= 178 << 8;
                break;
            }
            case BAUD_RATE_19200: {
                value2 |= 2;
                value2 |= 217 << 8;
                break;
            }
            case BAUD_RATE_38400: {
                value2 |= 3;
                value2 |= 100 << 8;
                break;
            }
            /*
            case BAUD_RATE_57600: {
                value2 |= 3;
                value2 |= 152 << 8;
                break;
            }
            case BAUD_RATE_115200: {
                value2 |= 3;
                value2 |= 204 << 8;
                break;
            }
            case BAUD_RATE_230400: {
                value2 |= 3;
                value2 |= 230 << 8;
                break;
            }
            case BAUD_RATE_460800: {
                value2 |= 3;
                value2 |= 243 << 8;
                break;
            }
            case BAUD_RATE_500000: {
                value2 |= 3;
                value2 |= 244 << 8;
                break;
            }
            case BAUD_RATE_921600: {
                value2 |= 7;
                value2 |= 243 << 8;
                break;
            }
            case BAUD_RATE_1000000: {
                value2 |= 3;
                value2 |= 250 << 8;
                break;
            }
            case BAUD_RATE_2000000: {
                value2 |= 3;
                value2 |= 253 << 8;
                break;
            }
            case BAUD_RATE_3000000: {
                value2 |= 3;
                value2 |= 254 << 8;
                break;
            }
            */
            default: {
                throw new IOException("Invalid baud rate value.");
            }
        }
        int ret = connection.controlTransfer(64, 161, value1, value2, null, 0, serial.getWriteTimeout());
        if (ret < 0) {
            throw new IOException("Status failed: " + String.valueOf(ret));
        }
    }

    public boolean open(GXSerial serial, UsbDeviceConnection connection, byte[] rawDescriptors) throws IOException {
        byte[] buffer = new byte[8];
        int ret = connection.controlTransfer(64, 161, 0, 0, null, 0, serial.getWriteTimeout());
        if (ret < 0) {
            throw new IOException("Status failed: " + String.valueOf(ret));
        }
        ret = connection.controlTransfer(192, 95, 0, 0, buffer, buffer.length, serial.getWriteTimeout());
        if (ret < 0) {
            throw new IOException("Init failed1." + String.valueOf(ret));
        }
        //Set baud rate.
        ret = connection.controlTransfer(64, 154, 4882, 55682, null, 0, serial.getWriteTimeout());
        if (ret < 0) {
            throw new IOException("Init set baud rate failed: " + String.valueOf(ret));
        }
        ret = connection.controlTransfer(64, 154, 3884, 4, null, 0, serial.getWriteTimeout());
        if (ret < 0) {
            throw new IOException("Init failed3: " + String.valueOf(ret));
        }
        //End baud rate.
        ret = connection.controlTransfer(64, 154, 10023, 0, null, 0, serial.getWriteTimeout());
        if (ret < 0) {
            throw new IOException("Init End baud rate failed: " + String.valueOf(ret));
        }
        //writeHandshakeByte
        ret = connection.controlTransfer(64, 164, 255, 0, null, 0, serial.getWriteTimeout());
        if (ret < 0) {
            throw new IOException("Init writeHandshakeByte failed: " + String.valueOf(ret));
        }
        setConfig(serial, connection);

        //Set baud rate
        //setBaudRate(mConnection, serial.getBaudRate().getValue());
        return true;
    }

    @Override
    boolean getDtrEnable(final UsbDeviceConnection connection) {
        throw new UnsupportedOperationException();
    }

    @Override
    void setDtrEnable(final UsbDeviceConnection connection, final boolean value)  {
        throw new UnsupportedOperationException();
    }

    @Override
    boolean getRtsEnable(final UsbDeviceConnection connection) {
        throw new UnsupportedOperationException();
    }

    @Override
    void setRtsEnable(final UsbDeviceConnection connection, final boolean value) {
        throw new UnsupportedOperationException();
    }
}

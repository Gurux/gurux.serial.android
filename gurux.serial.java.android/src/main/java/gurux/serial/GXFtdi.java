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

import android.hardware.usb.UsbDeviceConnection;

import java.io.IOException;
import gurux.io.BaudRate;
import gurux.serial.enums.Chipset;

/**
 * FTDI chipset settings.
 */
class GXFtdi extends GXChipset {

    public Chipset getChipset() {
        return Chipset.FTDI;
    }
     /* Setup Data Constants */

    private static final int USB_SETUP_HOST_TO_DEVICE = 0x00;
    // Device Request bmRequestType transfer direction - host to device transfer
    private static final int USB_SETUP_DEVICE_TO_HOST = 0x80;
    // Device Request bmRequestType transfer direction - device to host transfer

    private static final int USB_SETUP_TYPE_STANDARD = 0x00;
    // Device Request bmRequestType type - standard
    private static final int USB_SETUP_TYPE_CLASS = 0x20;
    // Device Request bmRequestType type - class
    private static final int USB_SETUP_TYPE_VENDOR = 0x40;
    // Device Request bmRequestType type - vendor

    private static final int USB_SETUP_RECIPIENT_DEVICE = 0x00;
    // Device Request bmRequestType recipient - device
    private static final int USB_SETUP_RECIPIENT_INTERFACE = 0x01;
    // Device Request bmRequestType recipient - interface
    private static final int USB_SETUP_RECIPIENT_ENDPOINT = 0x02;
    // Device Request bmRequestType recipient - endpoint
    private static final int USB_SETUP_RECIPIENT_OTHER = 0x03;
    // Device Request bmRequestType recipient - other

    public static final int USB_ENDPOINT_IN = 0x80;
    public static final int USB_ENDPOINT_OUT = 0x00;

    private static final int FTDI_SIO_SET_DATA_REQUEST_TYPE = 0x40;
    private static final int FTDI_SIO_GET_DATA_REQUEST_TYPE = 0x80;

    private static final int FTDI_SIO_RESET = 0;
    /* Reset the port */
    private static final int FTDI_SIO_MODEM_CTRL = 1;
    /* Set the modem control register */
    private static final int FTDI_SIO_SET_FLOW_CTRL = 2;
    /* Set flow control register */
    private static final int FTDI_SIO_SET_BAUD_RATE = 3;
    /* Set baud rate */
    private static final int FTDI_SIO_SET_DATA = 4;
    /* Set the data characteristics of the port */
    private static final int FTDI_SIO_GET_MODEM_STATUS = 5;
    /* Retrieve current value of modern status register */
    private static final int FTDI_SIO_SET_EVENT_CHAR = 6;
    /* Set the event character */
    private static final int FTDI_SIO_SET_ERROR_CHAR = 7;
    /* Set the error character */
    private static final int FTDI_SIO_SET_BREAK = (0x1 << 14);
    /**
     * Lenght of modem status header.
     */
    private static final int STATUS_LENGTH = 2;

    public static boolean isUsing(final String stringManufacturer, final int vendor, final int product) {
        if ((vendor == 1027 && vendor == 24557) ||
                "FTDI".equalsIgnoreCase(stringManufacturer)) {
            return true;
        }
        return false;
    }

    @Override
    public boolean isfilterStatus() {
        return true;
    }

    @Override
    public int removeStatus(byte[] data, int size, int maxPacketSize) {
        System.arraycopy(data,
                STATUS_LENGTH,
                data,
                0,
                size - STATUS_LENGTH);
        return size - STATUS_LENGTH;
    }

    private static int getBaudRateValue(BaudRate baudRate) {
        int value = 0;
        switch (baudRate) {
            case BAUD_RATE_1200:
                value = 0x09C4;
                break;
            case BAUD_RATE_14400:
                value = 0x80D0;
                break;
            case BAUD_RATE_19200:
                value = 0x809C;
                break;
            case BAUD_RATE_2400:
                value = 0x04E2;
                break;
            case BAUD_RATE_300:
                value = 0x2710;
                break;
            case BAUD_RATE_38400:
                value = 0xC04E;
                break;
            case BAUD_RATE_4800:
                value = 0x0271;
                break;
            case BAUD_RATE_600:
                value = 0x1388;
                break;
            case BAUD_RATE_9600:
                value = 0x4138;
                break;
            default:
                throw new RuntimeException("Invalid baud rate value.");
        }
        return value;
    }

    public boolean open(GXSerial serial, UsbDeviceConnection connection, byte[] rawDescriptors) throws IOException {
        // reset
        int ret = connection.controlTransfer(0x40, 0, 0, 0, null, 0, 0);
        if (ret == -1) {
            return false;
        }
        int value = serial.getDataBits() + (serial.getParity().ordinal() << 8) + (serial.getStopBits().ordinal() << 11);

        ret = connection.controlTransfer(FTDI_SIO_SET_DATA_REQUEST_TYPE, FTDI_SIO_SET_DATA, value, 0, null,
                0, 0);
        if (ret == -1) {
            return false;
        }

        ret = connection.controlTransfer(FTDI_SIO_SET_DATA_REQUEST_TYPE, FTDI_SIO_SET_BAUD_RATE,
                getBaudRateValue(serial.getBaudRate()), 0, null, 0, 0);
        if (ret == -1) {
            return false;
        }
        return true;
    }
}

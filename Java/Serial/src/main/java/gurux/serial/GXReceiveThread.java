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
// Gurux Device Framework is distributed mInput the hope that it will be useful,
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
import android.hardware.usb.UsbEndpoint;

import java.io.ByteArrayOutputStream;
import java.lang.reflect.Array;

import gurux.common.GXSynchronousMediaBase;
import gurux.common.ReceiveEventArgs;
import gurux.common.TraceEventArgs;
import gurux.common.enums.TraceLevel;
import gurux.common.enums.TraceTypes;

/**
 * Receive thread listens serial port and sends received data to the listeners.
 *
 * @author Gurux Ltd.
 */
class GXReceiveThread extends Thread {

    /**
     * If receiver buffer is empty how long is waited for new data.
     */
    private static final int WAIT_TIME = 200;

    /**
     * Serial port mConnection.
     */
    private final UsbDeviceConnection mConnection;
    private final UsbEndpoint mInput;

    private final GXChipset mChipset;

    /**
     * Parent component where notifies are send.
     */
    private final GXSerial mParentMedia;

    /**
     * Amount of bytes received.
     */
    private long mBytesReceived = 0;

    /**
     * Constructor.
     *
     * @param parent Parent component.
     * @param conn   Usb device mConnection.
     */
    GXReceiveThread(final GXSerial parent, final UsbDeviceConnection conn, final UsbEndpoint input) {
        mConnection = conn;
        mInput = input;
        mParentMedia = parent;
        if (parent.mChipset.isfilterStatus()) {
            mChipset = parent.mChipset;
        } else {
            mChipset = null;
        }
    }

    /**
     * Get amount of received bytes.
     *
     * @return Amount of received bytes.
     */
    final long getBytesReceived() {
        return mBytesReceived;
    }

    /**
     * Reset amount of received bytes.
     */
    final void resetBytesReceived() {
        mBytesReceived = 0;
    }

    /**
     * Handle received data.
     *
     * @param buffer Received data from the serial port.
     */
    private void handleReceivedData(final byte[] buffer, final int len) {
        mBytesReceived += len;
        int totalCount = 0;
        if (mParentMedia.getIsSynchronous()) {
            TraceEventArgs arg = null;
            synchronized (mParentMedia.getSyncBase().getSync()) {
                mParentMedia.getSyncBase().appendData(buffer, 0, len);
                // Search End of Packet if given.
                if (mParentMedia.getEop() != null) {
                    if (mParentMedia.getEop() instanceof Object[]) {
                        for (Object eop : (Object[]) mParentMedia.getEop()) {
                            totalCount = GXSynchronousMediaBase.indexOf(buffer,
                                    GXSynchronousMediaBase.getAsByteArray(eop), 0, len);
                            if (totalCount != -1) {
                                break;
                            }
                        }
                    } else {
                        totalCount = GXSynchronousMediaBase.indexOf(buffer,
                                GXSynchronousMediaBase.getAsByteArray(mParentMedia.getEop()), 0, len);
                    }
                }
                if (totalCount != -1) {
                    if (mParentMedia.getTrace() == TraceLevel.VERBOSE) {
                        arg = new gurux.common.TraceEventArgs(TraceTypes.RECEIVED, buffer, 0, totalCount + 1);
                    }
                    mParentMedia.getSyncBase().setReceived();
                }
            }
            if (arg != null) {
                mParentMedia.notifyTrace(arg);
            }
        } else {
            mParentMedia.getSyncBase().resetReceivedSize();
            byte[] data = new byte[len];
            System.arraycopy(buffer, 0, data, 0, len);
            if (mParentMedia.getTrace() == TraceLevel.VERBOSE) {
                mParentMedia.notifyTrace(new gurux.common.TraceEventArgs(TraceTypes.RECEIVED, data));
            }
            ReceiveEventArgs arg = new ReceiveEventArgs(data, mParentMedia.getPort().getPort());
            mParentMedia.notifyReceived(arg);
        }
    }

    @Override
    public final void run() {
        byte[] buff = new byte[mInput.getMaxPacketSize()];
        while (!Thread.currentThread().isInterrupted()) {
            try {
                int len = mConnection.bulkTransfer(mInput, buff, buff.length, WAIT_TIME);
                //Len is -1 if timeout for some chipsets.
                //http://b.android.com/28023
                // If mConnection is closed.
                if (len == 0 && Thread.currentThread().isInterrupted()) {
                    break;
                }
                if (mChipset != null && len > 0) {
                    len = mChipset.removeStatus(buff, len, buff.length);
                }
                if (len > 0) {
                    if (mParentMedia.getReceiveDelay() > 0) {
                        long start = System.currentTimeMillis();
                        int elapsedTime = 0;
                        ByteArrayOutputStream tmp = new ByteArrayOutputStream();
                        tmp.write(buff, 0, len);
                        while ((len = mConnection.bulkTransfer(mInput, buff, 0, buff.length, mParentMedia.getReceiveDelay() - elapsedTime)) > 0) {
                            if (mChipset != null) {
                                len = mChipset.removeStatus(buff, len, buff.length);
                            }
                            tmp.write(buff, 0, len);
                            elapsedTime = (int) (System.currentTimeMillis() - start);
                            if (mParentMedia.getReceiveDelay() - elapsedTime < 1) {
                                break;
                            }
                        }
                        buff = tmp.toByteArray();
                        len = buff.length;
                    }
                    handleReceivedData(buff, len);
                }
            } catch (Exception ex) {
                if (!Thread.currentThread().isInterrupted()) {
                    mParentMedia.notifyError(new RuntimeException(ex.getMessage()));
                }
            }
        }
    }
}
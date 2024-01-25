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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbConstants;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;
import android.util.Log;
import android.util.Xml;
import android.view.View;

import androidx.fragment.app.Fragment;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import gurux.common.GXCommon;
import gurux.common.GXSync;
import gurux.common.GXSynchronousMediaBase;
import gurux.common.IGXMedia2;
import gurux.common.IGXMediaListener;
import gurux.common.MediaStateEventArgs;
import gurux.common.PropertyChangedEventArgs;
import gurux.common.ReceiveEventArgs;
import gurux.common.ReceiveParameters;
import gurux.common.TraceEventArgs;
import gurux.common.enums.MediaState;
import gurux.common.enums.TraceLevel;
import gurux.common.enums.TraceTypes;
import gurux.io.BaudRate;
import gurux.io.Parity;
import gurux.io.StopBits;
import gurux.serial.R;
import gurux.serial.enums.AvailableMediaSettings;
import gurux.serial.enums.Chipset;

/**
 * The GXSerial component determines methods that make the communication possible using serial port
 * connection.
 */
public class GXSerial implements IGXMedia2, AutoCloseable {

    private int receiveDelay;

    private int asyncWaitTime;
    /**
     * List of available serial ports.
     */
    private static List<GXPort> mPorts;
    /**
     * Amount of default data bits.
     */
    static final int DEFAULT_DATA_BITS = 8;

    /**
     * User chipset.
     */
    GXChipset mChipset = null;

    // Values are saved if port is not open and user try to set them.
    /**
     * Serial port baud rate.
     */
    private BaudRate mBaudRate = BaudRate.BAUD_RATE_9600;
    /**
     * Used data bits.
     */
    private int mDataBits = DEFAULT_DATA_BITS;
    /**
     * Stop bits.
     */
    private StopBits mStopBits = StopBits.ONE;
    /**
     * Used parity.
     */
    private Parity mParity = Parity.NONE;

    /**
     * Write timeout.
     */
    private int mWriteTimeout = 5000;
    /**
     * Read timeout.
     */
    private int mReadTimeout = 5000;

    /**
     * Receiver thread.
     */
    private GXReceiveThread mReceiver;

    /**
     * Serial port connection.
     */
    UsbDeviceConnection mConnection;

    /**
     * connection interface.
     */
    UsbInterface mUsbIf;

    UsbEndpoint mOut;

    /*
     * Name of serial port.
     */
    private GXPort mPort;
    /*
     * Synchronously class.
     */
    private GXSynchronousMediaBase mSyncBase;
    /*
     * Amount of bytes sent.
     */
    private long mBytesSend = 0;
    /*
     * Synchronous counter.
     */
    private int mSynchronous = 0;
    /*
     * Trace level.
     */
    private TraceLevel mTrace = TraceLevel.OFF;
    /*
     * End of packet.
     */
    private Object mEop;
    /*
     * Configurable settings.
     */
    private int mConfigurableSettings;

    private Context mContext;

    private Activity mActivity;

    /*
     * Receive notifications if serial port is removed or added.
     */
    private GXUsbReciever mUsbReciever;

    /**
     * Media listeners.
     */
    private final List<IGXMediaListener> mMediaListeners = new ArrayList<IGXMediaListener>();

    /**
     * Serial port listeners.
     */
    private final List<IGXSerialListener> mPortListeners = new ArrayList<IGXSerialListener>();

    /**
     * Constructor.
     *
     * @param context Context.
     */
    public GXSerial(Context context) {
        if (context == null) {
            throw new IllegalArgumentException("context");
        }
        mContext = context;
        mUsbReciever = new GXUsbReciever(this);
        String name = "gurux.serial";
        IntentFilter filter2 = new IntentFilter(name);
        filter2.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);
        filter2.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
        mContext.registerReceiver(mUsbReciever, filter2);
        mSyncBase = new GXSynchronousMediaBase(200);
        setConfigurableSettings(AvailableMediaSettings.ALL.getValue());
    }

    /**
     * Constructor.
     *
     * @param activity Activity.
     */
    public GXSerial(Activity activity) {
        this((Context) activity);
        mActivity = activity;
    }

    /**
     * Constructor.
     *
     * @param port     Serial port.
     * @param baudRate Baud rate.
     * @param dataBits Data bits.
     * @param parity   Parity.
     * @param stopBits Stop bits.
     */
    public GXSerial(Context context, final String port, final BaudRate baudRate,
                    final int dataBits, final Parity parity,
                    final StopBits stopBits) {
        this(context);
        for (GXPort it : getPorts()) {
            if (port.compareToIgnoreCase(it.getPort()) == 0) {
                setPort(it);
                break;
            }
        }
        setBaudRate(baudRate);
        setDataBits(dataBits);
        setParity(parity);
        setStopBits(stopBits);
    }


    /**
     * Constructor.
     *
     * @param port     Serial port.
     * @param baudRate Baud rate.
     * @param dataBits Data bits.
     * @param parity   Parity.
     * @param stopBits Stop bits.
     */
    public GXSerial(final Activity activity, final String port, final BaudRate baudRate,
                    final int dataBits, final Parity parity,
                    final StopBits stopBits) {
        this((Context) activity);
        mActivity = activity;
        for (GXPort it : getPorts()) {
            if (port.compareToIgnoreCase(it.getPort()) == 0) {
                setPort(it);
                break;
            }
        }
        setBaudRate(baudRate);
        setDataBits(dataBits);
        setParity(parity);
        setStopBits(stopBits);
    }


    /**
     * Find used chipset.
     *
     * @param stringManufacturer Manufacturer name.
     * @param vendor             Vendor ID.
     * @param productId          Product ID.
     * @return Chipset settings.
     */
    private static GXChipset getChipSet(final String stringManufacturer, int vendor, int productId) {
        if (GXCP21xx.isUsing(stringManufacturer, vendor, productId)) {
            return new GXCP21xx();
        } else if (GXProfilic.isUsing(stringManufacturer, vendor, productId)) {
            return new GXProfilic();
        } else if (GXFtdi.isUsing(stringManufacturer, vendor, productId)) {
            return new GXFtdi();
        } else if (GXFtdi.isUsing(stringManufacturer, vendor, productId)) {
            return new GXFtdi();
        } else if (GXCh34x.isUsing(stringManufacturer, vendor, productId)) {
            return new GXCh34x();
        }
        return null;
    }

    private static GXChipset getChipSet(Chipset chipset) {
        GXChipset value;
        switch (chipset) {
            case PROFILIC:
                value = new GXProfilic();
                break;
            case CP21XX:
                value = new GXCP21xx();
                break;
            case FTDI:
                value = new GXFtdi();
                break;
            case CH34X:
                value = new GXCh34x();
                break;
            default:
                throw new RuntimeException("Invalid chipset.");
        }
        return value;
    }

    /**
     * Returns synchronous class used to communicate synchronously.
     *
     * @return Synchronous class.
     */
    final GXSynchronousMediaBase getSyncBase() {
        return mSyncBase;
    }

    /**
     * Remove USB serial port.
     *
     * @param device USB device to remove.
     */
    void removePort(final UsbDevice device) {
        synchronized (GXPort.class) {
            int pos = 0;
            for (GXPort port : mPorts) {
                if (port.getPort().equals(device.getDeviceName())) {
                    mPorts.remove(port);
                    for (IGXSerialListener it : mPortListeners) {
                        it.onPortRemoved(port, pos);
                    }
                    break;
                }
                ++pos;
            }
        }
    }

    void addPort(final UsbManager m, final UsbDevice device, final boolean notify) {
        UsbManager manager = m;
        if (manager == null) {
            manager = (UsbManager) mContext.getSystemService(Context.USB_SERVICE);
        }
        byte[] buffer = new byte[255];
        String name = "gurux.serial";
        PendingIntent permissionIntent = PendingIntent.getBroadcast(mContext, 0,
                new Intent(name),  PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);
        UsbEndpoint in = null, out = null;
        if (!manager.hasPermission(device)) {
            manager.requestPermission(device, permissionIntent);
            if (!manager.hasPermission(device)) {
                return;
            }
        }
        for (int i = 0; i != device.getInterfaceCount(); ++i) {
            UsbInterface usbIf = device.getInterface(i);
            for (int pos = 0; pos != usbIf.getEndpointCount(); ++pos) {
                int direction = usbIf.getEndpoint(pos).getDirection();
                if (usbIf.getEndpoint(pos).getType() == UsbConstants.USB_ENDPOINT_XFER_BULK) {
                    if (direction == UsbConstants.USB_DIR_IN) {
                        in = usbIf.getEndpoint(pos);
                    } else if (direction == UsbConstants.USB_DIR_OUT) {
                        out = usbIf.getEndpoint(pos);
                    }
                    if (out != null && in != null) {
                        break;
                    }
                }
            }
            if (out != null && in != null) {
                try {
                    GXPort port = new GXPort();
                    port.setPort(device.getDeviceName());
                    port.setVendorId(device.getVendorId());
                    port.setProductId(device.getProductId());
                    Map.Entry<String, String> info = find(mContext, device.getVendorId(), device.getProductId());
                    if (info != null) {
                        port.setVendor(info.getKey());
                        port.setProduct(info.getValue());
                    }
                    UsbDeviceConnection connection = manager.openDevice(device);
                    try {
                        port.setSerial(connection.getSerial());
                        byte[] rawDescriptors = connection.getRawDescriptors();
                        port.setRawDescriptors(rawDescriptors);
                        String man = getManufacturer(connection, rawDescriptors, buffer);
                        String prod = getProduct(connection, rawDescriptors,buffer);
                        port.setManufacturer(man + ": " + prod);
                        GXChipset chipset = getChipSet(man, device.getVendorId(), device.getProductId());
                        if (chipset != null) {
                            port.setChipset(chipset.getChipset());
                        }
                    } finally {
                        connection.close();
                    }
                    synchronized (GXPort.class) {
                        mPorts.add(port);
                    }
                    if (notify) {
                        for (IGXSerialListener it : mPortListeners) {
                            it.onPortAdded(port);
                        }
                    }
                } catch (IOException e) {
                    Log.i("gurux.serial", e.getMessage());
                }
            }
        }
    }

    /**
     * Gets an array of serial port names for the current computer.
     *
     * @return Collection of available serial ports.
     */
    public GXPort[] getPorts() {
        synchronized (GXPort.class) {
           if (mPorts == null)
           {
                mPorts = new ArrayList<GXPort>();
                UsbManager manager = (UsbManager) mContext.getSystemService(Context.USB_SERVICE);
                Map<String, UsbDevice> devices = manager.getDeviceList();
                for (Map.Entry<String, UsbDevice> it : devices.entrySet()) {
                    addPort(manager, it.getValue(), false);
                }
                if (mPorts.isEmpty())
                {
                    mPorts = null;
                    return new GXPort[0];
                }
            }
        }
        return mPorts.toArray(new GXPort[mPorts.size()]);
    }

    /**
     * Get baud rates supported by given serial port.
     *
     * @param port Selected serial port.
     * @return Collection of available baud rates.
     */
    public static final int[] getAvailableBaudRates(final GXPort port) {
        return new int[]{BaudRate.BAUD_RATE_300.getValue(), BaudRate.BAUD_RATE_600.getValue(),
                BaudRate.BAUD_RATE_1200.getValue(), BaudRate.BAUD_RATE_2400.getValue(),
                BaudRate.BAUD_RATE_4800.getValue(), BaudRate.BAUD_RATE_9600.getValue(),
                BaudRate.BAUD_RATE_19200.getValue(), BaudRate.BAUD_RATE_38400.getValue()};
    }

    @Override
    protected final void finalize() throws Throwable {
        super.finalize();
        if (isOpen()) {
            close();
        }
    }

    @Override
    public final TraceLevel getTrace() {
        return mTrace;
    }

    @Override
    public final void setTrace(final TraceLevel value) {
        mTrace = value;
        mSyncBase.setTrace(value);
    }

    /**
     * Notify that property has changed.
     *
     * @param info Name of changed property.
     */
    private void notifyPropertyChanged(final String info) {
        if (mActivity != null) {
            //New data is coming from worker thread.
            mActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    for (IGXMediaListener listener : mMediaListeners) {
                        listener.onPropertyChanged(this, new PropertyChangedEventArgs(info));
                    }
                }
            });
        } else {
            for (IGXMediaListener listener : mMediaListeners) {
                listener.onPropertyChanged(this, new PropertyChangedEventArgs(info));
            }
        }
    }

    /**
     * Notify clients from error occurred.
     *
     * @param ex Occurred error.
     */
    final void notifyError(final RuntimeException ex) {
        if (mActivity != null) {
            //New data is coming from worker thread.
            mActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    for (IGXMediaListener listener : mMediaListeners) {
                        listener.onError(this, ex);
                        if (mTrace.ordinal() >= TraceLevel.ERROR.ordinal()) {
                            listener.onTrace(this, new TraceEventArgs(TraceTypes.ERROR, ex));
                        }
                    }
                }
            });
        } else {
            for (IGXMediaListener listener : mMediaListeners) {
                listener.onError(this, ex);
                if (mTrace.ordinal() >= TraceLevel.ERROR.ordinal()) {
                    listener.onTrace(this, new TraceEventArgs(TraceTypes.ERROR, ex));
                }
            }
        }
    }

    /**
     * Notify clients from new data received.
     *
     * @param arg Received event argument.
     */
    final void notifyReceived(final ReceiveEventArgs arg) {
        if (mActivity != null) {
            //New data is coming from worker thread.
            mActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    for (IGXMediaListener listener : mMediaListeners) {
                        listener.onReceived(this, arg);
                    }
                }
            });
        } else {
            for (IGXMediaListener listener : mMediaListeners) {
                listener.onReceived(this, arg);
            }
        }
    }

    /**
     * Notify clients from trace events.
     *
     * @param arg Trace event argument.
     */
    final void notifyTrace(final TraceEventArgs arg) {
        if (mActivity != null) {
            //New data is coming from worker thread.
            mActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    for (IGXMediaListener listener : mMediaListeners) {
                        listener.onTrace(this, arg);
                    }
                }
            });
        } else {
            for (IGXMediaListener listener : mMediaListeners) {
                listener.onTrace(this, arg);
            }
        }
    }

    @Override
    public final int getConfigurableSettings() {
        return mConfigurableSettings;
    }

    @Override
    public final void setConfigurableSettings(final int value) {
        mConfigurableSettings = value;
    }

    @Override
    public final void send(final Object data, final String target) throws Exception {
        send(data);
    }

    public final void send(final Object data) throws Exception {
        if (mOut == null) {
            throw new RuntimeException("Serial port is not open.");
        }
        if (mTrace == TraceLevel.VERBOSE) {
            notifyTrace(new TraceEventArgs(TraceTypes.SENT, data));
        }
        // Reset last position if end of packet is used.
        mSyncBase.resetLastPosition();
        byte[] buff = GXSynchronousMediaBase.getAsByteArray(data);
        if (buff == null) {
            throw new IllegalArgumentException("Data send failed. Invalid data.");
        }
        int ret, pos = 0, dataSize = mOut.getMaxPacketSize();
        while (pos != buff.length) {
            if (buff.length - pos < dataSize) {
                dataSize = buff.length - pos;
            }
            ret = mConnection.bulkTransfer(mOut, buff, pos, dataSize, mWriteTimeout);
            if (ret != dataSize) {
                throw new IllegalArgumentException("Data send failed.");
            }
            pos += ret;
        }
        this.mBytesSend += buff.length;
    }

    /**
     * Notify client from media state change.
     *
     * @param state New media state.
     */
    private void notifyMediaStateChange(final MediaState state) {
        for (IGXMediaListener listener : mMediaListeners) {
            if (mTrace.ordinal() >= TraceLevel.ERROR.ordinal()) {
                listener.onTrace(this, new TraceEventArgs(TraceTypes.INFO, state));
            }
            listener.onMediaStateChange(this, new MediaStateEventArgs(state));
        }
    }

    private static final int MANUFACTURER_INDEX = 14;
    private static final int PRODUCT_INDEX = 15;
    private static final int STD_USB_REQUEST_GET_DESCRIPTOR = 0x06;
    private static final int LIBUSB_DT_STRING = 0x03;

    private static String getManufacturer(UsbDeviceConnection connection, byte[] rawDescriptors,
                                          byte[] buff) throws UnsupportedEncodingException {
        int lengthManufacturer = connection.controlTransfer(
                UsbConstants.USB_DIR_IN | UsbConstants.USB_TYPE_STANDARD,
                STD_USB_REQUEST_GET_DESCRIPTOR,
                (LIBUSB_DT_STRING << 8) | rawDescriptors[MANUFACTURER_INDEX],
                0,
                buff,
                0xFF,
                0);
        if (lengthManufacturer > 0) {
            return new String(buff, 2, lengthManufacturer - 2, "UTF-16LE");
        }
        return null;
    }

    private static String getProduct(UsbDeviceConnection connection, byte[] rawDescriptors,
                                     byte[] buff) throws UnsupportedEncodingException {
        int lengthProduct = connection.controlTransfer(
                UsbConstants.USB_DIR_IN | UsbConstants.USB_TYPE_STANDARD,
                STD_USB_REQUEST_GET_DESCRIPTOR,
                (LIBUSB_DT_STRING << 8) | rawDescriptors[PRODUCT_INDEX],
                0,
                buff,
                0xFF,
                0);
        if (lengthProduct > 0) {
            return new String(buff, 2, lengthProduct - 2, "UTF-16LE");
        }
        return null;
    }

    /**
     * Find vendor and product name.
     *
     * @param context
     * @param vendor  Vendor ID.
     * @param product Product ID.
     * @return Vendor and product entry or null.
     * @throws IOException
     */
    private static Map.Entry<String, String> find(Context context, int vendor,
                                                  int product) throws IOException {
        InputStream is = context.getResources().openRawResource(R.raw.usbs);
        if (is == null) {
            throw new IOException("Invalid USB list.");
        }
        String vendorName = null, productName = null;
        BufferedReader r = new BufferedReader(new InputStreamReader(is));
        String line;
        while ((line = r.readLine()) != null) {
            if (line.startsWith("C 00")) {
                // If all manufacturers are read.
                break;
            }
            if (!line.isEmpty() && !line.startsWith("#")) {
                if (vendorName == null) {
                    // Find vendor.
                    if (!line.startsWith("\t")) {
                        int v = Integer.parseInt(line.substring(0, 4), 16);
                        if (v == vendor) {
                            vendorName = line.substring(5).trim();
                        }
                    }
                } else {
                    // Find product.
                    if (!line.startsWith("\t")) {
                        break;
                    }
                    int v = Integer.parseInt(line.substring(1, 5), 16);
                    if (v == product) {
                        productName = line.substring(6).trim();
                        break;
                    }
                }
            }
        }
        if (vendorName == null) {
            return null;
        }
        return new AbstractMap.SimpleEntry<String, String>(vendorName, productName);
    }

    @Override
    public final void open() throws Exception {
        close();
        try {
            if (mPort == null) {
                throw new IllegalArgumentException("Serial port is not selected.");
            }
            if (!mContext.getPackageManager().hasSystemFeature("android.hardware.usb.host")) {
                throw new IllegalArgumentException("Usb feature is not supported.");
            }

            synchronized (mSyncBase.getSync()) {
                mSyncBase.resetLastPosition();
            }
            notifyMediaStateChange(MediaState.OPENING);
            if (mTrace.ordinal() >= TraceLevel.INFO.ordinal()) {
                String eopString = "None";
                if (getEop() instanceof byte[]) {
                    eopString = GXCommon.bytesToHex((byte[]) getEop());
                } else if (getEop() != null) {
                    eopString = getEop().toString();
                }
                notifyTrace(new TraceEventArgs(TraceTypes.INFO,
                        "Settings: Port: " + this.getPort() + " Baud Rate: " + getBaudRate()
                                + " Data Bits: " + String.valueOf(getDataBits()) + " Parity: "
                                + getParity().toString() + " Stop Bits: " + getStopBits().toString() + " Eop:"
                                + eopString));
            }
            UsbEndpoint in = null;
            UsbManager manager = (UsbManager) mContext.getSystemService(Context.USB_SERVICE);
            Map<String, UsbDevice> devices = manager.getDeviceList();
            int vendor = 0, productId = 0;
            for (Map.Entry<String, UsbDevice> it : devices.entrySet()) {
                if (it.getKey().compareTo(mPort.getPort()) == 0) {
                    mConnection = manager.openDevice(it.getValue());
                    UsbInterface usbIf = it.getValue().getInterface(0);
                    for (int pos = 0; pos != usbIf.getEndpointCount(); ++pos) {
                        int direction = usbIf.getEndpoint(pos).getDirection();
                        if (usbIf.getEndpoint(pos).getType() == UsbConstants.USB_ENDPOINT_XFER_BULK) {
                            if (direction == UsbConstants.USB_DIR_IN) {
                                in = usbIf.getEndpoint(pos);
                            } else if (direction == UsbConstants.USB_DIR_OUT) {
                                mOut = usbIf.getEndpoint(pos);
                            }
                            if (mOut != null && in != null) {
                                vendor = it.getValue().getVendorId();
                                productId = it.getValue().getProductId();
                                //Claims exclusive access to a Usb interface.
                                //This must done to send or receive data.
                                mConnection.claimInterface(usbIf, true);
                                break;
                            }
                        }
                    }
                    break;
                }
            }
            if (mOut == null || in == null) {
                throw new IllegalArgumentException("Invalid serial port endpoint.");
            }
            if (mChipset == null) {
                throw new RuntimeException("Invalid vendor id: " + vendor + " product Id: " + productId);
            }
            byte[] rawDescriptors = mConnection.getRawDescriptors();
            if (!mChipset.open(this, mConnection, rawDescriptors)) {
                throw new Exception("Failed to open serial port.");
            }
            mReceiver = new GXReceiveThread(this, mConnection, in);
            mReceiver.start();
            notifyMediaStateChange(MediaState.OPEN);
        } catch (Exception ex) {
            close();
            throw ex;
        }
    }

    @Override
    public final void close() {
        if (mUsbIf != null) {
            mConnection.releaseInterface(mUsbIf);
            mUsbIf = null;
        }

        if (mConnection != null) {
            if (mReceiver != null) {
                mReceiver.interrupt();
                mReceiver = null;
            }
            try {
                notifyMediaStateChange(MediaState.CLOSING);
            } catch (RuntimeException ex) {
                notifyError(ex);
                throw ex;
            } finally {
                mOut = null;
                mConnection.close();
                mConnection = null;
                notifyMediaStateChange(MediaState.CLOSED);
                mBytesSend = 0;
                mSyncBase.resetReceivedSize();
            }
        }
    }

    /**
     * Used baud rate for communication. Can be changed without disconnecting.
     *
     * @return Used baud rate.
     */

    public final BaudRate getBaudRate() {
        return mBaudRate;
    }

    /**
     * Set new baud rate.
     *
     * @param value New baud rate.
     */
    public final void setBaudRate(final BaudRate value) {
        boolean change = getBaudRate() != value;
        if (change) {
            mBaudRate = value;
            notifyPropertyChanged("BaudRate");
        }
    }

    /**
     * Get is Data Terminal Ready (DTR) signal enabled.
     *
     * @return Is DTR enabled.
     */
    public final boolean getDtrEnable() {
        if (isOpen()) {
            return mChipset.getDtrEnable(mConnection);
        }
        return false;
    }

    /**
     * Set is Data Terminal Ready (DTR) signal enabled.
     *
     * @param value
     *            Is DTR enabled.
     */
    public final void setDtrEnable(final boolean value) throws IOException  {
        if (isOpen())
        {
            boolean change = getDtrEnable() != value;
            mChipset.setDtrEnable(mConnection, value);
            if (change) {
                notifyPropertyChanged("DtrEnable");
            }
        }
    }

    /**
     * Gets a value indicating whether the Request to Send (RTS) signal is
     * enabled during serial communication.
     *
     * @return Is RTS enabled.
     */
    public final boolean getRtsEnable() {
        if (isOpen()) {
            return mChipset.getRtsEnable(mConnection);
        }
        return false;
    }

    /**
     * Sets a value indicating whether the Request to Send (RTS) signal is
     * enabled during serial communication.
     *
     * @param value
     *            Is RTS enabled.
     */
    public final void setRtsEnable(final boolean value) throws IOException  {
        if (isOpen()) {
            boolean change = getRtsEnable() != value;
            mChipset.setRtsEnable(mConnection, value);
            if (change) {
                notifyPropertyChanged("RtsEnable");
            }
        }
    }

    /**
     * Used serial port chipset.
     *
     * @return Used chipset.
     */
    public final Chipset getChipset() {
        if (mChipset == null) {
            return Chipset.NONE;
        }
        return mChipset.getChipset();
    }

    /**
     * Set new serial port chipset.
     *
     * @param value New chipset.
     */
    public final void setChipset(final Chipset value) {
        boolean change = mChipset == null || mChipset.getChipset() != value;
        if (change) {
            mChipset = getChipSet(value);
            notifyPropertyChanged("Chipset");
        }
    }

    /**
     * Gets the standard length of data bits per byte.
     *
     * @return Amount of data bits.
     */
    public final int getDataBits() {
        return mDataBits;
    }

    /**
     * Sets the standard length of data bits per byte.
     *
     * @param value Amount of data bits.
     */
    public final void setDataBits(final int value) {
        boolean change;
        change = getDataBits() != value;
        if (change) {
            mDataBits = value;
            notifyPropertyChanged("DataBits");
        }
    }

    @Override
    public final boolean isOpen() {
        return mConnection != null;
    }

    /**
     * Gets the parity-checking protocol.
     *
     * @return Used parity.
     */
    public final Parity getParity() {
        return mParity;
    }

    /**
     * Sets the parity-checking protocol.
     *
     * @param value Used parity.
     */
    public final void setParity(final Parity value) {
        boolean change;
        change = getParity() != value;
        if (change) {
            mParity = value;
            notifyPropertyChanged("Parity");
        }
    }

    /**
     * Gets the port for communications, including but not limited to all available COM ports.
     *
     * @return Used serial port
     */
    public final GXPort getPort() {
        return mPort;
    }

    /**
     * Sets the port for communications, including but not limited to all available COM ports.
     *
     * @param value Used serial port.
     */
    public final void setPort(final GXPort value) {
        boolean change;
        change = value != mPort;
        mPort = value;
        if (change) {
            notifyPropertyChanged("PortName");
            if (value != null && value.getVendorId() != 0 && value.getProductId() != 0) {
                mChipset = getChipSet(null, value.getVendorId(), value.getProductId());
            }
        }
    }

    /**
     * Gets the number of milliseconds before a time-out occurs when a read operation does not finish.
     *
     * @return Read timeout.
     */
    public final int getReadTimeout() {
        return mReadTimeout;
    }

    /**
     * Sets the number of milliseconds before a time-out occurs when a read operation does not finish.
     *
     * @param value Read timeout.
     */
    public final void setReadTimeout(final int value) {
        boolean change = mReadTimeout != value;
        mReadTimeout = value;
        if (change) {
            notifyPropertyChanged("ReadTimeout");
        }
    }

    /**
     * Gets the standard number of stop bits per byte.
     *
     * @return Used stop bits.
     */
    public final StopBits getStopBits() {
        return mStopBits;
    }

    /**
     * Sets the standard number of stop bits per byte.
     *
     * @param value Used stop bits.
     */
    public final void setStopBits(final StopBits value) {
        boolean change;
        change = getStopBits() != value;
        if (change) {
            mStopBits = value;
            notifyPropertyChanged("StopBits");
        }
    }

    /**
     * Gets the number of milliseconds before a time-out occurs when a write operation does not
     * finish.
     *
     * @return Used time out.
     */
    public final int getWriteTimeout() {
        return mWriteTimeout;
    }

    /**
     * Sets the number of milliseconds before a time-out occurs when a write operation does not
     * finish.
     *
     * @param value Used time out.
     */
    public final void setWriteTimeout(final int value) {
        boolean change = mWriteTimeout != value;
        if (change) {
            mWriteTimeout = value;
            notifyPropertyChanged("WriteTimeout");
        }
    }

    @Override
    public final <T> boolean receive(final ReceiveParameters<T> args) {
        return mSyncBase.receive(args);
    }

    @Override
    public final long getBytesSent() {
        return mBytesSend;
    }

    @Override
    public final long getBytesReceived() {
        return mReceiver.getBytesReceived();
    }

    @Override
    public final void resetByteCounters() {
        mBytesSend = 0;
        mReceiver.resetBytesReceived();
    }

    @Override
    public final String getSettings() {
        StringBuilder sb = new StringBuilder();
        String nl = System.getProperty("line.separator");
        if (mPort != null) {
            sb.append("<Port>");
            sb.append(mPort.getPort());
            sb.append("</Port>");
            sb.append(nl);
        }
        if (mBaudRate != BaudRate.BAUD_RATE_9600) {
            sb.append("<BaudRate>");
            sb.append(String.valueOf(mBaudRate.getValue()));
            sb.append("</BaudRate>");
            sb.append(nl);
        }
        if (mStopBits != StopBits.ONE) {
            sb.append("<StopBits>");
            sb.append(String.valueOf(mStopBits.ordinal()));
            sb.append("</StopBits>");
            sb.append(nl);
        }
        if (mParity != Parity.NONE) {
            sb.append("<Parity>");
            sb.append(String.valueOf(mParity.ordinal()));
            sb.append("</Parity>");
            sb.append(nl);
        }
        if (mDataBits != DEFAULT_DATA_BITS) {
            sb.append("<DataBits>");
            sb.append(String.valueOf(mDataBits));
            sb.append("</DataBits>");
            sb.append(nl);
        }
        return sb.toString();
    }

    private static String readText(XmlPullParser parser) throws
            IOException, XmlPullParserException {
        String result = "";
        if (parser.next() == XmlPullParser.TEXT) {
            result = parser.getText();
            parser.nextTag();
        }
        return result;
    }

    @Override
    public final void setSettings(final String value) {
        //Reset to default values.
        mPort = null;
        mBaudRate = BaudRate.BAUD_RATE_9600;
        mStopBits = StopBits.ONE;
        mParity = Parity.NONE;
        mDataBits = DEFAULT_DATA_BITS;
        if (value != null && !value.isEmpty()) {
            try {
                XmlPullParser parser = Xml.newPullParser();
                parser.setInput(new StringReader(value));
                int event;
                while ((event = parser.next()) != XmlPullParser.END_TAG && event != XmlPullParser.END_DOCUMENT) {
                    if (event == XmlPullParser.START_TAG) {
                        String target = parser.getName();
                        boolean found = false;
                        if ("Port".equalsIgnoreCase(target)) {
                            String name = readText(parser);
                            for (GXPort it : getPorts()) {
                                if (name.equalsIgnoreCase(it.getPort())) {
                                    setPort(it);
                                    found = true;
                                    break;
                                }
                            }
                            if (!found) {
                                setPort(null);
                            }
                        } else if ("BaudRate".equalsIgnoreCase(target)) {
                            setBaudRate(BaudRate.forValue(Integer.parseInt(readText(parser))));
                        } else if ("StopBits".equalsIgnoreCase(target)) {
                            setStopBits(StopBits.values()[Integer.parseInt(readText(parser))]);
                        } else if ("Parity".equalsIgnoreCase(target)) {
                            setParity(Parity.values()[Integer.parseInt(readText(parser))]);
                        } else if ("DataBits".equalsIgnoreCase(target)) {
                            setDataBits(Integer.parseInt(readText(parser)));
                        }
                    }
                }
            } catch (Exception e) {
                throw new RuntimeException(e.getMessage());
            }
        }
    }

    public boolean properties(final Activity activity) {
        GXPropertiesBase.setSerial(this);
        Intent intent = new Intent(activity, GXProperties.class);
        activity.startActivity(intent);
        return true;
    }

    public Fragment properties() {
        GXPropertiesBase.setSerial(this);
        return new GXPropertiesFragment();
    }

    public void showBaudRate(View view) {
        try {
            int[] tmp = getAvailableBaudRates(null);
            String[] values = new String[tmp.length];
            int pos = 0;
            for (int it : tmp) {
                values[pos] = String.valueOf(it);
                ++pos;
            }
            AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
            builder.setTitle(R.string.baudRate)
                    .setItems(values, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            // The 'which' argument contains the index position
                            // of the selected item
                        }
                    });
        } catch (Exception ex) {
        }
    }

    @Override
    public final void copy(final Object target) {
        GXSerial tmp = (GXSerial) target;
        setPort(tmp.getPort());
        setBaudRate(tmp.getBaudRate());
        setStopBits(tmp.getStopBits());
        setParity(tmp.getParity());
        setDataBits(tmp.getDataBits());
    }

    @Override
    public final String getName() {
        if (getPort() == null) {
            return "";
        }
        return getPort().getPort();
    }

    @Override
    public final String getMediaType() {
        return "Serial";
    }

    @Override
    public final Object getSynchronous() {
        synchronized (this) {
            int[] tmp = new int[]{mSynchronous};
            GXSync obj = new GXSync(tmp);
            mSynchronous = tmp[0];
            return obj;
        }
    }

    @Override
    public final boolean getIsSynchronous() {
        synchronized (this) {
            return mSynchronous != 0;
        }
    }

    @Override
    public final void resetSynchronousBuffer() {
        synchronized (mSyncBase.getSync()) {
            mSyncBase.resetReceivedSize();
        }
    }

    @Override
    public final void validate() {
        if (getPort() == null) {
            throw new RuntimeException("Invalid port name.");
        }
    }

    @Override
    public final Object getEop() {
        return mEop;
    }

    @Override
    public final void setEop(final Object value) {
        mEop = value;
    }

    @Override
    public final void addListener(final IGXMediaListener listener) {
        mMediaListeners.add(listener);
        if (listener instanceof IGXSerialListener) {
            mPortListeners.add((IGXSerialListener) listener);
        }
    }

    @Override
    public final void removeListener(final IGXMediaListener listener) {
        mMediaListeners.remove(listener);
        if (listener instanceof IGXSerialListener) {
            mPortListeners.remove((IGXSerialListener) listener);
        }
    }

    @Override
    public int getReceiveDelay() {
        return receiveDelay;
    }

    @Override
    public void setReceiveDelay(final int value) {
        receiveDelay = value;
    }

    @Override
    public int getAsyncWaitTime() {
        return asyncWaitTime;
    }

    @Override
    public void setAsyncWaitTime(final int value) {
        asyncWaitTime = value;
    }

    @Override
    public Object getAsyncWaitHandle() {
        return null;
    }
}
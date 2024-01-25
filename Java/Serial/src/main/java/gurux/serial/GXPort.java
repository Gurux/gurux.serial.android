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

import gurux.common.GXCommon;
import gurux.serial.enums.Chipset;

public class GXPort {
    /**
     * USB port.
     */
    private String mPort;

    /**
     * Manufacturer name.
     */
    private String mManufacturer;

    /**
     * Vendor name.
     */
    private String mVendor;
    /**
     * Product name.
     */
    private String mProduct;

    /**
     * Product ID.
     */
    private int mProductId;

    /**
     * Vendor ID.
     */
    private int mVendorId;

    /**
     * Serial number.
     */
    private String mSerial;

    /**
     * Raw descriptors.
     */
    private byte[] mRawDescriptors;

    /**
     * Used serial port chipset.
     */
    Chipset mChipset = Chipset.NONE;

    /**
     * @return USB port.
     */
    public final String getPort() {
        return mPort;
    }

    /**
     * @param value USB port.
     */
    public final void setPort(final String value) {
        mPort = value;
    }

    /**
     * @return Vendor name
     */
    public final String getVendor() {
        return mVendor;
    }

    /**
     * @param value Vendor name
     */
    public final void setVendor(final String value) {
        mVendor = value;
    }

    /**
     * @return Product name.
     */
    public final String getProduct() {
        return mProduct;
    }

    /**
     * @param value Product name.
     */
    public final void setProduct(final String value) {
        mProduct = value;
    }

    /**
     * @return Product Id.
     */
    public final int getProductId() {
        return mProductId;
    }

    /**
     * @param value Product Id.
     */
    public final void setProductId(final int value) {
        mProductId = value;
    }

    /**
     * @return Vendor name
     */
    public final int getVendorId() {
        return mVendorId;
    }

    /**
     * @param value Vendor name
     */
    public final void setVendorId(final int value) {
        mVendorId = value;
    }


    /**
     * Used serial port chipset.
     *
     * @return Used chipset.
     */
    public final Chipset getChipset() {
        return mChipset;
    }

    /**
     * Set new serial port chipset.
     *
     * @param value New chipset.
     */
    public final void setChipset(final Chipset value) {
        mChipset = value;
    }

    /**
     * @return Manufacturer name.
     */
    public String getManufacturer() {
        return mManufacturer;
    }

    /**
     * @param value Manufacturer name.
     */
    public void setManufacturer(String value) {
        mManufacturer = value;
    }


    @Override
    public String toString() {
        if (mProduct != null && !mProduct.isEmpty()) {
            return mProduct;
        }
        if (mVendor != null && !mVendor.isEmpty()) {
            return mVendor;
        }
        return mPort;
    }

    public String getInfo() {
        String nl = System.getProperty("line.separator");
        StringBuffer sb = new StringBuffer();
        sb.append("Manufacturer info: ");
        sb.append(mManufacturer);
        sb.append(nl);
        if (mVendorId != 0) {
            sb.append("Vendor: ");
            sb.append(mVendor);
            sb.append(" ID: ");
            sb.append(Integer.toHexString(mVendorId));
            sb.append(nl);
        }
        sb.append("Product: ");
        sb.append(mProduct);
        sb.append(" ID: ");
        sb.append(Integer.toHexString(mProductId));
        sb.append(nl);
        if (mSerial != null) {
            sb.append("Serial: ");
            sb.append(mSerial);
            sb.append(nl);
        }
        sb.append("Chipset: ");
        if (mChipset != Chipset.NONE) {
            sb.append(mChipset.toString());
        } else {
            sb.append("Unknown");
        }
        sb.append(nl);
        if (mRawDescriptors != null) {
            sb.append("Raw: ");
            sb.append(GXCommon.bytesToHex(mRawDescriptors));
            sb.append(nl);
        }
        return sb.toString();
    }


    /**
     * @return Serial number.
     */
    public String getSerial() {
        return mSerial;
    }

    /**
     * @param value Serial number.
     */
    public void setSerial(String value) {
        mSerial = value;
    }

    /**
     * @return Raw descriptors.
     */
    public byte[] getRawDescriptors() {
        return mRawDescriptors;
    }

    /**
     * @param value Raw descriptors.
     */
    public void setRawDescriptors(byte[] value) {
        mRawDescriptors = value;
    }
}
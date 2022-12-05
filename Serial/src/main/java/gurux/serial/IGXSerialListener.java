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

/**
 * Serial port listener interface. This interface is used when new USB port is added or removed.
 *
 * @author Gurux Ltd
 */
public interface IGXSerialListener {
    /**
     * Called when the when new USB port is added.
     *
     * @param port Added serial port.
     */
    void onPortAdded(final GXPort port);

    /**
     * Called when the when new USB port is removed.
     *
     * @param port  Removed serial port.
     * @param index Index where port is removed.
     */
    void onPortRemoved(final GXPort port, final int index);
}

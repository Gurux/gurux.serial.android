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
// This code is licensed under the GNU General Public License v2.
// Full text may be retrieved at http://www.gnu.org/licenses/gpl-2.0.txt
//---------------------------------------------------------------------------

using Android.Bluetooth;
using Android.Content;
using Android.Hardware.Usb;

namespace Gurux.Serial
{
    /// <summary>
    /// Handle USB register events.
    /// </summary>
    class GXUsbReciever : BroadcastReceiver
    {
        private readonly GXSerial _serial;

        /// <summary>
        /// Constructor.
        /// </summary>
        /// <param name="serial">Serial owner.</param>
        public GXUsbReciever(GXSerial serial)
        {
            _serial = serial;
        }

        public override void OnReceive(Context context, Intent intent)
        {
            if (UsbManager.ActionUsbDeviceDetached == intent.Action)
            {
                lock (this)
                {
                    UsbDevice device = (UsbDevice)intent.GetParcelableExtra(UsbManager.ExtraDevice,
                        Java.Lang.Class.FromType(typeof(UsbDevice)));
                    _serial.RemovePort(device);
                }
            }
            else if (UsbManager.ActionUsbDeviceAttached == intent.Action)
            {
                lock (this)
                {
                    UsbDevice device = (UsbDevice)intent.GetParcelableExtra(UsbManager.ExtraDevice,
                        Java.Lang.Class.FromType(typeof(UsbDevice)));
                    _serial.AddPort(null, device, true);
                }
            }
        }
    }
}

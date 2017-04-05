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

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import gurux.io.BaudRate;
import gurux.io.Parity;
import gurux.io.StopBits;

import static org.junit.Assert.*;

/**
 * Instrumentation test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class GXSerialInstrumentedTest {
    @Test
    public void useAppContext() throws Exception {
        // Context of the app under test.
        Context appContext = InstrumentationRegistry.getTargetContext();
        assertEquals("gurux.serial.java.android.test", appContext.getPackageName());
    }

    /**
     * Get available ports.
     */
    @Test
    public void availablePorts() {
        Context appContext = InstrumentationRegistry.getTargetContext();
        GXSerial serial = new GXSerial(appContext);
        GXPort[] ports = serial.getPorts();
    }

    /**
     * Open port
     */
    @Test
    public void openPort() throws Exception {
        Context appContext = InstrumentationRegistry.getTargetContext();
        try (GXSerial serial = new GXSerial(appContext)) {
            GXPort[] ports = serial.getPorts();
            if (ports.length != 0) {
                serial.setPort(ports[0]);
                serial.open();
            }
        }
    }

    /**
     * Settings test.
     */
    @Test
    public final void settingsTest() {
        Context appContext = InstrumentationRegistry.getTargetContext();
        String nl = System.getProperty("line.separator");
        GXSerial tmp = new GXSerial(appContext);
        GXPort[] ports = tmp.getPorts();
        if (ports.length != 0) {
            GXSerial serial =
                    new GXSerial(appContext, ports[0].getPort(), BaudRate.BAUD_RATE_300, 7, Parity.EVEN, StopBits.ONE);
            String expected = "<Port>" + ports[0].getPort()+ "</Port>" + nl
                    + "<BaudRate>300</BaudRate>" + nl + "<Parity>2</Parity>"
                    + nl + "<DataBits>7</DataBits>" + nl;
            String actual = serial.getSettings();
            assertEquals(expected, actual);
            GXSerial serial1 = new GXSerial(appContext);
            serial1.setSettings(actual);
            actual = serial1.getSettings();
            assertEquals(expected, actual);
        }
    }

}

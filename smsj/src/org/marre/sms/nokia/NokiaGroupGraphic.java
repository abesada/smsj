/*
    SMS Library for the Java platform
    Copyright (C) 2002  Markus Eriksson

    This library is free software; you can redistribute it and/or
    modify it under the terms of the GNU Lesser General Public
    License as published by the Free Software Foundation; either
    version 2.1 of the License, or (at your option) any later version.

    This library is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
    Lesser General Public License for more details.

    You should have received a copy of the GNU Lesser General Public
    License along with this library; if not, write to the Free Software
    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
*/
package org.marre.sms.nokia;

import java.io.*;
import java.awt.image.BufferedImage;

import org.apache.commons.logging.*;

import org.marre.sms.SmsConstants;
import org.marre.sms.SmsConcatMessage;
import org.marre.sms.SmsUdhElement;
import org.marre.sms.util.SmsPduUtil;
import org.marre.sms.util.SmsUdhUtil;

/**
 * Nokia Group Graphic (CLI) message
 *
 * @author Markus Eriksson
 * @version $Id$
 */
public class NokiaGroupGraphic extends SmsConcatMessage
{
    static Log myLog = LogFactory.getLog(NokiaGroupGraphic.class);

    /**
     *
     * @param theOtaBitmap
     */
    public NokiaGroupGraphic(OtaBitmap theOtaBitmap)
    {
        super(SmsConstants.DCS_DEFAULT_8BIT);
        setContent(theOtaBitmap.getBytes());
    }

    /**
     *
     * @param theImg
     */
    public NokiaGroupGraphic(BufferedImage theImg)
    {
        this(new OtaBitmap(theImg));
    }

    /**
     *
     * @param theOtaImage The ota image as an hexstring
     */
    public NokiaGroupGraphic(String theOtaImage)
    {
        super(SmsConstants.DCS_DEFAULT_8BIT);
        setContent(SmsPduUtil.hexStringToBytes(theOtaImage));
    }

    /**
     *
     * @param theOtaBitmap
     */
    private void setContent(byte[] theOtaBitmap)
    {
        SmsUdhElement[] udhElements = new SmsUdhElement[1];
        ByteArrayOutputStream baos = new ByteArrayOutputStream(140);

        // Port
        udhElements[0] = SmsUdhUtil.get16BitApplicationPortUdh(SmsConstants.PORT_NOKIA_CLI_LOGO, 0);

        // Payload
        try
        {
            // Type?
            baos.write(0x30);
            // bitmap
            baos.write(theOtaBitmap);

            baos.close();
        }
        catch (IOException ex)
        {
            // Should not happen!
            myLog.fatal("Failed to write to ByteArrayOutputStream", ex);
        }

        // Let SmsConcatMessage build the pdus...
        setContent(udhElements, baos.toByteArray(), baos.size());
    }
}

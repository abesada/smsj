/* ***** BEGIN LICENSE BLOCK *****
 * Version: MPL 1.1/GPL 2.0/LGPL 2.1
 *
 * The contents of this file are subject to the Mozilla Public License Version
 * 1.1 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * The Original Code is "SMS Library for the Java platform".
 *
 * The Initial Developer of the Original Code is Markus Eriksson.
 * Portions created by the Initial Developer are Copyright (C) 2002
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 *
 * Alternatively, the contents of this file may be used under the terms of
 * either the GNU General Public License Version 2 or later (the "GPL"), or
 * the GNU Lesser General Public License Version 2.1 or later (the "LGPL"),
 * in which case the provisions of the GPL or the LGPL are applicable instead
 * of those above. If you wish to allow use of your version of this file only
 * under the terms of either the GPL or the LGPL, and not to allow others to
 * use your version of this file under the terms of the MPL, indicate your
 * decision by deleting the provisions above and replace them with the notice
 * and other provisions required by the GPL or the LGPL. If you do not delete
 * the provisions above, a recipient may use your version of this file under
 * the terms of any one of the MPL, the GPL or the LGPL.
 *
 * ***** END LICENSE BLOCK ***** */
package org.marre.mms.transport.mm1;

import java.io.IOException;
import java.io.OutputStream;

import org.marre.mime.MimeBodyPart;
import org.marre.mime.MimeMultipart;
import org.marre.mime.encoder.MimeEncoder;
import org.marre.mms.MmsException;
import org.marre.mms.MmsHeaders;
import org.marre.wap.WapConstants;
import org.marre.wap.WapMimeEncoder;
import org.marre.wap.WspUtil;
import org.marre.wap.mms.MmsHeaderEncoder;

/**
 * 
 * 
 * @author Markus Eriksson
 * @version $Id$
 */
public final class Mm1Encoder
{
    private static MimeEncoder myWapMimeEncoder = new WapMimeEncoder();

    private static byte myWspEncodingVersion = WapConstants.WSP_ENCODING_VERSION_1_2;

    private Mm1Encoder()
    {
    }

    public static void writeMessageToStream(OutputStream out, MimeBodyPart theMessage, MmsHeaders theHeaders)
            throws MmsException
    {
        try
        {
            // Add headers
            writeHeadersToStream(out, theHeaders);

            // Add content-type

            // Prefer "vnd.wap.multipart..." instead of "multipart/..."
            if (theMessage instanceof MimeMultipart)
            {
                // Convert multipart headers...
                // TODO: Clone content type... We shouldn't change theMsg...
                String ct = theMessage.getContentType().getValue();
                String newCt = WspUtil.convertMultipartContentType(ct);
                theMessage.getContentType().setValue(newCt);
            }

            MmsHeaderEncoder.writeHeaderContentType(myWspEncodingVersion, out, theMessage.getContentType());

            // Add content
            myWapMimeEncoder.writeBody(out, theMessage);
        }
        catch (IOException ex)
        {
            throw new MmsException(ex.getMessage());
        }
    }

    private static void writeHeadersToStream(OutputStream out, MmsHeaders theHeaders) throws IOException
    {
        MmsHeaderEncoder.writeHeaderXMmsMessageType(out, theHeaders.getMessageType());
        MmsHeaderEncoder.writeHeaderXMmsTransactionId(out, theHeaders.getTransactionId());
        MmsHeaderEncoder.writeHeaderXMmsMmsVersion(out, theHeaders.getVersion());

        if (theHeaders.getSubject() != null)
        {
            MmsHeaderEncoder.writeHeaderSubject(out, theHeaders.getSubject());
        }

        if (theHeaders.getFrom() != null)
        {
            MmsHeaderEncoder.writeHeaderFrom(out, theHeaders.getFrom());
        }

        // TODO: Add the rest of the headers
    }
}

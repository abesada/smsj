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
package org.marre.mms.util;

import java.io.UnsupportedEncodingException;

import org.marre.mime.MimeBodyPart;
import org.marre.mime.MimeHeader;
import org.marre.mime.util.MimeUtil;

public class MmsUtil
{
		private MmsUtil()
		{
		}
    
		public static final MimeBodyPart createTextBodyPart(String str, String contentId)
		{
				return createTextBodyPart(str, "text/plain", contentId);
		}
    
		public static final MimeBodyPart createTextBodyPart(byte[] data, String contentId)
		{
				return createBinaryBodyPart(data, "text/plain", contentId);
		}

		public static final MimeBodyPart createSmilBodyPart(String str, String contentId)
		{
				return createTextBodyPart(str, "application/smil", contentId);
		}

		public static final MimeBodyPart createSmilBodyPart(byte[] data, String contentId)
		{
				return createBinaryBodyPart(data, "application/smil", contentId);
		}

		public static final MimeBodyPart createGifBodyPart(byte[] data, String contentId)
		{
				return createBinaryBodyPart(data, "image/gif", contentId);
		}

		public static final MimeBodyPart createJpegBodyPart(byte[] data, String contentId)
		{
				return createBinaryBodyPart(data, "image/jpeg", contentId);
		}

		public static final MimeBodyPart createAmrBodyPart(byte[] data, String contentId)
		{
				return createBinaryBodyPart(data, "audio/amr", contentId);
		}

		public static final MimeBodyPart createTextBodyPart(String str, String contentType, String contentId)
		{
				byte[] data = null;

				try 
				{
						data = str.getBytes("UTF-8");
				}
				catch (UnsupportedEncodingException ex)
				{
						// Shouldn't happen.... UTF-8 is standard...
				}
        
				return createBinaryBodyPart(data, contentType, contentId);
		}

		public static final MimeBodyPart createBinaryBodyPart(byte[] content, String contentType, String contentId)
		{
				MimeBodyPart binBodyPart = new MimeBodyPart();

				binBodyPart.setContent(content, contentType);
				binBodyPart.addHeader("Content-ID", "<" + contentId + ">");
				return binBodyPart;
		}    
}

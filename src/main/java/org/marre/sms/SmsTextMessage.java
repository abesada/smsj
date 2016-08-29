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
package org.marre.sms;

import java.io.UnsupportedEncodingException;
import java.text.Normalizer;

import org.marre.util.StringUtil;

/**
 * Represents a text message.
 * <p>
 * The text can be sent in unicode (max 70 chars/SMS), 8-bit (max 140 chars/SMS) or GSM encoding (max 160 chars/SMS).
 *
 * @author Markus Eriksson
 * @version $Id$
 */
public class SmsTextMessage extends SmsConcatMessage
{
	private String text_;
	private SmsDcs dcs_;

	/**
	 * Creates an SmsTextMessage with the given dcs.
	 * 
	 * @param msg
	 *            The message
	 * @param dcs
	 *            The data coding scheme
	 */
	public SmsTextMessage(String msg, SmsDcs dcs)
	{
		setText(msg, dcs);
	}

	/**
	 * Creates an SmsTextMessage with the given alphabet and message class.
	 *
	 * @param msg
	 *            The message
	 * @param alphabet
	 *            The alphabet
	 * @param messageClass
	 *            The messageclass
	 */
	public SmsTextMessage(String msg, SmsAlphabet alphabet, SmsMsgClass messageClass)
	{
		this(msg, alphabet, messageClass, false);
	}

	/**
	 * Creates an SmsTextMessage with the given alphabet and message class.
	 *
	 * @param msg
	 *            The message
	 * @param alphabet
	 *            The alphabet
	 * @param messageClass
	 *            The messageclass
	 */
	public SmsTextMessage(String msg, SmsAlphabet alphabet, SmsMsgClass messageClass, boolean longMessageMode)
	{
		this(msg, SmsDcs.getGeneralDataCodingDcs(alphabet, messageClass));
		this.setLongMessageMode(longMessageMode);
	}

	/**
	 * Creates an SmsTextMessage with automatic Alphabet and autodetect encoding
	 *
	 * @param msg
	 *            The message
	 */
	public SmsTextMessage(String msg)
	{
		SmsAlphabet smsAlphabet = SmsAlphabet.GSM;
		String encoding = StringUtil.detectEncodingType(msg);
		System.err.println("Encoding detected: " + encoding);
		if (!encoding.equals("US-ASCII"))
		{
			try
			{
				msg = new String(msg.getBytes(encoding), "UTF-8");
				smsAlphabet = SmsAlphabet.UCS2;
			}
			catch (UnsupportedEncodingException ignore)
			{
			}
		}
		this.setText(msg, SmsDcs.getGeneralDataCodingDcs(smsAlphabet, SmsMsgClass.CLASS_UNKNOWN));
	}

	/**
	 * Returns the text message.
	 */
	public String getText()
	{
		return text_;
	}

	/**
	 * Sets the text.
	 * 
	 * @param text
	 */
	public void setText(String text)
	{
		if (text == null)
		{
			throw new IllegalArgumentException("Text cannot be null, use an empty string instead.");
		}
		if (dcs_ != null && dcs_.getAlphabet() == SmsAlphabet.GSM)
		{
			text = Normalizer.normalize(text, Normalizer.Form.NFD);
			text = text.replaceAll("[\\p{InCombiningDiacriticalMarks}]", "");
			text = text.replaceAll("€", "euros");
		}
		text_ = text;
	}

	/**
	 * Sets the text.
	 * 
	 * @param text
	 */
	public void setText(String text, SmsDcs dcs)
	{
		// Check input for null
		if (text == null)
		{
			throw new IllegalArgumentException("text cannot be null, use an empty string instead.");
		}
		if (dcs == null)
		{
			throw new IllegalArgumentException("dcs cannot be null.");
		}
		dcs_ = dcs;
		this.setText(text);
	}

	/**
	 * Returns the dcs.
	 */
	public SmsDcs getDcs()
	{
		return dcs_;
	}

	/**
	 * Returns the user data.
	 * 
	 * @return user data
	 */
	public SmsUserData getUserData()
	{
		SmsUserData ud = null;
		try
		{
			switch (dcs_.getAlphabet())
			{
			case GSM:
				ud = new SmsUserData(SmsPduUtil.getSeptets(text_), text_.length(), dcs_);
				break;
			case LATIN1:
				byte[] bytesUTF8 = text_.getBytes("UTF-8");
				ud = new SmsUserData(bytesUTF8, bytesUTF8.length, dcs_);
				break;
			case UCS2:
				byte[] bytesUTF16 = text_.getBytes("UTF_16BE");
				ud = new SmsUserData(bytesUTF16, bytesUTF16.length, dcs_);
				break;
			default:
				ud = null;
				break;
			}
		}
		catch (Exception e)
		{
		}
		return ud;
	}

	/**
	 * Returns null.
	 */
	public SmsUdhElement[] getUdhElements()
	{
		return null;
	}
}

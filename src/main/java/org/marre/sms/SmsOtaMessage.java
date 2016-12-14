package org.marre.sms;

import org.marre.util.StringUtil;

public class SmsOtaMessage extends SmsTextMessage
{
	public SmsOtaMessage(String msg)
	{
		super(msg, SmsAlphabet.LATIN1, SmsMsgClass.CLASS_0);
	}

	@Override
	public SmsUdhElement[] getUdhElements()
	{
		return new SmsUdhElement[] { SmsUdhUtil.get16BitApplicationPortUdh(SmsPort.WAP_PUSH, SmsPort.WAP_WSP) };
	}

	/**
	 * Returns the user data.
	 * 
	 * @return user data
	 */
	@Override
	public SmsUserData getUserData()
	{
		byte[] bytesUTF8 = StringUtil.hexStringToBytes(text_);
		SmsUserData ud = new SmsUserData(bytesUTF8, bytesUTF8.length, dcs_);
		return ud;
	}
}

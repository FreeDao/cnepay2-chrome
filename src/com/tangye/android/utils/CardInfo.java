package com.tangye.android.utils;

import com.tangye.android.iso8583.IsoMessage;
import com.tangye.android.iso8583.IsoType;

public class CardInfo {
	
	private String formatID, ksn, track2, card;
	
	public CardInfo(String...args) throws IllegalStateException {
		if (args.length > 3) {
			formatID = args[0];
			if (!testID(formatID)) {
				throw new IllegalStateException("formatID " + formatID + " is not supported");
			}
			ksn = args[1];
			track2 = args[2];
			card = args[3];
		} else {
			throw new IllegalArgumentException("Args should be at least 4");
		}
	}
	
	private boolean testID(String id) {
		// TODO to test formatID, must = 34 or 39
		if (id.equals("40")) {
			return true;
		}
		return false;
	}
	
	public void loadMessage(IsoMessage req, boolean useTrack2) {
		if (useTrack2) {
			req.setValue(35, track2, IsoType.LLLVAR);
		}
		req.setValue(58, ksn, IsoType.LLLVAR);
	}
	
	public String getCard(boolean masked) {
		if (masked && card.length() > 10) {
			return PublicHelper.getMaskedString(card, 6, 4, 'X');
		}
		return card;
	}
}

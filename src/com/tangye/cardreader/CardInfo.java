package com.tangye.cardreader;

/**
 * The second magnetic strip data on the bank card
 * @author tangye
 *
 */
public class CardInfo {

	private final String raw;
	
	/**
	 * Constructor of the bank card data class
	 * 
	 * @param data decoded data from the second magnetic strip, without the first and last character
	 * @throws IllegalStateException data cannot be recognized
	 */
	public CardInfo(String data) throws IllegalStateException {
		if(data.indexOf("=") < 0) throw new IllegalStateException("Card track Unidentified");
		raw = updateNumber(data);
	}

	/**
	 * get the card number from the info data
	 * @return card number, digits in front of "=" 
	 */
	public String getCardNumber() {
		return raw.substring(0, raw.indexOf("="));
	}
	
	/**
	 * get the whole decoded data from the class
	 * @return the raw data, equals to the argument of the constructor of this class
	 */
	@Override
	public String toString() {
		return raw;
	}
	
	private String updateNumber(String data) {
		String track2 = data;
		String[] temp = null;
		temp = track2.split("=");
		
		if(temp[0].charAt(0) == ';'){
        	temp[0] = temp[0].substring(1);
        }
        String[] t2 = temp[1].split("\\D");
        temp[1] = t2[0];
        
        return temp[0] + "=" + temp[1];
	}
}

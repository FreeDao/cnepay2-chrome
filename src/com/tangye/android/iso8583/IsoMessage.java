/*
j8583 A Java implementation of the ISO8583 protocol
Copyright (C) 2007 Enrique Zamudio Lopez

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
Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA
*/
package com.tangye.android.iso8583;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/** Represents an ISO8583 message. This is the core class of the framework.
 * Contains the bitmap which is modified as fields are added/removed.
 * This class makes no assumptions as to what types belong in each field,
 * nor what fields should each different message type have; that is left
 * for the developer, since the different ISO8583 implementations can vary
 * greatly.
 * 
 * @author Enrique Zamudio
 */
public class IsoMessage {

	static final byte[] HEX = new byte[]{ '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F' };

	/** The message type. */
    private int type;
    /** Indicates if the message is binary-coded. */
    private boolean binary;
    /** This is where the values are stored. */
    private Map<Integer,IsoValue<?>> fields = new ConcurrentHashMap<Integer,IsoValue<?>>();
    /** Stores the optional ISO header. */
    private String isoHeader;
    private int etx = -1;
    /** Flag to enforce secondary bitmap even if empty. */
    private boolean forceb2;
    
    /** Flag to enforce mac. */
    private boolean usemac;

    /** Creates a new empty message with no values set. */
    public IsoMessage() {
    }

    /** Creates a new message with the specified ISO header. This will be prepended to the message. */
    IsoMessage(String header) {
    	isoHeader = header;
    }

    /** If set, this flag will cause the secondary bitmap to be written even if it's not needed. */
    public void setForceSecondaryBitmap(boolean flag) {
    	forceb2 = flag;
    }
    
    public boolean getForceSecondaryBitmap() {
    	return forceb2;
    }
    
    public void setUseMac64(boolean use) {
        usemac = use;
        boolean init = fields.containsKey(64);
        if(usemac && !init) {
            // init 64 field with 8 ascii 48
            // important to decide bitmap, so need to firstly init a default value
            setValue(64, "000000", IsoType.ALPHA, 8);
        } else if(!use && init) {
            fields.remove(64);
        }
    }
    
    public boolean getUseMac() {
        return usemac;
    }

    /** Sets the string to be sent as ISO header, that is, after the length header but before the message type. 
     * This is useful in case an application needs some custom data in the ISO header of each message (very rare). */
    public void setIsoHeader(String value) {
    	isoHeader = value;
    }
    /** Returns the ISO header that this message was created with. */
    public String getIsoHeader() {
    	return isoHeader;
    }

    /** Sets the ISO message type. Common values are 0x200, 0x210, 0x400, 0x410, 0x800, 0x810. */
    public void setType(int value) {
    	type = value;
    }
    /** Returns the ISO message type. */
    public int getType() {
    	return type;
    }

    /** Indicates whether the message should be binary. Default is false. */
    public void setBinary(boolean flag) {
    	binary = flag;
    }
    /** Returns true if the message is binary coded; default is false. */
    public boolean isBinary() {
    	return binary;
    }

    /** Sets the ETX character, which is sent at the end of the message as a terminator.
     * Default is -1, which means no terminator is sent. */
    public void setEtx(int value) {
    	etx = value;
    }

    /** Returns the stored value in the field, without converting or formatting it.
     * @param field The field number. 1 is the secondary bitmap and is not returned as such;
     * real fields go from 2 to 128. */
    public Object getObjectValue(int field) {
    	IsoValue<?> v = fields.get(field);
    	if (v == null) {
    		return null;
    	}
    	return v.getValue();
    }

    /** Returns the IsoValue for the specified field. First real field is 2. */
    public IsoValue<?> getField(int field) {
    	return fields.get(field);
    }

    /** Stored the field in the specified index. The first field is the secondary bitmap and has index 1,
     * so the first valid value for index must be 2. */
    public void setField(int index, IsoValue<?> field) {
    	if (index < 2 || index > 128) {
    		throw new IndexOutOfBoundsException("Field index must be between 2 and 128");
    	}
    	if (field == null) {
    		fields.remove(index);
    	} else {
    		fields.put(index, field);
    	}
    }
    
    public void setValue(int index, Object value, IsoType t) {
        setValue(index, value, t, t.getLength());
    }

    /** Sets the specified value in the specified field, creating an IsoValue internally.
     * @param index The field number (2 to 128)
     * @param value The value to be stored.
     * @param t The ISO type.
     * @param length The length of the field, used for ALPHA and NUMERIC values only, ignored
     * with any other type. */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void setValue(int index, Object value, IsoType t, int length) {
    	if (index < 2 || index > 128) {
    		throw new IndexOutOfBoundsException("Field index must be between 2 and 128");
    	}
    	if (value == null) {
    		fields.remove(index);
    	} else {
    		IsoValue v = null;
    		if (t.needsLength()) {
    			v = new IsoValue(t, value, length);
    		} else {
    			v = new IsoValue(t, value);
    		}
    		fields.put(index, v);
    	}
    }

    /** Returns true is the message has a value in the specified field.
     * @param idx The field number. */
    public boolean hasField(int idx) {
    	return fields.get(idx) != null;
    }

    /** Writes a message to a stream, after writing the specified number of bytes indicating
     * the message's length. The message will first be written to an internal memory stream
     * which will then be dumped into the specified stream. This method flushes the stream
     * after the write. There are at most three write operations to the stream: one for the
     * length header, one for the message, and the last one with for the ETX.
     * @param outs The stream to write the message to.
     * @param lengthBytes The size of the message length header. Valid ranges are 0 to 4.
     * @throws IllegalArgumentException if the specified length header is more than 4 bytes.
     * @throws IOException if there is a problem writing to the stream. */
    public void write(OutputStream outs, int lengthBytes) throws IOException {
    	if (lengthBytes > 4) {
    		throw new IllegalArgumentException("The length header can have at most 4 bytes");
    	}
    	byte[] data = writeInternal();

    	if (lengthBytes > 0) {
    		int l = data.length;
    		if (etx > -1) {
    			l++;
    		}
    		byte[] buf = new byte[lengthBytes];
    		int pos = 0;
    		if (lengthBytes == 4) {
    			buf[0] = (byte)((l & 0xff000000) >> 24);
    			pos++;
    		}
    		if (lengthBytes > 2) {
    			buf[pos] = (byte)((l & 0xff0000) >> 16);
    			pos++;
    		}
    		if (lengthBytes > 1) {
    			buf[pos] = (byte)((l & 0xff00) >> 8);
    			pos++;
    		}
    		/*
            //byte[] buf = new byte[lengthBytes];
            String lstr = String.format("%0" + String.valueOf(lengthBytes*2) + "d", l);
            android.util.Log.i("HEAD", lstr);
            byte[] buf = IsoUtil.hex2byte(lstr);
            buf[1] = (byte) 0x51;
            */
    		buf[pos] = (byte)(l & 0xff);
    		outs.write(buf);
    		// android.util.Log.i("HEAD", IsoUtil.byte2hex(buf) + " " + String.valueOf(l));
    	}
    	outs.write(data);
    	//ETX
    	if (etx > -1) {
    		outs.write(etx);
    	}
    	outs.flush();
    }

    /** Creates and returns a ByteBuffer with the data of the message, including the length header.
     * The returned buffer is already flipped, so it is ready to be written to a Channel. */
    public ByteBuffer writeToBuffer(int lengthBytes) {
    	if (lengthBytes > 4) {
    		throw new IllegalArgumentException("The length header can have at most 4 bytes");
    	}

    	byte[] data = writeInternal();
    	ByteBuffer buf = ByteBuffer.allocate(lengthBytes + data.length + (etx > -1 ? 1 : 0));
    	if (lengthBytes > 0) {
    		int l = data.length;
    		if (etx > -1) {
    			l++;
    		}
    		byte[] bbuf = new byte[lengthBytes];
    		int pos = 0;
    		if (lengthBytes == 4) {
    			bbuf[0] = (byte)((l & 0xff000000) >> 24);
    			pos++;
    		}
    		if (lengthBytes > 2) {
    			bbuf[pos] = (byte)((l & 0xff0000) >> 16);
    			pos++;
    		}
    		if (lengthBytes > 1) {
    			bbuf[pos] = (byte)((l & 0xff00) >> 8);
    			pos++;
    		}
    		bbuf[pos] = (byte)(l & 0xff);
    		buf.put(bbuf);
    	}
    	buf.put(data);
    	//ETX
    	if (etx > -1) {
    		buf.put((byte)etx);
    	}
    	buf.flip();
    	return buf;
    }

    /** This calls writeInternal(), allowing applications to get the byte buffer containing the
     * message data, without the length header. */
    public byte[] writeData() {
    	return writeInternal();
    }
    
    public int[] getBitmapArray() {
        ArrayList<Integer> keys = new ArrayList<Integer>();
        keys.addAll(fields.keySet());
        Collections.sort(keys);
        int[] bm = new int[keys.size()];
        for(int i = 0; i < bm.length; i++) {
            bm[i] = keys.get(i);
        }
        return bm;
    }

    /** Writes the message to a memory buffer and returns it. The message does not include
     * the ETX character or the header length. */
    protected byte[] writeInternal() {
    	ByteArrayOutputStream bout = new ByteArrayOutputStream();
    	ByteArrayOutputStream header = new ByteArrayOutputStream();
    	if (isoHeader != null) {
    		try {
    		    header.write(isoHeader.getBytes());
    		} catch (IOException ex) {
    			//should never happen, writing to a ByteArrayOutputStream
    		}
    	}
    	//Message Type
    	if (binary) {
        	bout.write((type & 0xff00) >> 8);
        	bout.write(type & 0xff);
    	} else {
    		try {
    			bout.write(String.format("%04x", type).getBytes());
    		} catch (IOException ex) {
    			//should never happen, writing to a ByteArrayOutputStream
    		}
    	}

    	//Bitmap
    	ArrayList<Integer> keys = new ArrayList<Integer>();
    	keys.addAll(fields.keySet());
    	Collections.sort(keys);
    	BitSet bs = new BitSet(forceb2 ? 128 : 64);
    	for (Integer i : keys) {
    		bs.set(i - 1);
    	}
    	if (forceb2) {
    		bs.set(0);
    	} else if (bs.length() > 64) {
        	//Extend to 128 if needed
    		BitSet b2 = new BitSet(128);
    		b2.or(bs);
    		bs = b2;
    		bs.set(0);
    	}
    	//Write bitmap to stream
    	if (binary) {
    		int pos = 128;
    		int b = 0;
    		for (int i = 0; i < bs.size(); i++) {
    			if (bs.get(i)) {
    				b |= pos;
    			}
    			pos >>= 1;
    			if (pos == 0) {
    				bout.write(b);
    				pos = 128;
    				b = 0;
    			}
    		}
    	} else {
            int pos = 0;
            int lim = bs.size() / 4;
            for (int i = 0; i < lim; i++) {
                int nibble = 0;
                if (bs.get(pos++))
                    nibble |= 8;
                if (bs.get(pos++))
                    nibble |= 4;
                if (bs.get(pos++))
                    nibble |= 2;
                if (bs.get(pos++))
                    nibble |= 1;
                bout.write(HEX[nibble]);
            }
    	}

    	//Fields
    	if(usemac) {
    	    int index = keys.size() - 1;
    	    int pos = keys.get(index);
    	    if(pos == 128 || pos == 64) {
    	        keys.remove(index);
    	    }
    	}
    	
    	for (Integer i : keys) {
    		IsoValue<?> v = fields.get(i);
    		try {
    			v.write(bout, binary);
    		} catch (IOException ex) {
    			//should never happen, writing to a ByteArrayOutputStream
    		}
    	}
    	
    	byte[] macraw = bout.toByteArray();
    	try {
    	    header.write(macraw);
    	    if(usemac) {
    	        byte[] mac = POSHelper.getPOSSession().getMac(macraw);
    	        header.write(mac);
    	    }
    	} catch (Exception e) {
    	  //should never happen, writing to a ByteArrayOutputStream
        }
    	return header.toByteArray();
    }

    /** Copies the specified fields from the other message into the recipient. If a specified field is
     * not present in the source message it is simply ignored. */
    public void copyFieldsFrom(IsoMessage src, int...idx) {
    	for (int i : idx) {
    		IsoValue<?> v = src.getField(i);
    		if (v != null) {
        		setValue(i, v.getValue(), v.getType(), v.getLength());
    		}
    	}
    }
    
    public static boolean parseMessageFrom(Socket socket, IsoMessage msg, 
            IsoTemplate temp, BufferedInputStream in) throws IOException, ParseException {
        boolean result = false;
        byte[] head = new byte[2];
        byte[] data = new byte[104];
        ArrayList<Integer> bitmapArr = null;
        
        int len;
        int datalength = 0;
        len = in.read(data, 0, 2);
        int total = (data[0]&0xff) << 8 | (data[1] & 0xff);
        // android.util.Log.i("TOTAL", String.valueOf(total));
        String IsoHeader = temp.getIsoHeader();
        len = in.read(data, 0, IsoHeader.length());
        datalength += len;
        // android.util.Log.i("TPDU", IsoUtil.byte2hex(data, 0, IsoHeader.length()));
        // TODO TEST IsoHeader == data
        msg.setIsoHeader(IsoHeader);
        len = in.read(head);
        datalength += len;
        if(len == 2 && String.format("%04x", temp.getType()).equals(IsoUtil.byte2hex(head))) {
            msg.setType(temp.getType());
        } else {
            Err();
        }
        len = in.read(data, 0, 8);
        datalength += len;
        //android.util.Log.i("BITMAP", IsoUtil.byte2hex(data, 0, 8));
        if(len == 8) {
            bitmapArr = temp.testAndGetBitmap(data);
            if(bitmapArr == null) Err();
        } else {
            Err();
        }
        int i = 0;
        while(!Thread.currentThread().isInterrupted() && i < bitmapArr.size() && socket != null && socket.isConnected()) {
            int idx = bitmapArr.get(i); // 获取返回的Bitmap Index
            Template field = temp.getField(idx);
            IsoType type = field.type;
            // Binary Parse
            if(type == IsoType.LLVAR || type == IsoType.LLLVAR || type == IsoType.LLVARBCD || type == IsoType.LLLVARBCD) {
                int size = 0;
                if(type == IsoType.LLLVAR || type == IsoType.LLLVARBCD) {
                    // 0x00 - 0x09, so it is just the number we need
                    size = in.read();
                    datalength += 1;
                }
                len = in.read();
                datalength += 1;
                size = size * 100 + (len >> 4) * 10 + (len & 0x0f);
                if(type == IsoType.LLVAR || type == IsoType.LLLVAR) {
                    len = in.read(data, 0, size);
                    datalength += len;
                    msg.setValue(idx, new String(data, 0, size, "ISO-8859-1"), type);
                } else {
                    // LLxBCD code, need to read half length of size, 4bit on behalf of a BCD code
                    len = in.read(data, 0, (size + 1) / 2);
                    datalength += len;
                    String num = IsoUtil.byte2hex(data, 0, len);
                    if(size % 2 ==1) num = num.substring(0, num.length() - 1);
                    msg.setValue(idx, num, type);
                }
            } else if(type == IsoType.ALPHA) {
                len = in.read(data, 0, field.length);
                datalength += len;
                if(len != field.length) Err();
                msg.setValue(idx, new String(data, 0, len, "ISO-8859-1"), field.type, field.length);
            } else if(type == IsoType.NUMERIC) {
                int size = field.length;
                len = in.read(data, 0, (size + 1) / 2);
                datalength += len;
                String num = IsoUtil.byte2hex(data, 0, len);
                if(size % 2 ==1) num = num.substring(0, num.length() - 1);
                // TOTO translate it to long or big-decimal
                msg.setValue(idx, num, type, field.length);
            } else if(type == IsoType.AMOUNT) {
                char[] digits = new char[13];
                digits[10] = '.';
                int start = 0;
                len = in.read(data, 0, 6);
                datalength += len;
                for (int j = 0; j < 6; j++) {
                    digits[start++] = (char)(((data[j] & 0xf0) >> 4) + 48);
                    digits[start++] = (char)((data[j] & 0x0f) + 48);
                    if (start == 10) {
                        start++;
                    }
                }
                msg.setValue(idx, new BigDecimal(new String(digits)), type);
            } else if(type == IsoType.DATE10 || type == IsoType.DATE4 || type == IsoType.DATE_EXP || type == IsoType.TIME) {
                int[] tens = new int[(type.getLength() / 2) + (type.getLength() % 2)];
                len = in.read(data, 0, tens.length);
                datalength += len;
                for (int j = 0; j < tens.length; j++) {
                    tens[j] = (((data[j] & 0xf0) >> 4) * 10) + (data[j] & 0x0f);
                }
                Calendar cal = Calendar.getInstance();
                if (type == IsoType.DATE10) {
                    //A SimpleDateFormat in the case of dates won't help because of the missing data
                    //we have to use the current date for reference and change what comes in the buffer
                    //Set the month in the date
                    cal.set(Calendar.MONTH, tens[0] - 1);
                    cal.set(Calendar.DATE, tens[1]);
                    cal.set(Calendar.HOUR_OF_DAY, tens[2]);
                    cal.set(Calendar.MINUTE, tens[3]);
                    cal.set(Calendar.SECOND, tens[4]);
                    if (cal.getTime().after(new Date())) {
                        cal.add(Calendar.YEAR, -1);
                    }
                } else if (type == IsoType.DATE4) {
                    cal.set(Calendar.HOUR, 0);
                    cal.set(Calendar.MINUTE, 0);
                    cal.set(Calendar.SECOND, 0);
                    //Set the month in the date
                    cal.set(Calendar.MONTH, tens[0] - 1);
                    cal.set(Calendar.DATE, tens[1]);
                    if (cal.getTime().after(new Date())) {
                        cal.add(Calendar.YEAR, -1);
                    }
                } else if (type == IsoType.DATE_EXP) {
                    cal.set(Calendar.HOUR, 0);
                    cal.set(Calendar.MINUTE, 0);
                    cal.set(Calendar.SECOND, 0);
                    cal.set(Calendar.DATE, 1);
                    //Set the month in the date
                    cal.set(Calendar.YEAR, cal.get(Calendar.YEAR)
                            - (cal.get(Calendar.YEAR) % 100) + tens[0]);
                    cal.set(Calendar.MONTH, tens[1] - 1);
                } else if (type == IsoType.TIME) {
                    cal.set(Calendar.HOUR_OF_DAY, tens[0]);
                    cal.set(Calendar.MINUTE, tens[1]);
                    cal.set(Calendar.SECOND, tens[2]);
                }
                msg.setValue(idx, cal.getTime(), type);
            }
            i++;
        }
        if(i == bitmapArr.size()) {
            result = (datalength == total);
        }
        return result;
    }
    
    private static void Err() throws ParseException {
        throw new ParseException("Parse Error", 0);
    }
}

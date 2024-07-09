package util;

import java.util.Vector;
public class Address {
	public byte[] address;
	/**
	* @param address
	*/
	public Address(byte[] address) {
	super();
	this.address = address;
	}
	/**
	* @return the address
	*/
	public byte[] getAddress() {
	return address;
	}
	/**
	* @param address
	*/
	public void setAddress(byte[] address) {
	this.address = address;
	}
	
	
	/**
	* @param 
	* 
	*/
	public void decompresser(){
		String s=Integer.toHexString(this.address[0]& 0xFF);
		s=s+Integer.toHexString(this.address[1]& 0xFF);
		int t= Integer.parseInt(s, 16);
		t= t & 0xFFE0;
		t=t>>2;
		int a1= t & 0xFF00;
		a1=a1>>8;
		int a2= t & 0x00FF;
		this.address[0]=(byte)a1;
		this.address[1]=(byte)a2;
	}
	

	public static String byteToHexString(byte b) {
		StringBuffer s = new StringBuffer();
		s.append(Integer.toHexString(b & 0xff).toUpperCase());
		return s.toString();
	}
	
	/**
	* @param address : the address that is subtracted from <i>this</i>
	* @throws Exception
	*/
	public Address sub(Address address) throws Exception {
	Vector<Byte> v = new Vector<Byte>();
	byte r = (byte) 0;
	if (address.leq(this)) {
	for (int i = this.address.length - 1; i >= 0; i--) {
	int a = (this.address[i] & 0xFF);
	int b;
	if (i - (this.address.length - address.address.length) >=
	0) {
	b = (address.address[i - (this.address.length -
	address.address.length)]
	& 0xFF);
	} else {
	b = (byte) 0;
	}
	if (a >= (b + r)) {
		v.insertElementAt(new Byte((byte) (a - (b + r))),
		0);
		r = (byte) 0;
		} else {
		v.insertElementAt(new Byte((byte) ((256 + a) - (b +
		r))), 0);
		r = (byte) 1;
		}
		}
		int blanks = 0;
		while ((v.size() != 0) && (v.elementAt(0).byteValue() == (byte)
		0)) {
		v.remove(0);
		blanks++;
		}
		Byte[] Ba = new Byte[this.address.length - blanks];
		v.toArray(Ba);
		byte[] ba = new byte[Ba.length];
		for (int i = 0; i < ba.length; i++) {
		ba[i] = Ba[i].byteValue();
		}
		return (new Address(ba));
		} else {
		throw (new Exception("subtraction error"));
		}
		}
		/**
		* @param address : the address that should be added to this
		* @return : this + address
		*/
		public Address add (Address address) {
		Address ret;
		Vector <Byte> c = new Vector <Byte> ();
		int r = (int) 0;
		for (int i = this.address.length - 1; i >= 0; i--) {
		int a = (this.address[i] & 0xFF);
		int b;
		if (i - (this.address.length - address.address.length) >= 0) {
		b = (address.address[i - (this.address.length -
		address.address.length)]
		& 0xFF);
		} else {
		b = (byte) 0;
		}
		if ((a + b + r) < 256) {
		c.insertElementAt(new Byte ((byte)(a + b + r)), 0);
		r = (byte) 0;
		} else {
		c.insertElementAt(new Byte ((byte)(a + b + r - 256)), 0);
		r = (byte) 1;
		}
		}
		if (r == 1) {
		c.insertElementAt(new Byte ((byte) 1), 0);
		}
		Byte[] Ba = new Byte[c.size()];
		c.toArray(Ba);
		byte[] ba = new byte[Ba.length];
		for (int i = 0; i < ba.length; i++) {
		ba[i] = Ba[i].byteValue();
		}
		ret = new Address (ba);
		return (ret);
		}
		
		/**
		 * * @param address :
* @return this <= address
*/
public boolean leq(Address address) {
if (address.address.length > this.address.length)
return true;
if (address.address.length < this.address.length)
return false;
for (int i = 0; i < address.address.length; i++) {
if ((address.address[i] & 0xFF) < (this.address[i] & 0xFF))
return false;
if ((address.address[i] & 0xFF) > (this.address[i] & 0xFF))
return true;
}
return true;
}
/**
* @param address
* @return this < address
*/
public boolean le(Address address) {
if (address.address.length > this.address.length)
return true;
if (address.address.length < this.address.length)
return false;
for (int i = 0; i < address.address.length; i++) {
if ((address.address[i] & 0xFF) < (this.address[i] & 0xFF))
return false;
if ((address.address[i] & 0xFF) > (this.address[i] & 0xFF))
return true;
}
return false;
}
/* (non-Javadoc)
* @see java.lang.Object#toString()
*/

public String toString() {
return Tools.ba2s(getAddress());
}

}
		 


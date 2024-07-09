package util;

public class Tools {
private static String b2s (byte b) {
StringBuffer sb = new StringBuffer();
sb.append (quartet2c((byte)((b & 0xF0) >> 4)));
sb.append (quartet2c((byte)(b & 0x0F)));
return(sb.toString());
}
/**
* @param byteArray
* @return the String value for the byte array
*/
public static String ba2s (byte [] byteArray) {
StringBuffer sb = new StringBuffer();
for (int i = 0; i < byteArray.length; i++) {
sb.append(b2s(byteArray[i]));
sb.append(' ');
}
return (sb.toString());
}
/**
*
* @param add : the address for the first byte
* @param rapdu : the rAPDU that needs to be
* @return the String representation of the apdu indexed with the addresses
*/
public static String contentRAPDU (Address address, byte [] rapdu) {
StringBuffer sb = new StringBuffer();
Address local = address;
byte [] badd = {(byte) 0x08};
Address add = new Address(badd);
int i;
for (i = 0; i < rapdu.length - 5; i+=4) {
sb.append (ba2s(local.getAddress()) + ": ");
byte [] ba = {rapdu[i], rapdu[i+1], rapdu[i+2], rapdu[i+3]};
sb.append(ba2s(ba));
local = local.add(add);
}
if (i < rapdu.length - 2) {
sb.append (ba2s(local.getAddress()) + ": ");
}
for (; i < rapdu.length - 2; i++) {
byte [] ba = {rapdu[i]};
sb.append(ba2s(ba));
}
sb.append("\nSW : " + b2s(rapdu[i]) + b2s(rapdu[i+1]));
return sb.toString();
}
private static char quartet2c (byte b) {
char c;
switch (b) {
case 0 : c = '0'; break;
case 1 : c = '1'; break;
case 2 : c = '2'; break;
case 3 : c = '3'; break;
case 4 : c = '4'; break;
case 5 : c = '5'; break;
case 6 : c = '6'; break;
case 7 : c = '7'; break;
case 8 : c = '8'; break;
case 9 : c = '9'; break;
case 10 : c = 'A'; break;
case 11 : c = 'B'; break;
case 12 : c = 'C'; break;
case 13 : c = 'D'; break;
case 14 : c = 'E'; break;
case 15 : c = 'F'; break;
default : c = '.'; break;
}
return (c);
}

    static String byteArray2string(byte[] b) {
        return null;
    }

    
}
package com.whpe.nfcdemo.common;

/**
 * Created by Administrator on 2017/1/17.
 */
public class CommonUtils {

    public static String Bytes2HexString(byte[] b,boolean mSpace) {
        String ret = "";
        for (int i = 0; i < b.length; i++) {
            String hex = Integer.toHexString(b[i] & 0xFF);
            if (hex.length() == 1) {
                hex = '0' + hex;
            }
            ret += hex.toUpperCase();
            if(mSpace)ret+=" ";
        }
        return ret;
    }

    public static byte[] HexString2Bytes(String src){
        src=src.replace(" ", "");
        byte[] ret = new byte[src.length()/2];
        byte[] tmp = src.getBytes();
        for(int i=0; i<ret.length; i++){
            ret[i] = uniteBytes(tmp[i*2], tmp[i*2+1]);
        }
        return ret;
    }

    /**
     * 将两个ASCII字符合成一个字节；
     * 如："EF"--> 0xEF
     * @param src0 byte
     * @param src1 byte
     * @return byte
     */
    private static byte uniteBytes(byte src0, byte src1)
    {
        byte _b0 = Byte.decode("0x" + new String(new byte[]{src0})).byteValue();
        _b0 = (byte)(_b0 << 4);
        byte _b1 = Byte.decode("0x" + new String(new byte[]{src1})).byteValue();
        byte ret = (byte)(_b0 | _b1);
        return ret;
    }


    public static boolean isEmpty(String str){
        return str == null || str.length() == 0;
    }
}

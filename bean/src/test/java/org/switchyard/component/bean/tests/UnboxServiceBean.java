package org.switchyard.component.bean.tests;

import org.switchyard.component.bean.Service;

@Service(UnboxService.class)
public class UnboxServiceBean implements UnboxService {

	@Override
	public boolean unboxBoolean(boolean boolValue) {
		return boolValue;
	}

	@Override
	public byte unboxByte(byte byteValue) {
		return byteValue;
	}

	@Override
	public char unboxChar(char charValue) {
		return charValue;
	}

	@Override
	public double unboxDouble(double doubleValue) {
		return doubleValue;
	}

	@Override
	public float unboxFloat(float floatValue) {
		return floatValue;
	}

	@Override
	public int unboxInt(int intValue) {
		return intValue;
	}

	@Override
	public long unboxLong(long longValue) {
		return longValue;
	}

	@Override
	public short unboxShort(short shortValue) {
		return shortValue;
	}

}

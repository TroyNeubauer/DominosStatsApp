package com.troy.core;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.KryoSerializable;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

public class PointData implements KryoSerializable
{
	public double lat,lng;
	public DateTime time;

	public static final DateTimeFormatter FORMATTER = ISODateTimeFormat.dateTime();

	@Override
	public void write(Kryo kryo, Output output) {
		output.writeDouble(lat);
		output.writeDouble(lng);
		output.writeString(FORMATTER.print(time));
	}

	@Override
	public void read(Kryo kryo, Input input) {
		lat = input.readDouble();
		lng = input.readDouble();
		time = FORMATTER.parseDateTime(input.readString());
	}
}

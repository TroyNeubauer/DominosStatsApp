package com.troy.core;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.KryoSerializable;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import org.joda.time.DateTime;

public class DeliveryData implements KryoSerializable {
	public double lat, lng;

	public boolean male;
	public double tip, orderTotal;
	public int age;

	public DateTime time;

	@Override
	public void write(Kryo kryo, Output output) {
		output.writeDouble(lat);
		output.writeDouble(lng);

		output.writeBoolean(male);

		output.writeDouble(tip);
		output.writeDouble(orderTotal);

		output.writeInt(age);
		output.writeString(PointData.FORMATTER.print(time));
	}

	@Override
	public void read(Kryo kryo, Input input) {
		lat = input.readDouble();
		lng = input.readDouble();

		male = input.readBoolean();

		tip = input.readDouble();
		orderTotal = input.readDouble();

		age = input.readInt();
		time = PointData.FORMATTER.parseDateTime(input.readString());
	}
}

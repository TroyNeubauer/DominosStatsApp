package com.troy.core;

import com.esotericsoftware.kryo.Kryo;

import org.joda.time.DateTime;

public class Core
{
	public static final ThreadLocal<Kryo> KRYO = new ThreadLocal<Kryo>()
	{
		@Override
		protected Kryo initialValue()
		{
			Kryo kryo = new Kryo();
			kryo.register(DeliveryData.class);
			kryo.register(PointData.class);
			return kryo;
		}
	};

}


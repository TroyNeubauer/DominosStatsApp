package com.troy.kryotest;


import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.troy.core.Core;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;

import javax.swing.JFileChooser;

public class Main
{

	static class FileData
	{
		FileData(String name)
		{
			this.fileName = name;
			this.objects = new ArrayList<>();
		}

		String fileName;
		ArrayList<Object> objects;
	}



	public static void main(String[] args)
	{

		JsonSerializer<DateTime> serializer = new JsonSerializer<DateTime>() {
			@Override
			public JsonElement serialize(DateTime src, Type typeOfSrc, JsonSerializationContext context) {
				return new JsonPrimitive(DateTimeFormat.mediumDateTime().print(src));
			}
		};

		final Kryo kryo = Core.KRYO.get();
		final Gson gson = new GsonBuilder().registerTypeAdapter(DateTime.class, serializer).setPrettyPrinting().create();



		JFileChooser chooser = new JFileChooser();
		chooser.setMultiSelectionEnabled(true);
		chooser.showOpenDialog(null);
		ArrayList<FileData> files = new ArrayList<>();
		for (File file : chooser.getSelectedFiles())
		{
			FileData data = new FileData(file.toString());
			files.add(data);
			try
			{
				Input in = new Input(new FileInputStream(file));
				while (!in.eof())
				{
					Object o = kryo.readClassAndObject(in);
					data.objects.add(o);
				}
			}
			catch (IOException e)
			{
				throw new RuntimeException(e);
			}
		}
		System.out.println(gson.toJson(files));
	}
}

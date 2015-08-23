package com.example.whiteboard;

import java.util.ArrayList;
import java.util.List;

public class Data {
	
	private List<List<Integer>> data;
	
	public Data()
	{
		data = new ArrayList<List<Integer>>();
	}
	
	public synchronized void loadData(int[][] original_data)
	{
		for (int i = 0; i < original_data.length; i++)
		{
			ArrayList<Integer> values = new ArrayList<Integer>();
			for (int j = 0; j < original_data[i].length; j++)
			{
				values.add(original_data[i][j]);
			}
			data.add(values);
		}
	}
	
	public synchronized List<List<Integer>> getData()
	{
		List<List<Integer>> temp = new ArrayList<List<Integer>>();
		for (int i = 0; i < data.size(); i++)
		{
			ArrayList<Integer> values = new ArrayList<Integer>();
			for (int j = 0; j < data.get(i).size(); j++)
			{
				values.add(data.get(i).get(j));
			}
			temp.add(values);
		}
		return temp;
	}
	
	public synchronized void removeData(int index)
	{
		data.remove(index);
	}
	
	public synchronized void EmptyData()
	{
		data.clear();
	}
}

package org.subsurface.model;

import java.text.SimpleDateFormat;
import java.util.Locale;

public class GpxFileInfo
{
	private String name;
	private String path;
	private String date;
	private String directory;
	private final SimpleDateFormat sdf;

	public GpxFileInfo(String name, String path, long timestamp, String directory)
	{
		super();
		this.name = name;
		this.path = path;
		sdf = new SimpleDateFormat("MM/dd/yy HH:mm:ss", Locale.getDefault());
		this.date = sdf.format(timestamp);
		this.directory = directory;

	}

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
		return;
	}

	public String getPath()
	{
		return path;
	}

	public void setPath(String path)
	{
		this.path = path;
		return;
	}

	public String getDate()
	{
		return date;
	}

	public void setDate(long timestamp)
	{
		this.date = sdf.format(timestamp);
		return;
	}


	public String getDirectory()
	{
		return directory;
	}

	public void setDirectory(String directory)
	{
		this.directory = directory;
		return;
	}
}


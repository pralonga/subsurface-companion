package org.subsurface.model;

import java.text.SimpleDateFormat;
import java.util.Locale;
/**
 * Model for information on Gpx present in the SD card
 * @author Venkatesh Shukla
 */
public class GpxFileInfo
{
	private String name;
	private String path;
	private String date;
	private String directory;
	private static final String DATEFORMAT = "MM/dd/yy HH:mm:ss";
	
	public GpxFileInfo(String name, String path, long timestamp, String directory)
	{
		super();
		this.name = name;
		this.path = path;
		SimpleDateFormat sdf = new SimpleDateFormat(DATEFORMAT, Locale.getDefault());
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
	}

	public String getPath()
	{
		return path;
	}

	public void setPath(String path)
	{
		this.path = path;
	}

	public String getDate()
	{
		return date;
	}

	public void setDate(long timestamp)
	{
		SimpleDateFormat sdf = new SimpleDateFormat(DATEFORMAT, Locale.getDefault());
		this.date = sdf.format(timestamp);
	}


	public String getDirectory()
	{
		return directory;
	}

	public void setDirectory(String directory)
	{
		this.directory = directory;
	}
}


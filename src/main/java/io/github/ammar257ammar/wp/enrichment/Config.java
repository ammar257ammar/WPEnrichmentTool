package io.github.ammar257ammar.wp.enrichment;

import java.io.File;

import org.bridgedb.bio.DataSourceTxt;
import org.pathvisio.core.preferences.PreferenceManager;

public class Config {
	
	public static void init() {
		
		PreferenceManager.init();
		DataSourceTxt.init();
				
		try
		{
			Class.forName ("org.bridgedb.file.IDMapperText");
			Class.forName ("org.bridgedb.rdb.IDMapperRdb");
		}
		catch (ClassNotFoundException ex)
		{
			System.out.println("Could not initilize GDB Manager");
		}
	}
	
	public static void initFolders(String inputPath) {
		
		File pgexFolder = new File(inputPath+"/pgex");
		File pathwaysFolder = new File(inputPath+"/pathways");
		
		if(!pgexFolder.exists())
			pgexFolder.mkdir();
			
		if(!pathwaysFolder.exists())
			pathwaysFolder.mkdir();
			
	}

}

package com.polydes.datastruct.data.structure;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map.Entry;

import org.apache.commons.io.FileUtils;

import com.polydes.common.nodes.DefaultBranch;
import com.polydes.common.nodes.DefaultLeaf;
import com.polydes.datastruct.DataStructuresExtension;
import com.polydes.datastruct.data.structure.elements.StructureField;
import com.polydes.datastruct.data.types.HaxeTypeConverter;
import com.polydes.datastruct.io.FolderInfo;
import com.polydes.datastruct.io.Text;

public class Structures
{
//	private static final Logger log = Logger.getLogger(Structures.class);
	
	private static int nextID = 0;
	private static Structures _instance;
	public static StructureFolder root;
	
	private Structures()
	{
		root = new StructureFolder("Structures");
	}
	
	public static Structures get()
	{
		if(_instance == null)
			_instance = new Structures();
		
		return _instance;
	}
	
	//Loading is done in two passes.
	
	//Lightload: The first pass just gets id and type of each object
	//Maps are populated with this data
	
	//Deepload: The second pass reads in the key-value pairs, which might include refs to other structures.
	
	private HashMap<String, HashMap<String, String>> fmaps;
	
	public void load(File folder)
	{
		fmaps = new HashMap<String, HashMap<String,String>>();
		
		FolderInfo info = new FolderInfo(folder);
		ArrayList<String> order = info.getFileOrder();
		for(String fname : order)
		{
			File file = new File(folder, fname);
			lightload(file);
		}
		for(String fname : order)
		{
			File file = new File(folder, fname);
			deepload(root, file);
		}
		order.clear();
		info.clear();
		root.setDirty(false);
		
		fmaps.clear();
		fmaps = null;
	}
	
	public void lightload(File file)
	{
		if(!file.exists())
			return;
		
		if(file.isDirectory())
		{
			for(String fname : file.list())
				if(!fname.equals(FolderInfo.FOLDER_INFO_FILENAME))
					lightload(new File(file, fname));
		}
		else
		{
			HashMap<String, String> map = Text.readKeyValues(file);
			fmaps.put(file.getAbsolutePath(), map);
			
			String name = file.getName();
			int id = Integer.parseInt(map.get("struct_id"));
			String type = map.remove("struct_type");
			nextID = Math.max(nextID, id + 1);
			
			Structure model = new Structure(id, name, type);
			structures.get(model.getTemplate()).add(model);
			structuresByID.put(model.getID(), model);
		}
	}
	
	public void deepload(StructureFolder folder, File file)
	{
		if(!file.exists())
			return;
		
		if(file.isDirectory())
		{
			StructureFolder newFolder = new StructureFolder(file.getName());
			FolderInfo info = new FolderInfo(file);
			ArrayList<String> order = info.getFileOrder();
			if(info.containsKey("childType"))
				newFolder.childType = DataStructuresExtension.get().getStructureDefinitions().getItem(info.get("childType"));
			for(String fname : order)
				deepload(newFolder, new File(file, fname));
			folder.addItem(newFolder);
		}
		else
		{
			HashMap<String, String> map = fmaps.get(file.getAbsolutePath());
			Structure model = structuresByID.get(Integer.parseInt(map.remove("struct_id")));
			
			boolean unknown = model.getTemplate().isUnknown();
			
			for(Entry<String, String> entry : map.entrySet())
			{
				StructureField f = model.getField(entry.getKey());
				if(f == null || unknown)
				{
					model.setUnknownProperty(entry.getKey(), entry.getValue());
					continue;
				}
				model.setPropertyFromString(f, entry.getValue());
				model.setPropertyEnabled(f, true);
			}
			
			folder.addItem(model.dref);
		}
	}
	
	public void saveChanges(File file) throws IOException
	{
		if(root.isDirty())
		{
			FolderInfo info = new FolderInfo();
			
			for(DefaultLeaf d : root.getItems())
			{
				save(d, file);
				info.addFilenameToOrder(d.getName());
			}
			
			info.writeToFolder(file);
			info.clear();
		}
		root.setDirty(false);
	}
	
	public void save(DefaultLeaf item, File file) throws IOException
	{
		if(item instanceof DefaultBranch)
		{
			File saveDir = new File(file, item.getName());
			if(!saveDir.exists())
				saveDir.mkdirs();
			
			FolderInfo info = new FolderInfo();
			
			for(DefaultLeaf d : ((DefaultBranch) item).getItems())
			{
				save(d, saveDir);
				info.addFilenameToOrder(d.getName());
			}
			
			if(((StructureFolder) item).childType != null)
				info.put("childType", ((StructureFolder) item).childType.getName());
			
			info.writeToFolder(saveDir);
			info.clear();
		}
		else
		{
			Structure s = (Structure) item.getUserData();
			ArrayList<String> toWrite = new ArrayList<String>();
			toWrite.add("struct_id=" + s.getID());
			toWrite.add("struct_type=" + s.getTemplate().getFullClassname());
			
			for(StructureField field : s.getFields())
			{
				if(field.isOptional() && !s.isPropertyEnabled(field))
					continue;
				Object o = s.getProperty(field);
				if(o == null)
					continue;
				toWrite.add(field.getVarname() + "=" + HaxeTypeConverter.encode(field.getType().dataType, o));
			}
			if(s.getUnknownData() != null)
				for(Entry<String, String> entry : s.getUnknownData().entrySet())
					toWrite.add(entry.getKey() + "=" + entry.getValue());
			
			FileUtils.writeLines(new File(file, item.getName()), toWrite, "\n");
		}
	}

	public static HashMap<StructureDefinition, ArrayList<Structure>> structures = new HashMap<StructureDefinition, ArrayList<Structure>>();
	public static HashMap<Integer, Structure> structuresByID = new HashMap<Integer, Structure>();
	
	public static Collection<Structure> getList(StructureDefinition type)
	{
		return structures.get(type);
	}
	
	public static Structure getStructure(int i)
	{
		return structuresByID.get(i);
	}

	public static int newID()
	{
		return nextID++;
	}

	public static void dispose()
	{
		for(StructureDefinition key : structures.keySet())
			for(Structure s : structures.get(key))
				s.dispose();
		structures.clear();
		structuresByID.clear();
		_instance = null;
		root = null;
		nextID = 0;
	}
}

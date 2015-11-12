package com.polydes.datastruct.data.types;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.polydes.common.data.types.DataType;
import com.polydes.common.data.types.Types;
import com.polydes.common.ext.RegistryObject;
import com.polydes.datastruct.DataStructuresExtension;
import com.polydes.datastruct.ui.objeditors.StructureFieldPanel;

import stencyl.sw.editors.snippet.designer.Definition;

public abstract class HaxeDataType implements RegistryObject
{
	public String stencylType;
	private String haxeType; //registry key
	
	public DataType<?> dataType;
	
	public HaxeDataType(DataType<?> dataType, String haxeType, String stencylType)
	{
		this.dataType = dataType;
		this.haxeType = haxeType;
		this.stencylType = stencylType;
	}
	
	public String getSimpleClassname()
	{
		return StringUtils.substringAfterLast(haxeType, ".");
	}
	
	public String getPackage()
	{
		if(haxeType.indexOf('.') == -1)
			return StringUtils.EMPTY;
		else
			return StringUtils.substringBeforeLast(haxeType, ".");
	}
	
	//return null for classes that already exist
	public List<String> generateHaxeClass()
	{
		return null;
	}
	
	public List<String> generateHaxeReader()
	{
		return null;
	}
	
	/**
	 * From the passed in StructureFieldPanel, the following are accessible:	<br />
	 * - panel  :  StructureFieldPanel											<br />
	 * - extraProperties  :  Card												<br />
	 * 																			<br />
	 * - field  :  StructureField												<br />
	 * - preview  :  PropertiesSheet											<br />
	 * - previewKey  :  DataItem												<br />
	 */
	public /*abstract*/ void applyToFieldPanel(StructureFieldPanel panel)
	{
		System.out.println("APPLYING OTHER " + haxeType);
	};
	
	public ArrayList<Definition> getBlocks()
	{
		return null;
	}
	
	public String getHaxeType()
	{
		return haxeType;
	}
	
	public void changeHaxeType(String newType)
	{
		DataStructuresExtension.get().getHaxeTypes().renameItem(this, newType);
	}
	
	@Override
	public String getKey()
	{
		return haxeType;
	}
	
	@Override
	public void setKey(String newKey)
	{
		this.haxeType = newKey;
		Types.get().renameItem(dataType, newKey); //TODO don't do this??
	}
}
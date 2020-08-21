package com.polydes.datastruct;

import java.util.ArrayList;

import org.apache.log4j.Logger;

import com.polydes.datastruct.data.types.HaxeDataType;

import stencyl.core.lib.attribute.AttributeType;
import stencyl.core.lib.attribute.AttributeTypes;
import stencyl.sw.editors.snippet.designer.Definition;
import stencyl.sw.editors.snippet.designer.Definition.Category;
import stencyl.sw.editors.snippet.designer.Definitions;
import stencyl.sw.editors.snippet.designer.Definitions.DefinitionMap;
import stencyl.sw.editors.snippet.designer.Definitions.UnorderedDefinitionMap;
import stencyl.sw.editors.snippet.designer.block.AbstractField;
import stencyl.sw.editors.snippet.designer.block.Block;
import stencyl.sw.editors.snippet.designer.block.Block.BlockType;
import stencyl.sw.editors.snippet.designer.block.BlockField;
import stencyl.sw.editors.snippet.designer.block.BlockFieldOverride;
import stencyl.sw.editors.snippet.designer.block.BlockTheme;
import stencyl.sw.editors.snippet.designer.codebuilder.CodeBuilder;
import stencyl.sw.editors.snippet.designer.codebuilder.CodeElement;
import stencyl.sw.editors.snippet.designer.codebuilder.DesignModeCodeBuilder;
import stencyl.sw.editors.snippet.designer.codebuilder.SpecialCodeElement;
import stencyl.sw.editors.snippet.designer.codebuilder.XMLCodeBuilder;
import stencyl.sw.editors.snippet.designer.codebuilder.xml.XMLElement;
import stencyl.sw.editors.snippet.designer.codebuilder.xml.XMLElement.XMLCode;
import stencyl.sw.editors.snippet.designer.codemap.BasicCodeMap;
import stencyl.sw.editors.snippet.designer.dropdown.CodeConverter;
import stencyl.sw.editors.snippet.designer.dropdown.DropdownData;
import stencyl.sw.editors.snippet.designer.dropdown.DropdownData.DropdownConverter;

public class Blocks
{
	private static final Logger log = Logger.getLogger(Blocks.class);
	
	public static DefinitionMap tagCache = new UnorderedDefinitionMap();
	
	public static void addDesignModeBlocks()
	{
//		set [propname] for [object] to [value]		object.prop = value;
		
		String spec = "set %1 for %0 to %2";
		
		final CodeElement arg = SpecialCodeElement.TILDE;
		final CodeElement codeArg = new CodeElement()
		{
			@Override
			public void toCode(CodeBuilder builder)
			{
				if(builder instanceof DesignModeCodeBuilder)
				{
					AbstractField child = ((DesignModeCodeBuilder) builder).getCurrentField();
					
					if (child instanceof BlockFieldOverride && ((BlockFieldOverride) child).isOverridden())
					{
						BlockField c = ((BlockFieldOverride) child).getBlockField();
						
						if (c.hasNested())
						{
							builder.error();
						}
						else
						{
							builder.append(c.getFieldValue());
						}
					}
					else
					{
						builder.error();
					}
				}
				else if(builder instanceof XMLCodeBuilder)
				{
					XMLElement child = ((XMLCodeBuilder) builder).getCurrentField();
					
					if (child != null)
					{
						if(child instanceof XMLCode)
						{
							builder.append(((XMLCode) child).val);
						}
						else
						{
							builder.error();
						}
					}
					else
					{
						builder.error();
					}
				}
				
				builder.nextField();
			}
		};
		
		Definition blockDef = new Definition
		(
			Category.CUSTOM,
			"ds-set-prop1",
			new AttributeType[] { AttributeTypes.OBJECT, AttributeTypes.TEXT, AttributeTypes.OBJECT },
			new CodeElement()
			{
				@Override
				public void toCode(CodeBuilder builder)
				{
					builder.append(arg, ".", codeArg, " = ", arg, ";");
				}
			},
			null,
			spec,
			BlockType.ACTION,
			AttributeTypes.VOID,
			null
		);
		
		blockDef.guiTemplate = spec;
		blockDef.customBlockTheme = BlockTheme.THEMES.get("blue");
		
		Definitions.get().put(blockDef.tag, blockDef);
		tagCache.put(blockDef.tag, blockDef);

//		get [propname] for [object]					object.prop
		
		spec = "get %1 from %0";
		
		blockDef = new Definition
		(
			Category.CUSTOM,
			"ds-get-prop1",
			new AttributeType[] { AttributeTypes.OBJECT, AttributeTypes.TEXT },
			new CodeElement()
			{
				@Override
				public void toCode(CodeBuilder builder)
				{
					builder.append(arg, ".", codeArg);
				}
			},
			null,
			spec,
			BlockType.NORMAL,
			AttributeTypes.OBJECT,
			null
		);
		
		blockDef.guiTemplate = spec;
		blockDef.customBlockTheme = BlockTheme.THEMES.get("blue");
		
		Definitions.get().put(blockDef.tag, blockDef);
		tagCache.put(blockDef.tag, blockDef);
		
//		set [propname] for [objectname] to [value]	DataStructures.get(objectname).propname = value;
		
		spec = "set %1 for %0 to %2";
		
		blockDef = new Definition
		(
			Category.CUSTOM,
			"ds-set-prop2",
			new AttributeType[] { AttributeTypes.TEXT, AttributeTypes.TEXT, AttributeTypes.OBJECT },
			new CodeElement()
			{
				@Override
				public void toCode(CodeBuilder builder)
				{
					builder.append("DataStructures.get(", arg, ").", codeArg, " = ", arg, ";");
				}
			},
			null,
			spec,
			BlockType.ACTION,
			AttributeTypes.VOID,
			null
		);
		
		blockDef.guiTemplate = spec;
		blockDef.customBlockTheme = BlockTheme.THEMES.get("blue");
		
		Definitions.get().put(blockDef.tag, blockDef);
		tagCache.put(blockDef.tag, blockDef);
		
//		get [propname] for [objectname]				DataStructures.get(objectname).propname
		
		spec = "get %1 from %0";
		
		blockDef = new Definition
		(
			Category.CUSTOM,
			"ds-get-prop2",
			new AttributeType[] { AttributeTypes.TEXT, AttributeTypes.TEXT },
			new CodeElement()
			{
				@Override
				public void toCode(CodeBuilder builder)
				{
					builder.append("DataStructures.get(", arg, ".", codeArg);
				}
			},
			null,
			spec,
			BlockType.NORMAL,
			AttributeTypes.OBJECT,
			null
		);
		
		blockDef.guiTemplate = spec;
		blockDef.customBlockTheme = BlockTheme.THEMES.get("blue");
		
		Definitions.get().put(blockDef.tag, blockDef);
		tagCache.put(blockDef.tag, blockDef);
		
//		transformer setter: set insets for window to [(top, bottom, left, right)]
//		transformer getter: top of [get insets for window]
		
		spec = "get data with name: %0";
		
		blockDef = new Definition
		(
			Category.CUSTOM,
			"ds-get-data",
			new AttributeType[] { AttributeTypes.TEXT },
			new BasicCodeMap("DataStructures.get(~)"),
			null,
			spec,
			BlockType.NORMAL,
			AttributeTypes.OBJECT,
			null
		);
		
		blockDef.guiTemplate = spec;
		blockDef.customBlockTheme = BlockTheme.THEMES.get("blue");
		
		Definitions.get().put(blockDef.tag, blockDef);
		tagCache.put(blockDef.tag, blockDef);
	}
	
	public static void addDesignModeBlocks(HaxeDataType type)
	{
		ArrayList<Definition> blocks = type.getBlocks();
		if(blocks != null)
		{
			for(Definition def : blocks)
			{
				Definitions.get().put(def.tag, def);
				tagCache.put(def.tag, def);
			}
		}
	}
	
	public static DropdownData createGenericDropdown(final String[] phrases, final String[] codeTexts)
	{
		return new DropdownData
		(
			new DropdownConverter()
			{
				@Override
				public int getIDForItem(Object o)
				{
					return -1;
				}
				
				@Override
				public Object[] getItems()
				{
					return phrases;
				}
			},
			new CodeConverter()
			{
				@Override
				public void toCode(CodeBuilder builder, int dropdownID, Block b, int index, Object o)
				{
					try
					{
						builder.append(codeTexts[index]);
					}
	
					catch(ArrayIndexOutOfBoundsException e)
					{
						log.error(e.getMessage(), e);
						builder.error();
					}
				}
			}
		);
	}
	
	public static void dispose()
	{
		for(String tag : tagCache.keySet())
			Definitions.get().remove(tag);
		tagCache.clear();
	}

	public static void removeDesignModeBlocks(HaxeDataType type)
	{
		ArrayList<Definition> blocks = type.getBlocks();
		if(blocks != null)
		{
			for(Definition def : blocks)
			{
				Definitions.get().remove(def.tag);
				tagCache.remove(def.tag);
			}
		}
	}
}

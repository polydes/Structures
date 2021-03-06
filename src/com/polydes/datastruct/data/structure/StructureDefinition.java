package com.polydes.datastruct.data.structure;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;

import javax.swing.ImageIcon;

import org.apache.commons.lang3.StringUtils;

import com.polydes.common.data.types.EditorProperties;
import com.polydes.common.ext.RegistryObject;
import com.polydes.common.nodes.DefaultEditableLeaf;
import com.polydes.common.nodes.DefaultLeaf;
import com.polydes.common.ui.object.EditableObject;
import com.polydes.datastruct.DataStructuresExtension;
import com.polydes.datastruct.data.core.Pair;
import com.polydes.datastruct.data.folder.Folder;
import com.polydes.datastruct.data.folder.FolderPolicy;
import com.polydes.datastruct.data.structure.elements.StructureField;
import com.polydes.datastruct.data.structure.elements.StructureTab;
import com.polydes.datastruct.data.structure.elements.StructureTabset;
import com.polydes.datastruct.data.types.ExtrasMap;
import com.polydes.datastruct.data.types.HaxeDataType;
import com.polydes.datastruct.ui.objeditors.StructureDefinitionEditor;
import com.polydes.datastruct.ui.page.StructurePage;

public class StructureDefinition extends EditableObject implements RegistryObject
{
	public static FolderPolicy STRUCTURE_DEFINITION_POLICY = new StructureDefinitionEditingPolicy();
	
	private BufferedImage iconImg;
	private ImageIcon icon;
	
	private String name;
	private String classname; // registry key
	
	public String iconSource;
	public String customCode = "";
	private final LinkedHashMap<String, StructureField> fields;
	public DefaultLeaf dref;
	public Folder guiRoot; //this is passed in from elsewhere.
	private StructureDefinitionEditor editor;
	
	public StructureDefinition parent = null;
	
	public StructureDefinition(String name, String classname)
	{
		this.name = name;
		this.classname = classname;
		fields = new LinkedHashMap<String, StructureField>();
		customCode = "";
		
		Structure.addType(this);
		
		dref = new DefaultEditableLeaf(name, this);
		dref.setIcon(icon);
		
		guiRoot = new Folder("root", new StructureTable(this));
		guiRoot.setPolicy(STRUCTURE_DEFINITION_POLICY);
		guiRoot.addListener(DefaultLeaf.DIRTY, event -> {
			dref.setDirty(guiRoot.isDirty());
		});
		dref.addListener(DefaultLeaf.DIRTY, event -> {
			guiRoot.setDirty(dref.isDirty());
		});
	}
	
	public void dispose()
	{
		disposeEditor();
		Structure.removeType(this);
		dref = null;
		guiRoot = null;
	}
	
	public void setImage(BufferedImage image)
	{
		this.iconImg = image;
		icon = new ImageIcon(image);
		dref.setIcon(icon);
	}
	
	public void setName(String name)
	{
		this.name = name;
	}
	
	/**
	 * Display name
	 */
	public String getName()
	{
		return name;
	}
	
	/**
	 * com.package.Class
	 */
	public String getFullClassname()
	{
		return classname;
	}
	
	/**
	 * (com.package.)Class
	 */
	public String getSimpleClassname()
	{
		return StringUtils.substringAfterLast(classname, ".");
	}
	
	/**
	 * com.package(.Class)
	 */
	public String getPackage()
	{
		if(classname.indexOf('.') == -1)
			return StringUtils.EMPTY;
		else
			return StringUtils.substringBeforeLast(classname, ".");
	}

	public void changeClassname(String newClassname)
	{
		DataStructuresExtension.get().getStructureDefinitions().renameItem(this, newClassname);
		Structures.root.setDirty(true);
	}
	
	public BufferedImage getIconImg()
	{
		return iconImg;
	}
	
	public ImageIcon getIcon()
	{
		return icon;
	}
	
	public StructureField getField(String name)
	{
		return fields.get(name);
	}
	
	public Collection<StructureField> getFields()
	{
		return fields.values();
	}
	
	@Override
	public String toString()
	{
		return name;
	}
	
	@Override
	public StructureDefinitionEditor getEditor()
	{
		if(editor == null)
		{
			editor = new StructureDefinitionEditor(this);
			savedDefinitionDirtyState = dref.isDirty();
		}
		
		return editor;
	}
	
	public void addField(StructureField f)
	{
		fields.put(f.getVarname(), f);
	}
	
	public void removeField(StructureField f)
	{
		fields.remove(f.getVarname());
	}
	
	public void setDirty(boolean value)
	{
		guiRoot.setDirty(value);
	}
	
	public boolean isDirty()
	{
		return guiRoot.isDirty();
	}

	//=== For runtime updating
	
	private ArrayList<StructureField> addedFields;
	private ArrayList<StructureField> removedFields;
	private HashMap<StructureField, TypeUpdate> typeUpdates;
	private HashMap<StructureField, Pair<String>> nameUpdates;
	
	private class TypeUpdate
	{
		//l is original type/optionalArgs
		//r is new type/optionalArgs
		
		Pair<HaxeDataType> type;
		Pair<EditorProperties> optArgs;
		
		public TypeUpdate(Pair<HaxeDataType> type, Pair<EditorProperties> optArgs)
		{
			this.type = type;
			this.optArgs = optArgs;
		}
	}
	
	public void addField(StructureField f, Structure s)
	{
		if(addedFields == null)
			addedFields = new ArrayList<StructureField>();
		addedFields.add(f);
		
		fields.put(f.getVarname(), f);
		s.clearProperty(f);
	}
	
	public void removeField(StructureField f, Structure s)
	{
		if(removedFields == null)
			removedFields = new ArrayList<StructureField>();
		removedFields.add(f);
		
		s.clearProperty(f);
		fields.remove(f.getVarname());
	}
	
	public void setFieldTypeForPreview(StructureField f, HaxeDataType type)
	{
		if(typeUpdates == null)
			typeUpdates = new HashMap<StructureField, TypeUpdate>();
		
		if(!typeUpdates.containsKey(f))
			typeUpdates.put(f,
				new TypeUpdate(
					new Pair<HaxeDataType>(
						f.getType(),
						null
					),
					new Pair<EditorProperties>(
						f.getEditorProperties(),
						null
					)
				)
			);
		
		TypeUpdate update = typeUpdates.get(f);
		update.type.r = type;
		update.optArgs.r = type.loadExtras(new ExtrasMap());
		
		editor.preview.clearProperty(f);
		f.setEditorProperties(update.optArgs.r);
	}
	
	public void update()
	{
		updateTypes();
		refreshFields(true);
		refreshEditors();
	}
	
	@Override
	public void revertChanges()
	{
		revertTypes();
		revertNames();
		refreshFields(false);
		
		if(!savedDefinitionDirtyState)
			dref.setDirty(false);
	}
	
	public void updateTypes()
	{
		if(typeUpdates != null)
		{
			for(StructureField field : typeUpdates.keySet())
			{
				Pair<HaxeDataType> types = typeUpdates.get(field).type;
				if(types.l == types.r)
					continue;
				setFieldType(field, types.r);
			}
			typeUpdates.clear();
			typeUpdates = null;
		}
	}
	
	public void revertTypes()
	{
		if(typeUpdates != null)
		{
			for(StructureField field : typeUpdates.keySet())
			{
				field.setEditorProperties(typeUpdates.get(field).optArgs.l);
				field.setType(typeUpdates.get(field).type.l);
			}
			typeUpdates.clear();
			typeUpdates = null;
		}
	}
	
	public void setFieldType(StructureField f, HaxeDataType type)
	{
		for(Structure s : Structure.getAllOfType(this))
			s.clearProperty(f);
		if(f.getType() != type)
		{
			f.setType(type);
			f.setEditorProperties(type.loadExtras(new ExtrasMap()));
		}
	}
	
	public void refreshFields(boolean commit)
	{
		if(commit)
		{
			//if(removeField != null) ... was in here before. Why?
			//This does nothing now.
		}
		else //revert
		{
			if(addedFields != null)
			{
				for(StructureField f : addedFields)
					removeField(f);
				addedFields.clear();
				addedFields = null;
			}
			if(removedFields != null)
			{
				for(StructureField f : removedFields)
					addField(f);
				removedFields.clear();
				removedFields = null;
			}
		}
	}
	
	private void revertNames()
	{
		if(nameUpdates != null)
		{
			for(StructureField f : nameUpdates.keySet())
				setFieldName(f, nameUpdates.get(f).l);
			nameUpdates.clear();
			nameUpdates = null;
		}
	}
	
	public void refreshEditors()
	{
		for(Structure s : Structure.getAllOfType(this))
			s.disposeEditor();
	}
	
	public void setFieldName(StructureField f, String name)
	{
		fields.remove(f.getVarname());
		fields.put(name, f);
	}
	
	/*-------------------------------------*\
	 * Inheritence
	\*-------------------------------------*/ 
	
	public boolean is(StructureDefinition def)
	{
		return this == def || (parent != null && parent.is(def));
	}
	
	//===
	
	private boolean savedDefinitionDirtyState;
	
	@Override
	public void disposeEditor()
	{
		if(editor != null)
			editor.dispose();
		editor = null;
	}
	
	static class StructureDefinitionEditingPolicy extends FolderPolicy
	{
		public StructureDefinitionEditingPolicy()
		{
			duplicateItemNamesAllowed = false;
			folderCreationEnabled = false;
			itemCreationEnabled = true;
			itemEditingEnabled = false;
			itemRemovalEnabled = true;
		}
		
		@Override
		public boolean canAcceptItem(Folder folder, DefaultLeaf item)
		{
			boolean tabset = folder.getUserData() instanceof StructureTabset;
			boolean tab = item.getUserData() instanceof StructureTab;
			
			if(tabset != tab)
				return false;
			
			return super.canAcceptItem(folder, item);
		}
	}

	public void remove()
	{
		for(Structure s : Structures.structures.get(this))
			StructurePage.get().getFolderModel().removeItem(s.dref, s.dref.getParent());
		
		DataStructuresExtension.get().getStructureDefinitions().unregisterItem(this);
		DataStructuresExtension.get().getHaxeTypes().unregisterItem(classname);
		Structures.structures.remove(this);
		
		dispose();
	}
	
	/*-------------------------------------*\
	 * Unknown Definitions
	\*-------------------------------------*/ 
	
	private boolean unknown;

	public boolean isUnknown()
	{
		return unknown;
	}
	
	public void realize(String name, String classname)
	{
		this.name = name;
		this.classname = classname;
		dref.setName(name);
		unknown = false;
	}
	
	public static StructureDefinition newUnknown(String name)
	{
		StructureDefinition def = new StructureDefinition(name, name);
		def.unknown = true;
		return def;
	}

	public void realizeFieldHaxeType(StructureField field, HaxeDataType t)
	{
		//like StructureField::realizeRO, this requires than when a HaxeDataType
		//has been registered, it already has a valid DataType<?>
		if(Structures.structures.containsKey(this))
			for(Structure struct : Structures.structures.get(this))
				if(struct.getProperty(field) != null)
					struct.setPropertyFromString(field, (String) struct.getProperty(field));
	}
	
	@Override
	public String getKey()
	{
		return classname;
	}
	
	@Override
	public void setKey(String newKey)
	{
		this.classname = newKey;
	}

	@Override
	public boolean fillsViewHorizontally()
	{
		return false;
	}
}
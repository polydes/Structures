package com.polydes.datastruct.data.types.haxe;

import static com.polydes.common.data.types.builtin.extra.SetType.EDITOR;
import static com.polydes.common.data.types.builtin.extra.SetType.GEN_TYPE;
import static com.polydes.common.data.types.builtin.extra.SetType.SOURCE;
import static com.polydes.common.data.types.builtin.extra.SetType.SOURCE_FILTER;

import java.util.function.Predicate;

import com.polydes.common.data.core.DataList;
import com.polydes.common.data.types.EditorProperties;
import com.polydes.common.data.types.PropertyKey;
import com.polydes.common.data.types.Types;
import com.polydes.common.data.types.builtin.extra.SetType.Editor;
import com.polydes.common.ui.propsheet.PropertiesSheetBuilder;
import com.polydes.common.ui.propsheet.PropertiesSheetSupport;
import com.polydes.datastruct.DataStructuresExtension;
import com.polydes.datastruct.data.structure.Structures;
import com.polydes.datastruct.data.structure.elements.StructureCondition;
import com.polydes.datastruct.data.types.ExtrasKey;
import com.polydes.datastruct.data.types.ExtrasMap;
import com.polydes.datastruct.data.types.HaxeDataType;
import com.polydes.datastruct.data.types.HaxeTypes;
import com.polydes.datastruct.ui.objeditors.StructureFieldPanel;

public class SetHaxeType extends HaxeDataType
{
	public SetHaxeType()
	{
		super(Types._Set, "com.polydes.datastruct.Set", "OBJECT");
	}
	
	public  static final PropertyKey<SourceType>                 SOURCE_TYPE            = new PropertyKey<>("sourceType");
	public  static final PropertyKey<String>                     SOURCE_ID              = new PropertyKey<>("sourceId");
	private static final PropertyKey<DataList>                   SOURCE_PROXY_LIST      = new PropertyKey<>("_" + SOURCE.id);
	private static final PropertyKey<StencylResourceHaxeType<?>> SOURCE_PROXY_RESOURCE  = new PropertyKey<>("_" + SOURCE.id);
	private static final PropertyKey<StructureHaxeType>          SOURCE_PROXY_STRUCTURE = new PropertyKey<>("_" + SOURCE.id);
	private static final PropertyKey<String>                     FILTER_PROXY           = new PropertyKey<>("_" + SOURCE_FILTER.id);
	
	public enum SourceType
	{
		Resource,
		Structure,
		Custom
	}
	
	//SERIALIZATION KEYS -- do not change these.
	private static final ExtrasKey<Editor>        KEY_EDITOR        = new ExtrasKey<>(EDITOR, "editor");
	private static final ExtrasKey<SourceType>    KEY_SOURCE_TYPE   = new ExtrasKey<>(SOURCE_TYPE, "sourceType");
	private static final ExtrasKey<String>        KEY_SOURCE_ID     = new ExtrasKey<>(SOURCE_ID, "sourceId");
	private static final ExtrasKey<DataList>      KEY_SOURCE        = new ExtrasKey<>(SOURCE_PROXY_LIST, "source");
	private static final ExtrasKey<String>        KEY_SOURCE_FILTER = new ExtrasKey<>(FILTER_PROXY, "sourceFilter");
	
	@Override
	public EditorProperties loadExtras(ExtrasMap extras)
	{
		EditorProperties props = new EditorProperties();
		props.put(EDITOR, extras.getEnum(KEY_EDITOR, Editor.Checklist));
		props.put(SOURCE_TYPE, extras.getEnum(KEY_SOURCE_TYPE, SourceType.Custom));
		String sourceId = extras.get(KEY_SOURCE_ID, Types._String, null);
		switch(props.get(SOURCE_TYPE))
		{
			case Custom:
				DataList list = extras.getTyped(KEY_SOURCE, Types._Array, null);
				props.put(SOURCE, list);
				props.put(SOURCE_PROXY_LIST, list);
				props.put(GEN_TYPE, Types._String);
				break;
			case Resource:
				DataStructuresExtension.get().getHaxeTypes().requestValue(sourceId, htype -> {
					StencylResourceHaxeType<?> srht = (StencylResourceHaxeType<?>) htype;
					props.put(SOURCE, srht.srt.getList());
					props.put(SOURCE_PROXY_RESOURCE, srht);
					props.put(SOURCE_ID, sourceId);
					props.put(GEN_TYPE, htype.dataType);
				});
				break;
			case Structure:
				DataStructuresExtension.get().getHaxeTypes().requestValue(sourceId, htype -> {
					StructureHaxeType sht = (StructureHaxeType) htype;
					props.put(SOURCE, Structures.getList(sht.type.def));
					props.put(SOURCE_PROXY_STRUCTURE, sht);
					props.put(SOURCE_ID, sourceId);
					props.put(GEN_TYPE, htype.dataType);
				});
				break;
		}
		String filter = extras.get(KEY_SOURCE_FILTER, Types._String, null);
		if(filter != null)
		{
			props.put(SOURCE_FILTER, new StructureConditionPredicate(new StructureCondition(null, filter)));
			props.put(FILTER_PROXY, filter);
		}
		return props;
	}

	@Override
	public ExtrasMap saveExtras(EditorProperties props)
	{
		ExtrasMap emap = new ExtrasMap();
		emap.putEnum(KEY_EDITOR, props.get(EDITOR));
		
		SourceType sourceType = props.get(SOURCE_TYPE);
		emap.putEnum(KEY_SOURCE_TYPE, sourceType);
		if(sourceType == SourceType.Custom)
			emap.putTyped(KEY_SOURCE, Types._Array, (DataList) props.get(SOURCE));
		else
			emap.put(KEY_SOURCE_ID, Types._String, props.get(SOURCE_ID)); 
		if(props.containsKey(SOURCE_FILTER))
		{
			Predicate<?> predicate = props.get(SOURCE_FILTER);
			if(predicate instanceof StructureConditionPredicate)
			{
				StructureCondition condition = ((StructureConditionPredicate) predicate).condition;
				if(condition != null)
					emap.put(KEY_SOURCE_FILTER, Types._String, condition.getText());
			}
		}
		
		return emap;
	}
	
	@Override
	public void applyToFieldPanel(final StructureFieldPanel panel)
	{
		EditorProperties props = panel.getExtras();
		
		HaxeTypes types = DataStructuresExtension.get().getHaxeTypes();
		SourceType sourceType = props.get(SOURCE_TYPE);
		
		PropertiesSheetSupport sheet = panel.getEditorSheet();
		PropertiesSheetBuilder builder = sheet.build();
		
		builder
			.field(SOURCE_TYPE.id)._enum(SourceType.class).add();
		
		switch(sourceType)
		{
			case Custom:
				builder.field(SOURCE_PROXY_LIST.id)
					._array().simpleEditor().genType(Types._String).add();
				break;
			case Resource:
				builder.field(SOURCE_PROXY_RESOURCE.id)
					._collection(types.values()).filter(htype -> (htype instanceof StencylResourceHaxeType)).add();
				break;
			case Structure:
				builder.field(SOURCE_PROXY_STRUCTURE.id)
					._collection(types.values()).filter(htype -> (htype instanceof StructureHaxeType)).add();
				break;
		}
		
		builder
			.field(FILTER_PROXY.id).optional()._string().add()
			.finish();
		
		updateSource(panel, sheet, props);
		
		sheet.addPropertyChangeListener(SOURCE_TYPE.id, event -> {
			updateSourceType(panel, sheet, props);
		});
		
		sheet.addPropertyChangeListener(SOURCE_PROXY_LIST.id, event -> {
			updateSource(panel, sheet, props);
		});
		
		sheet.addPropertyChangeListener(FILTER_PROXY.id, event -> {
			Predicate<?> predicate = props.get(SOURCE_FILTER);
			String conditionText = props.get(FILTER_PROXY);
			StructureCondition condition = null;
			if(predicate != null && predicate instanceof StructureConditionPredicate)
				condition = ((StructureConditionPredicate) predicate).condition;
			
			if(condition == null && !conditionText.isEmpty())
				condition = new StructureCondition(null, conditionText);
			else if(condition != null && conditionText.isEmpty())
				condition = null;
			else if(condition != null && !conditionText.isEmpty())
				condition.setText(conditionText);
			
			if(predicate == null && condition != null)
				predicate = new StructureConditionPredicate(condition);
			else if(predicate != null && condition == null)
				condition = null;
			
			//remove SOURCE_FILTER first so it forces the PropertyChangeEvent to be fired.
			props.remove(SOURCE_FILTER);
			sheet.writeField(props, SOURCE_FILTER.id, predicate);
		});
		
		panel.setRowVisibility(sheet, FILTER_PROXY.id, sourceType != SourceType.Custom);
	}
	
	private static final class StructureConditionPredicate implements Predicate<Object>
	{
		private StructureCondition condition;
		
		public StructureConditionPredicate(StructureCondition condition)
		{
			this.condition = condition;
		}
		
		@Override
		public boolean test(Object t)
		{
			return condition.check(null, t);
		}
	}
	
	private void updateSourceType(StructureFieldPanel panel, PropertiesSheetSupport sheet, EditorProperties props)
	{
		SourceType type = props.get(SOURCE_TYPE);
		
		HaxeTypes types = DataStructuresExtension.get().getHaxeTypes();
		
		props.remove(SOURCE_PROXY_LIST);
		
		switch(type)
		{
			case Custom:
				sheet.change().field(SOURCE_PROXY_LIST.id)
					._array().simpleEditor().genType(Types._String).change().finish();
				break;
			case Resource:
				sheet.change().field(SOURCE_PROXY_RESOURCE.id)
					._collection(types.values()).filter(htype -> (htype instanceof StencylResourceHaxeType)).change().finish();
				break;
			case Structure:
				sheet.change().field(SOURCE_PROXY_STRUCTURE.id)
					._collection(types.values()).filter(htype -> (htype instanceof StructureHaxeType)).change().finish();
				break;
		}
		
		panel.setRowVisibility(sheet, FILTER_PROXY.id, type != SourceType.Custom);
	}
	
	private void updateSource(StructureFieldPanel panel, PropertiesSheetSupport sheet, EditorProperties props)
	{
		SourceType type = props.get(SOURCE_TYPE);
		
		if(props.get(SOURCE_PROXY_LIST) == null)
			return;
		
		//remove GEN_TYPE first so it forces the PropertyChangeEvent to be fired.
		props.remove(GEN_TYPE);
		
		switch(type)
		{
			case Custom:
				props.put(SOURCE, props.get(SOURCE_PROXY_LIST));
				props.remove(SOURCE_ID);
				sheet.writeField(props, GEN_TYPE.id, Types._String);
				break;
			case Resource:
				StencylResourceHaxeType<?> srht = props.get(SOURCE_PROXY_RESOURCE);
				props.put(SOURCE, srht.srt.getList());
				props.put(SOURCE_ID, srht.getKey());
				sheet.writeField(props, GEN_TYPE.id, srht.dataType);
				break;
			case Structure:
				StructureHaxeType sht = props.get(SOURCE_PROXY_STRUCTURE);
				props.put(SOURCE, Structures.getList(sht.type.def));
				props.put(SOURCE_ID, sht.getKey());
				sheet.writeField(props, GEN_TYPE.id, sht.dataType);
				break;
		}
	}
}

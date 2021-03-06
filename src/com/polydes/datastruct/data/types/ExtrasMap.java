package com.polydes.datastruct.data.types;

import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Set;

import com.polydes.common.data.types.DataType;
import com.polydes.common.data.types.EditorProperties;
import com.polydes.common.ext.RORealizer;
import com.polydes.datastruct.DataStructuresExtension;

/**
 * A map used for serialization of {@link EditorProperties}.
 */
public class ExtrasMap
{
	/** This map contains only two types of values: String and ExtrasMap */
	private final HashMap<String, Object> map = new HashMap<>();
	
	public <T> T get(ExtrasKey<T> key, DataType<T> type, T defaultValue)
	{
		String s = (String) map.get(key.id);
		if(s == null)
			return defaultValue;
		else
			return type.decode(s);
	}
	
	public <T> T get(ExtrasKey<T> key, DataType<T> type)
	{
		String s = (String) map.get(key.id);
		if(s == null)
			return null;
		else
			return type.decode(s);
	}
	
	/*-------------------------------------*\
	 * Map Delegation
	\*-------------------------------------*/ 
	
	public <T> void put(ExtrasKey<T> key, DataType<T> type, T value)
	{
		if(value != null)
			map.put(key.id, type.encode(value));
	}

	public boolean containsKey(ExtrasKey<?> key)
	{
		return map.containsKey(key.id);
	}

	/**
	 * Directly get the entrySet from the backing map.
	 * All values are either String or ExtrasMap.
	 */
	public Set<Entry<String, Object>> backingEntrySet()
	{
		return map.entrySet();
	}

	/**
	 * Directly place into the backing map
	 */
	public void backingPutAll(HashMap<String, String> stringMap)
	{
		map.putAll(stringMap);
	}
	
	/**
	 * Directly place into the backing map
	 */
	public void backingPut(String key, ExtrasMap value)
	{
		map.put(key, value);
	}
	
	/**
	 * Directly place into the backing map
	 */
	public void backingPut(String key, String value)
	{
		map.put(key, value);
	}

	public boolean isEmpty()
	{
		return map.isEmpty();
	}
	
	/*-------------------------------------*\
	 * Special Cases: Haxe Types
	\*-------------------------------------*/ 
	
	@SuppressWarnings("unchecked")
	public <T> T getTyped(ExtrasKey<T> key, DataType<T> type, T defaultValue)
	{
		String s = (String) map.get(key.id);
		if(s == null)
			return defaultValue;
		else
			return (T) HaxeTypeConverter.decode(type, s);
	}
	
	public <T,U extends T> String putTyped(ExtrasKey<T> key, DataType<T> type, U value)
	{
		return (String) map.put(key.id, HaxeTypeConverter.encode(type, value));
	}
	
	/*-------------------------------------*\
	 * Special Cases: Map, Enum, DataType
	\*-------------------------------------*/ 
	
	public ExtrasMap getMap(ExtrasKey<EditorProperties> key)
	{
		return (ExtrasMap) map.get(key.id);
	}
	
	public void putMap(ExtrasKey<EditorProperties> key, ExtrasMap value)
	{
		map.put(key.id, value);
	}
	
	@SuppressWarnings("unchecked")
	public <T extends Enum<T>> T getEnum(ExtrasKey<T> key, Enum<T> enm)
	{
		String name = (String) map.get(key.id);
		if(name == null)
			return (T) enm;
		
		try
		{
			return (T) Enum.valueOf(enm.getClass(), name);
		}
		catch(IllegalArgumentException | NullPointerException ex)
		{
			return (T) enm;
		}
	}

	public <T extends Enum<T>> void putEnum(ExtrasKey<T> key, Enum<T> enm)
	{
		if(enm != null)
			map.put(key.id, enm.name());
	}
	
	public void requestDataType(ExtrasKey<DataType<?>> key, HaxeDataType defaultType, RORealizer<HaxeDataType> tr)
	{
		String s = (String) map.get(key.id);
		if(s == null)
			tr.realizeRO(defaultType);
		else
			DataStructuresExtension.get().getHaxeTypes().requestValue(s, tr);
	}
	
	public void putDataType(ExtrasKey<DataType<?>> key, DataType<?> type)
	{
		String haxeTypeID = DataStructuresExtension.get().getHaxeTypes().getHaxeFromDT(type.getId()).getHaxeType();
		map.put(key.id, haxeTypeID);
	}
}
package com.polydes.datastruct.ui.objeditors;

import com.polydes.common.data.types.EditorProperties;
import com.polydes.common.nodes.DefaultLeaf;
import com.polydes.common.ui.propsheet.PropertiesSheetStyle;
import com.polydes.common.ui.propsheet.PropertiesSheetSupport;
import com.polydes.datastruct.data.structure.elements.StructureField;
import com.polydes.datastruct.data.types.HaxeDataType;
import com.polydes.datastruct.data.types.HaxeDataTypeType;
import com.polydes.datastruct.ui.table.PropertiesSheet;

public class StructureFieldPanel extends StructureObjectPanel
{
	StructureField field;
	
	boolean oldDirty;
	
	PropertiesSheetSupport editorSheet;
	
	public StructureFieldPanel(final StructureField field, PropertiesSheetStyle style)
	{
		super(style, field);
		
		this.field = field;

		String nameHint = 
			"Variable Name Format:<br/>" + 
			"A letter or underscore, followed by any<br/>" + 
			"number of letters, numbers, or underscores.";
		
		sheet.build()
			
			.field("label")._string().add()
			
			.field("type")._editor(new HaxeDataTypeType()).add()
			
			.field("varname").label("Name").hint(nameHint)._string().regex("([a-zA-Z_][a-z0-9A-Z_]*)?").add()
			
			.field("hint")._string().expandingEditor().add()
			
			.field("optional")._boolean().add().onUpdate(() -> preview.refreshLeaf(previewKey))
			
			.field("defaultValue").label("Default")._editor(field.getType().dataType).loadProps(field.getEditorProperties()).add()
			
			.finish();
		
		sheet.addPropertyChangeListener("label", event -> {
			previewKey.setName(field.getLabel());
			
			String oldLabel = (String) event.getOldValue();
			if(StructureField.formatVarname(oldLabel).equals(field.getVarname()))
				sheet.updateField("varname", StructureField.formatVarname(field.getLabel()));
			
			preview.lightRefreshLeaf(previewKey);
		});
		
		sheet.addPropertyChangeListener("type", event -> {
			HaxeDataType type = field.getType();
			
			field.setDefaultValue(type.dataType.decode(""));
			sheet.change().field("defaultValue")._editor(type.dataType).change().finish();
			
			field.setTypeForPreview(type);
			refreshFieldEditors();
			preview.refreshLeaf(previewKey);
			
			layoutContainer();
			revalidate();
			setSize(getPreferredSize());
		});
			
		sheet.addPropertyChangeListener("hint", event -> {
			String oldV = (String) event.getOldValue();
			String newV = (String) event.getNewValue();
			if(oldV.isEmpty() || newV.isEmpty())
				preview.refreshLeaf(previewKey);
			else
				preview.lightRefreshLeaf(previewKey);
		});
		
		refreshFieldEditors();
	}
	
	// === Methods for DataType extra property appliers.
	
	@Override
	public void setPreviewSheet(PropertiesSheet sheet, DefaultLeaf key)
	{
		super.setPreviewSheet(sheet, key);
		refreshFieldEditors();
	}
	
	private void refreshFieldEditors()
	{
		if(editorSheet != null)
			clearSheetExtension("editor");
		editorSheet = createSheetExtension(field.getEditorProperties(), "editor");
		editorSheet.addPropertyChangeListener(event -> refreshGeneratedEditor());
		field.getType().applyToFieldPanel(StructureFieldPanel.this);
	}
	
	protected void refreshGeneratedEditor()
	{
		sheet.change()
			.field("defaultValue")._editor(field.getType().dataType).loadProps(field.getEditorProperties()).change()
			.finish();
		if(preview != null)
			preview.refreshLeaf(previewKey);
	}
	
	public StructureField getField()
	{
		return field;
	}
	
	public EditorProperties getExtras()
	{
		return field.getEditorProperties();
	}
	
	public PropertiesSheet getPreview()
	{
		return preview;
	}
	
	public DefaultLeaf getPreviewKey()
	{
		return previewKey;
	}
	
	public PropertiesSheetSupport getSheet()
	{
		return sheet;
	}
	
	public PropertiesSheetSupport getEditorSheet()
	{
		return editorSheet;
	}
	
	// ===
	
	public void revert()
	{
		super.revertChanges();
	}
}

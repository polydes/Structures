package com.polydes.datastruct.ui.page;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.AbstractAction;
import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.polydes.common.comp.UpdatingCombo;
import com.polydes.datastruct.DataStructuresExtension;
import com.polydes.datastruct.data.structure.StructureDefinition;
import com.polydes.datastruct.data.structure.StructureFolder;

import stencyl.sw.lnf.Theme;
import stencyl.sw.util.comp.ButtonBarFactory;
import stencyl.sw.util.comp.GroupButton;
import stencyl.sw.util.dg.DialogPanel;
import stencyl.sw.util.dg.StencylDialog;

public class EditFolderDialog extends StencylDialog
{
	StructureFolder folder;
	StructureDefinition childType;
	
	JCheckBox structureExclusiveField;
	UpdatingCombo<StructureDefinition> typeChooser;
	
	JButton okButton;
	
	public EditFolderDialog(JFrame parent)
	{
		super(parent, "Edit Folder", 350, 200, true);
	}
	
	@Override
	public JComponent createContentPanel()
	{
		JPanel panel = new JPanel(new BorderLayout());
		panel.setBackground(Theme.LIGHT_BG_COLOR);
		panel.setBorder(BorderFactory.createMatteBorder(0, 1, 0, 1, Theme.BORDER_COLOR));
		
		DialogPanel dp = new DialogPanel(Theme.LIGHT_BG_COLOR);
		
		dp.startBlock();
		dp.addHeader("Edit Folder");
		dp.addGenericRow("Structure Exclusive", structureExclusiveField = new JCheckBox());
		dp.addGenericRow("Structure", typeChooser = new UpdatingCombo<StructureDefinition>(DataStructuresExtension.get().getStructureDefinitions().values(), null));
		dp.finishBlock();
		
		structureExclusiveField.setBackground(null);
		
		structureExclusiveField.addChangeListener(new ChangeListener()
		{
			@Override
			public void stateChanged(ChangeEvent e)
			{
				typeChooser.setEnabled(structureExclusiveField.isSelected());
			}
		});
		
		panel.add(dp, BorderLayout.CENTER);

		return panel;
	}
	
	public void setFolder(final StructureFolder folder)
	{
		this.folder = folder;
		childType = folder.childType;
		
		if(childType != null)
		{
			structureExclusiveField.setSelected(true);
			typeChooser.setEnabled(true);
			typeChooser.setSelectedItem(childType);
		}
		else
		{
			structureExclusiveField.setSelected(false);
			typeChooser.setEnabled(false);
		}
		
		typeChooser.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				childType = typeChooser.getSelected();
			}
		});
		
		setVisible(true);
	}
	
	@Override
	public JPanel createButtonPanel()
	{
		okButton = new GroupButton(0);

		okButton.setAction(new AbstractAction("OK")
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				ok();
				setVisible(false);
			}
		});

		AbstractButton cancelButton = new GroupButton(0);

		cancelButton.setAction(new AbstractAction("Cancel")
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				cancel();
			}
		});

		return ButtonBarFactory.createButtonBar
				(
					this,
					new AbstractButton[] { okButton, cancelButton },
					0
				);
	}
	
	private void ok()
	{
		folder.childType = childType;
	}
	
	@Override
	public void cancel()
	{
		setVisible(false);
	}
	
	@Override
	public void dispose()
	{		
		folder = null;
		childType = null;
		structureExclusiveField = null;
		typeChooser = null;
		okButton = null;
		
		super.dispose();
	}
}

package com.polydes.datastruct.data.structure.elements;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.swing.JPanel;

import org.w3c.dom.Element;

import com.polydes.common.io.XML;
import com.polydes.common.nodes.DefaultBranch;
import com.polydes.common.nodes.DefaultLeaf;
import com.polydes.common.res.ResourceLoader;
import com.polydes.common.res.Resources;
import com.polydes.common.ui.propsheet.PropertiesSheetStyle;
import com.polydes.common.util.Lang;
import com.polydes.datastruct.data.structure.SDE;
import com.polydes.datastruct.data.structure.SDEType;
import com.polydes.datastruct.data.structure.Structure;
import com.polydes.datastruct.data.structure.StructureDefinition;
import com.polydes.datastruct.grammar.ExpressionParser;
import com.polydes.datastruct.grammar.RuntimeLanguage;
import com.polydes.datastruct.grammar.SyntaxException;
import com.polydes.datastruct.grammar.SyntaxNode;
import com.polydes.datastruct.ui.objeditors.StructureConditionPanel;
import com.polydes.datastruct.ui.table.Card;
import com.polydes.datastruct.ui.table.GuiObject;
import com.polydes.datastruct.ui.table.PropertiesSheet;
import com.polydes.datastruct.ui.table.RowGroup;

public class StructureCondition extends SDE
{
	private static Resources res = ResourceLoader.getResources("com.polydes.datastruct");
	
	public StructureDefinition def;
	public SyntaxNode root;
	
	private String text;
	
	public StructureCondition(StructureDefinition def, String text)
	{
		this.def = def;
		setText(text);
	}
	
	public boolean check(Structure s)
	{
		return check(s, null);
	}
	
	public boolean check(Structure s, Object listItem)
	{
//		System.out.println("check");
		
		if(root == null)
			return false;
		
		try
		{
//			System.out.println("eval begin");
			structureRef = s;
			idMap.put("this", s);
			idMap.put("item", listItem);
			return check(root);
		}
		catch (SyntaxException e)
		{
//			System.out.println("Bad syntax, returning false");
//			System.out.println("===");
//			e.printStackTrace();
//			System.out.println("===");
			return false;
		}
	}
	
	//State variables.
	private static HashMap<String, Object> idMap = new HashMap<String, Object>(2);
	private static Structure structureRef;
	
	public static void dispose()
	{
		idMap.clear();
		structureRef = null;
	}
	
	private boolean check(SyntaxNode n) throws SyntaxException
	{
		try
		{
			return (Boolean) eval(n);
		}
		catch(ClassCastException ex)
		{
			throw new SyntaxException(ex);
		}
		catch(NullPointerException ex)
		{
			throw new SyntaxException(ex);
		}
	}
	
	private Object eval(SyntaxNode n) throws SyntaxException
	{
		try
		{
//			if(n.children != null)
//				System.out.println(n.type + ": " + StringUtils.join(n.children.toArray(), ", "));
//			else
//				System.out.println(n.type + ": no children");
			
			switch(n.type)
			{
				case BOOL: return (Boolean) n.data;
				case FLOAT: return (Float) n.data;
				case INT: return (Integer) n.data;
				case STRING: return (String) n.data;
				case NULL: return null;
				case REFERENCE:
					String refName = (String) n.data;
					Object ref = idMap.get(refName);
					if(ref == null && structureRef != null)
						ref = structureRef.getPropByName(refName);
					return ref;
				
				case AND: return RuntimeLanguage.and(eval(n.get(0)), eval(n.get(1)));
				case OR: return RuntimeLanguage.or(eval(n.get(0)), eval(n.get(1)));
				case NOT: return RuntimeLanguage.not(eval((SyntaxNode) n.data));
				case EQUAL: return RuntimeLanguage.equals(eval(n.get(0)), eval(n.get(1)));
				case NOTEQUAL: return !RuntimeLanguage.equals(eval(n.get(0)), eval(n.get(1)));
				case GT: return RuntimeLanguage.gt(eval(n.get(0)), eval(n.get(1)));
				case LT: return RuntimeLanguage.lt(eval(n.get(0)), eval(n.get(1)));
				case GE: return RuntimeLanguage.ge(eval(n.get(0)), eval(n.get(1)));
				case LE: return RuntimeLanguage.le(eval(n.get(0)), eval(n.get(1)));
				
				case ADD: return RuntimeLanguage.add(eval(n.get(0)), eval(n.get(1)));
				case SUB: return RuntimeLanguage.sub(eval(n.get(0)), eval(n.get(1)));
				case MOD: return RuntimeLanguage.mod(eval(n.get(0)), eval(n.get(1)));
				case DIVIDE: return RuntimeLanguage.divide(eval(n.get(0)), eval(n.get(1)));
				case MULTIPLY: return RuntimeLanguage.multiply(eval(n.get(0)), eval(n.get(1)));
				case NEGATE: return RuntimeLanguage.negate(eval((SyntaxNode) n.data));
				
				case FIELD:
					Object o = eval(n.get(0));
					String fieldName = (String) n.get(1).data;
					
//					System.out.println("FIELD");
//					System.out.println(o);
//					System.out.println(fieldName);
					
					if(o instanceof Structure)
						return ((Structure) o).getPropByName(fieldName);
					else
						return RuntimeLanguage.field(o, fieldName);
					
				case METHOD:
					//TODO: May not work with fields that have primary parameters.
					//Can look at Haxe's Runtime callField() to see what happens there
					Object callOn = eval(n.get(0));
					String methodName = (String) n.get(1).data;
					List<Object> args = new ArrayList<Object>();
					for(int i = 2; i < n.getNumChildren(); ++ i)
						args.add(eval(n.get(i)));
					
//					System.out.println("METHOD");
//					System.out.println(callOn);
//					System.out.println(methodName);
//					System.out.println(StringUtils.join(args.toArray(), ", "));
					
					return RuntimeLanguage.invoke(callOn, methodName, args);
			}
		}
		catch(ClassCastException ex)
		{
			throw new SyntaxException(ex);
		}
		catch(NullPointerException ex)
		{
			throw new SyntaxException(ex);
		}
		
		return null;
	}
	
	public void setText(String text)
	{
		this.text = text;
		root = ExpressionParser.buildTree(text);
	}
	
	public String getText()
	{
		return text;
	}
	
	public StructureCondition copy()
	{
		StructureCondition sc = new StructureCondition(def, text);
		return sc;
	}
	
	private StructureConditionPanel editor;
	
	@Override
	public void disposeEditor()
	{
		if(editor != null)
			editor.dispose();
		
		editor = null;
	}
	
	@Override
	public JPanel getEditor()
	{
		if(editor == null)
			editor = new StructureConditionPanel(this, PropertiesSheetStyle.LIGHT);
		
		return editor;
	}
	
	@Override
	public void revertChanges()
	{
		if(editor != null)
			editor.revertChanges();
	}
	
	@Override
	public String toString()
	{
		return "if " + text;
	}
	
	@Override
	public String getDisplayLabel()
	{
		return toString();
	}
	
	public static class ConditionType extends SDEType<StructureCondition>
	{
		public ConditionType()
		{
			sdeClass = StructureCondition.class;
			tag = "if";
			isBranchNode = true;
			icon = res.loadThumbnail("condition.png", 16);
			childTypes = Lang.arraylist(
				StructureCondition.class,
				StructureField.class,
				StructureHeader.class,
				StructureTabset.class,
				StructureText.class
			);
		}
		
		@Override
		public StructureCondition read(StructureDefinition model, Element e)
		{
			return new StructureCondition(model, XML.read(e, "condition"));
		}
		
		@Override
		public void write(StructureCondition object, Element e)
		{
			XML.write(e, "condition", object.getText());
		}

		@Override
		public StructureCondition create(StructureDefinition def, String nodeName)
		{
			return new StructureCondition(def, "");
		}
		
		@Override
		public GuiObject psAdd(PropertiesSheet sheet, DefaultBranch parent, DefaultLeaf node, StructureCondition value, int i)
		{
			Card parentCard = sheet.getFirstCardParent(parent);
			
			RowGroup group = new RowGroup(value);
			Card card = createConditionalCard(value, (DefaultBranch) node, sheet.model, sheet);
			
			group.addSubcard(card, parentCard);
			parentCard.addGroup(i, group);
			
			card.setCondition(value);
			sheet.conditionalCards.add(card);
			
			if(!sheet.isChangingLayout)
				parentCard.layoutContainer();
			
			return group;
		}
		
		@Override
		public void psRefresh(PropertiesSheet sheet, GuiObject gui, DefaultLeaf node, StructureCondition value)
		{
			
		}
		
		@Override
		public void psRemove(PropertiesSheet sheet, GuiObject gui, DefaultLeaf node, StructureCondition value)
		{
			RowGroup group = (RowGroup) gui;
			Card card = group.card;
			
			int groupIndex = card.indexOf(group);
			card.removeGroup(groupIndex);
			
			sheet.conditionalCards.remove(group.removeSubcard());
			
			card.layoutContainer();
		}

		@Override
		public void psLightRefresh(PropertiesSheet sheet, GuiObject gui, DefaultLeaf node, StructureCondition value)
		{
			((RowGroup) gui).getSubcard().setCondition(value);
		}
		
		private Card createConditionalCard(final StructureCondition c, final DefaultBranch n, final Structure model, final PropertiesSheet sheet)
		{
			return new Card("", false)
			{
				@Override
				public boolean checkCondition()
				{
					return model.checkCondition(condition); 
				}
				
				@Override
				public void check()
				{
					boolean visible = super.visible;
					
					super.check();
					
					if(visible && !super.visible)
						for(StructureField f : sheet.allDescendentsOfType(StructureField.class, null, n))
							model.clearProperty(f);
				}
			};
		}
	}
}
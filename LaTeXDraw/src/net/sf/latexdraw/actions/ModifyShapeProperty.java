package net.sf.latexdraw.actions;

import java.util.List;

import net.sf.latexdraw.glib.models.interfaces.IGroup;

import org.malai.undo.Undoable;

/**
 * This action modifies a shape property of the given shape.<br>
 * <br>
 * This file is part of LaTeXDraw.<br>
 * Copyright (c) 2005-2012 Arnaud BLOUIN<br>
 * <br>
 * LaTeXDraw is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later version.
 * <br>
 * LaTeXDraw is distributed without any warranty; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.<br>
 * <br>
 * 11/01/2010<br>
 * @author Arnaud BLOUIN
 * @since 3.0
 */
public class ModifyShapeProperty extends ShapePropertyAction implements Undoable, Modifying {
	/** The shape to modify. */
	protected IGroup shapes;

	/** The old value of the property. */
	protected List<?> oldValue;


	/**
	 * Creates the action.
	 * @since 3.0
	 */
	public ModifyShapeProperty() {
		super();
	}


	@Override
	public void flush() {
		super.flush();

		if(shapes!=null) {
			shapes.clear();
			shapes = null;
		}

		if(oldValue!=null) {
			oldValue.clear();
			oldValue = null;
		}
	}


	@Override
	public boolean canDo() {
		return super.canDo() && shapes!=null;
	}


	@Override
	public void undo() {
		property.setPropertyValueList(shapes, oldValue);
		shapes.setModified(true);
	}


	@Override
	public void redo() {
		applyValue(value);
	}


	@Override
	public String getUndoName() {
		return property==null ? "" : property.getMessage(); //$NON-NLS-1$
	}


	@Override
	public boolean isRegisterable() {
		return true;
	}


	@Override
	protected void applyValue(final Object obj) {
		property.setPropertyValue(shapes, obj);
		shapes.setModified(true);
	}


	@Override
	protected void doActionBody() {
		oldValue = property.getPropertyValues(shapes);
		applyValue(value);
	}


	/**
	 * Sets the group of shapes to modify.
	 * @param group The group of shapes to modify.
	 * @since 3.0
	 */
	public void setGroup(final IGroup group) {
		this.shapes = group;
	}



	@Override
	protected boolean isPropertySupported() {
		if(super.isPropertySupported())
			switch(property) {
				case ARROW_BRACKET_NUM:
				case ARROW_DOT_SIZE_DIM:
				case ARROW_DOT_SIZE_NUM:
				case ARROW_R_BRACKET_NUM:
				case ARROW_SIZE_DIM:
				case ARROW_SIZE_NUM:
				case ARROW_T_BAR_SIZE_DIM:
				case ARROW_T_BAR_SIZE_NUM:
				case ARROW_INSET:
				case ARROW_LENGTH:
				case ARROW1_STYLE:
				case ARROW2_STYLE:		return shapes.isArrowable();
				case BORDER_POS:		return shapes.isBordersMovable();
				case COLOUR_FILLING:	return shapes.isFillable();
				case COLOUR_GRADIENT_END:
				case COLOUR_GRADIENT_START:
				case DOT_FILLING_COL:
				case LINE_STYLE:		return shapes.isLineStylable();
				case LINE_THICKNESS:	return shapes.isThicknessable();
				case DBLE_BORDERS_SIZE:
				case DBLE_BORDERS:
				case COLOUR_DBLE_BORD:	return shapes.isDbleBorderable();
				case SHADOW_ANGLE:
				case SHADOW_SIZE:
				case COLOUR_SHADOW:
				case SHADOW:			return shapes.isShadowable();
				case GRAD_ANGLE:
				case GRAD_MID_POINT:
				case HATCHINGS_ANGLE:
				case HATCHINGS_SEP:
				case HATCHINGS_WIDTH:
				case COLOUR_HATCHINGS:
				case FILLING_STYLE:		return shapes.isInteriorStylable();
				case ROUND_CORNER_VALUE:
				case DOT_SIZE:
				case DOT_STYLE:
				case ROTATION_ANGLE:
				case TEXT:
				case TEXT_POSITION:
				case ARC_END_ANGLE:
				case ARC_START_ANGLE:
				case ARC_STYLE:
				case GRID_START:
				case GRID_END:
				case GRID_SIZE_LABEL:
				case COLOUR_LINE:		return true;
			}

		return false;
	}
}

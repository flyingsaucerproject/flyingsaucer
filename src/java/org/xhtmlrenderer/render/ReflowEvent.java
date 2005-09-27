package org.xhtmlrenderer.render;

import java.awt.Dimension;
import java.awt.Graphics;
import org.xhtmlrenderer.resource.ImageResource;

public class ReflowEvent {
	private int type;
	public static int CANVAS_RESIZED = 1;
	public static int IMAGE_SIZE_CHANGED = 2;
	public static int PARENT_SIZE_CHANGED = 3;
	public static int EXTERNAL_RESIZE = 4;
	public static int EXTERNAL_REPAINT = 5;
	public static int USER_INTERACTION_RESIZE = 6;
	public static int MORE_BOXES_AVAILABLE = 7;
	public static int IMAGE_CONTENT_LOADED = 8;
	public static int IMAGE_CONTENT_CHANGED = 9;
	public static int BLOCK_CONTENT_CHANGED = 10;
	public static int CANVAS_EXPOSED = 11;
	public static int LAYOUT_COMPLETE = 12;
    
    public static int DOCUMENT_SET = 13;


	public static int UNKNOWN_EVENT = 99;
	
	private Box box;
	private Dimension dim;
	private ImageResource img_res;
	public ReflowEvent(int type) {
		this.type = type;
	}
	public ReflowEvent(int type, Dimension dim) {
		this(type);
		this.dim = dim;
		this.g = g;
	}
	public ReflowEvent(int type, Box box) {
		this.type = type;
		this.box = box;
	}
	public ReflowEvent(int type, Box box, Dimension dim) {
		this.type = type;
		this.box = box;
		this.dim = dim;
	}
	public ReflowEvent(int type, ImageResource ir) {
		this(type);
		this.img_res = ir;
	}
	public Box getBox() {
		return this.box;
	}
	public int getType() {
		return this.type;
	}
	public Dimension getDimension() {
		return this.dim;
	}
	
	private Graphics g;
	public Graphics getGraphics() {
		return g;
	}
	
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("ReflowEvent: ");
		sb.append(" type = " + type);
		
		if(type == this.CANVAS_RESIZED) {
			sb.append(" CANVAS_RESIZED event");
		}
		
		if(type == this.MORE_BOXES_AVAILABLE) {
			sb.append(" MORE_BOXES_AVAILABLE event");
		}
		
		if(type == this.LAYOUT_COMPLETE) {
			sb.append(" LAYOUT_COMPLETE event");
		}
		
		if(type == this.IMAGE_CONTENT_LOADED) {
			sb.append(" IMAGE_CONTENT_LOADED event");
		}
		
		if(dim != null) {
			sb.append(" "+dim.getWidth() + " x " + dim.getHeight());
		}
		return sb.toString();
	}
	
	/*
	
	could put in code to record the damaged part of the canvas so that
	the render thread would have less work to do.
	
	the more boxes available type should store the last safe box
	
	*/

}

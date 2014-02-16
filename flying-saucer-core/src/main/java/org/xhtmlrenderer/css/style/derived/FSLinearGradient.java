package org.xhtmlrenderer.css.style.derived;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import org.w3c.dom.css.CSSPrimitiveValue;
import org.xhtmlrenderer.css.constants.CSSName;
import org.xhtmlrenderer.css.constants.Idents;
import org.xhtmlrenderer.css.parser.FSColor;
import org.xhtmlrenderer.css.parser.FSFunction;
import org.xhtmlrenderer.css.parser.FSRGBColor;
import org.xhtmlrenderer.css.parser.PropertyValue;
import org.xhtmlrenderer.css.parser.property.BuilderUtil;
import org.xhtmlrenderer.css.parser.property.Conversions;
import org.xhtmlrenderer.css.style.CalculatedStyle;
import org.xhtmlrenderer.css.style.CssContext;
import org.xhtmlrenderer.util.GeneralUtil;

public class FSLinearGradient
{
	private final float angle;
	
	public static class StopValue
	{
		private final FSColor color;
		private final short lengthType;
		private final Float length;
		private Float dotsValue;
		
		private StopValue(FSColor color, Float value, short lengthType)
		{
			this.color = color;
			this.length = value;
			this.lengthType = lengthType;
		}
		
		private StopValue(FSColor color)
		{
			this.color = color;
			this.length = null;
			this.lengthType = 0;
		}
		
		public FSColor getColor()
		{
			return this.color;
		}
		
		public float getLength()
		{
			return this.dotsValue;
		}
		
		@Override
		public String toString() 
		{
			return "[" + this.color.toString() + "](" + this.dotsValue + ")";
		}
	}
	
	public float getAngle()
	{
		return angle;
	}
	
	private final List<StopValue> stopPoints = new ArrayList<StopValue>(2);
	
	public FSLinearGradient(FSFunction func, CalculatedStyle style, float width, CssContext ctx)
	{
		List<PropertyValue> params = func.getParameters();
		int i = 1;
		
		if (GeneralUtil.ciEquals(params.get(0).getStringValue(), "to"))
		{
			// The to keyword is followed by one or two position
			// idents (in any order).
			// linear-gradient( to left top, blue, red);
			// linear-gradient( to top right, blue, red);
			for ( ; i < params.size(); i++)
			{
				if (!Idents.looksLikeABGPosition(params.get(i).getStringValue()))
					break;
			}
			
			List<String> positions = Collections.emptyList();
			
			if (i == 2)
			{
				positions = Collections.singletonList(params.get(1).getStringValue().toLowerCase(Locale.US));
			}
			else if (i == 3)
			{
				positions = Arrays.asList(
						params.get(1).getStringValue().toLowerCase(Locale.US),
						params.get(2).getStringValue().toLowerCase(Locale.US));
			}
			
			if (positions.contains("top") && positions.contains("left"))
				angle = 315f;
			else if (positions.contains("top") && positions.contains("right"))
				angle = 45f;
			else if (positions.contains("bottom") && positions.contains("left"))
				angle = 225f;
			else if (positions.contains("bottom") && positions.contains("right"))
				angle = 135f;
			else if (positions.contains("bottom"))
				angle = 180f;
			else
				angle = 0f;
		}
		else if (params.get(0).getPrimitiveType() == CSSPrimitiveValue.CSS_DEG)
		{
			// linear-gradient(45deg, ...)
			angle = params.get(0).getFloatValue();
		}
		else if (params.get(0).getPrimitiveType() == CSSPrimitiveValue.CSS_RAD)
		{
			// linear-gradient(2rad)
			angle = params.get(0).getFloatValue() * (float) (180 / Math.PI);
		}
		else
		{
			angle = 0f;
		}
		
		for (; i < params.size(); i++)
		{
			// Each stop point can have a color and optionally a length.
			PropertyValue value = params.get(i);
			
			if (value.getPrimitiveType() == CSSPrimitiveValue.CSS_IDENT) {
                FSRGBColor color = Conversions.getColor(value.getStringValue());
                if (i + 1 < params.size() &&
                	(BuilderUtil.isLength(params.get(i + 1)) || 
                	params.get(i + 1).getPrimitiveType() == CSSPrimitiveValue.CSS_PERCENTAGE))
                {
                	PropertyValue val2 = params.get(i + 1);
                	stopPoints.add(new StopValue(color, val2.getFloatValue(), val2.getPrimitiveType())); 
                }
                else
                {
                	stopPoints.add(new StopValue(color));
                }
			}
		}

		// Normalize lengths into dots values.
		for (int m = 0; m < stopPoints.size(); m++)
		{
			StopValue pt = stopPoints.get(m);
			if (pt.length != null)
			{
				pt.dotsValue = 
            			LengthValue.calcFloatProportionalValue(style, CSSName.BACKGROUND_IMAGE, "", pt.length, pt.lengthType, width, ctx);
			}
			else if (m == 0)
			{
				// First value is zero.
				pt.dotsValue = 0f;
			}
			else if (m == stopPoints.size() - 1)
			{
				// Last value is 100%.
				pt.dotsValue = 
           			LengthValue.calcFloatProportionalValue(style, CSSName.BACKGROUND_IMAGE, "100%", 100f, CSSPrimitiveValue.CSS_PERCENTAGE, width, ctx);
			}
		}
		
		float lastValue = 0f;
		float nextValue = 100f;
		float increment = 0f;

		// TODO: Confirm below is correct, no divide by zero and 
		// no endless loop.

		// Now normalize those stop points without a length.
		for (int j = 1; j < stopPoints.size(); j++)
		{
			if (j + 1 < stopPoints.size() &&
				stopPoints.get(j).dotsValue == null &&
				increment == 0f)
			{
				int k = j + 1;
				
				for (; k < stopPoints.size(); k++)
				{
					if (stopPoints.get(k).dotsValue != null)
					{
						nextValue = stopPoints.get(k).dotsValue;
						break;
					}
				}

				// k now contains the number of values that we had to skip to find a provided
				// value. We use this to get the increment for unprovided values.
				increment = (nextValue - lastValue) / k;
			}

			if (stopPoints.get(j).dotsValue != null)
			{
				increment = 0;
				lastValue = stopPoints.get(j).dotsValue;
			}
			else
			{
				stopPoints.get(j).dotsValue = lastValue + increment;
				lastValue = stopPoints.get(j).dotsValue;
			}
		}
	}
	
	@Override
	public String toString() 
	{
		return "[" + angle + "](" + stopPoints.toString() + ")";
	}
}

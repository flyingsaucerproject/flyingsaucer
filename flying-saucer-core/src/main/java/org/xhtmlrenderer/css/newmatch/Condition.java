/*
 * Condition.java
 * Copyright (c) 2004, 2005 Torbjoern Gannholm
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 2.1
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.	See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 *
 */
package org.xhtmlrenderer.css.newmatch;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.xhtmlrenderer.css.extend.AttributeResolver;
import org.xhtmlrenderer.css.extend.TreeResolver;
import org.xhtmlrenderer.css.parser.CSSParseException;


/**
 * Part of a Selector
 *
 * @author tstgm
 */
abstract class Condition {

    abstract boolean matches(Object e, AttributeResolver attRes, TreeResolver treeRes);

    /**
     * the CSS condition [attribute]
     *
     * @param name PARAM
     * @return Returns
     */
    static Condition createAttributeExistsCondition(String namespaceURI, String name) {
        return new AttributeExistsCondition(namespaceURI, name);
    }

    /**
     * the CSS condition [attribute^=value]
     */
    static Condition createAttributePrefixCondition(String namespaceURI, String name, String value) {
        return new AttributePrefixCondition(namespaceURI, name, value);
    }
    
    /**
     * the CSS condition [attribute$=value]
     */
    static Condition createAttributeSuffixCondition(String namespaceURI, String name, String value) {
        return new AttributeSuffixCondition(namespaceURI, name, value);
    }
    
    /**
     * the CSS condition [attribute*=value]
     */
    static Condition createAttributeSubstringCondition(String namespaceURI, String name, String value) {
        return new AttributeSubstringCondition(namespaceURI, name, value);
    }
    
    /**
     * the CSS condition [attribute=value]
     */
    static Condition createAttributeEqualsCondition(String namespaceURI, String name, String value) {
        return new AttributeEqualsCondition(namespaceURI, name, value);
    }

    /**
     * the CSS condition [attribute~=value]
     *
     * @param name  PARAM
     * @param value PARAM
     * @return Returns
     */
    static Condition createAttributeMatchesListCondition(String namespaceURI, String name, String value) {
        return new AttributeMatchesListCondition(namespaceURI, name, value);
    }

    /**
     * the CSS condition [attribute|=value]
     *
     * @param name  PARAM
     * @param value PARAM
     * @return Returns
     */
    static Condition createAttributeMatchesFirstPartCondition(String namespaceURI, String name, String value) {
        return new AttributeMatchesFirstPartCondition(namespaceURI, name, value);
    }

    /**
     * the CSS condition .class
     *
     * @param className PARAM
     * @return Returns
     */
    static Condition createClassCondition(String className) {
        return new ClassCondition(className);
    }

    /**
     * the CSS condition #ID
     *
     * @param id PARAM
     * @return Returns
     */
    static Condition createIDCondition(String id) {
        return new IDCondition(id);
    }

    /**
     * the CSS condition lang(Xx)
     *
     * @param lang PARAM
     * @return Returns
     */
    static Condition createLangCondition(String lang) {
        return new LangCondition(lang);
    }

    /**
     * the CSS condition that element has pseudo-class :first-child
     *
     * @return Returns
     */
    static Condition createFirstChildCondition() {
        return new FirstChildCondition();
    }
    
    /**
     * the CSS condition that element has pseudo-class :last-child
     *
     * @return Returns
     */
    static Condition createLastChildCondition() {
        return new LastChildCondition();
    }
    
    /**
     * the CSS condition that element has pseudo-class :nth-child(an+b)
     *
     * @param number PARAM
     * @return Returns
     */
    static Condition createNthChildCondition(String number) {
        return NthChildCondition.fromString(number);
    }

    /**
     * the CSS condition that element has pseudo-class :even
     * 
     * @return Returns
     */
    static Condition createEvenChildCondition() {
        return new EvenChildCondition();
    }
    
    /**
     * the CSS condition that element has pseudo-class :odd
     * 
     * @return Returns
     */
    static Condition createOddChildCondition() {
        return new OddChildCondition();
    }

    /**
     * the CSS condition that element has pseudo-class :link
     *
     * @return Returns
     */
    static Condition createLinkCondition() {
        return new LinkCondition();
    }

    /**
     * for unsupported or invalid CSS
     *
     * @return Returns
     */
    static Condition createUnsupportedCondition() {
        return new UnsupportedCondition();
    }
    
    private static abstract class AttributeCompareCondition extends Condition {
        private String _namespaceURI;
        private String _name;
        private String _value;
        
        protected abstract boolean compare(String attrValue, String conditionValue);

        AttributeCompareCondition(String namespaceURI, String name, String value) {
            _namespaceURI = namespaceURI;
            _name = name;
            _value = value;
        }

        boolean matches(Object e, AttributeResolver attRes, TreeResolver treeRes) {
            if (attRes == null) {
                return false;
            }
            String val = attRes.getAttributeValue(e, _namespaceURI, _name);
            if (val == null) {
                return false;
            }
            
            return compare(val, _value);
        }
    }

    private static class AttributeExistsCondition extends AttributeCompareCondition {
        AttributeExistsCondition(String namespaceURI, String name) {
            super(namespaceURI, name, null);
        }
        
        protected boolean compare(String attrValue, String conditionValue) {
            return ! attrValue.equals("");
        }
    }
    
    private static class AttributeEqualsCondition extends AttributeCompareCondition {
        AttributeEqualsCondition(String namespaceURI, String name, String value) {
            super(namespaceURI, name, value);
        }

        protected boolean compare(String attrValue, String conditionValue) {
            return attrValue.equals(conditionValue);
        }
    }
    
    private static class AttributePrefixCondition extends AttributeCompareCondition {
        AttributePrefixCondition(String namespaceURI, String name, String value) {
            super(namespaceURI, name, value);
        }

        protected boolean compare(String attrValue, String conditionValue) {
            return attrValue.startsWith(conditionValue);
        }
    }
    
    private static class AttributeSuffixCondition extends AttributeCompareCondition {
        AttributeSuffixCondition(String namespaceURI, String name, String value) {
            super(namespaceURI, name, value);
        }

        protected boolean compare(String attrValue, String conditionValue) {
            return attrValue.endsWith(conditionValue);
        }
    }
    
    private static class AttributeSubstringCondition extends AttributeCompareCondition {
        AttributeSubstringCondition(String namespaceURI, String name, String value) {
            super(namespaceURI, name, value);
        }

        protected boolean compare(String attrValue, String conditionValue) {
            return attrValue.indexOf(conditionValue) > -1;
        }
    }
    
    private static class AttributeMatchesListCondition extends AttributeCompareCondition {
        AttributeMatchesListCondition(String namespaceURI, String name, String value) {
            super(namespaceURI, name, value);
        }
        
        protected boolean compare(String attrValue, String conditionValue) {
            String[] ca = split(attrValue, ' ');
            boolean matched = false;
            for (int j = 0; j < ca.length; j++) {
                if (conditionValue.equals(ca[j])) {
                    matched = true;
                }
            }
            return matched;
        }
    }

    private static class AttributeMatchesFirstPartCondition extends AttributeCompareCondition {
        AttributeMatchesFirstPartCondition(String namespaceURI, String name, String value) {
            super(namespaceURI, name, value);
        }
        
        protected boolean compare(String attrValue, String conditionValue) {
            String[] ca = split(attrValue, '-');
            if (conditionValue.equals(ca[0])) {
                return true;
            }
            return false;
        }
    }

    private static class ClassCondition extends Condition {

        private String _paddedClassName;

        ClassCondition(String className) {
            _paddedClassName = " " + className + " ";
        }

        boolean matches(Object e, AttributeResolver attRes, TreeResolver treeRes) {
            if (attRes == null) {
                return false;
            }
            String c = attRes.getClass(e);
            if (c == null) {
                return false;
            }

            // This is much faster than calling `split()` and comparing individual values in a loop.
            // NOTE: In jQuery, for example, the attribute value first has whitespace normalized to spaces. But
            // in an XML DOM, space normalization in attributes is supposed to have happened already.
            return (" " + c + " ").indexOf(_paddedClassName) != -1;
        }

    }

    private static class IDCondition extends Condition {

        private String _id;

        IDCondition(String id) {
            _id = id;
        }

        boolean matches(Object e, AttributeResolver attRes, TreeResolver treeRes) {
            if (attRes == null) {
                return false;
            }
            if (!_id.equals(attRes.getID(e))) {
                return false;
            }
            return true;
        }

    }

    private static class LangCondition extends Condition {
        private String _lang;

        LangCondition(String lang) {
            _lang = lang;
        }

        boolean matches(Object e, AttributeResolver attRes, TreeResolver treeRes) {
            if (attRes == null) {
                return false;
            }
            String lang = attRes.getLang(e);
            if (lang == null) {
                return false;
            }
            if(_lang.equalsIgnoreCase(lang)) {
                return true;
            }
            String[] ca = split(lang, '-');
            if (_lang.equalsIgnoreCase(ca[0])) {
                return true;
            }
            return false;
        }

    }

    private static class FirstChildCondition extends Condition {

        FirstChildCondition() {
        }

        boolean matches(Object e, AttributeResolver attRes, TreeResolver treeRes) {
            return treeRes.isFirstChildElement(e);
        }

    }
    
    private static class LastChildCondition extends Condition {

        LastChildCondition() {
        }

        boolean matches(Object e, AttributeResolver attRes, TreeResolver treeRes) {
            return treeRes.isLastChildElement(e);
        }

    }

    private static class NthChildCondition extends Condition {

        private static final Pattern pattern = Pattern.compile("([-+]?)(\\d*)n(\\s*([-+])\\s*(\\d+))?");

        private final int a;
        private final int b;

        NthChildCondition(int a, int b) {
            this.a = a;
            this.b = b;
        }

        boolean matches(Object e, AttributeResolver attRes, TreeResolver treeRes) {
            // getPositionOfElement() starts at 0, CSS spec starts at 1
            int position = treeRes.getPositionOfElement(e)+1;


            //<An+B> from https://developer.mozilla.org/en-US/docs/Web/CSS/:nth-child
            //Represents elements whose numeric position in a series of siblings matches the pattern An+B, 
            //for every positive integer or zero value of n. The index of the first element is 1. 
            //The values A and B must both be <integer>s.             
            
            // an+b generates a sequence b, a+b, 2a+b, 3a+b, 4a+b
            // e.g. if 
            //a=2 b=3, it generates the sequence: 3, 5, 7, 9, 11... for values of n=0,1,2,3,4...
            //a=2 b=0, the sequence is 0 (which is moot), 2, 4, 6... - i.e. even
            //a=2 b=1, gives 1, 3, 5, 7... - i.e. even
            //a=1 b=2, gives 2, 3, 4, 5, 6... - i.e. not first
            //a=1 b=3, gives 3, 4, 5, 6, 7... 
            //a=-1 b=5, gives 5, 4, 3, 2, 1. So only matches the first 5 - it won't reverse the order of the elements!
            //a=-2 b=5, gives 5, 3, 1. So only matches the odd 3 of the first 5 
            //a=0 b=1, gives 1, just the first element
            //a=0 b=7, gives 7. Just the seventh element
            
//            p = ( a * n ) + b  - is n zero, or a positive integer?
//            p-b = ( a * n )
//            (p-b)/a = n 
                
            //Clearly n==0 iff p==b, for any value of a
            if ( position == b )
                return true;
            
            //And if a==0 then a x n is 0 for all n, and if we didn't match position==b above then n cannot be valid (0 or +ve integer).
            if ( a == 0 )
                return false;
            
            //return true if n is an integer and 0 or +ve
            if  (    ( ( ( position - b ) % a ) == 0 )   // n is an integer
                  && ( ( ( position - b ) / a ) >= 0 ) ) // n is 0 or +ve 
                return true;
                        
            return false;
            
//            
//            position -= b;
//
//            if (a == 0) {
//                return position == 0;
//            } else if ((a < 0) && (position > 0)) {
//                return false; // n is negative
//            } else {
//                return position % a == 0;
//            }
        }

        static NthChildCondition fromString(String number) {
            number = number.trim().toLowerCase();

            if ("even".equals(number)) {
                return new NthChildCondition(2, 0);
            } else if ("odd".equals(number)) {
                return new NthChildCondition(2, 1);
            } else {
                try {
                    return new NthChildCondition(0, Integer.parseInt(number));
                } catch (NumberFormatException e) {
                    Matcher m = pattern.matcher(number);

                    if (!m.matches()) {
                        throw new CSSParseException("Invalid nth-child selector: " + number, -1);
                    } else {
                        int a = m.group(2).equals("") ? 1 : Integer.parseInt(m.group(2));
                        int b = (m.group(5) == null) ? 0 : Integer.parseInt(m.group(5));
                        if ("-".equals(m.group(1))) {
                            a *= -1;
                        }
                        if ("-".equals(m.group(4))) {
                            b *= -1;
                        }

                        return new NthChildCondition(a, b);
                    }
                }
            }
        }
    }

    private static class EvenChildCondition extends Condition {

        EvenChildCondition() {
        }

        boolean matches(Object e, AttributeResolver attRes, TreeResolver treeRes) {
            int position = treeRes.getPositionOfElement(e);
            return position >= 0 && position % 2 == 0;
        }
    }
    
    private static class OddChildCondition extends Condition {

        OddChildCondition() {
        }

        boolean matches(Object e, AttributeResolver attRes, TreeResolver treeRes) {
            int position = treeRes.getPositionOfElement(e);
            return position >= 0 && position % 2 == 1;
        }
    }

    private static class LinkCondition extends Condition {

        LinkCondition() {
        }

        boolean matches(Object e, AttributeResolver attRes, TreeResolver treeRes) {
            return attRes.isLink(e);
        }

    }

    /**
     * represents unsupported (or invalid) css, never matches
     */
    private static class UnsupportedCondition extends Condition {

        UnsupportedCondition() {
        }

        boolean matches(Object e, AttributeResolver attRes, TreeResolver treeRes) {
            return false;
        }

    }
    
    private static String[] split(String s, char ch) {
        if (s.indexOf(ch) == -1) {
            return new String[] { s };
        } else {
            List result = new ArrayList();
            
            int last = 0;
            int next = 0;
            
            while ((next = s.indexOf(ch, last)) != -1) {
                if (next != last) {
                    result.add(s.substring(last, next));
                }
                last = next + 1;
            }
            
            if (last != s.length()) {
                result.add(s.substring(last));
            }
            
            return (String[])result.toArray(new String[result.size()]);
        }
    }    
}


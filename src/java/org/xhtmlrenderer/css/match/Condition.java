/*
 *
 * Condition.java
 * Copyright (c) 2004 Torbjörn Gannholm
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

package org.xhtmlrenderer.css.match;

/**
 * Part of a Selector
 * @author  tstgm
 */
abstract class Condition {
    
    private static class AttributeExistsCondition extends Condition {
        
        AttributeExistsCondition(String name) {
            _name = name;
        }
        
        boolean matches(org.w3c.dom.Element e, AttributeResolver attRes) {
                if(attRes == null) return false;
                if(attRes.getAttributeValue(e, _name) != null) return true;
                return false;
        }
        
        private String _name;
        
    }
    
    private static class AttributeEqualsCondition extends Condition {
        
        AttributeEqualsCondition(String name, String value) {
            _name = name;
            _value = value;
        }
        
        boolean matches(org.w3c.dom.Element e, AttributeResolver attRes) {
                if(attRes == null) return false;
                String val = attRes.getAttributeValue(e, _name);
                if(val == null) return false;
                if(val.equals(_value)) return true;
                return false;
        }
        
        private String _name;
        private String _value;
    }
    
    private static class AttributeMatchesListCondition extends Condition {
        
        AttributeMatchesListCondition(String name, String value) {
            _name = name;
            _value = value;
        }
        
        boolean matches(org.w3c.dom.Element e, AttributeResolver attRes) {
                if(attRes == null) return false;
                String val = attRes.getAttributeValue(e, _name);
                if(val == null) return false;
                String[] ca = val.split(" ");
                boolean matched=false;
                for(int j=0; j < ca.length; j++) {
                    if(_value.equals(ca[j])) matched=true;
                }
                return matched;
        }
        
        private String _name;
        private String _value;
    }
    
    private static class AttributeMatchesFirstPartCondition extends Condition {
        
        AttributeMatchesFirstPartCondition(String name, String value) {
            _name = name;
            _value = value;
        }
        
        boolean matches(org.w3c.dom.Element e, AttributeResolver attRes) {
                if(attRes == null) return false;
                String val = attRes.getAttributeValue(e, _name);
                if(val == null) return false;
                String[] ca = val.split("-");
                if(_value.equals(ca[0])) return true;
                return false;
        }
        
        private String _name;
        private String _value;
    }
    
    private static class ClassCondition extends Condition {
        
        ClassCondition(String className) {
            _className = className;
        }
        
        boolean matches(org.w3c.dom.Element e, AttributeResolver attRes) {
                if(attRes == null) return false;
                String c = attRes.getClass(e);
                if(c == null) return false;
                String[] ca = c.split(" ");
                boolean matched=false;
                for(int j=0; j < ca.length; j++) {
                    if(_className.equals(ca[j])) matched=true;
                }
                return matched;
        }
        
        private String _className;
        
    }
    
    private static class IDCondition extends Condition {
        
        IDCondition(String id) {
            _id = id;
        }
        
        boolean matches(org.w3c.dom.Element e, AttributeResolver attRes) {
                if(attRes == null) return false;
                if(!_id.equals(attRes.getID(e))) return false;
                return true;
        }
        
        private String _id;
        
    }
    
    private static class LangCondition extends Condition {
        
        LangCondition(String lang) {
            _lang = lang;
        }
        
        boolean matches(org.w3c.dom.Element e, AttributeResolver attRes) {
                if(attRes == null) return false;
                String lang = attRes.getLang(e);
                if(lang == null) return false;
                String[] ca = lang.split("-");
                if(_lang.equals(ca[0])) return true;
                return false;
        }
        
        private String _lang;
        
    }
    
    private static class FirstChildCondition extends Condition {
        
        FirstChildCondition() {
        }
        
        boolean matches(org.w3c.dom.Element e, AttributeResolver attRes) {
                org.w3c.dom.Node parent = e.getParentNode();
                org.w3c.dom.NodeList nl = parent.getChildNodes();
                int i = 0;
                while(i<nl.getLength() && nl.item(i).getNodeType() != org.w3c.dom.Node.ELEMENT_NODE) i++;
                return(nl.item(i) == e);
        }
        
    }
    
    abstract boolean matches(org.w3c.dom.Element e, AttributeResolver attRes);
    
    /** the CSS condition [attribute] */
    static Condition createAttributeExistsCondition(String name) {
        return new AttributeExistsCondition(name);
    }
    
   /** the CSS condition [attribute=value] */
   static Condition createAttributeEqualsCondition(String name, String value) {
        return new AttributeEqualsCondition(name, value);
    }
    
    /** the CSS condition [attribute~=value] */
    static Condition createAttributeMatchesListCondition(String name, String value) {
        return new AttributeMatchesListCondition(name, value);
    }
    
    /** the CSS condition [attribute|=value] */
    static Condition createAttributeMatchesFirstPartCondition(String name, String value) {
        return new AttributeMatchesFirstPartCondition(name, value);
    }
    
    /** the CSS condition .class */
    static Condition createClassCondition(String className) {
        return new ClassCondition(className);
    }
    
    /** the CSS condition #ID */
    static Condition createIDCondition(String id) {
        return new IDCondition(id);
    }
    
    /** the CSS condition lang(x) */
    static Condition createLangCondition(String lang) {
        return new LangCondition(lang);
    }
    
    /** the CSS condition that element has pseudo-class :first-child */
    static Condition createFirstChildCondition() {
        return new FirstChildCondition();
    }
    
}

/*
 * Condition.java
 * Copyright (c) 2004, 2005 Torbjörn Gannholm
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

import org.xhtmlrenderer.css.extend.AttributeResolver;
import org.xhtmlrenderer.css.extend.TreeResolver;


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

        private String _className;

        ClassCondition(String className) {
            _className = className;
        }

        boolean matches(Object e, AttributeResolver attRes, TreeResolver treeRes) {
            if (attRes == null) {
                return false;
            }
            String c = attRes.getClass(e);
            if (c == null) {
                return false;
            }
            String[] ca = split(c, ' ');
            boolean matched = false;
            for (int j = 0; j < ca.length; j++) {
                if (_className.equals(ca[j])) {
                    matched = true;
                }
            }
            return matched;
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
            String[] ca = split(lang, '-');
            if (_lang.equals(ca[0])) {
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


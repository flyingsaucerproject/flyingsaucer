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
    static Condition createAttributeExistsCondition(String name) {
        return new AttributeExistsCondition(name);
    }

    /**
     * the CSS condition [attribute=value]
     *
     * @param name  PARAM
     * @param value PARAM
     * @return Returns
     */
    static Condition createAttributeEqualsCondition(String name, String value) {
        return new AttributeEqualsCondition(name, value);
    }

    /**
     * the CSS condition [attribute~=value]
     *
     * @param name  PARAM
     * @param value PARAM
     * @return Returns
     */
    static Condition createAttributeMatchesListCondition(String name, String value) {
        return new AttributeMatchesListCondition(name, value);
    }

    /**
     * the CSS condition [attribute|=value]
     *
     * @param name  PARAM
     * @param value PARAM
     * @return Returns
     */
    static Condition createAttributeMatchesFirstPartCondition(String name, String value) {
        return new AttributeMatchesFirstPartCondition(name, value);
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

    private static class AttributeExistsCondition extends Condition {

        private String _name;

        AttributeExistsCondition(String name) {
            _name = name;
        }

        boolean matches(Object e, AttributeResolver attRes, TreeResolver treeRes) {
            if (attRes == null) {
                return false;
            }
            if (! attRes.getAttributeValue(e, _name).equals("")) {
                return true;
            }
            return false;
        }

    }

    private static class AttributeEqualsCondition extends Condition {

        private String _name;
        private String _value;

        AttributeEqualsCondition(String name, String value) {
            _name = name;
            _value = value;
        }

        boolean matches(Object e, AttributeResolver attRes, TreeResolver treeRes) {
            if (attRes == null) {
                return false;
            }
            String val = attRes.getAttributeValue(e, _name);
            if (val == null) {
                return false;
            }
            if (val.equals(_value)) {
                return true;
            }
            return false;
        }
    }

    private static class AttributeMatchesListCondition extends Condition {

        private String _name;
        private String _value;

        AttributeMatchesListCondition(String name, String value) {
            _name = name;
            _value = value;
        }

        boolean matches(Object e, AttributeResolver attRes, TreeResolver treeRes) {
            if (attRes == null) {
                return false;
            }
            String val = attRes.getAttributeValue(e, _name);
            if (val == null) {
                return false;
            }
            String[] ca = split(val, ' ');
            boolean matched = false;
            for (int j = 0; j < ca.length; j++) {
                if (_value.equals(ca[j])) {
                    matched = true;
                }
            }
            return matched;
        }
    }

    private static class AttributeMatchesFirstPartCondition extends Condition {

        private String _name;
        private String _value;

        AttributeMatchesFirstPartCondition(String name, String value) {
            _name = name;
            _value = value;
        }

        boolean matches(Object e, AttributeResolver attRes, TreeResolver treeRes) {
            if (attRes == null) {
                return false;
            }
            String val = attRes.getAttributeValue(e, _name);
            if (val == null) {
                return false;
            }
            String[] ca = split(val, '-');
            if (_value.equals(ca[0])) {
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


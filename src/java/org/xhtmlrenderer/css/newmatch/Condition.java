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

import org.xhtmlrenderer.css.extend.AttributeResolver;
import org.xhtmlrenderer.css.extend.TreeResolver;


/**
 * Part of a Selector
 *
 * @author tstgm
 */
abstract class Condition {
    /**
     * Description of the Method
     *
     * @param e       PARAM
     * @param attRes  PARAM
     * @param treeRes
     * @return Returns
     */
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

    /**
     * Description of the Class
     */
    private static class AttributeExistsCondition extends Condition {

        /**
         * Description of the Field
         */
        private String _name;

        /**
         * Constructor for the AttributeExistsCondition object
         *
         * @param name PARAM
         */
        AttributeExistsCondition(String name) {
            _name = name;
        }

        /**
         * Description of the Method
         *
         * @param e       PARAM
         * @param attRes  PARAM
         * @param treeRes
         * @return Returns
         */
        boolean matches(Object e, AttributeResolver attRes, TreeResolver treeRes) {
            if (attRes == null) {
                return false;
            }
            if (attRes.getAttributeValue(e, _name) != null) {
                return true;
            }
            return false;
        }

    }

    /**
     * Description of the Class
     */
    private static class AttributeEqualsCondition extends Condition {

        /**
         * Description of the Field
         */
        private String _name;
        /**
         * Description of the Field
         */
        private String _value;

        /**
         * Constructor for the AttributeEqualsCondition object
         *
         * @param name  PARAM
         * @param value PARAM
         */
        AttributeEqualsCondition(String name, String value) {
            _name = name;
            _value = value;
        }

        /**
         * Description of the Method
         *
         * @param e       PARAM
         * @param attRes  PARAM
         * @param treeRes
         * @return Returns
         */
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

    /**
     * Description of the Class
     */
    private static class AttributeMatchesListCondition extends Condition {

        /**
         * Description of the Field
         */
        private String _name;
        /**
         * Description of the Field
         */
        private String _value;

        /**
         * Constructor for the AttributeMatchesListCondition object
         *
         * @param name  PARAM
         * @param value PARAM
         */
        AttributeMatchesListCondition(String name, String value) {
            _name = name;
            _value = value;
        }

        /**
         * Description of the Method
         *
         * @param e       PARAM
         * @param attRes  PARAM
         * @param treeRes
         * @return Returns
         */
        boolean matches(Object e, AttributeResolver attRes, TreeResolver treeRes) {
            if (attRes == null) {
                return false;
            }
            String val = attRes.getAttributeValue(e, _name);
            if (val == null) {
                return false;
            }
            String[] ca = val.split(" ");
            boolean matched = false;
            for (int j = 0; j < ca.length; j++) {
                if (_value.equals(ca[j])) {
                    matched = true;
                }
            }
            return matched;
        }
    }

    /**
     * Description of the Class
     */
    private static class AttributeMatchesFirstPartCondition extends Condition {

        /**
         * Description of the Field
         */
        private String _name;
        /**
         * Description of the Field
         */
        private String _value;

        /**
         * Constructor for the AttributeMatchesFirstPartCondition object
         *
         * @param name  PARAM
         * @param value PARAM
         */
        AttributeMatchesFirstPartCondition(String name, String value) {
            _name = name;
            _value = value;
        }

        /**
         * Description of the Method
         *
         * @param e       PARAM
         * @param attRes  PARAM
         * @param treeRes
         * @return Returns
         */
        boolean matches(Object e, AttributeResolver attRes, TreeResolver treeRes) {
            if (attRes == null) {
                return false;
            }
            String val = attRes.getAttributeValue(e, _name);
            if (val == null) {
                return false;
            }
            String[] ca = val.split("-");
            if (_value.equals(ca[0])) {
                return true;
            }
            return false;
        }
    }

    /**
     * Description of the Class
     */
    private static class ClassCondition extends Condition {

        /**
         * Description of the Field
         */
        private String _className;

        /**
         * Constructor for the ClassCondition object
         *
         * @param className PARAM
         */
        ClassCondition(String className) {
            _className = className;
        }

        /**
         * Description of the Method
         *
         * @param e       PARAM
         * @param attRes  PARAM
         * @param treeRes
         * @return Returns
         */
        boolean matches(Object e, AttributeResolver attRes, TreeResolver treeRes) {
            if (attRes == null) {
                return false;
            }
            String c = attRes.getClass(e);
            if (c == null) {
                return false;
            }
            String[] ca = c.split(" ");
            boolean matched = false;
            for (int j = 0; j < ca.length; j++) {
                if (_className.equals(ca[j])) {
                    matched = true;
                }
            }
            return matched;
        }

    }

    /**
     * Description of the Class
     */
    private static class IDCondition extends Condition {

        /**
         * Description of the Field
         */
        private String _id;

        /**
         * Constructor for the IDCondition object
         *
         * @param id PARAM
         */
        IDCondition(String id) {
            _id = id;
        }

        /**
         * Description of the Method
         *
         * @param e       PARAM
         * @param attRes  PARAM
         * @param treeRes
         * @return Returns
         */
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

    /**
     * Description of the Class
     */
    private static class LangCondition extends Condition {

        /**
         * Description of the Field
         */
        private String _lang;

        /**
         * Constructor for the LangCondition object
         *
         * @param lang PARAM
         */
        LangCondition(String lang) {
            _lang = lang;
        }

        /**
         * Description of the Method
         *
         * @param e       PARAM
         * @param attRes  PARAM
         * @param treeRes
         * @return Returns
         */
        boolean matches(Object e, AttributeResolver attRes, TreeResolver treeRes) {
            if (attRes == null) {
                return false;
            }
            String lang = attRes.getLang(e);
            if (lang == null) {
                return false;
            }
            String[] ca = lang.split("-");
            if (_lang.equals(ca[0])) {
                return true;
            }
            return false;
        }

    }

    /**
     * Description of the Class
     */
    private static class FirstChildCondition extends Condition {

        /**
         * Constructor for the FirstChildCondition object
         */
        FirstChildCondition() {
        }

        /**
         * Description of the Method
         *
         * @param e       PARAM
         * @param attRes  PARAM
         * @param treeRes
         * @return Returns
         */
        boolean matches(Object e, AttributeResolver attRes, TreeResolver treeRes) {
            return treeRes.isFirstChildElement(e);
        }

    }

    /**
     * Description of the Class
     */
    private static class LinkCondition extends Condition {

        /**
         * Constructor for the LinkCondition object
         */
        LinkCondition() {
        }

        /**
         * Description of the Method
         *
         * @param e       PARAM
         * @param attRes  PARAM
         * @param treeRes
         * @return Returns
         */
        boolean matches(Object e, AttributeResolver attRes, TreeResolver treeRes) {
            return attRes.isLink(e);
        }

    }

    /**
     * represents unsupported (or invalid) css, never matches
     */
    private static class UnsupportedCondition extends Condition {

        /**
         * Constructor for the UnsupportedCondition object
         */
        UnsupportedCondition() {
        }

        /**
         * Description of the Method
         *
         * @param e       PARAM
         * @param attRes  PARAM
         * @param treeRes
         * @return Returns
         */
        boolean matches(Object e, AttributeResolver attRes, TreeResolver treeRes) {
            return false;
        }

    }

}


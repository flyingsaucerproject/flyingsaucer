/*
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
 *
 * @author   tstgm
 */
abstract class Condition {
    /**
     * Description of the Method
     *
     * @param e       PARAM
     * @param attRes  PARAM
     * @return        Returns
     */
    abstract boolean matches( org.w3c.dom.Element e, AttributeResolver attRes );

    /**
     * the CSS condition [attribute]
     *
     * @param name  PARAM
     * @return      Returns
     */
    static Condition createAttributeExistsCondition( String name ) {
        return new AttributeExistsCondition( name );
    }

    /**
     * the CSS condition [attribute=value]
     *
     * @param name   PARAM
     * @param value  PARAM
     * @return       Returns
     */
    static Condition createAttributeEqualsCondition( String name, String value ) {
        return new AttributeEqualsCondition( name, value );
    }

    /**
     * the CSS condition [attribute~=value]
     *
     * @param name   PARAM
     * @param value  PARAM
     * @return       Returns
     */
    static Condition createAttributeMatchesListCondition( String name, String value ) {
        return new AttributeMatchesListCondition( name, value );
    }

    /**
     * the CSS condition [attribute|=value]
     *
     * @param name   PARAM
     * @param value  PARAM
     * @return       Returns
     */
    static Condition createAttributeMatchesFirstPartCondition( String name, String value ) {
        return new AttributeMatchesFirstPartCondition( name, value );
    }

    /**
     * the CSS condition .class
     *
     * @param className  PARAM
     * @return           Returns
     */
    static Condition createClassCondition( String className ) {
        return new ClassCondition( className );
    }

    /**
     * the CSS condition #ID
     *
     * @param id  PARAM
     * @return    Returns
     */
    static Condition createIDCondition( String id ) {
        return new IDCondition( id );
    }

    /**
     * the CSS condition lang(x)
     *
     * @param lang  PARAM
     * @return      Returns
     */
    static Condition createLangCondition( String lang ) {
        return new LangCondition( lang );
    }

    /**
     * the CSS condition that element has pseudo-class :first-child
     *
     * @return   Returns
     */
    static Condition createFirstChildCondition() {
        return new FirstChildCondition();
    }

    /**
     * Description of the Class
     *
     * @author   empty
     */
    private static class AttributeExistsCondition extends Condition {

        /** Description of the Field */
        private String _name;

        /**
         * Constructor for the AttributeExistsCondition object
         *
         * @param name  PARAM
         */
        AttributeExistsCondition( String name ) {
            _name = name;
        }

        /**
         * Description of the Method
         *
         * @param e       PARAM
         * @param attRes  PARAM
         * @return        Returns
         */
        boolean matches( org.w3c.dom.Element e, AttributeResolver attRes ) {
            if ( attRes == null ) {
                return false;
            }
            if ( attRes.getAttributeValue( e, _name ) != null ) {
                return true;
            }
            return false;
        }

    }

    /**
     * Description of the Class
     *
     * @author   empty
     */
    private static class AttributeEqualsCondition extends Condition {

        /** Description of the Field */
        private String _name;
        /** Description of the Field */
        private String _value;

        /**
         * Constructor for the AttributeEqualsCondition object
         *
         * @param name   PARAM
         * @param value  PARAM
         */
        AttributeEqualsCondition( String name, String value ) {
            _name = name;
            _value = value;
        }

        /**
         * Description of the Method
         *
         * @param e       PARAM
         * @param attRes  PARAM
         * @return        Returns
         */
        boolean matches( org.w3c.dom.Element e, AttributeResolver attRes ) {
            if ( attRes == null ) {
                return false;
            }
            String val = attRes.getAttributeValue( e, _name );
            if ( val == null ) {
                return false;
            }
            if ( val.equals( _value ) ) {
                return true;
            }
            return false;
        }
    }

    /**
     * Description of the Class
     *
     * @author   empty
     */
    private static class AttributeMatchesListCondition extends Condition {

        /** Description of the Field */
        private String _name;
        /** Description of the Field */
        private String _value;

        /**
         * Constructor for the AttributeMatchesListCondition object
         *
         * @param name   PARAM
         * @param value  PARAM
         */
        AttributeMatchesListCondition( String name, String value ) {
            _name = name;
            _value = value;
        }

        /**
         * Description of the Method
         *
         * @param e       PARAM
         * @param attRes  PARAM
         * @return        Returns
         */
        boolean matches( org.w3c.dom.Element e, AttributeResolver attRes ) {
            if ( attRes == null ) {
                return false;
            }
            String val = attRes.getAttributeValue( e, _name );
            if ( val == null ) {
                return false;
            }
            String[] ca = val.split( " " );
            boolean matched = false;
            for ( int j = 0; j < ca.length; j++ ) {
                if ( _value.equals( ca[j] ) ) {
                    matched = true;
                }
            }
            return matched;
        }
    }

    /**
     * Description of the Class
     *
     * @author   empty
     */
    private static class AttributeMatchesFirstPartCondition extends Condition {

        /** Description of the Field */
        private String _name;
        /** Description of the Field */
        private String _value;

        /**
         * Constructor for the AttributeMatchesFirstPartCondition object
         *
         * @param name   PARAM
         * @param value  PARAM
         */
        AttributeMatchesFirstPartCondition( String name, String value ) {
            _name = name;
            _value = value;
        }

        /**
         * Description of the Method
         *
         * @param e       PARAM
         * @param attRes  PARAM
         * @return        Returns
         */
        boolean matches( org.w3c.dom.Element e, AttributeResolver attRes ) {
            if ( attRes == null ) {
                return false;
            }
            String val = attRes.getAttributeValue( e, _name );
            if ( val == null ) {
                return false;
            }
            String[] ca = val.split( "-" );
            if ( _value.equals( ca[0] ) ) {
                return true;
            }
            return false;
        }
    }

    /**
     * Description of the Class
     *
     * @author   empty
     */
    private static class ClassCondition extends Condition {

        /** Description of the Field */
        private String _className;

        /**
         * Constructor for the ClassCondition object
         *
         * @param className  PARAM
         */
        ClassCondition( String className ) {
            _className = className;
        }

        /**
         * Description of the Method
         *
         * @param e       PARAM
         * @param attRes  PARAM
         * @return        Returns
         */
        boolean matches( org.w3c.dom.Element e, AttributeResolver attRes ) {
            if ( attRes == null ) {
                return false;
            }
            String c = attRes.getClass( e );
            if ( c == null ) {
                return false;
            }
            String[] ca = c.split( " " );
            boolean matched = false;
            for ( int j = 0; j < ca.length; j++ ) {
                if ( _className.equals( ca[j] ) ) {
                    matched = true;
                }
            }
            return matched;
        }

    }

    /**
     * Description of the Class
     *
     * @author   empty
     */
    private static class IDCondition extends Condition {

        /** Description of the Field */
        private String _id;

        /**
         * Constructor for the IDCondition object
         *
         * @param id  PARAM
         */
        IDCondition( String id ) {
            _id = id;
        }

        /**
         * Description of the Method
         *
         * @param e       PARAM
         * @param attRes  PARAM
         * @return        Returns
         */
        boolean matches( org.w3c.dom.Element e, AttributeResolver attRes ) {
            if ( attRes == null ) {
                return false;
            }
            if ( !_id.equals( attRes.getID( e ) ) ) {
                return false;
            }
            return true;
        }

    }

    /**
     * Description of the Class
     *
     * @author   empty
     */
    private static class LangCondition extends Condition {

        /** Description of the Field */
        private String _lang;

        /**
         * Constructor for the LangCondition object
         *
         * @param lang  PARAM
         */
        LangCondition( String lang ) {
            _lang = lang;
        }

        /**
         * Description of the Method
         *
         * @param e       PARAM
         * @param attRes  PARAM
         * @return        Returns
         */
        boolean matches( org.w3c.dom.Element e, AttributeResolver attRes ) {
            if ( attRes == null ) {
                return false;
            }
            String lang = attRes.getLang( e );
            if ( lang == null ) {
                return false;
            }
            String[] ca = lang.split( "-" );
            if ( _lang.equals( ca[0] ) ) {
                return true;
            }
            return false;
        }

    }

    /**
     * Description of the Class
     *
     * @author   empty
     */
    private static class FirstChildCondition extends Condition {

        /** Constructor for the FirstChildCondition object */
        FirstChildCondition() { }

        /**
         * Description of the Method
         *
         * @param e       PARAM
         * @param attRes  PARAM
         * @return        Returns
         */
        boolean matches( org.w3c.dom.Element e, AttributeResolver attRes ) {
            org.w3c.dom.Node parent = e.getParentNode();
            org.w3c.dom.NodeList nl = parent.getChildNodes();
            int i = 0;
            while ( i < nl.getLength() && nl.item( i ).getNodeType() != org.w3c.dom.Node.ELEMENT_NODE ) {
                i++;
            }
            return ( nl.item( i ) == e );
        }

    }

}

/*
 * $Id$
 *
 * $Log$
 * Revision 1.2  2004/10/23 13:29:06  pdoubleya
 * Re-formatted using JavaStyle tool.
 * Cleaned imports to resolve wildcards except for common packages (java.io, java.util, etc).
 * Added CVS log comments at bottom.
 *
 *
 */


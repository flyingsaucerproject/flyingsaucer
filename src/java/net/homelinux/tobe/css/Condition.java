/*
 * Condition.java
 *
 * Created on den 28 juli 2004, 15:37
 */

package net.homelinux.tobe.css;

/**
 * Part of a Selector
 * @author  tstgm
 */
abstract class Condition {
    
    private static class AttributeExistsCondition extends Condition {
        
        AttributeExistsCondition(String name) {
            _name = name;
        }
        
        boolean matches(org.w3c.dom.Element e, net.homelinux.tobe.css.AttributeResolver attRes) {
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
        
        boolean matches(org.w3c.dom.Element e, net.homelinux.tobe.css.AttributeResolver attRes) {
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
        
        boolean matches(org.w3c.dom.Element e, net.homelinux.tobe.css.AttributeResolver attRes) {
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
        
        boolean matches(org.w3c.dom.Element e, net.homelinux.tobe.css.AttributeResolver attRes) {
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
        
        boolean matches(org.w3c.dom.Element e, net.homelinux.tobe.css.AttributeResolver attRes) {
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
        
        boolean matches(org.w3c.dom.Element e, net.homelinux.tobe.css.AttributeResolver attRes) {
                if(attRes == null) return false;
                if(!_id.equals(attRes.getID(e))) return false;
                return true;
        }
        
        private String _id;
        
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
    
}

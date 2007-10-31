/*
 * {{{ header & license
 * Copyright (c) 2007 Wisconsin Court System
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 2.1
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 * }}}
 */
package org.xhtmlrenderer.css.parser;

public class Token {
    public static final int S = 1;
    public static final int CDO = 2;
    public static final int CDC = 3;
    public static final int INCLUDES = 4;
    public static final int DASHMATCH = 5;
    public static final int PREFIXMATCH = 6;
    public static final int SUFFIXMATCH = 7;
    public static final int SUBSTRINGMATCH = 8;
    public static final int LBRACE = 9;
    public static final int PLUS = 10;
    public static final int GREATER = 11;
    public static final int COMMA = 12;
    public static final int STRING = 13;
    public static final int INVALID = 14;
    public static final int IDENT = 15;
    public static final int HASH = 16;
    public static final int IMPORT_SYM = 17;
    public static final int PAGE_SYM = 18;
    public static final int MEDIA_SYM = 19;
    public static final int CHARSET_SYM = 20;
    public static final int NAMESPACE_SYM = 21;
    public static final int FONT_FACE_SYM = 22;
    public static final int AT_RULE = 23;
    public static final int IMPORTANT_SYM = 24;
    public static final int EMS = 25;
    public static final int EXS = 26;
    public static final int PX = 27;
    public static final int CM = 28;
    public static final int MM = 29;
    public static final int IN = 30;
    public static final int PT = 31;
    public static final int PC = 32;
    public static final int ANGLE = 33;
    public static final int TIME = 34;
    public static final int FREQ = 35;
    public static final int DIMENSION = 36;
    public static final int PERCENTAGE = 37;
    public static final int NUMBER = 38;
    public static final int URI = 39;
    public static final int FUNCTION = 40;
    public static final int OTHER = 41;
    public static final int RBRACE = 42;
    public static final int SEMICOLON = 43;
    public static final int VIRGULE = 44;
    public static final int COLON = 45;
    public static final int MINUS = 46;
    public static final int RPAREN = 47;
    public static final int LBRACKET = 48;
    public static final int RBRACKET = 49;
    public static final int PERIOD = 50;
    public static final int EQUALS = 51;
    public static final int ASTERISK = 52;
    public static final int VERTICAL_BAR = 53;
    public static final int EOF = 54;

    public static final Token TK_S = new Token(S, "S", "whitespace");
    public static final Token TK_CDO = new Token(CDO, "CDO", "<!--");
    public static final Token TK_CDC = new Token(CDC, "CDC", "-->");
    public static final Token TK_INCLUDES = new Token(INCLUDES, "INCLUDES", "an attribute word match");
    public static final Token TK_DASHMATCH = new Token(DASHMATCH, "DASHMATCH", "an attribute hyphen match");
    public static final Token TK_PREFIXMATCH = new Token(PREFIXMATCH, "PREFIXMATCH", "an attribute prefix match");
    public static final Token TK_SUFFIXMATCH = new Token(SUFFIXMATCH, "SUFFIXMATCH", "an attribute suffix match");
    public static final Token TK_SUBSTRINGMATCH = new Token(SUBSTRINGMATCH, "SUBSTRINGMATCH", "an attribute substring match");
    public static final Token TK_LBRACE = new Token(LBRACE, "LBRACE", "a {");
    public static final Token TK_PLUS = new Token(PLUS, "PLUS", "a +");
    public static final Token TK_GREATER = new Token(GREATER, "GREATER", "a >");
    public static final Token TK_COMMA = new Token(COMMA, "COMMA", "a comma");
    public static final Token TK_STRING = new Token(STRING, "STRING", "a string");
    public static final Token TK_INVALID = new Token(INVALID, "INVALID", "an unclosed string");
    public static final Token TK_IDENT = new Token(IDENT, "IDENT", "an identifier");
    public static final Token TK_HASH = new Token(HASH, "HASH", "a hex color");
    public static final Token TK_IMPORT_SYM = new Token(IMPORT_SYM, "IMPORT_SYM", "@import");
    public static final Token TK_PAGE_SYM = new Token(PAGE_SYM, "PAGE_SYM", "@page");
    public static final Token TK_MEDIA_SYM = new Token(MEDIA_SYM, "MEDIA_SYM", "@media");
    public static final Token TK_CHARSET_SYM = new Token(CHARSET_SYM, "CHARSET_SYM", "@charset");
    public static final Token TK_NAMESPACE_SYM = new Token(NAMESPACE_SYM, "NAMESPACE_SYM", "@namespace,");
    public static final Token TK_FONT_FACE_SYM = new Token(FONT_FACE_SYM, "FONT_FACE_SYM", "@font-face");
    public static final Token TK_AT_RULE = new Token(AT_RULE, "AT_RULE", "at rule");
    public static final Token TK_IMPORTANT_SYM = new Token(IMPORTANT_SYM, "IMPORTANT_SYM", "!important");
    public static final Token TK_EMS = new Token(EMS, "EMS", "an em value");
    public static final Token TK_EXS = new Token(EXS, "EXS", "an ex value");
    public static final Token TK_PX = new Token(PX, "PX", "a pixel value");
    public static final Token TK_CM = new Token(CM, "CM", "a centimeter value");
    public static final Token TK_MM = new Token(MM, "MM", "a millimeter value");
    public static final Token TK_IN = new Token(IN, "IN", "an inch value");
    public static final Token TK_PT = new Token(PT, "PT", "a point value");
    public static final Token TK_PC = new Token(PC, "PC", "a pica value");
    public static final Token TK_ANGLE = new Token(ANGLE, "ANGLE", "an angle value");
    public static final Token TK_TIME = new Token(TIME, "TIME", "a time value");
    public static final Token TK_FREQ = new Token(FREQ, "FREQ", "a freq value");
    public static final Token TK_DIMENSION = new Token(DIMENSION, "DIMENSION", "a dimension");
    public static final Token TK_PERCENTAGE = new Token(PERCENTAGE, "PERCENTAGE", "a percentage");
    public static final Token TK_NUMBER = new Token(NUMBER, "NUMBER", "a number");
    public static final Token TK_URI = new Token(URI, "URI", "a URI");
    public static final Token TK_FUNCTION = new Token(FUNCTION, "FUNCTION", "function");
    public static final Token TK_OTHER = new Token(OTHER, "OTHER", "other");
    public static final Token TK_RBRACE = new Token(RBRACE, "RBRACE", "}");
    public static final Token TK_SEMICOLON = new Token(SEMICOLON, "SEMICOLON", ";");
    public static final Token TK_VIRGULE = new Token(VIRGULE, "VIRGULE", "/");
    public static final Token TK_COLON = new Token(COLON, "COLON", ":");
    public static final Token TK_MINUS = new Token(MINUS, "MINUS", "-");
    public static final Token TK_RPAREN = new Token(RPAREN, "RPAREN", ")");
    public static final Token TK_LBRACKET = new Token(LBRACKET, "LBRACKET", "[");
    public static final Token TK_RBRACKET = new Token(RBRACKET, "RBRACKET", "]");
    public static final Token TK_PERIOD = new Token(PERIOD, "PERIOD", ".");
    public static final Token TK_EQUALS = new Token(EQUALS, "EQUALS", "=");
    public static final Token TK_ASTERISK = new Token(ASTERISK, "ASTERISK", "*");
    public static final Token TK_VERTICAL_BAR = new Token(VERTICAL_BAR, "VERTICAL_BAR", "|");
    public static final Token TK_EOF = new Token(EOF, "EOF", "end of file");

  
    private final int _type;
    private final String _name;
    private final String _externalName;
  
    private Token(int type, String name, String externalName) {
        _type = type;
        _name = name;
        _externalName = externalName;
    }
    
    public int getType() {
        return _type;
    }
    
    public String getName() {
        return _name;
    }
    
    public String getExternalName() {
        return _externalName;
    }
    
    public String toString() {
        return _name;
    }
    
    public static Token createOtherToken(String value) {
        return new Token(OTHER, "OTHER", value + " (other)");
    }
}

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

import java.io.IOException;

public class Parser {
    private Token _saved;
    private Lexer _lexer;
    
    public Parser(Lexer lexer) {
        _lexer = lexer;
    }
    
//  stylesheet
//  : [ CHARSET_SYM STRING ';' ]?
//    [S|CDO|CDC]* [ import [S|CDO|CDC]* ]*
//    [ [ ruleset | media | page ] [S|CDO|CDC]* ]*
//  ;    
    public void stylesheet() throws IOException {
        System.out.println("stylesheet()");
        Token t = la();
        try {
            if (t == Token.TK_CHARSET_SYM) {
                try {
                    t = next();
                    if (t == Token.TK_STRING) {
                        String charset = _lexer.yytext();
                        
                        t = next();
                        if (t != Token.TK_SEMICOLON) {
                            push(t);
                            throw new CSSParseException(t, Token.TK_SEMICOLON, _lexer.yyline());
                        }
                        
                        // Do something
                    } else {
                        push(t);
                        throw new CSSParseException(t, Token.TK_STRING, _lexer.yyline());
                    }
                } catch (CSSParseException e) {
                    error(e, "@charset rule");
                    recover(false, false);
                }
            } else {
                skip_whitespace_and_cdocdc();
                while (true) {
                    t = la();
                    if (t == Token.TK_IMPORT_SYM) {
                        import_statement();
                        skip_whitespace_and_cdocdc();
                    } else {
                        break;
                    }
                }
                while (true) {
                    t = la();
                    if (t == Token.TK_EOF) {
                        break;
                    }
                    switch (t.getType()) {
                        case Token.PAGE_SYM:
                            page();
                            break;
                        case Token.MEDIA_SYM:
                            media();
                            break;
                        case Token.IMPORT_SYM:
                            next();
                            error(new CSSParseException("@import not allowed", _lexer.yyline()),
                                    "@import rule");
                            recover(false, false);
                            break;
                        case Token.OTHER:
                            if (_lexer.yycharat(0) == '@') {
                                next();
                                error(new CSSParseException("Invalid at-rule", _lexer.yyline()), "rule");
                                recover(false, false);
                                break;
                            }
                            // fall through
                        default:
                            ruleset();
                    }
                    skip_whitespace_and_cdocdc();
                }
            }
        } catch (CSSParseException e) {
            // "shouldn't" happen
            if (! e.isCallerNotified()) {
                error(e, "stylesheet");
            }
        }
    }
    
//  import
//  : IMPORT_SYM S*
//    [STRING|URI] S* [ medium [ COMMA S* medium]* ]? ';' S*
//  ;
    private void import_statement() throws IOException {
        System.out.println("import()");
        try {
            Token t = next();
            if (t == Token.TK_IMPORT_SYM) {
                skip_whitespace();
                t = next();
                switch (t.getType()) {
                    case Token.STRING:
                    case Token.URI:
                        skip_whitespace();
                        t = la();
                        if (t == Token.TK_IDENT) {
                            medium();
                            while (true) {
                                t = la();
                                if (t == Token.TK_COMMA) {
                                    next();
                                    skip_whitespace();
                                    t = la();
                                    if (t == Token.TK_IDENT) {
                                        medium();
                                    } else {
                                        throw new CSSParseException(
                                                t, Token.TK_IDENT, _lexer.yyline());
                                    }
                                } else {
                                    break;
                                }
                            }
                        }
                        t = next();
                        if (t == Token.TK_SEMICOLON) {
                            skip_whitespace();
                        } else {
                            push(t);
                            throw new CSSParseException(
                                    t, Token.TK_SEMICOLON, _lexer.yyline());
                        }
                        break;
                    default:
                        push(t);
                        throw new CSSParseException(
                            t, new Token[] { Token.TK_STRING, Token.TK_URI }, _lexer.yyline());
                }
            } else {
                push(t);
                throw new CSSParseException(
                        t, Token.TK_IMPORT_SYM, _lexer.yyline());
            }
        } catch (CSSParseException e) {
            error(e, "@import rule");
            recover(false, false);
        }
    }
    
//  media
//  : MEDIA_SYM S* medium [ COMMA S* medium ]* LBRACE S* ruleset* '}' S*
//  ;
    private void media() throws IOException {
        System.out.println("media()");
        Token t = next();
        try {
            if (t == Token.TK_MEDIA_SYM) {
                skip_whitespace();
                t = la();
                if (t == Token.TK_IDENT) {
                    medium();
                    while (true) {
                        t = la();
                        if (t == Token.TK_COMMA) {
                            next();
                            skip_whitespace();
                            t = la();
                            if (t == Token.TK_IDENT) {
                                medium();
                            } else {
                                throw new CSSParseException(t, Token.TK_IDENT, _lexer.yyline());
                            }
                        } else {
                            break;
                        }
                    }
                    t = next();
                    if (t == Token.TK_LBRACE) {
                        skip_whitespace();
                        LOOP: 
                        while (true) {
                            t = la();
                            if (t == null) {
                                break;
                            }
                            switch (t.getType()) {
                                case Token.RBRACE:
                                    next();
                                    break LOOP;
                                default:
                                    ruleset();
                            }
                        }
                        skip_whitespace();
                    } else {
                        push(t);
                        throw new CSSParseException(t, Token.TK_LBRACE, _lexer.yyline());
                    }
                } else {
                    throw new CSSParseException(t, Token.TK_IDENT, _lexer.yyline());
                }
            } else {
                push(t);
                throw new CSSParseException(t, Token.TK_MEDIA_SYM, _lexer.yyline());
            }
        } catch (CSSParseException e) {
            error(e, "@media rule");
            recover(false, false);
        }
    }

// medium
// : IDENT S*
// ;
    private void medium() throws IOException {
        System.out.println("medium()");
        Token t = next();
        if (t == Token.TK_IDENT) {
            skip_whitespace();
        } else {
            push(t);
            throw new CSSParseException(t, Token.TK_IDENT, _lexer.yyline());
        }
    }
    
//  page
//    : PAGE_SYM S* pseudo_page? S*
//      LBRACE S* declaration [ ';' S* declaration ]* '}' S*
//    ;
    private void page() throws IOException {
        System.out.println("page()");
        Token t = next();
        try {
            if (t == Token.TK_PAGE_SYM) {
                skip_whitespace();
                t = la();
                if (t == Token.TK_COLON) {
                    pseudo_page();
                }
                skip_whitespace();
                t = next();
                if (t == Token.TK_LBRACE) {
                    skip_whitespace();
                    declaration_list();
                    t = next();
                    if (t == Token.TK_RBRACE) {
                        skip_whitespace();
                    } else {
                        push(t);
                        throw new CSSParseException(t, Token.TK_RBRACE, _lexer.yyline());
                    }
                } else {
                    push(t);
                    throw new CSSParseException(t, Token.TK_LBRACE, _lexer.yyline());
                }
            } else {
                push(t);
                throw new CSSParseException(t, Token.TK_PAGE_SYM, _lexer.yyline());
            }
        } catch (CSSParseException e) {
            error(e, "@page rule");
            recover(false, false);
        }
    }
    
//  pseudo_page
//    : ':' IDENT
//    ;
    private void pseudo_page() throws IOException {
        System.out.println("pseudo_page()");
        Token t = next();
        if (t == Token.TK_COLON) {
            t = next();
            if (t == Token.TK_IDENT) {
                // Do something
            } else {
                push(t);
                throw new CSSParseException(t, Token.TK_IDENT, _lexer.yyline());
            }
        } else {
            push(t);
            throw new CSSParseException(t, Token.TK_COLON, _lexer.yyline());
        }

    }
//  operator
//    : '/' S* | COMMA S* | /* empty */
//    ;
    private void operator() throws IOException {
        System.out.println("operator()");
        Token t = la();
        switch (t.getType()) {
        case Token.VIRGULE:
        case Token.COMMA:
            next();
            skip_whitespace();
            break;
        }
    }
    
//  combinator
//    : PLUS S*
//    | GREATER S*
//    | S
//    ;
    private void combinator() throws IOException {
        System.out.println("combinator()");
        Token t = next();
        if (t == Token.TK_PLUS || t == Token.TK_GREATER) {
            skip_whitespace();
        } else if (t != Token.TK_S) {
            push(t);
            throw new CSSParseException(
                    t, 
                    new Token[] { Token.TK_PLUS, Token.TK_GREATER, Token.TK_S }, 
                    _lexer.yyline());
        }
    }
    
//  unary_operator
//    : '-' | PLUS
//    ;
    private void unary_operator() throws IOException {
        System.out.println("unary_operator()");
        Token t = next();
        if (! (t == Token.TK_MINUS || t == Token.TK_PLUS)) {
            push(t);
            throw new CSSParseException(
                    t, new Token[] { Token.TK_MINUS, Token.TK_PLUS}, _lexer.yyline());
        }
    }
    
//  property
//    : IDENT S*
//    ;
    private void property() throws IOException {
        System.out.println("property()");
        Token t = next();
        if (t == Token.TK_IDENT) {
            skip_whitespace();
        } else {
            push(t);
            throw new CSSParseException(
                    t, Token.TK_IDENT, _lexer.yyline());
        }
    }
    
//  declaration_list
//    : [ declaration ';' S* ]*
    private void declaration_list() throws IOException {
        System.out.println("declaration_list()");
        Token t;
        LOOP:
        while (true) {
            t = la();
            switch (t.getType()) {
                case Token.SEMICOLON:
                    next();
                    skip_whitespace();
                    continue;
                case Token.RBRACE:
                    break LOOP;
                default:
                    declaration();
            }
        }
    }
    
//  ruleset
//    : selector [ COMMA S* selector ]*
//      LBRACE S* [ declaration ';' S* ]* '}' S*
//    ;
    private void ruleset() throws IOException {
        System.out.println("ruleset()");
        try {
            selector();
            Token t;
            while (true) {
                t = la();
                if (t == Token.TK_COMMA) {
                    next();
                    skip_whitespace();
                    selector();
                } else {
                    break;
                }
            }
            t = next();
            if (t == Token.TK_LBRACE) {
                skip_whitespace();
                declaration_list();
                t = next();
                if (t == Token.TK_RBRACE) {
                    skip_whitespace();
                } else {
                    push(t);
                    throw new CSSParseException(t, Token.TK_RBRACE, _lexer.yyline());
                }
            } else {
                push(t);
                throw new CSSParseException(
                        t, new Token[] { Token.TK_COMMA, Token.TK_LBRACE }, _lexer.yyline());
            }
        } catch (CSSParseException e) {
            error(e, "ruleset");
            recover(true, false);
        }
    }
    
//  selector
//    : simple_selector [ combinator simple_selector ]*
//    ;
    private void selector() throws IOException {
        System.out.println("selector()");
        simple_selector();
        LOOP:
        while (true) {
            Token t = la();
            switch (t.getType()) {
                case Token.PLUS:
                case Token.GREATER:
                case Token.S:
                    combinator();
                    t = la();
                    switch (t.getType()) {
                        case Token.IDENT:
                        case Token.ASTERISK:
                        case Token.HASH:
                        case Token.PERIOD:
                        case Token.LBRACKET:
                        case Token.COLON:
                            simple_selector();
                            break;
                        default:
                            throw new CSSParseException(t, new Token[] { Token.TK_IDENT,
                                    Token.TK_ASTERISK, Token.TK_HASH, Token.TK_PERIOD,
                                    Token.TK_LBRACKET, Token.TK_COLON }, _lexer.yyline());
                    }
                    break;
                default:
                    break LOOP;
            }
        }
    }
    
//  simple_selector
//    : element_name [ HASH | class | attrib | pseudo ]*
//    | [ HASH | class | attrib | pseudo ]+
//    ;
    private void simple_selector() throws IOException {
        System.out.println("simple_selector()");
        Token t = la();
        switch (t.getType()) {
            case Token.IDENT:
            case Token.ASTERISK:
                element_name();
                LOOP: while (true) {
                    t = la();
                    switch (t.getType()) {
                        case Token.HASH:
                            t = next();
                            break;
                        case Token.PERIOD:
                            class_selector();
                            break;
                        case Token.LBRACKET:
                            attrib();
                            break;
                        case Token.COLON:
                            pseudo();
                            break;
                        default:
                            break LOOP;
                    }
                }
                break;
            default:
                boolean found = false;
                LOOP: while (true) {
                    t = la();
                    switch (t.getType()) {
                        case Token.HASH:
                            t = next();
                            found = true;
                            break;
                        case Token.PERIOD:
                            class_selector();
                            found = true;
                            break;
                        case Token.LBRACKET:
                            attrib();
                            found = true;
                            break;
                        case Token.COLON:
                            pseudo();
                            found = true;
                            break;
                        default:
                            if (!found) {
                                throw new CSSParseException(t, new Token[] { Token.TK_HASH,
                                        Token.TK_PERIOD, Token.TK_LBRACKET, Token.TK_COLON },
                                        _lexer.yyline());
                            }
                            break LOOP;
                    }
                }
        }
    }
    
//  class
//    : '.' IDENT
//    ;
    private void class_selector() throws IOException {
        System.out.println("class_selector()");
        Token t = next();
        if (t == Token.TK_PERIOD) {
            t = next();
            if (t == Token.TK_IDENT) {
                // Do something
            } else {
                push(t);
                throw new CSSParseException(t, Token.TK_IDENT, _lexer.yyline());
            }
        } else {
            push(t);
            throw new CSSParseException(t, Token.TK_PERIOD, _lexer.yyline());
        }
    }
    
//  element_name
//    : IDENT | '*'
//    ;
    private void element_name() throws IOException {
        System.out.println("element_name()");
        Token t = next();
        if (t == Token.TK_IDENT || t == Token.TK_ASTERISK) {
            // Do something
        } else {
            push(t);
            throw new CSSParseException(
                    t, new Token[] { Token.TK_IDENT, Token.TK_ASTERISK }, _lexer.yyline());
        }
    }
    
//  attrib
//    : '[' S* IDENT S* [ [ '=' | INCLUDES | DASHMATCH ] S*
//      [ IDENT | STRING ] S* ]? ']'
//    ;
    private void attrib() throws IOException {
        System.out.println("attrib()");
        Token t = next();
        if (t == Token.TK_LBRACKET) {
            skip_whitespace();
            t = next();
            if (t == Token.TK_IDENT) {
                skip_whitespace();
                t = la();
                switch (t.getType()) {
                    case Token.EQUALS:
                    case Token.INCLUDES:
                    case Token.DASHMATCH:
                        next();
                        skip_whitespace();
                        t = next();
                        if (t == Token.TK_IDENT || t == Token.TK_STRING) {
                            skip_whitespace();
                        } else {
                            push(t);
                            throw new CSSParseException(t, new Token[] { Token.TK_IDENT,
                                    Token.TK_STRING }, _lexer.yyline());
                        }
                        skip_whitespace();
                        t = la();
                        break;
                }
                if (t == Token.TK_RBRACKET) {
                    next();
                } else {
                    throw new CSSParseException(t, new Token[] { Token.TK_EQUALS,
                            Token.TK_INCLUDES, Token.TK_DASHMATCH, Token.TK_RBRACKET }, 
                            _lexer.yyline());
                }
            } else {
                push(t);
                throw new CSSParseException(t, Token.TK_IDENT, _lexer.yyline());
            }
        } else {
            push(t);
            throw new CSSParseException(t, Token.TK_LBRACKET, _lexer.yyline());
        }
    }
    
//  pseudo
//    : ':' [ IDENT | FUNCTION S* IDENT? S* ')' ]
//    ;
    private void pseudo() throws IOException {
        System.out.println("pseudo()");
        Token t = next();
        if (t == Token.TK_COLON) {
            t = next();
            switch (t.getType()) {
                case Token.IDENT:
                    break;
                case Token.FUNCTION:
                    skip_whitespace();
                    t = next();
                    if (t == Token.TK_IDENT) {
                        skip_whitespace();
                        t = next();
                    }
                    if (t != Token.TK_RPAREN) {
                        push(t);
                        throw new CSSParseException(t, Token.TK_RPAREN, _lexer.yyline());
                    }
                    break;
                default:
                    push(t);
                    throw new CSSParseException(t,
                            new Token[] { Token.TK_IDENT, Token.TK_FUNCTION }, _lexer.yyline());
            }
        } else {
            push(t);
            throw new CSSParseException(t, Token.TK_COLON, _lexer.yyline());
        }
    }
    
//  declaration
//    : property ':' S* expr prio?
//    ;
    private void declaration() throws IOException {
        System.out.println("declaration()");
        try {
            Token t = la();
            if (t == Token.TK_IDENT) {
                property();
                t = next();
                if (t == Token.TK_COLON) {
                    skip_whitespace();
                    expr();
                    t = la();
                    if (t == Token.TK_IMPORTANT_SYM) {
                        prio();
                    }
                } else {
                    push(t);
                    throw new CSSParseException(t, Token.TK_COLON, _lexer.yyline());
                }
            } else {
                throw new CSSParseException(t, Token.TK_IDENT, _lexer.yyline());
            }
        } catch (CSSParseException e) {
            error(e, "declaration");
            recover(false, true);
        }
    }
    
//  prio
//    : IMPORTANT_SYM S*
//    ;
    private void prio() throws IOException {
        System.out.println("prio()");
        Token t = next();
        if (t == Token.TK_IMPORTANT_SYM) {
            skip_whitespace();
        } else {
            push(t);
            throw new CSSParseException(t, Token.TK_IMPORTANT_SYM, _lexer.yyline());
        }
    }
    
//  expr
//    : term [ operator term ]*
//    ;
    private void expr() throws IOException {
        System.out.println("expr()");
        term();
        LOOP: while (true) {
            Token t = la();
            boolean operator = false;
            switch (t.getType()) {
                case Token.VIRGULE:
                case Token.COMMA:
                    operator();
                    t = la();
                    operator = true;
                    break;
            }
            switch (t.getType()) {
                case Token.PLUS:
                case Token.MINUS:
                case Token.NUMBER:
                case Token.PERCENTAGE:
                case Token.LENGTH:
                case Token.EMS:
                case Token.EXS:
                case Token.ANGLE:
                case Token.TIME:
                case Token.FREQ:
                case Token.STRING:
                case Token.IDENT:
                case Token.URI:
                case Token.HASH:
                case Token.FUNCTION:
                    term();
                    break;
                default:
                    if (operator) {
                        throw new CSSParseException(t, new Token[] { 
                                Token.TK_PLUS, Token.TK_MINUS, Token.TK_NUMBER,
                                Token.TK_PERCENTAGE, Token.TK_LENGTH, Token.TK_EMS, Token.TK_EXS,
                                Token.TK_ANGLE, Token.TK_TIME, Token.TK_FREQ, Token.TK_STRING,
                                Token.TK_IDENT, Token.TK_URI, Token.TK_HASH, Token.TK_FUNCTION },
                                _lexer.yyline());
                    } else {
                        break LOOP;
                    }
            }
        }
    }
    
//  term
//    : unary_operator?
//      [ NUMBER S* | PERCENTAGE S* | LENGTH S* | EMS S* | EXS S* | ANGLE S* |
//        TIME S* | FREQ S* ]
//    | STRING S* | IDENT S* | URI S* | hexcolor | function
//    ;
    private void term() throws IOException {
        System.out.println("term()");
        Token t = la();
        if (t == Token.TK_PLUS || t == Token.TK_MINUS) {
            unary_operator();
            t = la();
        }
        switch (t.getType()) {
            case Token.NUMBER:
            case Token.PERCENTAGE:
            case Token.LENGTH:
            case Token.EMS:
            case Token.EXS:
            case Token.ANGLE:
            case Token.TIME:
            case Token.FREQ:
            case Token.STRING:
            case Token.IDENT:
            case Token.URI:
                next();
                skip_whitespace();
                break;
            case Token.HASH:
                hexcolor();
                break;
            case Token.FUNCTION:
                function();
                break;
            default:
                throw new CSSParseException(t, new Token[] { Token.TK_NUMBER,
                        Token.TK_PERCENTAGE, Token.TK_LENGTH, Token.TK_EMS, Token.TK_EXS,
                        Token.TK_ANGLE, Token.TK_TIME, Token.TK_FREQ, Token.TK_STRING,
                        Token.TK_IDENT, Token.TK_URI, Token.TK_HASH, Token.TK_FUNCTION },
                        _lexer.yyline());
        }
    }
    
//  function
//    : FUNCTION S* expr ')' S*
//    ;
    private void function() throws IOException {
        System.out.println("function()");
        Token t = next();
        if (t == Token.TK_FUNCTION) {
            skip_whitespace();
            expr();
            t = next();
            if (t != Token.TK_RPAREN) {
                push(t);
                throw new CSSParseException(t, Token.TK_RPAREN, _lexer.yyline());
            }
            skip_whitespace();
        } else {
            push(t);
            throw new CSSParseException(t, Token.TK_FUNCTION, _lexer.yyline());
        }
    }
    
//  /*
//  * There is a constraint on the color that it must
//  * have either 3 or 6 hex-digits (i.e., [0-9a-fA-F])
//  * after the "#"; e.g., "#000" is OK, but "#abcd" is not.
//  */
// hexcolor
//   : HASH S*
//   ;    
    private void hexcolor() throws IOException {
        System.out.println("hexcolor()");
        Token t = next();
        if (t == Token.TK_HASH) {
            skip_whitespace();
        } else {
            push(t);
            throw new CSSParseException(t, Token.TK_HASH, _lexer.yyline());
        }
    }
    
    private void skip_whitespace() throws IOException {
        Token t;
        while ( (t = next()) == Token.TK_S) {
            // skip
        }
        push(t);
    }
    
    private void skip_whitespace_and_cdocdc() throws IOException {
        Token t;
        while (true) {
            t = next();
            if (! (t == Token.TK_S || t == Token.TK_CDO || t == Token.TK_CDC)) {
                break;
            }
        }
        push(t);
    }
    
    private Token next() throws IOException {
        if (_saved != null) {
            Token result = _saved;
            _saved = null;
            return result;
        } else {
            return _lexer.yylex();
        }
    }
    
    private void push(Token t) {
        if (_saved != null) {
            throw new RuntimeException("saved must be null");
        }
        _saved = t;
    }
    
    private Token la() throws IOException {
        Token result = next();
        push(result);
        return result;
    }
    
    private void error(CSSParseException e, String where) throws IOException {
        if (! e.isCallerNotified()) {
            System.out.println("ERROR: " + e.getMessage() + " Skipping " + where + ".");
        }
        e.setCallerNotified(true);
        if (e.isEOF()) {
            throw e;
        }
    }
    
    private void recover(boolean needBlock, boolean stopBeforeBlockClose) throws IOException {
        int braces = 0;
        boolean foundBlock = false;
        LOOP:
        while (true) {
            Token t = next();
            if (t == Token.TK_EOF) {
                return;
            }
            switch (t.getType()) {
                case Token.LBRACE:
                    foundBlock = true;
                    braces++;
                    break;
                case Token.RBRACE:
                    if (braces == 0) {
                        if (stopBeforeBlockClose) {
                            push(t);
                            break LOOP;
                        }
                    } else {
                        braces--;
                        if (braces == 0) {
                            break LOOP;
                        }
                    }
                    break;
                case Token.SEMICOLON:
                    if (braces == 0 && ((! needBlock) || foundBlock)) {
                        break LOOP;
                    }
                    break;
            }
        }
        skip_whitespace();
    }
}

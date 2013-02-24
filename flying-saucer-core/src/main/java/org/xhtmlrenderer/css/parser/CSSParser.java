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
import java.io.Reader;
import java.io.StringReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.w3c.dom.css.CSSPrimitiveValue;
import org.xhtmlrenderer.css.constants.CSSName;
import org.xhtmlrenderer.css.constants.MarginBoxName;
import org.xhtmlrenderer.css.extend.TreeResolver;
import org.xhtmlrenderer.css.newmatch.Selector;
import org.xhtmlrenderer.css.parser.property.PropertyBuilder;
import org.xhtmlrenderer.css.sheet.FontFaceRule;
import org.xhtmlrenderer.css.sheet.MediaRule;
import org.xhtmlrenderer.css.sheet.PageRule;
import org.xhtmlrenderer.css.sheet.PropertyDeclaration;
import org.xhtmlrenderer.css.sheet.Ruleset;
import org.xhtmlrenderer.css.sheet.RulesetContainer;
import org.xhtmlrenderer.css.sheet.Stylesheet;
import org.xhtmlrenderer.css.sheet.StylesheetInfo;

public class CSSParser {
    private static final Set SUPPORTED_PSEUDO_ELEMENTS;
    private static final Set CSS21_PSEUDO_ELEMENTS;

    static {
        SUPPORTED_PSEUDO_ELEMENTS = new HashSet();
        SUPPORTED_PSEUDO_ELEMENTS.add("first-line");
        SUPPORTED_PSEUDO_ELEMENTS.add("first-letter");
        SUPPORTED_PSEUDO_ELEMENTS.add("before");
        SUPPORTED_PSEUDO_ELEMENTS.add("after");

        CSS21_PSEUDO_ELEMENTS = new HashSet();
        CSS21_PSEUDO_ELEMENTS.add("first-line");
        CSS21_PSEUDO_ELEMENTS.add("first-letter");
        CSS21_PSEUDO_ELEMENTS.add("before");
        CSS21_PSEUDO_ELEMENTS.add("after");
    }

    private Token _saved;
    private Lexer _lexer;

    private CSSErrorHandler _errorHandler;
    private String _URI;

    private Map _namespaces = new HashMap();
    private boolean _supportCMYKColors;

    public CSSParser(CSSErrorHandler errorHandler) {
        _lexer = new Lexer(new StringReader(""));
        _errorHandler = errorHandler;
    }

    public Stylesheet parseStylesheet(String uri, int origin, Reader reader)
            throws IOException {
        _URI = uri;
        reset(reader);

        Stylesheet result = new Stylesheet(uri, origin);
        stylesheet(result);

        return result;
    }

    public Ruleset parseDeclaration(int origin, String text) {
        try {
            // XXX Set this to something more reasonable
            _URI = "style attribute";
            reset(new StringReader(text));

            skip_whitespace();

            Ruleset result = new Ruleset(origin);

            try {
                declaration_list(result, true, false, false);
            } catch (CSSParseException e) {
                // ignore, already handled
            }

            return result;
        } catch (IOException e) {
            // "Shouldn't" happen
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    public PropertyValue parsePropertyValue(CSSName cssName, int origin, String expr) {
        _URI = cssName + " property value";
        try {
            reset(new StringReader(expr));
            List values = expr(
                    cssName == CSSName.FONT_FAMILY ||
                    cssName == CSSName.FONT_SHORTHAND ||
                    cssName == CSSName.FS_PDF_FONT_ENCODING);

            PropertyBuilder builder = CSSName.getPropertyBuilder(cssName);
            List props;
            try {
                props = builder.buildDeclarations(cssName, values, origin, false);
            } catch (CSSParseException e) {
                e.setLine(getCurrentLine());
                throw e;
            }

            if (props.size() != 1) {
                throw new CSSParseException(
                        "Builder created " + props.size() + "properties, expected 1", getCurrentLine());
            }

            PropertyDeclaration decl = (PropertyDeclaration)props.get(0);

            return (PropertyValue)decl.getValue();
        } catch (IOException e) {
            // "Shouldn't" happen
            throw new RuntimeException(e.getMessage(), e);
        } catch (CSSParseException e) {
            error(e, "property value", false);
            return null;
        }
    }

//    stylesheet
//    : [ CHARSET_SYM S* STRING S* ';' ]?
//      [S|CDO|CDC]* [ import [S|CDO|CDC]* ]*
//      [ namespace [S|CDO|CDC]* ]*
//      [ [ ruleset | media | page | font_face ] [S|CDO|CDC]* ]*
    private void stylesheet(Stylesheet stylesheet) throws IOException {
        //System.out.println("stylesheet()");
        Token t = la();
        try {
            if (t == Token.TK_CHARSET_SYM) {
                try {
                    t = next();
                    skip_whitespace();
                    t = next();
                    if (t == Token.TK_STRING) {
                        /* String charset = getTokenValue(t); */

                        skip_whitespace();
                        t = next();
                        if (t != Token.TK_SEMICOLON) {
                            push(t);
                            throw new CSSParseException(t, Token.TK_SEMICOLON, getCurrentLine());
                        }

                        // Do something
                    } else {
                        push(t);
                        throw new CSSParseException(t, Token.TK_STRING, getCurrentLine());
                    }
                } catch (CSSParseException e) {
                    error(e, "@charset rule", true);
                    recover(false, false);
                }
            }
            skip_whitespace_and_cdocdc();
            while (true) {
                t = la();
                if (t == Token.TK_IMPORT_SYM) {
                    import_rule(stylesheet);
                    skip_whitespace_and_cdocdc();
                } else {
                    break;
                }
            }
            while (true) {
                t = la();
                if (t == Token.TK_NAMESPACE_SYM) {
                    namespace();
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
                        page(stylesheet);
                        break;
                    case Token.MEDIA_SYM:
                        media(stylesheet);
                        break;
                    case Token.FONT_FACE_SYM:
                        font_face(stylesheet);
                        break;
                    case Token.IMPORT_SYM:
                        next();
                        error(new CSSParseException("@import not allowed here", getCurrentLine()),
                                "@import rule", true);
                        recover(false, false);
                        break;
                    case Token.NAMESPACE_SYM:
                        next();
                        error(new CSSParseException("@namespace not allowed here", getCurrentLine()),
                                "@namespace rule", true);
                        recover(false, false);
                        break;
                    case Token.AT_RULE:
                        next();
                        error(new CSSParseException(
                                "Invalid at-rule", getCurrentLine()), "at-rule", true);
                        recover(false, false);
                        // fall through
                    default:
                        ruleset(stylesheet);
                }
                skip_whitespace_and_cdocdc();
            }
        } catch (CSSParseException e) {
            // "shouldn't" happen
            if (! e.isCallerNotified()) {
                error(e, "stylesheet", false);
            }
        }
    }

//  import
//  : IMPORT_SYM S*
//    [STRING|URI] S* [ medium [ COMMA S* medium]* ]? ';' S*
//  ;
    private void import_rule(Stylesheet stylesheet) throws IOException {
        //System.out.println("import()");
        try {
            Token t = next();
            if (t == Token.TK_IMPORT_SYM) {
                StylesheetInfo info = new StylesheetInfo();
                info.setOrigin(stylesheet.getOrigin());
                info.setType("text/css");

                skip_whitespace();
                t = next();
                switch (t.getType()) {
                    case Token.STRING:
                    case Token.URI:
                        // first see if we can set URI via URL
                        try {
                            info.setUri(new URL(new URL(stylesheet.getURI()), getTokenValue(t)).toString());
                        } catch (MalformedURLException mue) {
                            // not a valid URL, may be a custom protocol which the user expects to handle
                            // in the user agent
                            //
                            // FIXME: using URI like this will not work for some cases of parent URI, depends
                            // on whether the URI class can parse the parent and child correctly
                            // This can lead to a bug where a stylesheet imported from another stylesheet ends
                            // up unresolved. This will be fixed in a later release by passing Stylesheet info
                            // all the way down to the UAC so that the end user can code for it
                            try {
                                URI parent = new URI(stylesheet.getURI());
                                String tokenValue = getTokenValue(t);
                                String resolvedUri = parent.resolve(tokenValue).toString();
                                System.out.println("Token: " + tokenValue + " resolved " + resolvedUri);
                                info.setUri(resolvedUri);
                            } catch (URISyntaxException use) {
                                throw new CSSParseException("Invalid URL, " + use.getMessage(), getCurrentLine());
                            }

                        }
                        skip_whitespace();
                        t = la();
                        if (t == Token.TK_IDENT) {
                            info.addMedium(medium());
                            while (true) {
                                t = la();
                                if (t == Token.TK_COMMA) {
                                    next();
                                    skip_whitespace();
                                    t = la();
                                    if (t == Token.TK_IDENT) {
                                        info.addMedium(medium());
                                    } else {
                                        throw new CSSParseException(
                                                t, Token.TK_IDENT, getCurrentLine());
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
                                    t, Token.TK_SEMICOLON, getCurrentLine());
                        }
                        break;
                    default:
                        push(t);
                        throw new CSSParseException(
                            t, new Token[] { Token.TK_STRING, Token.TK_URI }, getCurrentLine());
                }

                if (info.getMedia().size() == 0) {
                    info.addMedium("all");
                }
                stylesheet.addImportRule(info);
            } else {
                push(t);
                throw new CSSParseException(
                        t, Token.TK_IMPORT_SYM, getCurrentLine());
            }
        } catch (CSSParseException e) {
            error(e, "@import rule", true);
            recover(false, false);
        }
    }

//  namespace
//  : NAMESPACE_SYM S* [namespace_prefix S*]? [STRING|URI] S* ';' S*
//  ;
//  namespace_prefix
//  : IDENT
//  ;
    private void namespace() throws IOException {
        try {
            Token t = next();
            if (t == Token.TK_NAMESPACE_SYM) {
                String prefix = null;
                String url = null;

                skip_whitespace();
                t = next();

                if (t == Token.TK_IDENT) {
                    prefix = getTokenValue(t);
                    skip_whitespace();
                    t = next();
                }

                if (t == Token.TK_STRING || t == Token.TK_URI) {
                    url = getTokenValue(t);
                } else {
                    throw new CSSParseException(
                            t, new Token[] { Token.TK_STRING, Token.TK_URI }, getCurrentLine());
                }

                skip_whitespace();

                t = next();
                if (t == Token.TK_SEMICOLON) {
                    skip_whitespace();

                    _namespaces.put(prefix, url);
                } else {
                    throw new CSSParseException(
                            t, Token.TK_SEMICOLON, getCurrentLine());
                }
            } else {
                throw new CSSParseException(t, Token.TK_NAMESPACE_SYM, getCurrentLine());
            }
        } catch (CSSParseException e) {
            error(e, "@namespace rule", true);
            recover(false, false);
        }
    }

//  media
//  : MEDIA_SYM S* medium [ COMMA S* medium ]* LBRACE S* ruleset* '}' S*
//  ;
    private void media(Stylesheet stylesheet) throws IOException {
        //System.out.println("media()");
        Token t = next();
        try {
            if (t == Token.TK_MEDIA_SYM) {
                MediaRule mediaRule = new MediaRule(stylesheet.getOrigin());
                skip_whitespace();
                t = la();
                if (t == Token.TK_IDENT) {
                    mediaRule.addMedium(medium());
                    while (true) {
                        t = la();
                        if (t == Token.TK_COMMA) {
                            next();
                            skip_whitespace();
                            t = la();
                            if (t == Token.TK_IDENT) {
                                mediaRule.addMedium(medium());
                            } else {
                                throw new CSSParseException(t, Token.TK_IDENT, getCurrentLine());
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
                                    ruleset(mediaRule);
                            }
                        }
                        skip_whitespace();
                    } else {
                        push(t);
                        throw new CSSParseException(t, Token.TK_LBRACE, getCurrentLine());
                    }
                } else {
                    throw new CSSParseException(t, Token.TK_IDENT, getCurrentLine());
                }

                stylesheet.addContent(mediaRule);
            } else {
                push(t);
                throw new CSSParseException(t, Token.TK_MEDIA_SYM, getCurrentLine());
            }
        } catch (CSSParseException e) {
            error(e, "@media rule", true);
            recover(false, false);
        }
    }

//  medium
//  : IDENT S*
//  ;
    private String medium() throws IOException {
        //System.out.println("medium()");
        String result = null;
        Token t = next();
        if (t == Token.TK_IDENT) {
            result = getTokenValue(t);
            skip_whitespace();
        } else {
            push(t);
            throw new CSSParseException(t, Token.TK_IDENT, getCurrentLine());
        }
        return result;
    }

//  font_face
//    : FONT_FACE_SYM S*
//      '{' S* declaration [ ';' S* declaration ]* '}' S*
//    ;
    private void font_face(Stylesheet stylesheet) throws IOException {
        //System.out.println("font_face()");
        Token t = next();
        try {
            FontFaceRule fontFaceRule = new FontFaceRule(stylesheet.getOrigin());
            if (t == Token.TK_FONT_FACE_SYM) {
                skip_whitespace();

                Ruleset ruleset = new Ruleset(stylesheet.getOrigin());

                skip_whitespace();
                t = next();
                if (t == Token.TK_LBRACE) {
                    LOOP:
                    while (true) {
                        skip_whitespace();
                        t = la();
                        if (t == Token.TK_RBRACE) {
                            next();
                            skip_whitespace();
                            break LOOP;
                        } else {
                            declaration_list(ruleset, false, true, true);
                        }
                    }
                } else {
                    push(t);
                    throw new CSSParseException(t, Token.TK_LBRACE, getCurrentLine());
                }

                fontFaceRule.addContent(ruleset);
                stylesheet.addFontFaceRule(fontFaceRule);
            } else {
                push(t);
                throw new CSSParseException(t, Token.TK_FONT_FACE_SYM, getCurrentLine());
            }
        } catch (CSSParseException e) {
            error(e, "@font-face rule", true);
            recover(false, false);
        }
    }

//  page :
//    PAGE_SYM S* IDENT? pseudo_page? S* 
//    '{' S* [ declaration | margin ]? [ ';' S* [ declaration | margin ]? ]* '}' S*
//
    private void page(Stylesheet stylesheet) throws IOException {
        //System.out.println("page()");
        Token t = next();
        try {
            PageRule pageRule = new PageRule(stylesheet.getOrigin());
            if (t == Token.TK_PAGE_SYM) {
                skip_whitespace();
                t = la();
                if (t == Token.TK_IDENT) {
                    String pageName = getTokenValue(t);
                    if (pageName.equals("auto")) {
                        throw new CSSParseException("page name may not be auto", getCurrentLine());
                    }
                    next();
                    pageRule.setName(pageName);
                    t = la();
                }
                if (t == Token.TK_COLON) {
                    pageRule.setPseudoPage(pseudo_page());
                }
                Ruleset ruleset = new Ruleset(stylesheet.getOrigin());

                skip_whitespace();
                t = next();
                if (t == Token.TK_LBRACE) {
                    LOOP:
                    while (true) {
                        skip_whitespace();
                        t = la();
                        if (t == Token.TK_RBRACE) {
                            next();
                            skip_whitespace();
                            break LOOP;
                        } else if (t == Token.TK_AT_RULE) {
                            margin(stylesheet, pageRule);
                        } else {
                            declaration_list(ruleset, false, true, false);
                        }
                    }
                } else {
                    push(t);
                    throw new CSSParseException(t, Token.TK_LBRACE, getCurrentLine());
                }

                pageRule.addContent(ruleset);
                stylesheet.addContent(pageRule);
            } else {
                push(t);
                throw new CSSParseException(t, Token.TK_PAGE_SYM, getCurrentLine());
            }
        } catch (CSSParseException e) {
            error(e, "@page rule", true);
            recover(false, false);
        }
    }

//  margin :
//    margin_sym S* '{' declaration [ ';' S* declaration? ]* '}' S*
//    ;
    private void margin(Stylesheet stylesheet, PageRule pageRule) throws IOException {
        Token t = next();
        if (t != Token.TK_AT_RULE) {
            error(new CSSParseException(t, Token.TK_AT_RULE, getCurrentLine()), "at rule", true);
            recover(true, false);
            return;
        }
        String name = getTokenValue(t);
        MarginBoxName marginBoxName = MarginBoxName.valueOf(name);
        if (marginBoxName == null) {
            error(new CSSParseException(name + " is not a valid margin box name", getCurrentLine()), "at rule", true);
            recover(true, false);
            return;
        }

        skip_whitespace();
        try {
            t = next();
            if (t == Token.TK_LBRACE) {
                skip_whitespace();
                Ruleset ruleset = new Ruleset(stylesheet.getOrigin());
                declaration_list(ruleset, false, false, false);
                t = next();
                if (t != Token.TK_RBRACE) {
                    push(t);
                    throw new CSSParseException(t, Token.TK_RBRACE, getCurrentLine());
                }
                pageRule.addMarginBoxProperties(marginBoxName, ruleset.getPropertyDeclarations());
            } else {
                push(t);
                throw new CSSParseException(t, Token.TK_LBRACE, getCurrentLine());
            }
        } catch (CSSParseException e) {
            error(e, "margin box", true);
            recover(false, false);
        }
    }


//  pseudo_page
//    : ':' IDENT
//    ;
    private String pseudo_page() throws IOException {
        //System.out.println("pseudo_page()");
        String result = null;
        Token t = next();
        if (t == Token.TK_COLON) {
            t = next();
            if (t == Token.TK_IDENT) {
                result = getTokenValue(t);
                if (! (result.equals("first") || result.equals("left") || result.equals("right"))) {
                    throw new CSSParseException("Pseudo page must be one of first, left, or right", getCurrentLine());
                }
            } else {
                push(t);
                throw new CSSParseException(t, Token.TK_IDENT, getCurrentLine());
            }
        } else {
            push(t);
            throw new CSSParseException(t, Token.TK_COLON, getCurrentLine());
        }
        return result;
    }
//  operator
//    : '/' S* | COMMA S* | /* empty */
//    ;
    private void operator() throws IOException {
        //System.out.println("operator()");
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
    private Token combinator() throws IOException {
        //System.out.println("combinator()");
        Token t = next();
        if (t == Token.TK_PLUS || t == Token.TK_GREATER) {
            skip_whitespace();
        } else if (t != Token.TK_S) {
            push(t);
            throw new CSSParseException(
                    t,
                    new Token[] { Token.TK_PLUS, Token.TK_GREATER, Token.TK_S },
                    getCurrentLine());
        }
        return t;
    }

//  unary_operator
//    : '-' | PLUS
//    ;
    private int unary_operator() throws IOException {
        //System.out.println("unary_operator()");
        Token t = next();
        if (! (t == Token.TK_MINUS || t == Token.TK_PLUS)) {
            push(t);
            throw new CSSParseException(
                    t, new Token[] { Token.TK_MINUS, Token.TK_PLUS}, getCurrentLine());
        }
        if (t == Token.TK_MINUS) {
            return -1;
        } else { /* t == Token.TK_PLUS */
            return 1;
        }
    }

//  property
//    : IDENT S*
//    ;
    private String property() throws IOException {
        //System.out.println("property()");
        Token t = next();
        String result;
        if (t == Token.TK_IDENT) {
            result = getTokenValue(t);
            skip_whitespace();
        } else {
            push(t);
            throw new CSSParseException(
                    t, Token.TK_IDENT, getCurrentLine());
        }

        return result;
    }

//  declaration_list
//    : [ declaration ';' S* ]*
    private void declaration_list(
            Ruleset ruleset, boolean expectEOF, boolean expectAtRule, boolean inFontFace) throws IOException {
        //System.out.println("declaration_list()");
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
                case Token.AT_RULE:
                    if (expectAtRule) {
                        break LOOP;
                    } else {
                        declaration(ruleset, inFontFace);
                    }
                    // FIXME: intentional fall-thru here?
                case Token.EOF:
                    if (expectEOF) {
                        break LOOP;
                    }
                    // fall through
                default:
                    declaration(ruleset, inFontFace);
            }
        }
    }

//  ruleset
//    : selector [ COMMA S* selector ]*
//      LBRACE S* [ declaration ';' S* ]* '}' S*
//    ;
    private void ruleset(RulesetContainer container) throws IOException {
        //System.out.println("ruleset()");
        try {
            Ruleset ruleset = new Ruleset(container.getOrigin());

            selector(ruleset);
            Token t;
            while (true) {
                t = la();
                if (t == Token.TK_COMMA) {
                    next();
                    skip_whitespace();
                    selector(ruleset);
                } else {
                    break;
                }
            }
            t = next();
            if (t == Token.TK_LBRACE) {
                skip_whitespace();
                declaration_list(ruleset, false, false, false);
                t = next();
                if (t == Token.TK_RBRACE) {
                    skip_whitespace();
                } else {
                    push(t);
                    throw new CSSParseException(t, Token.TK_RBRACE, getCurrentLine());
                }
            } else {
                push(t);
                throw new CSSParseException(
                        t, new Token[] { Token.TK_COMMA, Token.TK_LBRACE }, getCurrentLine());
            }

            if (ruleset.getPropertyDeclarations().size() > 0) {
                container.addContent(ruleset);
            }
        } catch (CSSParseException e) {
            error(e, "ruleset", true);
            recover(true, false);
        }
    }

//  selector
//    : simple_selector [ combinator simple_selector ]*
//    ;
    private void selector(Ruleset ruleset) throws IOException {
        //System.out.println("selector()");
        List selectors = new ArrayList();
        List combinators = new ArrayList();
        selectors.add(simple_selector(ruleset));
        LOOP:
        while (true) {
            Token t = la();
            switch (t.getType()) {
                case Token.PLUS:
                case Token.GREATER:
                case Token.S:
                    combinators.add(combinator());
                    t = la();
                    switch (t.getType()) {
                        case Token.IDENT:
                        case Token.ASTERISK:
                        case Token.HASH:
                        case Token.PERIOD:
                        case Token.LBRACKET:
                        case Token.COLON:
                            selectors.add(simple_selector(ruleset));
                            break;
                        default:
                            throw new CSSParseException(t, new Token[] { Token.TK_IDENT,
                                    Token.TK_ASTERISK, Token.TK_HASH, Token.TK_PERIOD,
                                    Token.TK_LBRACKET, Token.TK_COLON }, getCurrentLine());
                    }
                    break;
                default:
                    break LOOP;
            }
        }
        ruleset.addFSSelector(mergeSimpleSelectors(selectors, combinators));
    }

    private Selector mergeSimpleSelectors(List selectors, List combinators) {
        int count = selectors.size();
        if (count == 1) {
            return (Selector)selectors.get(0);
        }

        int lastDescendantOrChildAxis = Selector.DESCENDANT_AXIS;
        Selector result = null;
        for (int i = 0; i < count - 1; i++) {
            Selector first = (Selector)selectors.get(i);
            Selector second = (Selector)selectors.get(i+1);
            Token combinator = (Token)combinators.get(i);

            if (first.getPseudoElement() != null) {
                throw new CSSParseException(
                        "A simple selector with a pseudo element cannot be " +
                        "combined with another simple selector", getCurrentLine());
            }

            boolean sibling = false;
            if (combinator == Token.TK_S) {
                second.setAxis(Selector.DESCENDANT_AXIS);
                lastDescendantOrChildAxis = Selector.DESCENDANT_AXIS;
            } else if (combinator == Token.TK_GREATER) {
                second.setAxis(Selector.CHILD_AXIS);
                lastDescendantOrChildAxis = Selector.CHILD_AXIS;
            } else if (combinator == Token.TK_PLUS) {
                first.setAxis(Selector.IMMEDIATE_SIBLING_AXIS);
                sibling = true;
            }

            second.setSpecificityB(second.getSpecificityB() + first.getSpecificityB());
            second.setSpecificityC(second.getSpecificityC() + first.getSpecificityC());
            second.setSpecificityD(second.getSpecificityD() + first.getSpecificityD());

            if (! sibling) {
                if (result == null) {
                    result = first;
                }
                first.setChainedSelector(second);
            } else {
                second.setSiblingSelector(first);
                if (result == null || result == first) {
                    result = second;
                }
                if (i > 0) {
                    for (int j = i-1; j >= 0; j--) {
                        Selector selector = (Selector)selectors.get(j);
                        if (selector.getChainedSelector() == first) {
                            selector.setChainedSelector(second);
                            second.setAxis(lastDescendantOrChildAxis);
                            break;
                        }
                    }
                }
            }
        }

        return result;
    }

//  simple_selector
//    : typed_value [ HASH | class | attrib | pseudo ]*
//    | [ HASH | class | attrib | pseudo ]+
//    ;
    private Selector simple_selector(Ruleset ruleset) throws IOException {
        //System.out.println("simple_selector()");
        Selector selector = new Selector();
        selector.setParent(ruleset);
        Token t = la();
        switch (t.getType()) {
            case Token.ASTERISK:
            case Token.IDENT:
            case Token.VERTICAL_BAR:
                NamespacePair pair = typed_value(false);
                selector.setNamespaceURI(pair.getNamespaceURI());
                selector.setName(pair.getName());

                LOOP: while (true) {
                    t = la();
                    switch (t.getType()) {
                        case Token.HASH:
                            t = next();
                            selector.addIDCondition(getTokenValue(t, true));
                            break;
                        case Token.PERIOD:
                            class_selector(selector);
                            break;
                        case Token.LBRACKET:
                            attrib(selector);
                            break;
                        case Token.COLON:
                            pseudo(selector);
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
                            selector.addIDCondition(getTokenValue(t, true));
                            found = true;
                            break;
                        case Token.PERIOD:
                            class_selector(selector);
                            found = true;
                            break;
                        case Token.LBRACKET:
                            attrib(selector);
                            found = true;
                            break;
                        case Token.COLON:
                            pseudo(selector);
                            found = true;
                            break;
                        default:
                            if (!found) {
                                throw new CSSParseException(t, new Token[] { Token.TK_HASH,
                                        Token.TK_PERIOD, Token.TK_LBRACKET, Token.TK_COLON },
                                        getCurrentLine());
                            }
                            break LOOP;
                    }
                }
        }
        return selector;
    }

//    type_selector
//    : [ namespace_prefix ]? element_name | IDENT
//    ;
//    namespace_prefix
//    : [ IDENT | '*' ]? '|'
//    ;
    private NamespacePair typed_value(boolean matchAttribute) throws IOException {
        String prefix = null;
        String name = null;

        Token t = la();
        if (t == Token.TK_ASTERISK || t == Token.TK_IDENT) {
            next();
            if (t == Token.TK_IDENT) {
                name = getTokenValue(t, true);
            }
            t = la();
        } else if (t == Token.TK_VERTICAL_BAR) {
            prefix = TreeResolver.NO_NAMESPACE;
        } else {
            throw new CSSParseException(
                    t, new Token[] { Token.TK_ASTERISK, Token.TK_IDENT, Token.TK_VERTICAL_BAR },
                    getCurrentLine());
        }

        if (t == Token.TK_VERTICAL_BAR) {
            next();
            t = next();
            if (t == Token.TK_ASTERISK || t == Token.TK_IDENT) {
                if (prefix == null) {
                    prefix = name;
                }
                if (t == Token.TK_IDENT) {
                    name = getTokenValue(t, true);
                }
            } else {
                throw new CSSParseException(
                        t, new Token[] { Token.TK_ASTERISK, Token.TK_IDENT }, getCurrentLine());
            }
        }

        String namespaceURI = null;
        if (prefix != null && prefix != TreeResolver.NO_NAMESPACE) {
            namespaceURI = (String)_namespaces.get(prefix.toLowerCase());
            if (namespaceURI == null) {
                throw new CSSParseException("There is no namespace with prefix " + prefix + " defined",
                        getCurrentLine());
            }
        } else if (prefix == null && ! matchAttribute) {
            namespaceURI = (String)_namespaces.get(null);
        }

        if (matchAttribute && name == null) {
            throw new CSSParseException("An attribute name is required", getCurrentLine());
        }

        return new NamespacePair(namespaceURI, name);
    }

//  class
//    : '.' IDENT
//    ;
    private void class_selector(Selector selector) throws IOException {
        //System.out.println("class_selector()");
        Token t = next();
        if (t == Token.TK_PERIOD) {
            t = next();
            if (t == Token.TK_IDENT) {
                selector.addClassCondition(getTokenValue(t, true));
            } else {
                push(t);
                throw new CSSParseException(t, Token.TK_IDENT, getCurrentLine());
            }
        } else {
            push(t);
            throw new CSSParseException(t, Token.TK_PERIOD, getCurrentLine());
        }
    }

//  element_name
//    : IDENT | '*'
//    ;
    /*
    private String element_name() throws IOException {
        //System.out.println("element_name()");
        Token t = next();
        if (t == Token.TK_IDENT || t == Token.TK_ASTERISK) {
            return getTokenValue(t, true);
        } else {
            push(t);
            throw new CSSParseException(
                    t, new Token[] { Token.TK_IDENT, Token.TK_ASTERISK }, getCurrentLine());
        }
    }
    */

//    attrib
//    : '[' S* [ namespace_prefix ]? IDENT S*
//          [ [ PREFIXMATCH |
//              SUFFIXMATCH |
//              SUBSTRINGMATCH |
//              '=' |
//              INCLUDES |
//              DASHMATCH ] S* [ IDENT | STRING ] S*
//          ]? ']'
//    ;
    private void attrib(Selector selector) throws IOException {
        //System.out.println("attrib()");
        Token t = next();
        if (t == Token.TK_LBRACKET) {
            skip_whitespace();
            t = la();
            if (t == Token.TK_IDENT || t == Token.TK_ASTERISK || t == Token.TK_VERTICAL_BAR) {
                boolean existenceMatch = true;
                NamespacePair pair = typed_value(true);
                String attrNamespaceURI = pair.getNamespaceURI();
                String attrName = pair.getName();
                skip_whitespace();
                t = la();
                switch (t.getType()) {
                    case Token.EQUALS:
                    case Token.INCLUDES:
                    case Token.DASHMATCH:
                    case Token.PREFIXMATCH:
                    case Token.SUFFIXMATCH:
                    case Token.SUBSTRINGMATCH:
                        existenceMatch = false;
                        Token selectorType = next();
                        skip_whitespace();
                        t = next();
                        if (t == Token.TK_IDENT || t == Token.TK_STRING) {
                            String value = getTokenValue(t, true);
                            switch (selectorType.getType()) {
                                case Token.EQUALS:
                                    selector.addAttributeEqualsCondition(attrNamespaceURI, attrName, value);
                                    break;
                                case Token.DASHMATCH:
                                    selector.addAttributeMatchesFirstPartCondition(attrNamespaceURI, attrName, value);
                                    break;
                                case Token.INCLUDES:
                                    selector.addAttributeMatchesListCondition(attrNamespaceURI, attrName, value);
                                    break;
                                case Token.PREFIXMATCH:
                                    selector.addAttributePrefixCondition(attrNamespaceURI, attrName, value);
                                    break;
                                case Token.SUFFIXMATCH:
                                    selector.addAttributeSuffixCondition(attrNamespaceURI, attrName, value);
                                    break;
                                case Token.SUBSTRINGMATCH:
                                    selector.addAttributeSubstringCondition(attrNamespaceURI, attrName, value);
                                    break;
                            }
                            skip_whitespace();
                        } else {
                            push(t);
                            throw new CSSParseException(t,
                                    new Token[] { Token.TK_IDENT, Token.TK_STRING },
                                    getCurrentLine());
                        }
                        skip_whitespace();
                        t = la();
                        break;
                }
                if (existenceMatch) {
                    selector.addAttributeExistsCondition(attrNamespaceURI, attrName);
                }
                if (t == Token.TK_RBRACKET) {
                    next();
                } else {
                    throw new CSSParseException(t, new Token[] { Token.TK_EQUALS,
                            Token.TK_INCLUDES, Token.TK_DASHMATCH, Token.TK_PREFIXMATCH,
                            Token.TK_SUFFIXMATCH, Token.TK_SUBSTRINGMATCH, Token.TK_RBRACKET },
                            getCurrentLine());
                }
            } else {
                throw new CSSParseException(
                        t, new Token[] { Token.TK_IDENT, Token.TK_ASTERISK }, getCurrentLine());
            }
        } else {
            push(t);
            throw new CSSParseException(t, Token.TK_LBRACKET, getCurrentLine());
        }
    }

    private void addPseudoClassOrElement(Token t, Selector selector) {
        String value = getTokenValue(t);
        if (value.equals("link")) {
            selector.addLinkCondition();
        } else if (value.equals("visited")) {
            selector.setPseudoClass(Selector.VISITED_PSEUDOCLASS);
        } else if (value.equals("hover")) {
            selector.setPseudoClass(Selector.HOVER_PSEUDOCLASS);
        } else if (value.equals("focus")) {
            selector.setPseudoClass(Selector.FOCUS_PSEUDOCLASS);
        } else if (value.equals("active")) {
            selector.setPseudoClass(Selector.ACTIVE_PSEUDOCLASS);
        } else if (value.equals("first-child")) {
            selector.addFirstChildCondition();
        } else if (value.equals("even")) {
            selector.addEvenChildCondition();
        } else if (value.equals("odd")) {
            selector.addOddChildCondition();
        } else if (value.equals("last-child")) {
            selector.addLastChildCondition();
        } else if (CSS21_PSEUDO_ELEMENTS.contains(value)){
            selector.setPseudoElement(value);
        } else {
            throw new CSSParseException(value + " is not a recognized pseudo-class", getCurrentLine());
        }
    }

    private void addPseudoClassOrElementFunction(Token t, Selector selector) throws IOException {
        String f = getTokenValue(t);
        f = f.substring(0, f.length()-1);

        if (f.equals("lang")) {
            skip_whitespace();
            t = next();
            if (t == Token.TK_IDENT) {
                String lang = getTokenValue(t);
                selector.addLangCondition(lang);
                skip_whitespace();
                t = next();
            } else {
                push(t);
                throw new CSSParseException(t, Token.TK_IDENT, getCurrentLine());
            }
        } else if (f.equals("nth-child")) {
            StringBuilder number = new StringBuilder();
            while ((t = next()) != null && (t == Token.TK_IDENT || t == Token.TK_S || t == Token.TK_NUMBER || t == Token.TK_DIMENSION || t == Token.TK_PLUS || t == Token.TK_MINUS)) {
                number.append(getTokenValue(t));
            }

            try {
                selector.addNthChildCondition(number.toString());
            } catch (CSSParseException e) {
                e.setLine(getCurrentLine());
                push(t);
                throw e;
            }
        } else {
            push(t);
            throw new CSSParseException(f + " is not a valid function in this context", getCurrentLine());
        }

        if (t != Token.TK_RPAREN) {
            push(t);
            throw new CSSParseException(t, Token.TK_RPAREN, getCurrentLine());
        }
    }

    private void addPseudoElement(Token t, Selector selector) {
        String value = getTokenValue(t);
        if (SUPPORTED_PSEUDO_ELEMENTS.contains(value)) {
            selector.setPseudoElement(value);
        } else {
            throw new CSSParseException(value + " is not a recognized psuedo-element", getCurrentLine());
        }
    }

//  pseudo
//    : ':' ':'? [ IDENT | FUNCTION S* IDENT? S* ')' ]
//    ;
    private void pseudo(Selector selector) throws IOException {
        //System.out.println("pseudo()");
        Token t = next();
        if (t == Token.TK_COLON) {
            t = next();
            switch (t.getType()) {
                case Token.COLON:
                    t = next();
                    addPseudoElement(t, selector);
                    break;
                case Token.IDENT:
                    addPseudoClassOrElement(t, selector);
                    break;
                case Token.FUNCTION:
                    addPseudoClassOrElementFunction(t, selector);
                    break;
                default:
                    push(t);
                    throw new CSSParseException(t,
                            new Token[] { Token.TK_IDENT, Token.TK_FUNCTION }, getCurrentLine());
            }
        } else {
            push(t);
            throw new CSSParseException(t, Token.TK_COLON, getCurrentLine());
        }
    }

    private boolean checkCSSName(CSSName cssName, String propertyName) {
        if (cssName == null) {
            _errorHandler.error(
                    _URI,
                    propertyName + " is an unrecognized CSS property at line "
                        + getCurrentLine() + ". Ignoring declaration.");
            return false;
        }

        if (! CSSName.isImplemented(cssName)) {
            _errorHandler.error(
                    _URI,
                    propertyName + " is not implemented at line "
                        + getCurrentLine() + ". Ignoring declaration.");
            return false;
        }

        PropertyBuilder builder = CSSName.getPropertyBuilder(cssName);
        if (builder == null) {
            _errorHandler.error(
                    _URI,
                    "(bug) No property builder defined for " + propertyName
                        + " at line " + getCurrentLine() + ". Ignoring declaration.");
            return false;
        }

        return true;
    }

//  declaration
//    : property ':' S* expr prio?
//    ;
    private void declaration(Ruleset ruleset, boolean inFontFace) throws IOException {
        //System.out.println("declaration()");
        try {
            Token t = la();
            if (t == Token.TK_IDENT) {
                String propertyName = property();
                CSSName cssName = CSSName.getByPropertyName(propertyName);

                boolean valid = checkCSSName(cssName, propertyName);

                t = next();
                if (t == Token.TK_COLON) {
                    skip_whitespace();

                    List values = expr(
                            cssName == CSSName.FONT_FAMILY ||
                            cssName == CSSName.FONT_SHORTHAND ||
                            cssName == CSSName.FS_PDF_FONT_ENCODING);
                    boolean important = false;

                    t = la();
                    if (t == Token.TK_IMPORTANT_SYM) {
                        prio();
                        important = true;
                    }

                    t = la();
                    if (! (t == Token.TK_SEMICOLON || t == Token.TK_RBRACE || t == Token.TK_EOF)) {
                        throw new CSSParseException(
                                t,
                                new Token[] { Token.TK_SEMICOLON, Token.TK_RBRACE },
                                getCurrentLine());
                    }

                    if (valid) {
                        try {
                            PropertyBuilder builder = CSSName.getPropertyBuilder(cssName);
                            ruleset.addAllProperties(builder.buildDeclarations(
                                    cssName, values, ruleset.getOrigin(), important, !inFontFace));
                        } catch (CSSParseException e) {
                            e.setLine(getCurrentLine());
                            error(e, "declaration", true);
                        }
                    }
                } else {
                    push(t);
                    throw new CSSParseException(t, Token.TK_COLON, getCurrentLine());
                }
            } else {
                throw new CSSParseException(t, Token.TK_IDENT, getCurrentLine());
            }
        } catch (CSSParseException e) {
            error(e, "declaration", true);
            recover(false, true);
        }
    }

//  prio
//    : IMPORTANT_SYM S*
//    ;
    private void prio() throws IOException {
        //System.out.println("prio()");
        Token t = next();
        if (t == Token.TK_IMPORTANT_SYM) {
            skip_whitespace();
        } else {
            push(t);
            throw new CSSParseException(t, Token.TK_IMPORTANT_SYM, getCurrentLine());
        }
    }

//  expr
//    : term [ operator term ]*
//    ;
    private List expr(boolean literal) throws IOException {
        //System.out.println("expr()");
        List result = new ArrayList(10);
        result.add(term(literal));
        LOOP: while (true) {
            Token t = la();
            boolean operator = false;
            Token operatorToken = null;
            switch (t.getType()) {
                case Token.VIRGULE:
                case Token.COMMA:
                    operatorToken = t;
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
                case Token.PX:
                case Token.CM:
                case Token.MM:
                case Token.IN:
                case Token.PT:
                case Token.PC:
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
                    PropertyValue term = term(literal);
                    if (operatorToken != null) {
                        term.setOperator(operatorToken);
                    }
                    result.add(term);
                    break;
                default:
                    if (operator) {
                        throw new CSSParseException(t, new Token[] {
                                Token.TK_NUMBER, Token.TK_PLUS, Token.TK_MINUS,
                                Token.TK_PERCENTAGE, Token.TK_PX, Token.TK_EMS, Token.TK_EXS,
                                Token.TK_PC, Token.TK_MM, Token.TK_CM, Token.TK_IN, Token.TK_PT,
                                Token.TK_ANGLE, Token.TK_TIME, Token.TK_FREQ, Token.TK_STRING,
                                Token.TK_IDENT, Token.TK_URI, Token.TK_HASH, Token.TK_FUNCTION },
                                getCurrentLine());
                    } else {
                        break LOOP;
                    }
            }
        }

        return result;
    }

    private String extractNumber(Token t) {
        String token = getTokenValue(t);

        int offset = 0;
        char[] ch = token.toCharArray();
        for (int i = 0; i < ch.length; i++) {
            char c = ch[i];
            if (c < '0' || c > '9') {
                break;
            }
            offset++;
        }
        if (ch[offset] == '.') {
            offset++;

            for (int i = offset; i < ch.length; i++) {
                char c = ch[i];
                if (c < '0' || c > '9') {
                    break;
                }
                offset++;
            }
        }

        return token.substring(0, offset);
    }

    private String extractUnit(Token t) {
        String s = extractNumber(t);
        return getTokenValue(t).substring(s.length());
    }

    private String sign(float sign) {
        return sign == -1.0f ? "-" : "";
    }

//  term
//    : unary_operator?
//      [ NUMBER S* | PERCENTAGE S* | LENGTH S* | EMS S* | EXS S* | ANGLE S* |
//        TIME S* | FREQ S* ]
//    | STRING S* | IDENT S* | URI S* | hexcolor | function
//    ;
    private PropertyValue term(boolean literal) throws IOException {
        //System.out.println("term()");
        float sign = 1;
        Token t = la();
        if (t == Token.TK_PLUS || t == Token.TK_MINUS) {
            sign = unary_operator();
            t = la();
        }
        PropertyValue result = null;
        switch (t.getType()) {
            case Token.ANGLE:
            case Token.TIME:
            case Token.FREQ:
            case Token.DIMENSION:
                throw new CSSParseException("Unsupported CSS unit " + extractUnit(t), getCurrentLine());
            case Token.NUMBER:
                result = new PropertyValue(
                        CSSPrimitiveValue.CSS_NUMBER,
                        sign*Float.parseFloat(getTokenValue(t)),
                        sign(sign) + getTokenValue(t));
                next();
                skip_whitespace();
                break;
            case Token.PERCENTAGE:
                result = new PropertyValue(
                        CSSPrimitiveValue.CSS_PERCENTAGE,
                        sign*Float.parseFloat(extractNumber(t)),
                        sign(sign) + getTokenValue(t));
                next();
                skip_whitespace();
                break;
            case Token.EMS:
                result = new PropertyValue(
                        CSSPrimitiveValue.CSS_EMS,
                        sign*Float.parseFloat(extractNumber(t)),
                        sign(sign) + getTokenValue(t));
                next();
                skip_whitespace();
                break;
            case Token.EXS:
                result = new PropertyValue(
                        CSSPrimitiveValue.CSS_EXS,
                        sign*Float.parseFloat(extractNumber(t)),
                        sign(sign) + getTokenValue(t));
                next();
                skip_whitespace();
                break;
            case Token.PX:
                result = new PropertyValue(
                        CSSPrimitiveValue.CSS_PX,
                        sign*Float.parseFloat(extractNumber(t)),
                        sign(sign) + getTokenValue(t));
                next();
                skip_whitespace();
                break;
            case Token.CM:
                result = new PropertyValue(
                        CSSPrimitiveValue.CSS_CM,
                        sign*Float.parseFloat(extractNumber(t)),
                        sign(sign) + getTokenValue(t));
                next();
                skip_whitespace();
                break;
            case Token.MM:
                result = new PropertyValue(
                        CSSPrimitiveValue.CSS_MM,
                        sign*Float.parseFloat(extractNumber(t)),
                        sign(sign) + getTokenValue(t));
                next();
                skip_whitespace();
                break;
            case Token.IN:
                result = new PropertyValue(
                        CSSPrimitiveValue.CSS_IN,
                        sign*Float.parseFloat(extractNumber(t)),
                        sign(sign) + getTokenValue(t));
                next();
                skip_whitespace();
                break;
            case Token.PT:
                result = new PropertyValue(
                        CSSPrimitiveValue.CSS_PT,
                        sign*Float.parseFloat(extractNumber(t)),
                        sign(sign) + getTokenValue(t));
                next();
                skip_whitespace();
                break;
            case Token.PC:
                result = new PropertyValue(
                        CSSPrimitiveValue.CSS_PC,
                        sign*Float.parseFloat(extractNumber(t)),
                        sign(sign) + getTokenValue(t));
                next();
                skip_whitespace();
                break;
            case Token.STRING:
                String s = getTokenValue(t);
                result = new PropertyValue(
                        CSSPrimitiveValue.CSS_STRING,
                        s,
                        getRawTokenValue());
                next();
                skip_whitespace();
                break;
            case Token.IDENT:
                String value = getTokenValue(t, literal);
                result = new PropertyValue(
                        CSSPrimitiveValue.CSS_IDENT,
                        value,
                        value);
                next();
                skip_whitespace();
                break;
            case Token.URI:
                result = new PropertyValue(
                        CSSPrimitiveValue.CSS_URI,
                        getTokenValue(t),
                        getRawTokenValue());
                next();
                skip_whitespace();
                break;
            case Token.HASH:
                result = hexcolor();
                break;
            case Token.FUNCTION:
                result = function();
                break;
            default:
                throw new CSSParseException(t, new Token[] { Token.TK_NUMBER,
                        Token.TK_PERCENTAGE, Token.TK_PX, Token.TK_EMS, Token.TK_EXS,
                        Token.TK_PC, Token.TK_MM, Token.TK_CM, Token.TK_IN, Token.TK_PT,
                        Token.TK_ANGLE, Token.TK_TIME, Token.TK_FREQ, Token.TK_STRING,
                        Token.TK_IDENT, Token.TK_URI, Token.TK_HASH, Token.TK_FUNCTION },
                        getCurrentLine());
        }
        return result;
    }

//  function
//    : FUNCTION S* expr ')' S*
//    ;
    private PropertyValue function() throws IOException {
        //System.out.println("function()");
        PropertyValue result = null;
        Token t = next();
        if (t == Token.TK_FUNCTION) {
            String f = getTokenValue(t);
            skip_whitespace();
            List params = expr(false);
            t = next();
            if (t != Token.TK_RPAREN) {
                push(t);
                throw new CSSParseException(t, Token.TK_RPAREN, getCurrentLine());
            }

            if (f.equals("rgb(")) {
                result = new PropertyValue(createRGBColorFromFunction(params));
            } else if (f.equals("cmyk(")) {
                if (! isSupportCMYKColors()) {
                    throw new CSSParseException(
                            "The current output device does not support CMYK colors", getCurrentLine());
                }
                //in accordance to http://www.w3.org/TR/css3-gcpm/#cmyk-colors
                result = new PropertyValue(createCMYKColorFromFunction(params));
            } else {
                result = new PropertyValue(new FSFunction(f.substring(0, f.length()-1), params));
            }

            skip_whitespace();
        } else {
            push(t);
            throw new CSSParseException(t, Token.TK_FUNCTION, getCurrentLine());
        }

        return result;
    }

    private FSCMYKColor createCMYKColorFromFunction(List params) {
        if (params.size() != 4) {
            throw new CSSParseException(
                    "The cmyk() function must have exactly four parameters",
                    getCurrentLine());
        }

        float[] colorComponents = new float[4];

        for (int i = 0; i < params.size(); i++) {
            colorComponents[i] = parseCMYKColorComponent((PropertyValue)params.get(i), (i+1)); //Warning on the truncation?
        }

        return new FSCMYKColor(colorComponents[0], colorComponents[1], colorComponents[2], colorComponents[3]);

    }

    private float parseCMYKColorComponent(PropertyValue value, int paramNo) {
        short type = value.getPrimitiveType();
        float result;
        if (type == CSSPrimitiveValue.CSS_NUMBER) {
            result = value.getFloatValue();
        } else if (type == CSSPrimitiveValue.CSS_PERCENTAGE) {
            result = value.getFloatValue() / 100.0f;
        } else {
            throw new CSSParseException(
                    "Parameter " + paramNo + " to the cmyk() function is " +
                    "not a number or a percentage", getCurrentLine());
        }

        if (result < 0.0f || result > 1.0f) {
            throw new CSSParseException(
                    "Parameter " + paramNo + " to the cmyk() function must be between zero and one", getCurrentLine());
        }

        return result;
    }

    private FSRGBColor createRGBColorFromFunction(List params) {
        if (params.size() != 3) {
            throw new CSSParseException(
                    "The rgb() function must have exactly three parameters",
                    getCurrentLine());
        }

        int red = 0;
        int green = 0;
        int blue = 0;
        for (int i = 0; i < params.size(); i++) {
            PropertyValue value = (PropertyValue)params.get(i);
            short type = value.getPrimitiveType();
            if (type != CSSPrimitiveValue.CSS_PERCENTAGE &&
                    type != CSSPrimitiveValue.CSS_NUMBER) {
                throw new CSSParseException(
                        "Parameter " + (i+1) + " to the rgb() function is " +
                        "not a number or percentage", getCurrentLine());
            }

            float f = value.getFloatValue();
            if (type == CSSPrimitiveValue.CSS_PERCENTAGE) {
                f = f/100 * 255;
            }
            if (f < 0) {
                f = 0;
            } else if (f > 255) {
                f = 255;
            }

            switch (i) {
                case 0:
                    red = (int)f;
                    break;
                case 1:
                    green = (int)f;
                    break;
                case 2:
                    blue = (int)f;
                    break;
            }
        }

        return new FSRGBColor(red, green, blue);
    }

//  /*
//  * There is a constraint on the color that it must
//  * have either 3 or 6 hex-digits (i.e., [0-9a-fA-F])
//  * after the "#"; e.g., "#000" is OK, but "#abcd" is not.
//  */
// hexcolor
//   : HASH S*
//   ;
    private PropertyValue hexcolor() throws IOException {
        //System.out.println("hexcolor()");
        PropertyValue result = null;
        Token t = next();
        if (t == Token.TK_HASH) {
            String s = getTokenValue(t);
            if ((s.length() != 3 && s.length() != 6) || ! isHexString(s)) {
                push(t);
                throw new CSSParseException('#' + s + " is not a valid color definition", getCurrentLine());
            }
            FSRGBColor color = null;
            if (s.length() == 3) {
                color = new FSRGBColor(
                            convertToInteger(s.charAt(0), s.charAt(0)),
                            convertToInteger(s.charAt(1), s.charAt(1)),
                            convertToInteger(s.charAt(2), s.charAt(2)));
            } else { /* s.length == 6 */
                color = new FSRGBColor(
                        convertToInteger(s.charAt(0), s.charAt(1)),
                        convertToInteger(s.charAt(2), s.charAt(3)),
                        convertToInteger(s.charAt(4), s.charAt(5)));
            }
            result = new PropertyValue(color);
            skip_whitespace();
        } else {
            push(t);
            throw new CSSParseException(t, Token.TK_HASH, getCurrentLine());
        }

        return result;
    }

    private boolean isHexString(String s) {
        for (int i = 0; i < s.length(); i++) {
            if (! isHexChar(s.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    private int convertToInteger(char hexchar1, char hexchar2) {
        int result = convertToInteger(hexchar1);
        result <<= 4;
        result |= convertToInteger(hexchar2);
        return result;
    }

    private int convertToInteger(char hexchar1) {
        if (hexchar1 >= '0' && hexchar1 <= '9') {
            return hexchar1 - '0';
        } else if (hexchar1 >= 'a' && hexchar1 <= 'f') {
            return hexchar1 - 'a' + 10;
        } else { /* if (hexchar1 >= 'A' && hexchar1 <= 'F') */
            return hexchar1 - 'A' + 10;
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

    private void error(CSSParseException e, String what, boolean rethrowEOF) {
        if (! e.isCallerNotified()) {
            String message = e.getMessage() + " Skipping " + what + ".";
            _errorHandler.error(_URI, message);
        }
        e.setCallerNotified(true);
        if (e.isEOF() && rethrowEOF) {
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

    public void reset(Reader r) {
        _saved = null;
        _namespaces.clear();
        _lexer.yyreset(r);
        _lexer.setyyline(0);
    }

    public CSSErrorHandler getErrorHandler() {
        return _errorHandler;
    }

    public void setErrorHandler(CSSErrorHandler errorHandler) {
        _errorHandler = errorHandler;
    }

    private String getRawTokenValue() {
        return _lexer.yytext();
    }

    private String getTokenValue(Token t) {
        return getTokenValue(t, false);
    }

    private String getTokenValue(Token t, boolean literal) {
        int start;
        int count;
        switch (t.getType()) {
            case Token.STRING:
                count = _lexer.yylength();
                return processEscapes(_lexer.yytext().toCharArray(), 1, count-1);
            case Token.HASH:
                count = _lexer.yylength();
                return processEscapes(_lexer.yytext().toCharArray(), 1, count);
            case Token.URI:
                char[] ch = _lexer.yytext().toCharArray();
                start = 4;
                while (ch[start] == '\t' || ch[start] == '\r' ||
                        ch[start] == '\n' || ch[start] == '\f') {
                    start++;
                }
                if (ch[start] == '\'' || ch[start] == '"') {
                    start++;
                }
                int end = ch.length-2;
                while (ch[end] == '\t' || ch[end] == '\r' ||
                        ch[end] == '\n' || ch[end] == '\f') {
                    end--;
                }
                if (ch[end] == '\'' || ch[end] == '"') {
                    end--;
                }

                String uriResult = processEscapes(ch, start, end+1);

                // Relative URIs are resolved relative to CSS file, not XHTML file
                if (isRelativeURI(uriResult)) {
                    int lastSlash = _URI.lastIndexOf('/');
                    if (lastSlash != -1) {
                        uriResult = _URI.substring(0, lastSlash+1) + uriResult;
                    }
                }

                return uriResult;
            case Token.AT_RULE:
            case Token.IDENT:
            case Token.FUNCTION:
                start = 0;
                count = _lexer.yylength();
                if (t.getType() == Token.AT_RULE) {
                    start++;
                }
                String result = processEscapes(_lexer.yytext().toCharArray(), start, count);
                if (! literal) {
                    result = result.toLowerCase();
                }
                return result;
            default:
                return _lexer.yytext();
        }
    }

    private boolean isRelativeURI(String uri) {
        try {
            return uri.length() > 0 && (uri.charAt(0) != '/' && ! new URI(uri).isAbsolute());
        } catch (URISyntaxException e) {
            return false;
        }
    }

    private int getCurrentLine() {
        return _lexer.yyline();
    }

    private static boolean isHexChar(char c) {
        return (c >= '0' && c <= '9') || (c >= 'A' && c <= 'F') || (c >= 'a' && c <= 'f');
    }

    private static String processEscapes(char[] ch, int start, int end) {
        StringBuffer result = new StringBuffer(ch.length + 10);

        for (int i = start; i < end; i++) {
            char c = ch[i];

            if (c == '\\') {
                // eat escaped newlines and handle te\st == test situations
                if (i < end - 2 && (ch[i+1] == '\r' && ch[i+2] == '\n')) {
                    i += 2;
                    continue;
                } else {
                    if ((ch[i+1] == '\n' || ch[i+1] == '\r' || ch[i+1] == '\f')) {
                        i++;
                        continue;
                    } else if (! isHexChar(ch[i+1])) {
                        continue;
                    }
                }

                // Unicode escapes
                int current = ++i;
                while (i < end && isHexChar(ch[i]) && i - current < 6) {
                    i++;
                }

                int cvalue = Integer.parseInt(new String(ch, current, i - current), 16);
                if (cvalue < 0xFFFF) {
                    result.append((char)cvalue);
                }

                i--;

                if (i < end - 2 && (ch[i+1] == '\r' && ch[i+2] == '\n')) {
                    i += 2;
                } else if (i < end - 1 &&
                        (ch[i+1] == ' ' || ch[i+1] == '\t' ||
                                ch[i+1] == '\n' || ch[i+1] == '\r' ||
                                ch[i+1] == '\f')) {
                    i++;
                }
            } else {
                result.append(c);
            }
        }

        return result.toString();
    }

    public boolean isSupportCMYKColors() {
        return _supportCMYKColors;
    }

    public void setSupportCMYKColors(boolean b) {
        _supportCMYKColors = b;
    }

    private static class NamespacePair {
        private final String _namespaceURI;
        private final String _name;

        public NamespacePair(String namespaceURI, String name) {
            _namespaceURI = namespaceURI;
            _name = name;
        }

        public String getNamespaceURI() {
            return _namespaceURI;
        }

        public String getName() {
            return _name;
        }
    }
}

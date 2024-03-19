# Changelog

## 9.7.1 (under construction) - see https://github.com/flyingsaucerproject/flyingsaucer/milestone/13
* #298 restore public method setDocument(Document) (#299)

## 9.7.0 (released 16.03.2024) - see https://github.com/flyingsaucerproject/flyingsaucer/milestone/12?closed=1
* #291 restore methods with `baseUrl` parameter (#292)
* Bump log4j from 2.23.0 to 2.23.1 (#287)
* [cleanup] cleanup CVS history in comments (#285)
* [cleanup] remove the old archive with ANT build scripts (#286)
* [cleanup] make few methods non-public (#286)
* [cleanup] remove a bunch of unused code (#286)
* [cleanup] remove unused classes (PermutationGenerator, Idents, XLayout, SystemPropertiesUtil, Zipper, Regress) (#286)
* [cleanup] replace "e.printStackTrace()" by a "standard" logging (#288)

## 9.6.1 (released 10.03.2024) - see https://github.com/flyingsaucerproject/flyingsaucer/milestone/11?closed=1
* #276 never consider table cells as "floated" (even if they have style "float: left" or "float:right") (#283)
* refactoring (remove unused methods, convert classes to records etc.) (#281) (#284)
* refactoring: make most of `setDocument()` methods private (#281)
* make toString method of Box elements more easily readable (#283)

## 9.6.0 (released 07.03.2024) - see https://github.com/flyingsaucerproject/flyingsaucer/milestone/10?closed=1
* Require Java 17 or later
* Bump openpdf from 1.3.40 to 2.0.1 (#274) (#278)
* simplify API for creating PDF (#277)
* Added F flag to make links pdf/a-1a conform (#280)

## 9.5.2 (released 26.02.2024) - see https://github.com/flyingsaucerproject/flyingsaucer/milestone/9?closed=1
* Bump openpdf from 1.3.39 to 1.3.40 (#269)
* Bump slf4j from 2.0.11 to 2.0.12 (#268)
* Bump log4j from 2.22.1 to 2.23.0 (#275)

## 9.5.1 (released 22.01.2024) - see https://github.com/flyingsaucerproject/flyingsaucer/milestone/8?closed=1
* Bump OpenPDF from 1.3.38 to 1.3.39 (#261)
* #260 change scope of jsr305 dependency from "provided" to "compile" (#263)

## 9.5.0 (released 21.01.2024) - see https://github.com/flyingsaucerproject/flyingsaucer/milestone/7?closed=1
* Require Java 11 or later (#257)
* Bump OpenPDF from 1.3.35 to 1.3.38 (#256) (#258)
* Bump Slf4j from 2.0.10 to 2.0.11 (#254)

## 9.4.1 (released 07.01.2024) - see https://github.com/flyingsaucerproject/flyingsaucer/milestone/6?closed=1
* Use URLStreamHandler for classpath protocol if available (#250)
* Add debug logs for font resolver (#252)
* Refactoring & cleanup (#253)

## 9.4.0 (released 30.12.2023) - see https://github.com/flyingsaucerproject/flyingsaucer/milestone/5?closed=1
* replace iText 2.x by OpenPDF (#245)  --  thanks to Andreas Rosdal for the initiative
* Added page size B5 (#246)  --  thanks to Mikhail Voronin
* add few overloaded constructors to ITextRenderer (#247)

## 9.3.2 (released 29.12.2023) - see https://github.com/flyingsaucerproject/flyingsaucer/milestone/4?closed=1
* #216 implement loading resources from classpath (#241)
* add nullability annotations (#238) (#240)
* refactoring: make fields final, rename fields, add nullability annotations etc. (#214) (#239)
* refactor exception handling & closing I/O resources (#243)
* upgrade from JUnit 3/4 to JUnit 5 (#244)
* Bump openpdf from 1.3.30 to 1.3.35
* Bump slf4j from 2.0.9 to 2.0.10
* Bump log4j from 2.20.0 to 2.22.1
* Bump bouncycastle from 1.76 to 1.77

## 9.3.1 (released 27.09.2023) - see https://github.com/flyingsaucerproject/flyingsaucer/milestone/3?closed=1
* #205 remove unneeded dependency `org.w3c:dom:2.3.0-jaxb-1.0.6`
* #206 do not expose JSR 302 as transitive dependency
* #207 Update maven plugins
* #212 Bump org.apache.xmlgraphics:fop from 2.2 to 2.9
* #211 Bump com.github.librepdf:openpdf from 1.3.11 to 1.3.30

## 9.3.0 (released 26.09.2023) - see https://github.com/flyingsaucerproject/flyingsaucer/milestone/1?closed=1
* #204 avoid loading CJK fonts by default
* Major refactoring: use Java generics everywhere - see PRs #196 ... #203 
* #195 setup GitHub Actions to build the project
* #173 Make VerifyGlyphExists compile using IntelliJ
* #173 add dependency `org.w3c:dom:2.3.0-jaxb-1.0.6`

## 9.2.2 (released 06.09.2023) - see https://github.com/flyingsaucerproject/flyingsaucer/milestone/2?closed=1
* #194 Remove profile definitions mistakenly reintroduced in merge
* #193 Whitespace clean-up repository-wide
* #192 Bump junit:junit from 4.10 to 4.13.1
* #189 Bump com.itextpdf:itextpdf from 5.3.0 to 5.5.13.3
* #188 Bump org.apache.xmlgraphics:fop from 2.1 to 2.2
* #186 update openpdf dependency to 1.3.30
* #184 Make CJKFont optional

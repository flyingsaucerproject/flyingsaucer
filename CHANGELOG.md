# Changelog

## 9.4.2 (work in progress) - see https://github.com/flyingsaucerproject/flyingsaucer/milestone/7
* ...

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

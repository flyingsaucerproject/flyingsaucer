# Changelog

## 10.0.6 (24.10.2025) - see https://github.com/flyingsaucerproject/flyingsaucer/milestone/44?closed=1
* TBD

## 10.0.5 (24.10.2025) - see https://github.com/flyingsaucerproject/flyingsaucer/milestone/43?closed=1
* #606 fix "Apply parent cell clipping to layers inside paginated tables" (#610)
* #596 fix "Incorrect background color area in table cells" (#608)
* #611 fix NPE when rendering tables (#608)

## 10.0.4 (17.10.2025) - see https://github.com/flyingsaucerproject/flyingsaucer/milestone/42?closed=1
* #596 Fix issue "The cell borders are rendered outside the table border" / by Chanhyuk Lee (#603)
* Minor refactoring of tables (#604)
* refactoring: Generate Lexer.java on every build (#601)

## 10.0.3 (21.10.2025) - see https://github.com/flyingsaucerproject/flyingsaucer/milestone/41?closed=1

* Enable PDF compression and specify PDF creator and producer / by Andreas Røsdal (#586)
* dynamically generate "pdf producer" containing actual FlyingSaucer and OpenPdf versions / by Andrei Solntsev (#588)

## 10.0.2 (19.10.2025) - see https://github.com/flyingsaucerproject/flyingsaucer/milestone/40?closed=1

* #477 Fix location of unordered list item marker for multiline items / by Harald Amon (#584)
* Refactoring / by Andrei Solntsev (#585)

## 10.0.1 (17.10.2025) - see https://github.com/flyingsaucerproject/flyingsaucer/milestone/39?closed=1

* fix: Exclude legacy XML libraries to prevent JPMS conflicts / by Andreas Røsdal (#580)
* cleanup: Remove leftover debug `System.out` write / by Tobias G (#563)
* cleanup: remove most of `System.out` usages / by Andrei Solntsev (#564)
* refactor: make `OutputDevice.drawImage()` parameter generic / by Andrei Solntsev (#576)
* refactor: don't reference subclass `NullImage` from superclass `AWTFSImage` to avoid classloading / by Andrei Solntsev (#579)
* refactor: convert setter to constructor parameter / by Andrei Solntsev (#564)
* dep: Bump SWT from 3.130.0 to 3.131.0 / by dependabot (#562)


## 10.0.0 (05.09.2025) - see https://github.com/flyingsaucerproject/flyingsaucer/milestone/35?closed=1
* Require Java 21 by Andreas Røsdal (#527)
* #554 update to OpenPDF 3.0.0 by Dennis Roppelt (#556) 

## 9.13.3 (24.08.2025) - see https://github.com/flyingsaucerproject/flyingsaucer/milestone/38?closed=1
* #550 fix image URL when it's not relative to CSS url (#558)
* load latest SWT binaries from Maven central repo (#559)

## 9.13.2 (11.08.2025) - see https://github.com/flyingsaucerproject/flyingsaucer/milestone/37?closed=1
* enable anti-aliasing by default

## 9.13.1 (17.07.2025) - see https://github.com/flyingsaucerproject/flyingsaucer/milestone/36?closed=1
* #530 fix `page-break-inside:` - thanks to xlaussel (#536)
* #537 #540 fix size of base64-encoded images (#541)
* #539 restore original design of ITextFSImage (#541)

## 9.13.0 (28.06.2025) - see https://github.com/flyingsaucerproject/flyingsaucer/milestone/34?closed=1
* Support SVG images (#523)
* support "classpath:" URLs with leading slash (#523)
* support "lower-greek" and "upper-greek" list styles (#522)
* bump log4j from 2.24.3 to 2.25.0 (#520)

## 9.12.1 (17.06.2025) - see https://github.com/flyingsaucerproject/flyingsaucer/milestone/33?closed=1
* Disable external entity access in XMLResource - thanks to Andreas Røsdal (#515)
* Adds a page to the root layer if there is none - thanks to @nicolaecismaru (#501)
* Refactoring (#503) (#516) (#517)
* Bump openpdf from 2.0.3 to 2.0.5  (#508) (#511)

## 9.12.0 (17.04.2025) - see https://github.com/flyingsaucerproject/flyingsaucer/milestone/32?closed=1
* Add support for linear gradients and opacity - thanks to Ondřej Španěl and haberbyte (#493) (#22)
* Fix border rounding issues and overlap singularities - thanks to Ondřej Španěl (#492)
* Make mockito a test scoped dependency - thanks to Pim Tegelaar (#497)
* Refactoring (#498)

## 9.11.6 (08.04.2025) - see https://github.com/flyingsaucerproject/flyingsaucer/milestone/31?closed=1
* #486 restore public method `setDocument` with `NamespaceHandler` argument (#488)
* refactoring: removed null checks & simplified code (#487)

## 9.11.5 (29.03.2025) - see https://github.com/flyingsaucerproject/flyingsaucer/milestone/30?closed=1
* #481 Inversion between right and left for the border-radius CSS property (#483) (#484)
* #482 Slightly decrease memory consumption in CSS parser

## 9.11.4 (21.02.2025) - see https://github.com/flyingsaucerproject/flyingsaucer/milestone/29?closed=1
* #471 restore method `setScaleToFit`  --  thanks to Wang Xinli (#472)

## 9.11.3 (29.01.2025) - see https://github.com/flyingsaucerproject/flyingsaucer/milestone/28?closed=1
* Add support for percent-encoded data URLs  --  thanks to Romain Moreau (#457)
* #466 restore class `ToPDF`
* bump PdfBox from 3.0.3 to 3.0.4 (#468)

## 9.11.2 (02.12.2024) - see https://github.com/flyingsaucerproject/flyingsaucer/milestone/27?closed=1
* restore class XHTMLPrintable (#451)

## 9.11.1 (22.11.2024) - see https://github.com/flyingsaucerproject/flyingsaucer/milestone/26?closed=1
* #447 Broken page headers in version 9.11.0 (#450)

## 9.11.0 (12.11.2024) - see https://github.com/flyingsaucerproject/flyingsaucer/milestone/25?closed=1
* Add support for `word-break:break-all` CSS property  --  thanks to Kyle Stephens (#438)
* #440 Fix relative background image (#441)
* Fix PDFAsImage scaling  --  thanks to JamesScrase-PortSwigger (#442)
* refactoring: remove setters and make many classes immutable (#434) (#435) (#443)
 
## 9.10.2 (29.10.2024) - see https://github.com/flyingsaucerproject/flyingsaucer/milestone/24?closed=1
* add overloaded method `addFontDirectory(... String encoding ...)`
* #431 fix interpretation of cell borders (#432)
* #429 fix NoSuchElementException in LayoutContext (#433)

## 9.10.1 (26.10.2024) - see https://github.com/flyingsaucerproject/flyingsaucer/milestone/23?closed=1
* #426 Fix pdf formatting (#428)
* #425 restore method `ITestRenderer.getWriter()` (#427)

## 9.10.0 (24.10.2024) - see https://github.com/flyingsaucerproject/flyingsaucer/milestone/22?closed=1
* #411 fix center li items with margin or padding  --  thanks to Harald Amon (#420)
* #404 Replace obsolete "jsr305" annotation library by JSpecify (#407)
* add @NullMarked, @Nullable and @CheckReturnValue annotations to many methods/classes/packages (#407) (#413) (#414)
* make multiple classes and fields final (#398) (#407) (#419) (#421)
* #409 remove many unused methods (#418) (#421) (#424)
* convert many constants to enums (#416)
* fix multiple warning reported by Google library "error-prone" (#421)
* #403 don't embed slf4j-api in flying-saucer-pdf-osgi (#410)  -  thanks to Konrad Windszus
* #412 Remove unused java sources from folders "src" and "flying-saucer-examples/nomaven" (#417)
* #409 remove unneeded module "flying-saucer-swt-examples" (#418)
* Bump org.apache.xmlgraphics:fop from 2.9 to 2.10 (#408)
* Bump commons-io:commons-io from 2.11.0 to 2.17.0

## 9.9.5 (30.09.2024) - see https://github.com/flyingsaucerproject/flyingsaucer/milestone/21?closed=1
* #392 Fix transparent background of resized base64 encoded images - thanks to @Openhelios (#393)
* dispose Graphics object after using it (#397)
* bump log4j from 2.24.0 to 2.24.1 (#399)

## 9.9.4 (20.09.2024) - see https://github.com/flyingsaucerproject/flyingsaucer/milestone/20?closed=1
* Set "Page only" as default initial view in PDF viewer (#390)
* #379 Revert "Overcoming Table Cell and Line Splitting Challenges (#358)"

## 9.9.3 (16.09.2024) - see https://github.com/flyingsaucerproject/flyingsaucer/milestone/19?closed=1
* #385 avoid using memory-mapped files for reading fonts (#388)
* #385 expose a public method `ITextFontResolver.addFont(BaseFont,...)` (#386)
* support `@page` property `size` with 3 values (e.g. "size: 40mm 20mm landscape") - thanks to @jelinj8 (#383)
* add details to all thrown exceptions  -  see commit 6aa47916
* remove unused classes Handler and DataURLConnection  -  see commit 65cd001b
* Bump org.apache.logging.log4j:log4j-core from 2.23.1 to 2.24.0 (#384)

## 9.9.2 (released 04.09.2024) - see https://github.com/flyingsaucerproject/flyingsaucer/milestone/18?closed=1
* #377 Add automatic module name to MANIFEST.MF files - thanks to @Openhelios (#378)

## 9.9.1 (released 26.08.2024) - see https://github.com/flyingsaucerproject/flyingsaucer/milestone/17?closed=1
* Modify pattern so that headings won't have an upper limitation for their level - thanks to Orosz Péter <peter.orosz@meta-inf.hu> (#359)
* #359 add tests for HTMLOutline
* remove links to iText from LICENSE
* #370 allow not-so-correct image url of form "data:image;base64,iVBORw...." (#371)
* Overcoming Table Cell and Line Splitting Challenges - thanks to Jérôme @syluna (#358)
* Bump com.github.librepdf:openpdf from 2.0.2 to 2.0.3 (#361)

## 9.9.0 (released 19.07.2024) - see https://github.com/flyingsaucerproject/flyingsaucer/milestone/16?closed=1
* #349 Moved `org.xhtmlrenderer.simple.PDFRenderer` to `org.xhtmlrenderer.pdf.PDFRenderer`  --  thanks to
  Aaron Verachtert (#350)
* #349 rename package "org.xhtmlrenderer.simple" in module "flaying-saucer-swt" (#351)
* #340 optimize CPU and memory consumption (#352) (#353)

## 9.8.0 (released 26.05.2024) - see https://github.com/flyingsaucerproject/flyingsaucer/milestone/15?closed=1
* Remove itext 5 support (#325)
* Remove libs (#324)
* disabled logging by default (#304)
* Add a test for the embedding of fonts declared in the css (#318)
* Bump slf4j from 2.0.12 to 2.0.13 (#310)

## 9.7.2 (released 10.04.2024) - see https://github.com/flyingsaucerproject/flyingsaucer/milestone/14?closed=1
* fix center image and glyph list item marker (#302)
* bump OpenPDF from 2.0.1 to 2.0.2 (#307)

## 9.7.1 (released 20.03.2024) - see https://github.com/flyingsaucerproject/flyingsaucer/milestone/13?closed=1
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

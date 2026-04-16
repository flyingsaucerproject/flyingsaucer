# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project

Flying Saucer is a pure-Java CSS 2.1 renderer that turns well-formed XML/XHTML into Java2D, PDF, or SWT output. It is published to Maven Central under `org.xhtmlrenderer:*`. Java **21+** is required (CI also runs on JDK 25).

## Build / test commands

```
mvn install                                     # full multi-module build (also runs tests)
mvn -B package                                  # what CI runs
mvn test                                        # unit tests across all modules
mvn -pl flying-saucer-pdf test                  # tests for one module (use -am to also build deps)
mvn -pl flying-saucer-pdf test -Dtest=ITextRendererTest             # single class
mvn -pl flying-saucer-pdf test -Dtest=ITextRendererTest#renderPdf   # single method
mvn versions:set -DnewVersion=X.Y.Z             # bump version across all poms (release flow)
```

Surefire only picks up tests under `org/**` (see `pom.xml`). JUnit 4 (`junit:junit`) is banned by the enforcer plugin — write tests in **JUnit 5 (Jupiter)** with **AssertJ**; PDF assertions use `com.codeborne:pdf-test`.

## Compilation specifics

- `maven-compiler-plugin` runs **Error Prone** as a `-Xplugin`. A handful of checks are disabled globally (`MissingSummary`, `JdkObsolete`, `ReferenceEquality`, `OperatorPrecedence`) and `Lexer.java` is excluded entirely. If you hit unexpected compile errors, suspect Error Prone before suspecting javac.
- `flying-saucer-core` regenerates `org/xhtmlrenderer/css/parser/Lexer.java` from `Lexer.flex` via the **JFlex** plugin during `process-resources`. **Do not hand-edit `Lexer.java`** — change `Lexer.flex` and rebuild.
- Nullness is annotated with **JSpecify** (`@Nullable`, `@NonNull`); honor those when changing signatures. Indent is 4 spaces (2 for XML), UTF-8 (see `.editorconfig`).

## Module layout

The build is a Maven multi-module reactor; the publishable artifacts are:

- `flying-saucer-core` — layout engine, CSS parser, Java2D output, Swing renderer (`XHTMLPanel`, `Graphics2DRenderer`, `ImageRenderer`).
- `flying-saucer-pdf` — PDF output via OpenPDF (LibrePDF fork of iText 2.x). Entry points: `ITextRenderer`, `PDFRenderer`, `Html2Pdf`. Includes Batik for SVG.
- `flying-saucer-pdf-osgi` — OSGi bundle wrapper around `flying-saucer-pdf`.
- `flying-saucer-swt` — SWT output device.
- `flying-saucer-log4j` — optional Log4j logging adapter.
- `flying-saucer-fop` — Apache FOP-based font/glyph support.
- `flying-saucer-examples` — runnable demos (e.g. `org.xhtmlrenderer.demo.browser.BrowserStartup`); not published.

Everything but the OpenPDF fork is exclusion-managed in the parent `pom.xml`; legacy XML libs (`xml-apis`, `xerces`, `xalan`) are explicitly excluded from Batik to avoid JPMS/`java.xml` conflicts.

## Architecture (core)

`org.xhtmlrenderer` in `flying-saucer-core` is organized as a layered renderer. Key packages:

- `css/` — CSS 2.1 implementation. `parser/` (JFlex-generated lexer + hand-written parser), `sheet/` (stylesheet model), `newmatch/` (selector matching), `style/` (computed style cascade), `constants/`, `value/`.
- `extend/` — **the SPI**. `OutputDevice`, `FontResolver`, `TextRenderer`, `UserAgentCallback`, `ReplacedElementFactory`, `NamespaceHandler`, `FSImage`. Each output backend (Java2D, PDF, SWT) is an implementation of these interfaces — that is the seam for adding a new renderer.
- `layout/` — block/inline layout, line breaking, floats, page breaks. `SharedContext` carries config that lives across the whole document; `LayoutContext` / `RenderingContext` are per-pass. `BoxBuilder` turns the styled DOM into a box tree.
- `render/` — the box tree itself (`Box`, `BlockBox`, `InlineBox`, `LineBox`, `PageBox`, `Layer`) and painting helpers (`BorderPainter`, `TextDecoration`).
- `newtable/` — CSS table layout (separate from generic block layout for historical reasons).
- `swing/` — Java2D `OutputDevice` and the Swing user agent / image loader.
- `simple/` — convenience facades (`XHTMLPanel`, `Graphics2DRenderer`, `ImageRenderer`).
- `context/`, `resource/`, `event/`, `util/`, `debug/` — supporting infrastructure.

PDF output (`flying-saucer-pdf`, package `org.xhtmlrenderer.pdf`) plugs into the same SPI: `ITextOutputDevice`, `ITextFontResolver`, `ITextUserAgent`, `ITextReplacedElementFactory`. `ITextRenderer` is the orchestration entry point.

## CI and dependency policy

- `.github/workflows/maven.yml` runs `mvn -B package` on JDK 21 and 25, then auto-merges Dependabot PRs that pass build (minor versions only, rebase merge).
- Dependabot PRs land frequently — keep version bumps confined to `pom.xml` properties (`<properties>` block in the parent), never inline a version in a child module.

## Release process

See `CONTRIBUTING.md`. Summary: bump version with `mvn versions:set`, commit, tag `vX.Y.Z`, `mvn clean deploy` (signs with GPG and publishes via `central-publishing-maven-plugin` with `autoPublish=true`), then bump to next `-SNAPSHOT`.

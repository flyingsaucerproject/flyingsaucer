# Flying Saucer
[![Maven Central](https://img.shields.io/maven-central/v/org.xhtmlrenderer/flying-saucer-pdf)](https://central.sonatype.com/artifact/org.xhtmlrenderer/flying-saucer-pdf)
[![License: LGPL v2.1](https://img.shields.io/badge/license-LGPL--2.1-blue.svg)](https://www.gnu.org/licenses/lgpl-2.1)
[![CI](https://github.com/flyingsaucerproject/flyingsaucer/actions/workflows/maven.yml/badge.svg)](https://github.com/flyingsaucerproject/flyingsaucer/actions/workflows/maven.yml)

## OVERVIEW

Flying Saucer is a pure-Java library for rendering arbitrary well-formed XML 
(or XHTML) using CSS 2.1 for layout and formatting, output to Swing panels, 
PDF, and images.

Comprehensive documentation available in [The Flying Saucer User's Guide](https://flyingsaucerproject.github.io/flyingsaucer/r8/guide/users-guide-R8.html).

If you use Flying Saucer in a project, please tell us; it helps suggest
directions for the code and may inspire other developers.


## LICENSE

Flying Saucer is distributed under the LGPL.  Flying Saucer itself is licensed 
under the GNU Lesser General Public License, version 2.1 or later, available at
http://www.gnu.org/copyleft/lesser.html. You can use Flying Saucer in any
way and for any purpose you want as long as you respect the terms of the 
license. A copy of the LGPL license is included as `LICENSE-LGPL-2.1.txt` or `LICENSE-LGPL-3.txt`
in our distributions and in our source tree.

Flying Saucer uses a couple of FOSS packages to get the job done. A list
of these, along with the license they each have, is listed in the 
[`LICENSE`](https://github.com/flyingsaucerproject/flyingsaucer/blob/main/LICENSE) file in our distribution.   


## GETTING FLYING SAUCER

New releases of Flying Saucer are distributed through Maven. The available artifacts are:

* `org.xhtmlrenderer:flying-saucer-core` - Core library and Java2D rendering
* `org.xhtmlrenderer:flying-saucer-pdf` - PDF output using OpenPDF (ex. iText 2.x)
* `org.xhtmlrenderer:flying-saucer-pdf-openpdf` - not supported anymore (replaced by `flying-saucer-pdf`)
* `org.xhtmlrenderer:flying-saucer-swt` - SWT output
* `org.xhtmlrenderer:flying-saucer-log4j` - Logging plugin for log4j

Flying Saucer from version 9.5.0, requires Java 11 or later.
Flying Saucer from version 9.6.0, requires Java 17 or later.

## GETTING STARTED

See the sample code under the `flying-saucer-examples` directory.

`flying-saucer-core`, `flying-saucer-pdf`, and `flying-saucer-swt` must be on the
build path, as well as the SWT JAR for your OS.

`org.xhtmlrenderer.demo.browser.BrowserStartup` will start the browser demo.

Other notable entry points include:

* `org.xhtmlrenderer.simple.XHTMLPanel`
* `org.xhtmlrenderer.pdf.PDFRenderer`
* `org.xhtmlrenderer.simple.ImageRenderer`


## CONTACT

See the [discussion group](https://groups.google.com/g/flying-saucer-users) for
ongoing discussions.


## PROJECT STATUS

Excerpt from the latest [merge/release policy](https://groups.google.com/forum/#!topic/flying-saucer-users/ySI8HrLI70A):

> If you create a PR that can merge without conflicts, I'll merge it. I might ask for additional changes (or feedback from others), but I expect that will be extremely rare.
> 
> The basic contract is this: If you're still using Flying Saucer, need something changed, and are willing to do the work yourself, the change will be accepted and released. 
> 
> Along that line, please also let me know if you need a release done right away. Otherwise, I'll bundle up the changes and do a release every four to six months as long as it's required.
 
This is also the reason why Issues are currently deactivated on GitHub.

### HISTORIC LINKS

* [Website](https://github.com/flyingsaucerproject/flyingsaucer)
* [Google Code project page (archived/read-only)](https://code.google.com/archive/p/flying-saucer/)

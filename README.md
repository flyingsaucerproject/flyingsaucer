# Flying Saucer [![Download](https://api.bintray.com/packages/flyingsaucerproject/maven/org.xhtmlrenderer%3Aflying-saucer/images/download.svg)](https://bintray.com/flyingsaucerproject/maven/org.xhtmlrenderer%3Aflying-saucer/_latestVersion) [![License: LGPL v2.1](https://img.shields.io/badge/license-LGPL--2.1-blue.svg)](https://www.gnu.org/licenses/lgpl-2.1)


## OVERVIEW

Flying Saucer is a pure-Java library for rendering arbitrary well-formed XML 
(or XHTML) using CSS 2.1 for layout and formatting, output to Swing panels, 
PDF, and images.

Comprehensive documentation available in our user's guide, linked from our website at https://code.google.com/archive/p/flying-saucer/

For information on our development releases, please contact us on our mailing lists.

If you end up using Flying Saucer for your own projects, please drop us an
email and tell us about it; it helps inform where we go next, and is interesting
and inspiring to other developers.


## LICENSE

Flying Saucer is distributed under the LGPL.  Flying Saucer itself is licensed 
under the GNU Lesser General Public License, version 2.1 or later, available at
http://www.gnu.org/copyleft/lesser.html. You can use Flying Saucer in any
way and for any purpose you want as long as you respect the terms of the 
license. A copy of the LGPL license is included as `LICENSE-LGPL-2.1.txt` or `LICENSE-LGPL-3.txt`
in our distributions and in our source tree.

Flying Saucer uses a couple of FOSS packages to get the job done. A list
of these, along with the license they each have, is listed in the 
[`LICENSE`](https://github.com/flyingsaucerproject/flyingsaucer/blob/master/LICENSE) file in our distribution.   


## GETTING FLYING SAUCER

New releases of Flying Saucer are distributed through Maven.  The available artifacts are:

* `org.xhtmlrenderer:flying-saucer-core` - Core library and Java2D rendering
* `org.xhtmlrenderer:flying-saucer-pdf` - PDF output using iText 2.x
* `org.xhtmlrenderer:flying-saucer-pdf-itext5` - PDF output using iText 5.x
* `org.xhtmlrenderer:flying-saucer-pdf-openpdf` - PDF output using OpenPDF
* `org.xhtmlrenderer:flying-saucer-swt` - SWT output
* `org.xhtmlrenderer:flying-saucer-log4j` - Logging plugin for log4j

iText 2.x has unfixed security bugs. New projects should avoid it.

## GETTING STARTED

There is a large amount of sample code under the `flying-saucer-examples` directory.

`flying-saucer-core`, `flying-saucer-pdf`, and `flying-saucer-swt` must be on the
build path, as well as the SWT JAR for your OS.

`org.xhtmlrenderer.demo.browser.BrowserStartup` will start the browser demo.

Some good entry points (classes) are:
* `org.xhtmlrenderer.simple.XHTMLPanel`
* `org.xhtmlrenderer.simple.PDFRenderer`
* `org.xhtmlrenderer.simple.ImageRenderer`


## STATE OF THE PROJECT

Ongoing discussions are carried out in [our online discussion group](https://groups.google.com/forum/#!forum/flying-saucer-users).

Excerpt from the latest [merge/release policy](https://groups.google.com/forum/#!topic/flying-saucer-users/ySI8HrLI70A):

> If you create a PR that can merge without conflicts, I'll merge it. I might ask for additional changes (or feedback from others), but I expect that will be extremely rare.
> 
> The basic contract is this: If you're still using Flying Saucer, need something changed, and are willing to do the work yourself, the change will be accepted and released. 
> 
> Along that line, please also let me know if you need a release done right away. Otherwise I'll bundle up the changes and do a release every four to six months as long as it's required.
 
This is also the reason why Issues are currently deactivated on GitHub.

### HISTORIC LINKS
* [Google Code project page (archived/read-only)](https://code.google.com/archive/p/flying-saucer/)
* [Google Code Issues (archived/read-only)](https://code.google.com/archive/p/flying-saucer/issues)

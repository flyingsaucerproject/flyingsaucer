Flying Saucer
=============

https://code.google.com/archive/p/flying-saucer/

Please see project website for links to git, mailing lists, issue tracker, etc.


OVERVIEW
--------
Flying Saucer is a pure-Java library for rendering arbitrary well-formed XML 
(or XHTML) using CSS 2.1 for layout and formatting, output to Swing panels, 
PDF, and images.

Comprehensive documentation available in our user's guide, linked from our website at https://code.google.com/archive/p/flying-saucer/

For information on our development releases, please contact us on our mailing lists.

If you end up using Flying Saucer for your own projects, please drop us an
email and tell us about it; it helps inform where we go next, and is interesting
and inspiring to other developers.


LICENSE
-------
Flying Saucer is distributed under the LGPL.  Flying Saucer itself is licensed 
under the GNU Lesser General Public License, version 2.1 or later, available at
http://www.gnu.org/copyleft/lesser.html. You can use Flying Saucer in any
way and for any purpose you want as long as you respect the terms of the 
license. A copy of the LGPL license is included as `LICENSE-LGPL-2.1.txt` or `LICENSE-LGPL-3.txt`
in our distributions and in our source tree.

Flying Saucer uses a couple of FOSS packages to get the job done. A list
of these, along with the license they each have, is listed in the 
[`LICENSE`](https://github.com/flyingsaucerproject/flyingsaucer/blob/master/LICENSE) file in our distribution.   

GETTING FLYING SAUCER
---------------------
New releases of Flying Saucer are distributed through Maven.  The available artifacts are:

* `org.xhtmlrenderer:flying-saucer-core` - Core library and Java2D rendering
* `org.xhtmlrenderer:flying-saucer-pdf` - PDF output using iText or OpenPDF
* `org.xhtmlrenderer:flying-saucer-swt` - SWT output
* `org.xhtmlrenderer:flying-saucer-log4j` - Logging plugin for log4j

Build with openpdf Maven profile to use OpenPDF.

GETTING STARTED
---------------
There is a large amount of sample code under the `flying-saucer-examples` directory.

`flying-saucer-core`, `flying-saucer-pdf`, and `flying-saucer-swt` must be on the
build path, as well as the SWT JAR for your OS.

`org.xhtmlrenderer.demo.browser.BrowserStartup` will start the browser demo.

Some good entry points (classes) are:
* `org.xhtmlrenderer.simple.XHTMLPanel`
* `org.xhtmlrenderer.simple.PDFRenderer`
* `org.xhtmlrenderer.simple.ImageRenderer`



Flying Saucer
Release R8
April 18, 2009

https://xhtmlrenderer.dev.java.net
Please see project website for links to CVS, mailing lists, issue tracker, etc.


OVERVIEW
--------
Flying Saucer is a pure-Java library for rendering arbitrary well-formed XML 
(or XHTML) using CSS 2.1 for layout and formatting, output to Swing panels, 
PDF, and images.

Comprehensive documentation available in our user's guide, linked from our website at http://xhtmlrenderer.dev.java.net.
For information on our development releases (R8preX), please contact us on our mailing lists.

If you end up using Flying Saucer for your own projects, please drop us an
email and tell us about it; it helps inform where we go next, and is interesting
and inspiring to other developers.


LICENSE
-------
Flying Saucer is distributed under the LGPL.Flying Saucer itself is licensed 
under the GNU Lesser General Public License, version 2.1 or later, available at
http://www.gnu.org/copyleft/lesser.html. You can use Flying Saucer in any
way and for any purpose you want as long as you respect the terms of the 
license. A copy of the LGPL license is included as license-lgpl-2.1.txt
in our distributions and in our source tree.

Flying Saucer uses a couple of FOSS packages to get the job done. A list
of these, along with the license they each have, is listed in the 
LICENSE file in our distribution.   


WHAT'S IN THE DISTRIBUTION
--------------------------

    "BINARY" DISTRIBUTION
    ---------------------
    The binary distribution includes only those files you need to *use* Fying Saucer
    in your own programs. The files are all Java JAR files and just need to be included
    in the CLASSPATH as described in the User's Guide.

    * core-renderer.jar: the main file, always in the classpath
    * itext*.jar: support for generating PDF files as output

    * core-renderer-minimal.jar: see note at end of this section

    In principle, for an application using only Swing JPanels for output, you could
    restrict yourself to using core-renderer.jar; however, we recommend you include
    core*, itext* and to avoid any linking errors/ClassNotFoundExceptions. When
    in doubt, contact us via mail.


    "SOURCE" DISTRIBUTION
    ---------------------
    The source distribution includes all the relevant source code, libraries and build
    scripts to build and extend Flying Saucer. You'll need Ant if you want to use our
    build scripts. There are some files under source control (notes, some docs) which
    are not included in the source distribution. You can pull the whole lot from our source
    tree by visiting our project website (URL at top of this doc) and extracting from
    source control.




GETTING STARTED
---------------
Please see the user's guide (URLs listed above) for info on how to get started.
At a minimum, you'll need core-renderer.jar in your classpath; itext*.jar 
for PDF output.
Some good entry points (classes) are:
org.xhtmlrenderer.simple.XHTMLPanel
org.xhtmlrenderer.simple.PDFRenderer
org.xhtmlrenderer.simple.ImageRenderer



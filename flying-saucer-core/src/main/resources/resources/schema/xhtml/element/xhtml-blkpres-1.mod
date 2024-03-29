<!-- ...................................................................... -->
<!-- XHTML Block Presentation Module  ..................................... -->
<!-- file: xhtml-blkpres-1.mod

     This is XHTML, a reformulation of HTML as a modular XML application.
     Copyright 1998-2001 W3C (MIT, INRIA, Keio), All Rights Reserved.

     This DTD module is identified by the PUBLIC and SYSTEM identifiers:

       PUBLIC "-//W3C//ELEMENTS XHTML Block Presentation 1.0//EN"
       SYSTEM "http://www.w3.org/TR/xhtml-modularization/DTD/xhtml-blkpres-1.mod"

     Revisions:
     (none)
     ....................................................................... -->

<!-- Block Presentational Elements

        hr

     This module declares the elements and their attributes used to
     support block-level presentational markup.
-->

<!ENTITY % hr.element  "INCLUDE" >
<![%hr.element;[
<!ENTITY % hr.content  "EMPTY" >
<!ENTITY % hr.qname  "hr" >
<!ELEMENT %hr.qname;  %hr.content; >
<!-- end of hr.element -->]]>

<!ENTITY % hr.attlist  "INCLUDE" >
<![%hr.attlist;[
<!ATTLIST %hr.qname;
      %Common.attrib;
>
<!-- end of hr.attlist -->]]>

<!-- end of xhtml-blkpres-1.mod -->

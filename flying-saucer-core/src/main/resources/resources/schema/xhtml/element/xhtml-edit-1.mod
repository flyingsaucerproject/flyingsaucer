<!-- ...................................................................... -->
<!-- XHTML Editing Elements Module  ....................................... -->
<!-- file: xhtml-edit-1.mod

     This is XHTML, a reformulation of HTML as a modular XML application.
     Copyright 1998-2001 W3C (MIT, INRIA, Keio), All Rights Reserved.

     This DTD module is identified by the PUBLIC and SYSTEM identifiers:

       PUBLIC "-//W3C//ELEMENTS XHTML Editing Markup 1.0//EN"
       SYSTEM "http://www.w3.org/TR/xhtml-modularization/DTD/xhtml-edit-1.mod"

     Revisions:
     (none)
     ....................................................................... -->

<!-- Editing Elements

        ins, del

     This module declares element types and attributes used to indicate
     inserted and deleted content while editing a document.
-->

<!-- ins: Inserted Text  ............................... -->

<!ENTITY % ins.element  "INCLUDE" >
<![%ins.element;[
<!ENTITY % ins.content
     "( #PCDATA | %Flow.mix; )*"
>
<!ENTITY % ins.qname  "ins" >
<!ELEMENT %ins.qname;  %ins.content; >
<!-- end of ins.element -->]]>

<!ENTITY % ins.attlist  "INCLUDE" >
<![%ins.attlist;[
<!ATTLIST %ins.qname;
      %Common.attrib;
      cite         %URI.datatype;           #IMPLIED
      datetime     %Datetime.datatype;      #IMPLIED
>
<!-- end of ins.attlist -->]]>

<!-- del: Deleted Text  ................................ -->

<!ENTITY % del.element  "INCLUDE" >
<![%del.element;[
<!ENTITY % del.content
     "( #PCDATA | %Flow.mix; )*"
>
<!ENTITY % del.qname  "del" >
<!ELEMENT %del.qname;  %del.content; >
<!-- end of del.element -->]]>

<!ENTITY % del.attlist  "INCLUDE" >
<![%del.attlist;[
<!ATTLIST %del.qname;
      %Common.attrib;
      cite         %URI.datatype;           #IMPLIED
      datetime     %Datetime.datatype;      #IMPLIED
>
<!-- end of del.attlist -->]]>

<!-- end of xhtml-edit-1.mod -->

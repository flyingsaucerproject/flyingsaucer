HTMLRenderer Version 0.03


This is a demonstration of the 100% Java HTML Renderer
written by Joshua Marinacci (joshy@joshy.org)

To start just double click on the icon for htmlrender.jar
or type this on the command line:

    java -jar htmlrender.jar
    
Select a test from the 'Test' menu to see different features.

This renderer does not support general HTML from the web.
It only supports XHTML Strict with CSS1/2 for styling.

It currently supports most basic html contstructs, including tables, lists, and
images. It supports normal, relative, floated, and a little absolute and fixed
positioning. Almost any form of DIV positioning is possible. Most textual CSS is
supported (font sizes, underlines, sub & super, text transformations, horizontal
and vertical alignment. see the 'general styled text', 'line breaking', and
'text alignment' for examples). There is mostly complete support for the box
model (padding, margins, border with styles).


One big thing it's missing is tables with cells that span rows and columns.

The code is pretty damn slow, but it will hopefully get better. I'm looking
for people to start using it and come up with crazy new ways to use it. Anyone
writing an rss reader, chat client or music store?  I'd also like to see
someone write a JTidy wrapper and webbrowser out of it.

Please send feedback to joshy@joshy.org



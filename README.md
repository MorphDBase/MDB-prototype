# Morph·D·Base: eScience-Compliant Standards for Morphology - the MDB Prototype
(version 0.2 beta)

The eScience-Compliant Standards for Morphology project is part of the ongoing projects of the online morphological 
data repository Morph·D·Base.

The project is presently primarily maintained on funds of the LIS call Standards for Indexing and/or Digitisation of 
Object Classes in Scientific Collections of the DFG through the Rheinische Friedrich-Wilhelms-Universität Bonn 
(programming and management), the Foundation Zoological Research Museum Alexander Koenig (ZFMK) 
(programming, management and use case), the Max Planck Institute of Colloids and Interfaces (use case), the Museum 
für Naturkunde Berlin (MfN) (use case), and the Universität Rostock (use case).

Further information on the project is available at http://escience.biowikifarm.net - feel free to contact us at 
dev@morphdbase.de

This repository contains the code to set up a local copy of the Morph·D·Base. It consists of

- the application ontologies
- the Java middleware to interprete the ontologies and to provide communication via a websocket between the ontologies and the frontend
- the AngularJS based frontend providing the user interface

**Please be aware that this project is ongoing research! This code is for demonstration purposes only. Do not use
 in production environment!**
  

## Server requirements:

- **Ontologies**:
  - [Protégé](https://protege.stanford.edu/) to explore the ontologies (not necessary to run system)
- **Middleware**: 
  - [Java 8](https://www.java.com/de/download/faq/java8.xml)
  - a Servlet/JSP container (e.g. [Jetty](https://www.eclipse.org/jetty/))
- **Frontend**: [MEAN stack](https://linuxacademy.com/howtoguides/posts/show/topic/11960-how-to-install-mean-on-ubuntu-1604): 
    - **M**ongoDB, a NoSQL database
    - **E**xpress.js, a web application framework that runs on Node.js
    - **A**ngular.js, a JavaScript MVC framework that runs in browser JavaScript engines
    - **N**ode.js, an execution environment for event-driven server-side and networking applications

Any platform with an up-to-date Java and MEAN stack is fine. We mainly used Ubuntu 14.04 during development. 
All installing instructions for 3rd party software refers to this platform. We recommend using a web server
like nginx or apache to serve the Angular application in productive mode.

## End user requirements
End users access the application using their web browser. To use all features a current browser is recommended: 

  * Chrome 62 or newer
  * Firefox 57 or newer
  * Safari 10 or newer
  * Android 4.5 or newer
  * iOS 9.3 or newer
  * Microsoft Edge
  * Internet Explorer 11 (deprecated, not 100% supported for some CSS3)

## Installation

See the READMEs of the individual packages for details. 

## Change log and further reading

  * [development report](http://escience.biowikifarm.net/wiki/Development_Report)
  * [publications](http://escience.biowikifarm.net/wiki/Publications)
  * [Wiki](http://escience.biowikifarm.net)

    

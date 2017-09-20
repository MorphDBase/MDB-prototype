# Morph·D·Base: eScience-Compliant Standards for Morphology - the MDB Prototype
(version 0.1 beta)

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

- **Middleware**: Java 8
- **Frontend**: MEAN stack: 
    - **M**ongoDB, a NoSQL database
    - **E**xpress.js, a web application framework that runs on Node.js
    - **A**ngular.js, a JavaScript MVC framework that runs in browser JavaScript engines
    - **N**ode.js, an execution environment for event-driven server-side and networking applications

Any platform with an up-to-date Java and MEAN stack is fine. We mainly used Ubuntu 14.04 during development. 
All installing instructions for 3rd party software refers to this plattform. We recommend using a web server
like nginx or apache to serve the Angular application in productive mode.

## End user requirements
End users access the application using their web browser. To use all features a current browser is recommended: 

  * Chrome 51 or newer
  * Firefox 47 or newer
  * Safari 9.1 or newer
  * Android 4.5 or newer
  * iOS 8.4 or newer (9.3 or newer recommended)
  * Microsoft Edge
  * Internet Explorer 11 (not 100% supported for some CSS3)

## Installation

See the READMEs of the individual packages for details. 

    

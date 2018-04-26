# Application ontologies of SOCCOMAS

This set of application ontologies is a central component of the **semantic ontology-controlled application for Web 
Content Management Systems** (SOCCOMAS). It is used for semantic programming of domain-independent eScience-compliant 
semantic web content management systems (S‑WCMS). The application ontologies define classes, individuals and 
properties, which are interpreted by the corresponding SOCCOMAS middleware as basic commands, subcommands and 
variables. The commands and variables serve as an ontology-based language for describing the GUI, data 
representations (i.e., composition and specification of HTML elements of all data entries in a S‑WCMS, including 
their functionality and input restrictions and logic), user interactions, basic programming-logic, and all workflow 
processes of a S‑WCMS. The application ontologies also contain a set of basic descriptions that describe various 
workflows, database processes, data views and input forms, which at their turn control each S‑WCMS run by SOCCOMAS.
The middleware dynamically generates the programming code of a S‑WCMS based on the descriptions. Like a 
Lego-system, we use the set of commands and variables already known to the programming code to describe and specify 
all relevant features of a S‑WCMS, thereby producing declarative specifications of the S‑WCMS that the middleware 
interprets and dynamically produces. In other words, based on the declarative specifications contained in the 
application ontology, the specification executes directly. By describing the S‑WCMS at the application-ontologies-level 
one implicitly writes the programming code of the S‑WCMS. 

The basic commands and subcommands of the application ontologies are defined as annotation properties, whereas values 
and variable-carrying resources are individuals. Relations between resources can be described using specific object 
properties and values using data properties. The descriptions themselves are added in the form of annotations of 
ontology classes and individuals. Each annotation consists of a basic command followed by some value, index or 
resource and can be further complemented by axiom annotations that contain subcommands, values and variables. In case 
of individuals, the descriptions may be complemented by property assertions.

The descriptions cover various features of a scientific S‑WCMS and have been adapted for our main use case, 
[Morph∙D∙Base](https://proto.morphdbase.de). However, they can be modified, extended and adapted to a S‑WCMS that 
meets the individual needs of any organization or any specific project. They describe user administration, covering 
the description of signup and login forms, user registration and login processes, as well as session management and the 
description of the template form of a user entry. They also describe the general organization and structure of the 
underlying Jena tuple store (https://jena.apache.org/) into five different workspaces, each of which is a specific 
directory in the tuple store. The descriptions also cover all life-cycle processes of a data entry, including creating 
a new data entry with a current draft version, saving a current draft version, restoring a saved draft version as the 
new current draft version, moving the current draft version to the recycle bin, recycling a recycle bin version to 
become the current draft version, deleting a recycle bin version or a saved draft version, publishing the current draft 
version, and starting a revision of the current published version, which results in creating a copy of this version to 
become the new current draft version. The descriptions also describe all HTML templates for input forms used in the 
S‑WCMS, including all the input forms and data views for the various types of data entries of the S‑WCMS. These 
descriptions also cover the specification of the input control and overall behavior of each input field, including the 
underlying data scheme that specifies how user input triggers the generation of data-scheme-compliant triple statements 
and where these triple statements must be saved in the Jena tuple store in terms of named graphs and workspaces. 
Moreover, the descriptions cover the specification of an access rights system that enables collaborative editing of the 
current draft version of a given data entry. Last, but not least, they describe automatic tracking procedures, in 
which 
(i) user contributions to any given data entry are tracked for both the entry and the user, 
(ii) the overall provenance of a data entry is tracked, and 
(iii) the change-history is being logged for each editing step a user conducts for any given entry version.

## Further information
More information and contact details available at our 
[Wiki page](http://escience.biowikifarm.net/wiki/SOCCOMAS:_an_application_for_semantic_ontology-controlled_Web-Content-Management-Systems).
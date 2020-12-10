# Model-railway
My Experiments to automate a model railway.

The program is trying to be a dashboard for a Mearklin CS2/3 as I find the screen a bit tiny.
So I am developing screens for:
* a Layout overview
* Controlling locomotives
* Turnout and Signals overview
* Diagnostic screen to see what is happening on the track and the communication between the CS2/3 and the PC.

Currently the following feature are implemented:
* Automatic discovery of the CS2/3 ip address
* React on CS2/2 events (Currently start/stop) 
* read the Locomotives from the CS2/3
* read the Accessories from from the CS2/3
* Show turnouts and signals on different screens
* Can create a layout and display the layout
* Control Locomotives
* Show some early diagnostics
* Deployment to an App (first added is MacOS as that is my main development platform)


All configuration and layout data is stored in an embedded H2 database.
The database files are placed in the $HOME/jcs directory.
When the database does not exist it is created on startup. 

TODO
Read out of more of the (inner) components of the CS2/3
Configuration screens to edit the Locomotives, Accessories and Sensors.
Readout of sensors when connected to CS2/3
Support for more then 5 functions for locomotives
Write documentation
Add deployment configuration for Windows and Linux

This project started as (and still is) hobby to automate my model rail layout.

So I hope you get inspired!

Frans

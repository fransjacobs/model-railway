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
* React on CS2/2 events (Currently start/stop only) 
* read the Locomotives from the CS2/3 (using the http connection, so only after a clean reboot of the CS new locomotice can be found)
* read the Accessories from from the CS2/3 (using the http connection, so only after a clean reboot of the CS new locomotice can be found)
* Control Locomotives , basically you can command a loco so that it moves forwards/backwards, turn the lights on/off and the functions 1 to 4
* Show some early diagnostics (display of all sensors grouped by modules, send accessory address 1 to 255)
+/- Can create a layout and display the layout, very basic editing
-/+ Deployment to an App (first added is MacOS as that is my main development platform) [Postponed this a while, but basics where done]

All configuration and layout data is stored in an embedded H2 database.
The database files are placed in the $HOME/jcs directory.
When the database does not exist it is created on startup. 

Last period I am more concentrating of the Layout track functionality.
The basic thought I have in my mind is that once a track is drawn, the program should be able to create routes.
Than the program should be able to control the trains.
So the first attempt now in this branch is to create a (nicer) layout screen.
To route the track.

TODO (and ther are a lots of todo's...)
Read out of more of the (inner) components of the CS2/3
Configuration screens to edit the Locomotives, Accessories and Sensors.
Readout of sensors when connected to CS2/3
Readout sensors when they are connected to the Link 88 module
Support for more then 5 functions for locomotives

Write documentation
Add deployment configuration for Windows and Linux

This project started as (and still is) hobby to automate my model rail layout.
As I am trying to do this project beside my work, family and other hobbies it is a project with very slow pace sometimes...

So I hope you get inspired!

Frans

# Model-railway
My Experiments to automate a model railway.

The aim of the program is dashboard for a Marklin CS3 as I find the screen a bit tiny.
So I am developing screens and functionality for:
* a Layout overview
* Controlling locomotives
* Turnout and Signals overview
* Diagnostic screen to see what is happening on the track and the communication between the CS3 and the PC.
* Automatic control of the running trains

So here is a first screenshot

![UI screenshot: locomotive control](assets/mainscreen.png?raw=true)

All configuration and layout data is stored in an embedded H2 database.
The database files are placed in the $HOME/jcs directory.
When the database does not exist it is created on startup. 

Last period I am more concentrating of the Layout track functionality.
The basic thought I have in my mind is that once a track is drawn, the program should be able to create routes.
Than the program should be able to control the trains.
So the first attempt now in this branch is to create a (nicer) layout screen.
To route the track.

Currently the following features are more or less implemented:
 (I started with a CS2 but it died unfortunatly, so bought a CS3. Some thinks are a bit diffrent).
* Adopt to JDK 17
* Automatic discovery of the CS3 ip address
* React on CS3 events (Currently start/stop only) 
* read the Locomotives from the CS3.
* Get the locomotive images from the CS3 
* read the Accessories from the CS3

TODO (and there are a lots of todo's...):
+/- Control Locomotives (was was working, but neede to redesign the UI to support 32 functions (Work in progress)
+/- due to redesign of UI the control of the accsoires is not functional
+/- Can create a layout and display the layout, very basic editing

Read out of more of the (inner) components of the CS3
Configuration screens to edit the Locomotives, Accessories and Sensors.
Readout of sensors when connected to CS3
Readout sensors when they are connected to the Link 88 module

Write documentation
Add deployment configuration for Windows and Linux and to an App (first added is MacOS as that is my main development platform) [Postponed this a while, but basics where done]

This project started as (and still is) hobby to automate my model rail layout.
As I am trying to do this project beside my work, family and other hobbies it is a project with very slow pace sometimes...

So I hope you get inspired!

Frans


**Copyright 2019- 2022 Frans Jacobs**

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"),
to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense,
and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

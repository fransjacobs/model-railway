# Model-railway

#Modelrailway control with JCS (Java Central Station)

My Experiments to automate a model railway.

The aim of the program is dashboard for a Marklin CS3 as I find the screen a bit tiny.
So I am developing screens and functionality for:
* a Layout overview
* Controlling locomotives
* Turnout and Signals overview
* Sensor overview
* Diagnostic screen to see what is happening on the track and the communication between the CS3 and the PC.
* Automatic control of the running trains

So here is a first screenshot

![UI screenshot: locomotive control](assets/mainscreen.png?raw=true)

Currently the following features are more or less implemented:
 (I started with a CS2 but it died unfortunatly, so bought a CS3. Some thinks are a bit diffrent).
* Adopt to JDK 17
* Automatic discovery of the CS3 ip address
* React on CS3 events (Currently start/stop only) 
* Read the Locomotives from the CS3.
* Get the locomotive images from the CS3 
* Read the Accessories from the CS3

Most of theconfiguration and layout data is stored in an embedded H2 database.
The database files are placed in the $HOME/jcs directory.
When the database does not exist it is created on startup. 

TODO (and there are still a lots of todo's...):
* Replace the JDBC calls for a tyny ORM library so that less boilerplate is needed, as my time is sometimes limited ;)
* Control Locomotives (was was working, but needed to redesign the UI to support 32 functions (Work in progress)
* Read out of more of the (inner) components of the CS3
* Configuration screens to edit the Locomotives, Accessories and Sensors.
* Automatic driving of the trains
* Add deployment configuration for Windows and Linux and to an App (first added is MacOS as that is my main development platform) [Postponed this a while, but basics where done]

This project started in 2019 as (and still is) hobby to automate my model-rail-layout.
As I am trying to do this project beside my work, family and other hobbies it is a project with very slow pace sometimes...


So I hope you get inspired!

Frans


Concepts

Train
Combination of a locomotive with or without wagons.
A train can move on the track. When it is moving it has a certain speed.
A train has a length.

Section or block
A part of the track plan in which there can be exactly zero or one (1) train.
If there is a train in the section, the section is occupied.
A section has a length that is longer than the train length.
A section is delimited by a switch or a signal.
A section has at least 1 detection point.
A section can have a mandatory direction of travel.

Section or block statuses:
Free, there is no train in the section. A train may run into it, all sensors indicate “free”.
Occupied, there is a train inside the section. The busy sensor indicates “occupied”. 
There was already a train in the section or a train is entering the section and should stop.
Entering, a train has entered the section. The entry sensor indicates "occupied".
Exiting, a train is exiting the section. The exit sensor indicates "occupied".
Braking, a train is entering the section and must slow down. The brake sensor indicates "occupied".
Reserved, a train is on its way to the track section. All sensors still indicate "free".
Out of use, no trains can use the track section. Sensor readings are ignored.

Section or block detection:
1 sensor: occupied or free
2 sensors: incoming and busy
3 sensors: incoming, occupied and exiting
4 sensors: incoming, braking, occupied and exiting.

Driveway
A driveway is the connection between two adjacent sections. There may be a switch or a signal between the sections.
In the case of a turnout, the turnout has a direction, straight ahead or turning.

Location
A location is a collection of sections that represent a specific location, for example a station.
A station can consist of several sections.

Route
A route is a collection of driveways between 2 locations.





**Copyright 2019 - 2023 Frans Jacobs**

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"),
to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense,
and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

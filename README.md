#Model-railway

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
* Adopted to JDK 17
* Included automatic discovery of the CS3 ip address
* React on CS3 events (Currently start/stop only, Feedback events (S88 are visible) 
* Read the Locomotives from the CS3.
* Get the locomotive images from the CS3 
* Get the locomotive functions images from the CS3 
* Read the Accessories from the CS3

Most of the configuration and layout data is stored in an embedded (H2) database.
The database files are placed in the $HOME/jcs directory.
When the database does not exist it is created on startup. 

#TODO's (and there are still a lots of todo's...):
* Replace the JDBC calls for a tiny ORM library (norm; https://github.com/dieselpoint/norm ).
* Redesign the Locomotive(s) control so tha it can support 32 functions
* Configuration screens to edit the Locomotives, Accessories and Sensors.
* Automatic driving of the trains
* Add deployment configuration for Windows and Linux and to an App
  (first added is MacOS as that is my main development platform) [Postponed this a while, but basics where done]

I started this project 2019 as (and still is) a hobby to automate my model rail layout.
As I am trying to do this project beside my work, family and other hobbies it is a project with very slow pace...


So I hope you get inspired!

Frans

** Copyright 2019 - 2023 Frans Jacobs **

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"),
to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense,
and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
# Java Central Station

![Static Badge](https://img.shields.io/badge/Model_Railroad-Automation-blue) [![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0) 

![GitHub commit activity](https://img.shields.io/github/commit-activity/w/fransjacobs/model-railway) 
[![Java CI with Maven](https://github.com/fransjacobs/model-railway/actions/workflows/maven.yml/badge.svg?branch=master)](https://github.com/fransjacobs/model-railway/actions/workflows/maven.yml)

![GitHub Gist last commit](https://img.shields.io/github/last-commit/fransjacobs/model-railway)
![GitHub issues](https://img.shields.io/github/issues-raw/fransjacobs/model-railway)
![GitHub Release](https://img.shields.io/github/v/release/fransjacobs/model-railway)


## Model railway control with JCS
Experiments wich should eventually lead to automated rail road control.

## Why?
To have fun!
I know there are ready to go products on the market. This project is not an attempt to compeat with any of them,
hence this project is Open Source so anyone can benefit.

## About The Project
JCS is an application to control a model railway. It is in an early stage of development.
The project mostly contains my experiments to automate my model railway.

I started this project 2019 as (and still is) a hobby to automate my model-rail layout.
As I am trying to do this project beside my work, family and other hobbies it is a project with sometimes a very slow pace...

The aim of the program is to automate the running of trains on my layout.

### Supported Hardware
As I own a [Maerklin CS 3](https://www.marklin.nl/producten/details/article/60216), the project started with support
for only the Maerklin CS-3 and also the CS-2 (allthough there are some differences between the CS2 and 3).
Recently I have aquired a DCC-EX so I have also added support fro DCC-EX now.

#### Supported Command Stations
* DCC-EX via Serial or Network
* Marklin CS-2 (Using CAN bus to retrieve locomotive and accessory information)
* Marklin CS-3 (Using JSON to retrieve locomotive and accessory information)
** also supports the S88 link for sensors
 
## Current status
Currently the following modules are build:

* A HAL for command Stations such that other hardware then the Marklin CS 2/3 can be used.
* Added support for DCC-EX JCS can function as a Throttle for DCC-EX.
** DCC-EX can be connected either via serial port or network
* Communication layer to "talk" to the Maerklin CS-3, (Included automatic discovery of the CS2/3 ip address).
* A Screen to see Sensor / feedback events from the CS2/3
* Locomotives overview (including automatic downloading of the Locomotive- and function button images) and control.
* Turnout and Signals overview (Synchronized with the CS2/3) and control.
* A Screen to edit/display de schematic Layout.

## Screenshots
So here are a few screenshots of the Project:
Main screen in control mode turnout and signals can be controlled by clicking on the turnout or signal. 
Sensor activation is shown.
![UI screenshot: JCS Main Screen](assets/mainscreen.png?raw=true)

Locomotives can be driven manually using the Diver Cab.
![UI screenshot: JCS Main Screen](assets/driver_cab.png?raw=true)

The Locomotive- and function images are displayed. Images are automatically downloaded from the Central Station.

In Editmode you can draw a layout using pre defined Tiles.
also the layout can be routed. (it the very first and early step for the preparation of automatic running).
![UI screenshot: JCS Edit Screen](assets/mainscreen_edit_route.png?raw=true)

Import Locomotives from the CS-3
![UI screenshot: JCS Preferences Locomotives](assets/prefs_locomotives.png?raw=true)

Import Signals from the CS-3
![UI screenshot: JCS Preferences Signals](assets/prefs_signals.png?raw=true)

Import Turnout from the CS-3
![UI screenshot: JCS Preferences Turnouts](assets/prefs_turnouts.png?raw=true)

Sensor Monitor
![UI screenshot: JCS Sensor Monitor](assets/sensor_monitor.png?raw=true)

## TODO's (and there are still a lots of todo's...):

Currently the following features are under active development:
* Improving communication with the CS-2/3 [more or less done :)]
* Automatically route the Layout. [more or less done :)]
* React on relevant CS-3 events like start/stop, Sensor events, Loco, Accessory, power etc events. [more or less done :)]
* Automatically run trains. [Work in progress]

* Configuration screens to edit the Locomotives, Accessories and Sensors. [Todo]
* Add deployment configuration for Windows and Linux and to an App
  (first added is MacOS as that is my main development platform) [Postponed this a while, but basics where done]
* Add more Unit tests
* ...

So I hope you get inspired!

Frans

## License
[LICENSE](LICENSE.md)


## Build JCS from source
[BUILDING](BUILDING.md)

## Contributors

<table>
<tr>
    <td align="center">
        <a href="https://github.com/fransjacobs">
            <img src="https://avatars.githubusercontent.com/u/41232225?v=4" width="100;" alt="frans"/>
            <br />
            <sub><b>Frans Jacobs</b></sub>
        </a>
    </td>
</tr>
</table>


** Copyright 2019 - 2023 Frans Jacobs **

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"),
to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense,
and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

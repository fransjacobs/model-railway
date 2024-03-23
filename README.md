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

## Supported Hardware
* [DCC-EX](https://dcc-ex.com) can be connected either via serial port or network
* [Marklin CS-2](https://www.marklin.nl/producten/details/article/60215)
* [Marklin CS-3](https://www.marklin.nl/producten/details/article/60216)
* [HSI-S88](https://www.ldt-infocenter.com/dokuwiki/doku.php?id=en:hsi-88-usb) or the [DIY version](https://mobatron.4lima.de/2020/05/s88-scanner-mit-arduino) for feedback
 
## Current status
Currently the following modules are build:
* A Throttle for driving locomotives
* Keyboard Screen for switching Turnouts or Signals
* Sensor Monitor to see the status of feedback sensors
* Locomotives overview (including automatic downloading of the Locomotive- and function button images) and control.
* Turnout and Signals overview (Synchronized with the CS2/3) and control.
* A Screen to edit/display de schematic Layout.
* A HAL for command Stations such that other hardware then the Marklin CS 2/3 can be used.
* [First Release V 0.0.1](https://github.com/fransjacobs/model-railway/releases/tag/V0.0.1)

## Screenshots
So here are a few screenshots of the Project:
#### Thottle / Driver Cab
The Locomotive- and function images are displayed. Images are automatically downloaded from the Central Station.
Or can be manually added.

![UI screenshot: JCS Throttle](assets/driver_cab.png?raw=true)

#### Keyboard Panel for switching accessories and viewing feedback sensor status

![UI screenshot: JCS keyboard Screen](assets/keyboard-panel.png?raw=true)

#### Sensor Monitor

![UI screenshot: JCS Sensor Monitor](assets/sensor_monitor.png?raw=true)

#### Layout display
![UI screenshot: JCS Main Screen](assets/mainscreen.png?raw=true)

In Editmode you can draw a layout using pre defined Tiles.
also the layout can be routed. (it the very first and early step for the preparation of automatic running).
![UI screenshot: JCS Edit Screen](assets/mainscreen_edit_route.png?raw=true)

#### Import Locomotives from a CS-2 or CS-3
![UI screenshot: JCS Preferences Locomotives](assets/prefs_locomotives.png?raw=true)

## TODO's (and there are still a lots of todo's...):
Currently the following features are under active development:
* Configuration screens to edit the Locomotives, Accessories and Sensors. [more or less done :)]
* Add deployment configuration for MacOS, Windows and Linux an App [more or less done :)]
* Automatically route the Layout. [more or less done :)]
* React on relevant CS-3 events like start/stop, Sensor events, Loco, Accessory, power etc events. [work in progress :)]
* Automatically run trains. [Work in progress]
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


** Copyright 2019 - 2024 Frans Jacobs **

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"),
to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense,
and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

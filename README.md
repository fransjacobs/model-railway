# Java Central Station

![Static Badge](https://img.shields.io/badge/Model_Railroad-Automation-blue) [![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0) 

![GitHub commit activity](https://img.shields.io/github/commit-activity/w/fransjacobs/model-railway) 
[![Java CI with Maven](https://github.com/fransjacobs/model-railway/actions/workflows/maven.yml/badge.svg?branch=master)](https://github.com/fransjacobs/model-railway/actions/workflows/maven.yml)

![GitHub Gist last commit](https://img.shields.io/github/last-commit/fransjacobs/model-railway)
![GitHub issues](https://img.shields.io/github/issues-raw/fransjacobs/model-railway)
![GitHub Release](https://img.shields.io/github/v/release/fransjacobs/model-railway)
***
## About the Project

JCS is a hobby project of me where I try to automate my Model Railway. Over the past years I have worked on and off on several aspects and modules of the software which are needed to drive automatically.
A short summary of the topics which are needed and used to be able to drive trains automatically:

- Connectivity to the Command Station hardware. (DCC-EX,HSI-S88, Marklin CS2/3)
- Edit and display graphically a layout
- With the layout be able to route all the possible drive ways
- Show the routes and driveways in the layout screen
- Graphically feedback events on track to the layout screen
- Input dialogs to setup Accessories, Locomotives, Command stations, etc
- Locomotive Drive Cap so tha you can manually run you locomotive
- Virtual Command Station, to ease testing and simulate automatic driving 
- Monitor Sensor events

I created a [short video](https://youtu.be/xP6eUdScMY0) demonstrating automatic running of locomotives. Also a [video of pysical locomotives running on the Test Layout](https://www.youtube.com/watch?v=CyLmGk6gfHA)

## Why?

There are already many "out of the box" working products. I wanted to create my own to learn and to have FUN!

As I am a great supporter of Open Source I have put the project on github with the purpose for others to use it, learn from it, or improve it.  

So I hope you get inspired!

Frans

## Guides

- Setup : [JCS_SETUP](JCS_SETUP.md)
- Driving : [DRIVING](DRIVING.md)

## Screenshots

So here are a few screenshots of the Project:

### Thottle / Driver Cab

The Locomotive- and function images are displayed. Images are automatically downloaded from the Central Station.
Or can be manually added.

![UI screenshot: JCS Throttle](assets/driver_cab.png?raw=true)

### Layout overview

The (schematic) layout is displayed. Locomotive can be Placed in a Block.
When the AutoPilot is switched on Locomotive will drive from block to block.
![UI screenshot: JCS Main Screen](assets/mainscreen.png?raw=true)

In Editmode you can draw a layout using pre defined Tiles.
also the layout can be routed. (it the very first and early step for the preparation of automatic running).
![UI screenshot: JCS Edit Screen](assets/mainscreen_edit_route.png?raw=true)

### Sensor Monitor

To debug or easly setup your feedback sensors 
![UI screenshot: JCS Sensor Monitor](assets/sensor_monitor.png?raw=true)

### Keyboard Panel for switching accessories and viewing feedback sensor status

![UI screenshot: JCS keyboard Screen](assets/keyboard-panel.png?raw=true)

### Import Locomotives from a CS-2 or CS-3

![UI screenshot: JCS Preferences Locomotives](assets/prefs_locomotives.png?raw=true)

## Releases

- [First Release V 0.0.1](https://github.com/fransjacobs/model-railway/releases/tag/V0.0.1)

## Supported Hardware

- [DCC-EX](https://dcc-ex.com) can be connected either via serial port or network
- [Marklin CS-2](https://www.marklin.nl/producten/details/article/60215)
- [Marklin CS-3](https://www.marklin.nl/producten/details/article/60216)
- [HSI-S88](https://www.ldt-infocenter.com/dokuwiki/doku.php?id=en:hsi-88-usb) or the [DIY version](https://mobatron.4lima.de/2020/05/s88-scanner-mit-arduino) for feedback

## Current status

Currently the following feature are in development:

- Documentation
- Internationalization enable multiple languages
- Add support for ESU ECOS
- Show Signal aspects in automatic driving
- Enhance GUI
- Add more Unit tests
- ...

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
    <td><a href="https://www.buymeacoffee.com/fransjacobs" target="_blank"><img src="https://cdn.buymeacoffee.com/buttons/default-orange.png" alt="Buy Me A Coffee" height="41" width="174"></a>
    </td>
</tr>
</table>

## Copyright 2019 - 2024 Frans Jacobs

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"),
to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense,
and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

# Model-railway

## Model railway control with JCS (Java Central Station)
Experiments wich should eventually lead to automater rail roud control.

# Why?
To have fun!
I know there are ready to go products on the market.
This project is not an attempt to compeat with any of them,
hence this project is Open Source so anyone can benefit.

## About The Project
JCS is an application to control a model railway. It is in an early stage of development.
The project mostly contains my experiments to automate my model railway.
I own a [Maerklin CS 3](https://www.marklin.nl/producten/details/article/60216), hence the (current) hardware supported is the Maerklin CS-3 and also the CS-2.
(allthough ther are some differences between the CS 3 and 2)

I started this project 2019 as (and still is) a hobby to automate my model-rail layout.
As I am trying to do this project beside my work, family and other hobbies it is a project with sometimes a very slow pace...

The aim of the program is to automate the running of trains on my layout.
Currently the following modules are build:

* A Screen or edit/display de schematic Layout.
* Communication layer to "talk" to the Maerklin CS-3, (Included automatic discovery of the CS-3 ip address).
* A Screen to see Sensor / feedback events from the CS-3
* Locomotives overview (including automatic downloading of the Locomotive- and function button images) and control.
* Turnout and Signals overview (Synchronized with the CS-3) and control.

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
* Improving communication with the CS-2/3
* Automatically route the Layout.
* React on relevant CS-3 events like start/stop, Sensor events, Loco, Accessory, power etc events. 
* Automatically run trains.

* Configuration screens to edit the Locomotives, Accessories and Sensors.
* Automatic driving of the trains
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
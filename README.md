# Model-railway

## Modelrailway control with JCS (Java Central Station)

## About The Project
JCS is an application to control a model railway. It is in an early stage of development.
The project mostly contains my experiments to automate my model railway.
I own a [Maerklin CS 3](https://www.marklin.nl/producten/details/article/60216), hence the (current) hardware supported is the Maerklin CS-3 (and probably also the CS-2).

I started this project 2019 as (and still is) a hobby to automate my model rail layout.
As I am trying to do this project beside my work, family and other hobbies it is a project with very slow pace...

The aim of the program is to automate the running of trains on my layout.
Currently the following modules are build:

* A Screen or edit/display de schematic Layout.
* Communication layer to "talk" to the Maerklin CS-3, (Included automatic discovery of the CS-3 ip address).
* A Screen to see Sensor / feedback events from the CS-3
* Locomotives overview (including automatic downloading of the Locomotive- and function button images) and control.
* Turnout and Signals overview (Synchronized with the CS-3) and control.

So here is a first screenshot of my layout:
![UI screenshot: My Layout](assets/mylayout.png?raw=true)

Currently the following features are under active development:
* Adapt to JDK 17 (LTS).
* React on relevant CS-3 events like start/stop, Sensor events, Loco, Accessory, power etc events. 
* Automatically route the Layout.
* Automatically run trains.

##TODO's (and there are still a lots of todo's...):
* Replace the JDBC calls for a tiny ORM library (norm; https://github.com/dieselpoint/norm ).
* Redesign the Locomotive(s) control so tha it can support 32 functions
* Configuration screens to edit the Locomotives, Accessories and Sensors.
* Automatic driving of the trains
* Add deployment configuration for Windows and Linux and to an App
  (first added is MacOS as that is my main development platform) [Postponed this a while, but basics where done]
* ...

So I hope you get inspired!

Frans

## License
[LICENSE.md](LICENSE.md).


## Build JCS from source

See [BUILDING.md](BUILDING.md).

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
# JCS Quick Setup Guide

***

## Get Started with driving your Trains automatically
This guide will explaint to you how to configure JCS for a layout
and automatically run trains. The following Layout is used
![Test Layout](assets/testlayout.png?raw=true)

This layout consist out of 4 blocks and 2 switches.
Every block has 2 sensors, intotal ther are 8 sensors used.

## Drawing the Layout

When JCS is started sele the Edit Button to enable edit mode.
![Start Edit](assets/startedit.png?raw=true)
JCS will show the Edit Layout Screen
![Start Edit](assets/layoutedittoolbar.png?raw=true)
The Toolbar has the most common elements to draw a layout. A layout consist out of tiles. A Tile mimics a component like a straight, sensor, block etc.
Use the + button to add a tile on the canvas. The Bin button will delete a tile.
Rightclick on a tile to see properties or rotate or flip if applicable.
When a tile is selected it can be dragged to the right position. Tiles are automatically saved. The example looks like this when all tiles are placed on the canvas.
![Start Edit](assets/layoutedit1.png?raw=true)

## Configuring Accessories and Sensors

The next step is to configure the Accessories and Sensors.
Accessories Sensore and locomotive are alway linked to a Command Station.
#### Step 1 selecting the Default Command Station
For this guide a [DCC-EX](https://dcc-ex.com) Command Station is used.
Direct feedback via [DCC-EX](https://dcc-ex.com) is not yet supported, see [issue 59](https://github.com/fransjacobs/model-railway/issues/59), hence the [HSI-S88](https://www.ldt-infocenter.com/dokuwiki/doku.php?id=en:hsi-88-usb) is used for Sensor feedback. 

Using the cog button or via Menu -> Tools Command Stations
![Setup DCC-EX](assets/command-station-DCC-EX.png?raw=true)
Use the Refresh button on the right side of the Serialport dropdown menu refresh
connected serial ports. In this example the [DCC-EX](https://dcc-ex.com) is connectd via the "ttyACM0" port.
The [DCC-EX](https://dcc-ex.com) is set as default and enabled.
The Test Button can be used to check if the connection can be established.
##### Setup the Feedback module
In the Command Station Dialog select the [HSI-S88](https://www.ldt-infocenter.com/dokuwiki/doku.php?id=en:hsi-88-usb) in the Command Stations dropdown menu
![Setup HSI-S88](assets/command-station-HSI-S88.png?raw=true) 
Do *NOT* set the [HSI-S88](https://www.ldt-infocenter.com/dokuwiki/doku.php?id=en:hsi-88-usb) as default, as the default Command Station is supposed to put the power on the Track.

For a feedback module the number of connected S88 modules, the Channel to which the S88 modules are connected have to be configured.
In this case we use 1 S88 module, connected to Channel 0. Click on Re-create Sensors button to create the individual sensors in the system.

### Step 2 create the Accessories 
The test track has 2 switcher which can be added using the Accessory Dialog.
Via Settings (Mac) or Menu -> Tools -> Options. To Add a new Accessory click on the + button. For a Turnout choose the right Turnout type in the Types dropdown menu. 
![Edit Accessory](assets/accessory-edit.png?raw=true)
To save the Accessory click the Save Button.

All Accessories will appear in the list on the left side. Can be filtered using the 3 radio buttons on top (All Turnouts Signals).

### Step 3 add locomotives
As we are in the options Dialog let also add the locomotives.
In this Example ar 2 locomotives used. For both locomotives an icon is already put in a subdirectory ...jcs/images. 
![Edit Locomotive](assets/locomotive-edit.png?raw=true)
In this test layout locomotive can oly commute back and forth, hence the commute check box is switched on for both locomotives. If this checkbox is switched off it means that the AutoPilot can *NOT* automatically change the direction of the Locomotive. 

### Step 4 Link sensors to their graphical representation
JCS "knows" where a locomotive is by monitoring the sensors on the track. Every sensor has to be linked to the location in the schematic layout on the screen.
as already mentioned a block alway need 2 sensors. A block is a peice of track where exactly 1 train can be. When a train enters a block the enter sensor detects that. When the train is fully in the block it is detected by the occupation or in sensor. When a sensor is active JCS will "see" tha as occupied so will not plot a route to an occupied block. Right click on a sensor.
![Sensor popup](assets/sensor-popup.png?raw=true)

Click on Properties and select the pysical sensor.

![Sensor popup](assets/select-sensor.png?raw=true)

If you do no exactly know which sensor is at this location you can use the Sensor Monitor. Click on the Sensor Monitor button.

![Sensor popup](assets/sensor-monitor-button.png?raw=true)

The Sensor monitor will appear. When you activate the sensor (pre condition is that the HSI-S88 is connected), it will be visible in the monitor as active.

![Sensor popup](assets/sensor-monitor-sensor1-active.png?raw=true)

Now you know the name, which you can select in the Sensor Properties dialog. Do this for all sensors.

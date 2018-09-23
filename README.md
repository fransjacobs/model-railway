# model-railway
My Experiments to automate a model railway.

I have inherited some old Marklin digital control boxes.
This re-started my model railway hobby which has been a old fascination of mine.
I came to the conclusion that the available Software no longer supported the 6050 serial interface.
As I do no have a high budget to spend so decided to build he software to control the track myself.

So although the software is not complete as other available programs is is purely a hobby project to
automate my track.

I have decided to share it on Github, because I think there might be more railway enthusiasts who have
some (old) stuff in the attic an want to restart again.

I started to get the Serial interface 6050 working
Then to control locomotives, solenoids.
When that was working I connected the S88 to get some feedback of the track to enable real track automation.

After that was working I created my first shadow station process, which is still experimental.

At this moment of writing the program consist out a a Java server process which talk using a usb serial adapter to the 6050.
A GUI which is build using NetBeans forms.

I run the server on a Raspberry pi which is connected to the 6050.

So I hope you get inspired
# Autopilot Module

## Overview
The **Autopilot** module is the engine that drives trains automatically within the JCS (Java Central Station) system.  
Once locomotives are assigned to **blocks** and their **travel directions** are set by the user, Autopilot can be engaged to handle fully automatic block-to-block operation.

Autopilot ensures that:
- A locomotive can only travel from its current block into a **free** neighboring block.
- When a locomotive **arrives** in the destination block:
  - The **departure block** is marked **free**.
  - The **destination block** is marked **occupied**.
- Locomotives will wait if no valid free block is available, preventing collisions.

---

## Core Concepts

- **Block**: Track segment with occupancy detection. No turnouts or switches are allowed inside blocks.
- **Locomotive**: Controllable train entity with direction, speed, and functions.
- **Dispatcher**: Runtime controller created per locomotive when Autopilot is enabled. Responsible for pathfinding, block reservation, and train control.
- **Command Station**: Abstracted hardware layer (CS3, ECoS, DCC-EX, etc.) through which Autopilot issues speed, direction, and accessory commands.
- **Sensors**: Feedback devices that confirm departures and arrivals, ensuring safe block handover.

---

## Operator Workflow

1. **Prepare the Layout**
   - Define blocks with valid sensors.
   - Place locomotives in their starting blocks.
   - Set each locomotive’s **direction** (via block menu tools if needed).

2. **Enable Autopilot**
   - Press the **Pilot** button in the UI to activate Autopilot.
   - A dispatcher is created for each locomotive currently on track.

3. **Start Running**
   - Start a single locomotive from its **Block menu**, or
   - Use **Cruise Control → Start All** to start every dispatcher simultaneously.

4. **During Operation**
   - Autopilot continuously:
     - Selects the next free block,
     - Sets required turnouts/driveways,
     - Commands the locomotive to run,
     - Updates block occupancy when arrival is confirmed.

---

## Rules & Safety

- **Block-to-block only**: Movement is always between whole blocks.
- **Collision avoidance**: Locomotives never enter an occupied destination block.
- **Waiting state**: A locomotive remains in its block if no adjacent block is free.
- **User override**: Locomotives can be stopped/started manually at any time.

---

## Developer Notes

- **Routing logic** can be extended to prioritize routes, avoid oscillations, or incorporate signal aspects.
- **Speed profiles** can be integrated for smoother acceleration/deceleration.
- **Testing**: Autopilot is covered by `AutoPilotTest` and related unit tests. Extend these when adding new logic.
- **Integration**: Relies on the `JCSCommandStation` interface for sending commands and listening to events.

---

## Example Lifecycle

```mermaid
sequenceDiagram
    participant User
    participant Dispatcher
    participant CommandStation
    participant Block

    User->>Dispatcher: Start locomotive in Block A
    Dispatcher->>CommandStation: Set turnouts, set speed/direction
    CommandStation->>Block: Train leaves Block A
    Block->>Dispatcher: Departure sensor triggered
    Dispatcher->>Block: Reserve Block B (destination)
    CommandStation->>Block: Train enters Block B
    Block->>Dispatcher: Arrival sensor triggered
    Dispatcher->>Block: Mark A=Free, B=Occupied
    Dispatcher->>Dispatcher: Repeat cycle


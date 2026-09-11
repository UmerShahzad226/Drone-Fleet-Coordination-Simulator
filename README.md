# Autonomous Drone Fleet Simulator

An advanced Java-based **Autonomous Drone Fleet Simulator** developed for the **CS-212 Object Oriented Programming** course. The project simulates multiple autonomous drones operating in a shared 3D-like environment, including autonomous navigation, formation control, collision avoidance, communication, real-time monitoring, and GUI-based interaction.

## Overview

The simulator provides a safe software environment for studying multi-drone behavior without requiring physical drone hardware.

Each drone has a 3D position and target and can autonomously move toward its assigned destination. The system continuously updates drone motion while applying formation and collision-avoidance logic.

A graphical interface provides real-time visualization and allows the user to configure and monitor the simulation.

### Main Workflow

```text
User
  ↓
SWT GUI
  ↓
Simulator
  ↓
Drone Objects
  ↓
Motion + Formation + Collision Avoidance
  ↓
Rendering / Logging
  ↓
Real-Time Results
```

## Key Features

- Multi-drone simulation environment
- Autonomous movement toward assigned targets
- 3D-like grid visualization
- Real-time drone position tracking
- Collision detection and avoidance
- Formation control
- Probabilistic communication model
- Dynamic target assignment
- Real-time flight status and elapsed-time display
- Start, Stop, and Restart controls
- Camera rotation, tilt, zoom, and pan
- Data logging and performance monitoring
- File-based input/output
- Adjustable simulation parameters
- Event-driven graphical interface

## Technologies Used

- **Programming Language:** Java
- **GUI Framework:** SWT
- **Development Environment:** Eclipse IDE
- **Programming Paradigm:** Object-Oriented Programming
- **Visualization:** 3D-style grid and drone rendering

## System Architecture

The system follows a modular object-oriented architecture consisting of:

### GUI Layer

Handles:

- User controls
- Camera controls
- Grid rendering
- Drone visualization
- Simulation settings

### Control Layer

Receives user commands and communicates with the simulation engine.

### Simulation Engine

Responsible for:

- Simulation time steps
- Drone movement
- Motion updates
- Collision avoidance
- Formation control

### Backend

Contains the main simulation classes including drones, environment, vector mathematics, communication, formation, and logging.

### Data Layer

Records drone positions, simulation outputs, and performance metrics.

## Project Classes

| Class | Responsibility |
|---|---|
| `Drone` | Calculates drone movement and target direction |
| `Simulator` | Controls simulation time steps and updates drones |
| `Environment` | Defines flight boundaries |
| `CollisionAvoidance` | Prevents distance-based collisions |
| `CommunicationModule` | Handles state sharing between drones |
| `FormationManager` | Controls fleet formation and placement |
| `Vector3D` | Performs 3D coordinate and vector calculations |
| `Logger` | Records simulation outputs and metrics |
| `GUI_Project` | Handles GUI controls, rendering, and visualization |

### Class Relationships

- `Simulator` aggregates multiple `Drone` objects.
- `Drone` uses `Vector3D` for movement calculations.
- GUI observes and displays simulator state.
- `CollisionAvoidance` interacts with the simulator.
- `Logger` receives simulation updates.

## Simulation Workflow

1. Initial drone and environment data is loaded.
2. The user can select the required number of drones.
3. Drones are created in a formation on the 3D grid.
4. The user can adjust the camera view.
5. The simulation starts using the **START** control.
6. Drones autonomously move toward their targets.
7. Drone positions are continuously updated.
8. Users can select a drone and assign a new target `(x, y, z)`.
9. Collision avoidance prevents drones from overlapping.
10. The simulation can be stopped or restarted at any time.

## Mathematical Modeling

The simulator models several aspects of drone behavior.

### Motion Model

Drone translational motion is updated using velocity and acceleration:

```text
v(t+Δt) = v(t) + a·Δt
p(t+Δt) = p(t) + v(t+Δt)·Δt
```

The system also models rotational dynamics using Euler's equations.

### PD Control

A PD-based control approach is used for position and velocity errors:

```text
a_cmd = kp·ep + kd·ev + g
```

where:

- `ep` = position error
- `ev` = velocity error
- `g` = gravity

### Formation Control

Neighboring drones contribute formation forces based on their relative positions and velocities.

### Collision Avoidance

Repulsive forces are applied when drones become too close:

```text
F_rep,i = Σ k_rep · (p_i - p_j) / ||p_i - p_j||²
```

### Communication Model

Drones communicate when they are within the communication range:

```text
||p_i - p_j|| < R_comm
```

Message delivery is probabilistic and includes a configurable loss probability.

## Object-Oriented Concepts

The project demonstrates major OOP principles:

- **Encapsulation** — drone state and functionality are organized inside classes.
- **Inheritance** — used as part of the Java object-oriented design.
- **Abstraction** — complex simulation behavior is separated behind class interfaces.
- **Polymorphism** — supports flexible object-oriented behavior.
- **Composition and modularity** — system functionality is divided into independent classes.

## GUI Controls

The graphical interface allows users to:

- Start and stop the simulation
- Restart the simulation
- Change the number of drones
- Assign individual drone targets
- Adjust simulation speed
- Configure collision distance
- Rotate and tilt the camera
- Zoom and pan the view
- Monitor drone positions
- Observe collision status
- View elapsed simulation time

## Results

The completed simulator provides:

- Live 3D-style drone visualization
- Autonomous drone movement
- Real-time coordinate updates
- Target following
- Collision prevention
- Formation behavior
- Flight status information
- Performance logging

Testing showed that drones accelerate smoothly and collision avoidance works as intended. Separating the simulation logic from the display thread helped maintain responsive GUI performance.

## Challenges and Solutions

| Challenge | Solution |
|---|---|
| Drone collisions | Implemented distance-based collision avoidance |
| GUI lag | Separated simulation logic from the display thread |
| Visual asset scaling | Used image resizing during rendering |
| Data organization | Separated functionality into modular OOP classes |

## Future Enhancements

Possible improvements include:

- More realistic 3D drone models
- Improved physics-based flight dynamics
- Altitude-dependent behavior
- Battery consumption monitoring
- Sensor-based navigation
- Dynamic weather effects
- Real-world map integration
- Machine-learning-based path planning
- More advanced fleet coordination
- Improved simulation realism

## Potential Applications

With further development, the simulator could be adapted for:

- Drone navigation research
- Pilot training simulations
- Logistics and delivery planning
- Educational demonstrations
- Engineering analysis
- Multi-drone fleet coordination research

## Project Team

- **Muhammad Umer Shahzad**
- **Hafiz Muzammil Hussain**
- **Aneeq Ahmed**

## Course Information

**CS-212 — Object Oriented Programming**

**Department of Computer and Software Engineering**  
**National University of Science and Technology (NUST), Islamabad**

## Development Tools

- Eclipse IDE
- Java
- SWT
- GitHub

## License

This project is intended for educational and academic purposes.

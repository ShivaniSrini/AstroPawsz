# AstroPaws  
### A Blind-First Accessible Arcade Game Prototype  

EECS 4088 – Capstone Project  
York University  

---

##  Project Overview

AstroPaws is a modular arcade-style game prototype designed primarily for players who are blind or have low vision.

The project explores how spatial audio, distance modeling, and adaptive feedback systems can replace visual HUD elements while preserving gameplay challenge and immersion.

Rather than simplifying gameplay, this project redesigns sensory feedback channels.

---

##  Motivation

Approximately 2.2 billion people worldwide live with some form of visual impairment (WHO, cited in CMS 2024).  

Most mainstream games rely heavily on visual information, making them inaccessible to blind players.

This project investigates:

- How spatial audio can replace visual orientation
- How distance modeling improves depth perception
- How adaptive feedback reduces frustration without reducing difficulty
- How accessibility systems can be modular and reusable

---

##  Core Accessibility Features

### 3D Spatial Audio (OpenAL)
- Real-time source positioning
- Listener orientation tracking
- Inverse distance attenuation
- Tuned rolloff factor for clearer depth perception
- Doppler velocity tracking for movement realism

### Dynamic Alignment System
- Dot-product-based directional alignment
- Distance-adaptive alignment thresholds
- Sticky precision rotation to prevent overshooting
- Continuous and discrete alignment feedback

### Distance-Based Feedback
- Volume attenuation based on distance
- Alignment-based pitch scaling
- Capture range pulse feedback
- Immediate action confirmation sounds

### Blind-First Gameplay Loop
- Continuous beacon navigation cue
- Whoosh feedback on capture attempt
- Kaching reward on successful capture
- No silent interactions

---

## ⚙️ Technical Architecture

### Audio Engine
- Built using LWJGL + OpenAL
- Modular source management
- Listener velocity tracking
- Distance model configuration
- Gain and pitch modulation

### Game Controller
- Dynamic alignment thresholds
- Sticky rotation assist
- Capture cooldown logic
- Range-based feedback system

### Model Components
- Ship (movement + orientation vectors)
- AnimalSpawner (timed spawning system)
- AudioBeacon (spatial target representation)

---

##  Accessibility Testing Plan

The project will be evaluated using a customized GUESS-based questionnaire and task-based usability testing.

Evaluation focuses on:

- Navigation accuracy without visual input
- Directional audio clarity
- Learnability of controls
- Player confidence and immersion

---

##  Research Foundation

This project draws from:

- López Ibáñez et al. (2022) – Accessible Game Design Review
- CMS (2024) – VI-DDE Framework
- Game Accessibility Guidelines
- Fourteen Forms of Fun (Game Developer)

The goal is not just to create a prototype, but to explore reusable accessibility modules for future development.

---

##  Current Status

✔ Spatial audio fully integrated  
✔ Distance attenuation tuned (inverse distance model, rolloff 2.5)  
✔ Doppler effect enabled  
✔ Dynamic alignment system implemented  
✔ Sticky precision rotation active  
✔ Capture feedback loop complete  

---

##  Future Work

- Haptic feedback integration
- Speech-based menu narration
- Expanded RPG-style prototype
- Additional accessibility presets
- Formal blind-user testing sessions

---

##  Technologies Used

- Java 17
- LWJGL 3.3.3
- OpenAL
- Maven

---

##  Project Goal

To demonstrate that accessible game design can maintain challenge, immersion, and fun — while being designed from the ground up for blind players.

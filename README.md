This is My coolest work in progress project.

# It is an highly configurable *Microcoded CPU Simulator*.

The simulator supports user-defined instruction sets (custom ISA).

---
# Overview/Capabilities :

-Arbitrary-sized address space (byte-addressable)

-Arbitrary memory size (in bytes)

-Custom register definitions (name + size in bytes)

-Configurable endianness (Little / Big endian)

-Hex-based opcode system mapped to user-defined microcode sequences

-Support for both CISC-style and RISC-style instruction design (user-defined)

-Fully microcoded execution model

---
# Design

It does not implement any fix cpu. Instead, it provides a deterministic execution using user defined cpu.

- Instruction behavior is defined entirely by microcode
- CPU architecture is fully user-defined
---

#Images 

<img width="274" height="540" alt="image" src="https://github.com/user-attachments/assets/9bdae66b-d4d3-4d87-8711-a7c87c1e75e3" />

<img width="268" height="248" alt="image" src="https://github.com/user-attachments/assets/5a241c34-119d-4dee-831a-a9c7be204dd4" />

<img width="214" height="133" alt="image" src="https://github.com/user-attachments/assets/21be0637-d148-4860-8767-8957ecdd5bd1" />

NOTE: The image for GUI was done as example for college evaluation and it is not final or compatible at the moment.
<img width="741" height="360" alt="image" src="https://github.com/user-attachments/assets/fbc273e5-a17b-4b24-8e54-783a5974bf83" />


---
TODO/WIP

- [] Implement flags definition

- [] Implement flags

- [] Implement a working GUI

- [] Make it useable on other computers with minimal setup

---
# Planned for flags

Flags are going to be designed to be user-defined boolean expressions over CPU state, including:

Current CPU state
Previous CPU state (1-step history)

Flag evaluation is:

-Explicitly controlled by microcode

-Triggered only when requested

-Fully user-defined per flag

Multiple definitions or update rules can exist for the same flag

Microcode decides when evaluation occurs



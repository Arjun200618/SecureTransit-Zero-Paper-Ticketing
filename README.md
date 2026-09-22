# SecureTransit: Zero-Paper Digital Bus Ticketing
**Built for the iQOO Hackathon 2026 - FinTech & Commerce Track**

## 🏗️ The Architecture 
This project is split into two working applications to handle offline transit:

### 1. Conductor POS Terminal (This Repository)
* **Tech Stack:** Android Native, Kotlin, Jetpack Compose.
* **On-Device AI:** Utilizes local Snapdragon edge inference for anomaly detection (sub-20ms latency).
* **Role:** Conductors use this to scan passenger QRs and verify Ed25519 cryptographic signatures 100% offline.

### 2. Passenger Ticketing Web App (PWA)
* **Live Demo:** [https://bus-e-ticket.vercel.app](https://bus-e-ticket.vercel.app)
* **Passenger App Source Code:** [https://github.com/Arjun200618/UPI-based-bus-e-ticket](https://github.com/Arjun200618/UPI-based-bus-e-ticket)
* **Role:** Passengers book tickets online and generate the cryptographically signed QR code.

## ⚙️ How to Test
1. Generate a ticket on the Live PWA.
2. Run this Android repository in an emulator or on an iQOO test device.
3. Use the `Inspector Squad` tab to simulate the offline scan and verify the payload.

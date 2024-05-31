# Simple HTTP Server

This repository contains a simple HTTP server built during a challenge with Codecrafters. The goal was to implement a basic HTTP server that can handle requests and respond appropriately, showcasing fundamental networking and server-side programming skills.

## Features

- Handles basic HTTP requests
- Serves static files from a specified directory
- Basic routing for different endpoints
- Logs requests and responses

## Getting Started

### Prerequisites

Ensure you have the following installed on your system:

- Java (21 or higher)

### Installation

1. Clone the repository:

    ```bash
    git clone https://github.com/DonnyLp/simple-http-server.git
    cd simple-http-server
    ```

2. Run
   ```bash
    ./your_server.sh
    ```
### Usage

Once the server is running, you can access it in your browser or via curl:

- Open your browser and go to `http://localhost:8080`
- Use curl to make a request:

    ```bash
    curl http://localhost:8080
    ```

## Project Structure
```
simple-http-server/
├── .vscode/ # VSCode settings
├── src/
│ └── main/
│ └── java/
│ └── Main.java # Main server implementation
├── codecrafters.yml # Codecrafters configuration
├── your_server.sh # Script to run the server
├── pom.xml # Maven configuration
├── .gitattributes
├── .gitignore
└── README.md # This file
```

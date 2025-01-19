# Load Balancer Demo

This repository includes a simple load balancer based on round robin, and a application server that returns the exact POST message. 

## How to run

Run the load balancer with docker compose
```bash
docker-compose up
```

## Project Structure

The project contains two modules - "Application" and "LoadBalancer" which can be opened in the same project in IntelliJ, making it easier to navigate code and run the project locally. 

## Monitoring

Prometheus and Grafana is included in the docker compose file, which can be accessed at `http://localhost:3000` with username `admin` and password `admin`.
# Secure Hybrid Cloud

A secure, containerized microservices project demonstrating application isolation using Docker, Kubernetes, Calico, and Kubernetes NetworkPolicies.

## Project Overview

This project contains two independent Spring Boot applications:

- **Student App** - provides student-related functionality.
- **Faculty App** - provides faculty-related functionality.

Both applications are packaged as Docker images and deployed into separate Kubernetes namespaces.

The project demonstrates network segmentation using Kubernetes NetworkPolicies so that:

- Student services can communicate within the Student namespace.
- Faculty services can communicate within the Faculty namespace.
- Student services cannot directly communicate with Faculty services.
- Faculty services cannot directly communicate with Student services.
- DNS communication remains available for Kubernetes service discovery.

## Architecture

The project currently uses two Spring Boot microservices deployed as separate Kubernetes namespaces.

```text
                         Kubernetes Cluster
                              |
                         Calico CNI
                              |
              +---------------+---------------+
              |                               |
       Student Namespace                Faculty Namespace
              |                               |
       +------+-------+                +------+-------+
       |              |                |              |
   Student Pod   Student Service   Faculty Pod   Faculty Service
       |              |                |              |
      :8081          :8081             :8082          :8082
              \                               /
               \                             /
                +-------- NetworkPolicy -----+
                           |
                    Cross-namespace
                       traffic
                       BLOCKED
```

### Application Layer

- **Student App:** Spring Boot application running on port `8081`.
- **Faculty App:** Spring Boot application running on port `8082`.

### Container Layer

Each Spring Boot application is packaged as an independent Docker image:

- `student-app:1.0`
- `faculty-app:1.0`

### Kubernetes Layer

Each application runs in its own namespace:

- `student`
- `faculty`

Kubernetes Services provide internal service discovery for each application.

### Network Security Layer

Calico provides the cluster networking layer and enables Kubernetes NetworkPolicy enforcement.

NetworkPolicies implement namespace-level isolation:

- Student-to-Faculty traffic is blocked.
- Faculty-to-Student traffic is blocked.
- Same-namespace communication is allowed.
- DNS traffic is allowed for Kubernetes service discovery.

## Technology Stack

### Backend

- Java 17
- Spring Boot
- Spring Web
- Maven

### Containerization

- Docker
- Docker Desktop
- Eclipse Temurin 17 JRE

### Orchestration

- Kubernetes
- Minikube
- kubectl

### Networking and Security

- Calico CNI
- Kubernetes NetworkPolicies
- Kubernetes Namespaces

### Development and Version Control

- IntelliJ IDEA
- Git
- GitHub

## Project Structure

```text
secure-hybrid-cloud/
│
├── student-app/
│   ├── src/
│   ├── pom.xml
│   └── Dockerfile
│
├── faculty-app/
│   ├── src/
│   ├── pom.xml
│   └── Dockerfile
│
├── k8s/
│   ├── namespaces.yaml
│   ├── student-deployment.yaml
│   ├── faculty-deployment.yaml
│   ├── student-network-policy.yaml
│   └── faculty-network-policy.yaml
│
├── .gitignore
└── README.md
```

## Dockerization

Both Spring Boot applications are packaged into Docker images using Eclipse Temurin 17 JRE.

### Student Application

Build the Student application:

```powershell
cd student-app
.\mvnw.cmd clean package -DskipTests
```

Build the Docker image:

```powershell
docker build -t student-app:1.0 .
```

### Faculty Application

Build the Faculty application:

```powershell
cd faculty-app
.\mvnw.cmd clean package -DskipTests
```

Build the Docker image:

```powershell
docker build -t faculty-app:1.0 .
```

## Kubernetes Deployment

### 1. Create Namespaces

```powershell
kubectl apply -f k8s\namespaces.yaml
```

This creates:

- `student`
- `faculty`

### 2. Load Images into Minikube

```powershell
minikube image load student-app:1.0
minikube image load faculty-app:1.0
```

### 3. Deploy Student Application

```powershell
kubectl apply -f k8s\student-deployment.yaml
```

### 4. Deploy Faculty Application

```powershell
kubectl apply -f k8s\faculty-deployment.yaml
```

Verify the Pods:

```powershell
kubectl get pods -n student
kubectl get pods -n faculty
```

Verify the Services:

```powershell
kubectl get services -n student
kubectl get services -n faculty
```

## Network Security

The project uses Calico as the Kubernetes Container Network Interface (CNI).

Kubernetes NetworkPolicies are used to isolate the Student and Faculty namespaces.

### Student NetworkPolicy

The Student policy allows:

- Communication within the Student namespace.
- DNS traffic to Kubernetes DNS on port `53`.

It blocks communication from Student Pods to the Faculty namespace.

### Faculty NetworkPolicy

The Faculty policy controls both ingress and egress traffic.

It allows:

- Communication within the Faculty namespace.
- DNS traffic to Kubernetes DNS on port `53`.

It blocks cross-namespace communication with the Student namespace.

## Security Testing

The network isolation was verified using temporary test Pods running the `curl` image.

### Student to Faculty

```powershell
kubectl run test-client -n student --rm -it --image=curlimages/curl:8.12.1 --restart=Never -- curl --max-time 5 http://faculty-service.faculty.svc.cluster.local:8082/faculty/data
```

Result:

```text
curl: (28) Connection timed out
```

**Result: BLOCKED**

### Faculty to Student

```powershell
kubectl run test-client -n faculty --rm -it --image=curlimages/curl:8.12.1 --restart=Never -- curl --max-time 5 http://student-service.student.svc.cluster.local:8081/student/data
```

Result:

```text
curl: (28) Connection timed out
```

**Result: BLOCKED**

### Student to Student

```powershell
kubectl run test-client -n student --rm -it --image=curlimages/curl:8.12.1 --restart=Never -- curl --max-time 5 http://student-service.student.svc.cluster.local:8081/student/data
```

Result:

```text
This is STUDENT data.
```

**Result: ALLOWED**

### Faculty to Faculty

```powershell
kubectl run test-client -n faculty --rm -it --image=curlimages/curl:8.12.1 --restart=Never -- curl --max-time 5 http://faculty-service.faculty.svc.cluster.local:8082/faculty/data
```

Result:

```text
This is FACULTY data.
```

**Result: ALLOWED**

## Security Test Summary

| Source | Destination | Result |
|---|---|---|
| Student | Student | Allowed |
| Student | Faculty | Blocked |
| Faculty | Faculty | Allowed |
| Faculty | Student | Blocked |

The tests demonstrate two-way namespace isolation while maintaining same-namespace communication and DNS-based Kubernetes service discovery.

## Current Security Architecture

```text
                 Kubernetes Cluster
                         |
                    Calico CNI
                         |
          +--------------+--------------+
          |                             |
     Student Namespace             Faculty Namespace
          |                             |
      Student App                   Faculty App
        :8081                         :8082
          |                             |
          |                             |
          +------------ X --------------+
                 Cross-namespace
                    BLOCKED
                       🔐
```

## Future Cloud Architecture

The current implementation runs locally using Minikube.

The next stage of the project will extend the architecture toward a cloud environment and introduce additional cloud security controls such as:

- Cloud networking
- Private subnets
- Security groups
- IAM-based access control
- Managed Kubernetes
- Secure service-to-service communication
- Monitoring and logging

These cloud components are planned extensions and are not yet part of the current implementation.

## Learning Objectives

This project demonstrates practical understanding of:

- Building Spring Boot microservices
- Maven-based Java applications
- Docker image creation
- Container execution
- Kubernetes Deployments
- Kubernetes Services
- Kubernetes Namespaces
- Minikube
- Calico CNI
- Kubernetes NetworkPolicies
- Network segmentation
- Least-privilege network access
- Git and GitHub workflow

## Status

### Completed

- [x] Student Spring Boot application
- [x] Faculty Spring Boot application
- [x] Docker containerization
- [x] Kubernetes cluster using Minikube
- [x] Separate Student and Faculty namespaces
- [x] Kubernetes Deployments
- [x] Kubernetes Services
- [x] Calico CNI
- [x] Student NetworkPolicy
- [x] Faculty NetworkPolicy
- [x] Two-way namespace isolation
- [x] Security testing and verification

### Planned

- [ ] Cloud deployment
- [ ] Cloud network architecture
- [ ] IAM configuration
- [ ] Cloud security controls
- [ ] Monitoring and logging
- [ ] Production-oriented deployment

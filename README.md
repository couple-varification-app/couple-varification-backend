# Couple Verification Backend

## Overview
Couple Verification is a backend service designed to help verify the authenticity of couples through a structured and secure verification flow.  
The system focuses on preventing fake profiles, improving trust, and enabling genuine interactions by validating relationship claims using controlled verification steps.

This repository contains the **backend implementation** built with Spring Boot and follows production-style architecture and best practices.

---

## Problem Statement
Many platforms suffer from fake couple profiles, misuse, and lack of trust.  
Manual verification is slow, inconsistent, and not scalable.

This project aims to:
- Reduce fake couple registrations
- Introduce structured and auditable verification
- Provide a scalable backend system that supports real-world growth

---

## Core Features
- User registration and authentication
- Couple pairing and request flow
- Verification workflow (multi-step)
- Secure APIs with role-based access
- Status tracking for verification requests
- Audit-friendly design for future moderation

---

## Tech Stack
- Java 17
- Spring Boot
- Spring Security (JWT-based authentication)
- JPA / Hibernate
- Relational Database (PostgreSQL / MySQL)
- Docker (for containerization)
- RESTful API design

---

## High-Level Flow
1. Users register and authenticate
2. One user sends a couple verification request
3. The other user confirms participation
4. Verification steps are completed
5. Final verification status is updated and stored

---

## Project Structure (Simplified)

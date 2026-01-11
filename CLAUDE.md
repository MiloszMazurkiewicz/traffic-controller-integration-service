# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Traffic Controller Integration Service - a Java-based service (based on .gitignore configuration).

## Current State

This is a newly initialized repository. Build system and source code structure have not yet been established.

## Build Commands

*To be added once build configuration (Maven/Gradle) is set up.*

## Architecture

*To be documented as the codebase develops.*

## How Claude should work
- Always propose a step-by-step plan and wait for my approval before editing any files.
- Prefer small, incremental changes with clear diffs.
- For every change, draft GitHub-ready comments or PR review notes.
- When unsure, ask a clarifying question instead of guessing.
- Test your changes thoroughly before submitting a PR.

## Spring Boot Guidelines
- Follow standard Spring Boot conventions, similar to Baeldung’s recommendations.
- Prefer constructor-based dependency injection, avoid field injection.
- Keep configuration in `application.yml`
- Write focused `@Service`, `@Repository`, and `@RestController` classes with clear responsibilities.
- When unsure about an approach, choose the idiomatic Spring Boot way as commonly described on Baeldung.
- Use lombok for boilerplate code
- Use Spring Data JPA for database access
- Use PostgreSql for database
- Use Flyway and Test Containers for database migrations and tests
- Use maven for build
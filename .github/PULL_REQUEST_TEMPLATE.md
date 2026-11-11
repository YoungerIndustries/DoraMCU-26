name: Pull Request
description: Submit a pull request for review
body:
  - type: markdown
    attributes:
      value: |
        Thanks for improving DoraMCU! Please fill out this form to help reviewers understand your changes.

  - type: textarea
    id: description
    attributes:
      label: Description
      description: Briefly describe your changes.
      placeholder: "This PR adds..."
    validations:
      required: true

  - type: textarea
    id: issue
    attributes:
      label: Related Issue
      description: Link to the related issue, if any.
      placeholder: "Fixes #123"

  - type: dropdown
    id: type
    attributes:
      label: Type of Change
      description: What type of change does this PR introduce?
      options:
        - Bug fix
        - New feature
        - Refactoring
        - Documentation update
        - CI/CD improvement
        - Dependency update
        - Other
    validations:
      required: true

  - type: dropdown
    id: module
    attributes:
      label: Affected Module
      description: Which module does this PR affect?
      options:
        - Core Application
        - Traveller
        - Star Trek 4X
        - Babylon 5
        - Earth 2026
        - D&D 5E
        - Hex Galaxy
        - AI / Deep Learning
        - Neuron Federation
        - Frontend (Electron/PWA)
        - Infrastructure (CI/CD, Docker, K8s)
        - Multiple modules
    validations:
      required: true

  - type: textarea
    id: testing
    attributes:
      label: Testing
      description: Describe the tests you ran and how to reproduce them.
      placeholder: |
        - Ran `./gradlew test` and all tests pass
        - Manually tested the new endpoint with curl/Postman
    validations:
      required: true

  - type: checkboxes
    id: checklist
    attributes:
      label: Checklist
      options:
        - label: "My code follows the existing code style"
          required: true
        - label: "I have performed a self-review of my code"
          required: true
        - label: "I have commented my code where necessary"
          required: false
        - label: "I have added tests that prove my fix/feature works"
          required: false
        - label: "New and existing unit tests pass locally"
          required: true
        - label: "I have updated documentation where necessary"
          required: false

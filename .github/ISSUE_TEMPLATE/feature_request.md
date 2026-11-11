name: Feature Request
description: Suggest a new feature or enhancement
title: "[FEATURE] "
labels: ["enhancement"]
body:
  - type: markdown
    attributes:
      value: |
        Thanks for suggesting a feature! Please fill out the form below to help us understand your idea.

  - type: textarea
    id: problem
    attributes:
      label: Is your feature request related to a problem?
      description: A clear description of the problem or limitation.
      placeholder: "I'm always frustrated when there's no way to..."
    validations:
      required: true

  - type: textarea
    id: solution
    attributes:
      label: Proposed Solution
      description: Describe the solution you'd like.
      placeholder: "It would be great if DoraMCU could..."
    validations:
      required: true

  - type: textarea
    id: alternatives
    attributes:
      label: Alternative Solutions
      description: Any alternative solutions or features you've considered.

  - type: dropdown
    id: module
    attributes:
      label: Related Module
      description: Which module would this feature affect?
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
        - Other
    validations:
      required: true

  - type: textarea
    id: additional
    attributes:
      label: Additional Context
      description: Any additional context, mockups, or references.

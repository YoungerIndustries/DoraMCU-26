name: Bug Report
description: Report a bug or unexpected behavior
title: "[BUG] "
labels: ["bug"]
body:
  - type: markdown
    attributes:
      value: |
        Thanks for reporting a bug! Please fill out the form below to help us reproduce and fix the issue.

  - type: textarea
    id: description
    attributes:
      label: Description
      description: A clear and concise description of what the bug is.
      placeholder: "When I try to generate a galaxy, the application crashes with..."
    validations:
      required: true

  - type: textarea
    id: steps
    attributes:
      label: Steps to Reproduce
      description: Exact steps to reproduce the behavior.
      placeholder: |
        1. Start the application with `./gradlew bootRun`
        2. Call POST /api/v1/babylon5/galaxy/generate
        3. Observe the error...
    validations:
      required: true

  - type: textarea
    id: expected
    attributes:
      label: Expected Behavior
      description: What you expected to happen.
      placeholder: "The galaxy should be generated successfully with 13 empires."
    validations:
      required: true

  - type: textarea
    id: actual
    attributes:
      label: Actual Behavior
      description: What actually happened.
      placeholder: "The application throws a NullPointerException..."
    validations:
      required: true

  - type: input
    id: version
    attributes:
      label: Version
      description: What version of DoraMCU are you running?
      placeholder: "0.3.0"
    validations:
      required: true

  - type: dropdown
    id: module
    attributes:
      label: Affected Module
      description: Which module is affected?
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
        - Other
    validations:
      required: true

  - type: textarea
    id: logs
    attributes:
      label: Relevant Log Output
      description: Paste any relevant log output here (auto-formatted as shell).
      render: shell

  - type: textarea
    id: additional
    attributes:
      label: Additional Context
      description: Any additional context, screenshots, or environment details.

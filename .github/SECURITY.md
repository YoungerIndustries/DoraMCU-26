# Security Policy

## Supported Versions

| Version | Supported          |
|---------|--------------------|
| 0.1.x   | :white_check_mark: |

## Reporting a Vulnerability

We take the security of DoraMCU seriously. If you discover a security vulnerability, please report it to us responsibly.

### How to Report

1. **Do NOT** create a public GitHub issue for security vulnerabilities.
2. Send an email to the repository maintainer with a detailed description of the vulnerability.
3. Include steps to reproduce the issue if possible.
4. We will acknowledge receipt of your report within **48 hours**.
5. We will provide a more detailed response within **7 days** indicating the next steps.

### What to Include

- A description of the vulnerability
- Steps to reproduce the issue
- Potential impact assessment
- Any suggested fixes (optional)

### Security Measures

DoraMCU implements the following security measures:

- **Authentication**: JWT-based authentication with rate limiting on login endpoints
- **Authorization**: Role-based access control (admin/user)
- **Input Validation**: Spring Boot validation on all API inputs
- **Rate Limiting**: Configurable rate limiting to prevent abuse
- **Dependency Scanning**: Dependabot configured for automated dependency updates

## Security Best Practices

- Always use the latest version of DoraMCU
- Keep dependencies up to date
- Use strong passwords for admin accounts
- Configure proper network security for production deployments
- Enable HTTPS in production environments

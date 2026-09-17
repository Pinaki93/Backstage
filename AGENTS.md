# Project overview

Backstage is a simple HTTP server that provides a utilities dashboard for a running Android app.

# Development requirements

- Treat `:backstage` as the public API module.
- Implement every API in both `:backstage:impl` and `:backstage:noOp`.
- Add corresponding tests for every code change.
- Run Android lint and ensure it passes before considering work complete.

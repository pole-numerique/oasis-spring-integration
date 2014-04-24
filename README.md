OASIS Spring Integration
========================

Spring 4 OASIS client libraries, spun off the Citizen Kin project.

_A word of warning_: this implementation has been largely driven by the needs of Citizen Kin (and later the OASIS Portal). It may not be feature-complete or even particularly relevant to other use cases. It is also fairly intrusive in terms of the technology stack used (Spring 4.0.x, Spring MVC, Spring Security 3.2.x, all used with Java Config, etc.) so if your goal is to build an application based on a slightly different framework / application, then it may not be the best choice around.

Integration of new/more sophisticated features is welcome through pull requests.

Features
--------

The library covers the following features:

- A Spring Security authentication provider for the OASIS Kernel
- Token Refresh interceptor to renew access tokens when they are about to expire
- User Info fetcher (with support for scopes)
- Token revocation through a logout handler

Features implemented in Citizen Kin which will progressively migrated here:
- Notifications (write-only at the present)
- Event bus publishing
- User Directory
- Data core (limited functionality, but working implementation with both read and write capabilities)

Features needed for the Portal will be directly implemented here, consisting of:
- Social Graph
- Notification read/update
- Possibly an event bus receiver helper
- Market
- Application / Organization creation
- Agent creation?


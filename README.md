=====================================================

OASIS - Java Spring Integration
http://www.ozwillo.com/
https://github.com/ozwillo/ozwillo-java-spring-integration
Copyright (c) 2013-2015 Open Wide - http://www.openwide.fr

=====================================================

Ozwillo Spring Integration
==========================

Spring 4 Ozwillo client libraries, spun off the Citizen Kin project.

_A word of warning_: this implementation has been largely driven by the needs of Citizen Kin (and later the Ozwillo Portal). It may not be feature-complete or even particularly relevant to other use cases. It is also fairly intrusive in terms of the technology stack used (Spring 4.0.x, Spring MVC, Spring Security 3.2.x, all used with Java Config, etc.) so if your goal is to build an application based on a slightly different framework / application, then it may not be the best choice around.

Integration of new/more sophisticated features is welcome through pull requests.

Features
--------

The library covers the following features:

- A Spring Security authentication provider for the Ozwillo Kernel
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


## License

This library is provided under LGPL v3.

```
Copyright (C) 2014 Atol Conseils et Développements

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Lesser General Public License as
published by the Free Software Foundation, either version 3 of the
License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.
```
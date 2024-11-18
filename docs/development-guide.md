# Development Guide

This documentation file will be the guide for developers who want to contribute to the MarkDock API project. It outlines some
key aspects of the project that developers should be aware of.

## Table of Contents
- [Authentication and Security](#authentication-and-security)
- [Defining Public and Private Routes](#defining-public-and-private-routes)
- [Organization Routes](#organization-routes)

---

## Authentication and Security
This API uses JWT tokens stored inside cookies for authentication. The API has two types of routes: public and private. Public routes are accessible
without any authentication, while private routes require a valid JWT token to access them.

Inside our `WebSecurityConfig` class (config/WebSecurityConfig.java), we have defined our setup for security. You can see that we have
defined a filter for JWT authorization. This filter checks if the incoming request has a valid JWT token. If the token is valid, the
request is allowed to pass through. If the token is invalid, the request is rejected.

If you are testing private routes, you will need to be an authenticated user. To do this, you can run our `/auth/login` endpoint to get a
valid JWT token. If you are using Postman, cookies will be stored automatically, and it will use the cookie for subsequent requests.

---

## Defining Public and Private Routes
To define a public route, you can go into the JWTAuthorizationFilter class (security/JWTAuthorizationFilter.java) and add the route to the `PUBLIC_ROUTES` list. This list contains
all the routes that are public and do not require authentication.

This is not super ideal as it requires developers to manually add routes to the list but considering the size of the project, it is manageable. This is
something that can be improved in the future.

Any route that is not in the `PUBLIC_ROUTES` list is considered a private route and will require a valid JWT token to access.

---

## Organization Routes
The organization routes are a special case. These routes are private but require a different type of authentication. The organization routes require
the user to be a member of the organization to access them. This is handled using AOP (Aspect Oriented Programming) by defining an annotation like
`@OrganizationViewer` and applying it to the organization route method that require the member to have an access level of `VIEWER` or higher.

Other routes that require the user to be an admin of the organization are annotated with `@OrganizationAdmin`. These annotations are defined in the
security/organization package. The aspect that handles these annotations is defined in the security/organization package as well.

### Structuring Organization Routes
Organization routes are structured in a way that the organization ID is passed as a path variable. This is done to ensure that the user is accessing
the organization that they are a member of. The organization ID is checked against the user organization relation table to ensure that the user is a member of
the organization.

Here is an example of an organization route:

```declarative
POST /organization/{organizationId}/create-invite-link
```

```java
// Inside organziation controller
@OrganizationAdmin
@PostMapping("/{org_id}/create-invite-link")
public ResponseEntity<Map<String, Object>> createInviteLink(
        @PathVariable("org_id") String organizationId {
```

This route is an organization route that requires the user to be an admin of the organization. The organization ID is passed as a path variable and
is checked against the user's organization ID to ensure that the user is a member of the organization and has the required access level (admin in this case).
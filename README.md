# GregBot
> REST Client for developers with a focus on [identity](#identity), [secret management](#secret-management) and 
> [request organisation](#request-organisation)

## Identity
First class identity support by providing:
1. a plug-in architecture for authentication providers
2. the ability to configure and select identities independently within an environment 
   * an environment (e.g. Production, Stage, ...) would hold values for many requests 
     (e.g. `service_1_host`, `service_2_host`)
   * within an environment, an identity would hold credentials (e.g. api-key, auth token, ...) that can be used by each
     request (e.g. `user_1_api_key`, `user_2_token`)

## Secret Management
Avoid the need to store secrets locally by providing:
1. a plug-in architecture for secret providers
2. a mechanism to allow requests to reference secrets not stored locally

## Request Organisation
Allow a request to exist in many groups (which can be heavily nested) without the need to copy/paste the request itself.
i.e. a request could appear in a grouping with all the other requests for that service as well as in a group for related
requests to many services (perhaps a workflow of some sort).

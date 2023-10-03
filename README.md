# GregBot
> REST Client for developers with a focus on [identity](#identity), [secret management](#secret-management) and 
> [request organisation](#request-organisation)

[![CircleCI](https://dl.circleci.com/status-badge/img/gh/michaelcowan/gregbot/tree/master.svg?style=svg)](https://dl.circleci.com/status-badge/redirect/gh/michaelcowan/gregbot/tree/master)

## Identity
First class identity support by providing:
1. a plug-in architecture for authentication providers
2. the ability to configure and select identities independently of an environment

## Secret Management
Avoid the need to store secrets locally by providing:
1. a plug-in architecture for secret providers
2. a mechanism to allow identities to reference secrets not stored locally

## Request Organisation
Allow a request to exist in many groups (which can be heavily nested) without the need to copy/paste the request itself.

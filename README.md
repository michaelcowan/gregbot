# GregBot
> REST Client for developers with a focus on [identity](#identity), [secret management](#secret-management) and 
> [request organisation](#request-organisation)

[![CircleCI](https://img.shields.io/circleci/build/github/michaelcowan/gregbot/master.svg)](https://dl.circleci.com/status-badge/redirect/gh/michaelcowan/gregbot/tree/master)
[![Codecov](https://img.shields.io/codecov/c/github/michaelcowan/gregbot)](https://codecov.io/github/michaelcowan/gregbot)
[![CodeFactor Grade](https://img.shields.io/codefactor/grade/github/michaelcowan/gregbot)](https://www.codefactor.io/repository/github/michaelcowan/gregbot)

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

# bookatop.com

Booking places around the World

## Services

-api-gateway - it provides entry point to the services environment

-auth - it provides user with authorization/authentication and refreshing security token

-discovery-registry - it helps the microservices to find each other

-hotel-radar - it supplies list of hotels depending on options

-property-reg - responsible for property registration

-catalog-book - provides information to simplify entering data

## Tasks

- **BKTBE-0006** - Disable validation of IPs on the gateway service except the auth service  

- **BKTBE-0004** - Throw a forbidden error when request has Bearer token and tries to get data from the endpoint annotated @AccessUserRoles(permitAll = true) 


# Spring Boot 3 + Spring Cloud Business Extension Examples

This project demonstrates how to adapt framework extension points to common business scenarios without forking framework source code.

## Modules

| Module | Port | Description |
|---|---|---|
| eureka-server | 8761 | Service registry |
| gateway | 8080 | Authentication, tenant identification, and gray-release tagging |
| user-service | 8082 / 8083 (v2) | User service |
| order-service | 8081 / 8084 (v2) | Order service with strategy-based pricing and Feign calls to user-service |
| common-starter | - | Custom starter for unified responses, exception handling, rate limiting, context propagation, and gray load balancing |

## Scenarios and Extension Points

| Scenario | Extension point | Location |
|---|---|---|
| End-to-end gray release | `ReactorServiceInstanceLoadBalancer` + `@LoadBalancerClients` | common-starter/gray |
| Gateway authentication, tenant identification, and gray tagging | `GlobalFilter` + `@ConfigurationProperties` | gateway |
| Propagating tenant and gray-release headers between services | Servlet `Filter` + Feign `RequestInterceptor` | common-starter/web |
| Unified responses and exception handling | `ResponseBodyAdvice` + `@RestControllerAdvice` | common-starter/web |
| API rate limiting | Custom annotation + AOP | common-starter/web |
| Standard, VIP, and flash-sale pricing | Strategy pattern + implementation switch with `@ConditionalOnProperty` | order-service/strategy |
| Applying shared enhancements to strategy beans (execution-time logging) | `BeanPostProcessor` | order-service/strategy |
| Auto-configuration | `AutoConfiguration.imports` | common-starter |

## Running the Demo (JDK 17 + Maven)

```bash
mvn clean install -DskipTests
# Start the services in this order
java -jar eureka-server/target/eureka-server-1.0.0.jar
java -jar user-service/target/user-service-1.0.0.jar                          # v1 :8082
java -jar user-service/target/user-service-1.0.0.jar --spring.profiles.active=v2   # v2 :8083
java -jar order-service/target/order-service-1.0.0.jar                        # v1 :8081
java -jar order-service/target/order-service-1.0.0.jar --spring.profiles.active=v2 # v2 :8084
java -jar gateway/target/gateway-1.0.0.jar
```

Wait about 30 seconds for the gateway and clients to fetch the service registry.

## Feature Verification

### Modified Classes

| Feature | Modified classes | What to verify |
|---|---|---|
| Auto-configuration | `CommonWebAutoConfiguration`, `GrayLoadBalancerAutoConfiguration`, `AutoConfiguration.imports` | After adding common-starter, unified responses, exception handling, context propagation, rate limiting, and gray load balancing are enabled |
| Gateway authentication and tenant identification | `AuthGlobalFilter` | Missing or malformed tokens return 401; valid tokens are parsed for tenant and user IDs and override client-supplied identity headers |
| Gateway gray-release tagging | `GrayGlobalFilter`, `GrayProperties` | Requests from allowlisted users receive `X-Version: v2` |
| Gray instance selection | `GrayLoadBalancer`, `GrayLoadBalancerConfiguration` | `X-Version` selects the matching instance version; without a version header, v1 is preferred |
| Request context and Feign propagation | `RequestContext`, `ContextFilter`, `FeignContextInterceptor` | Tenant, user, and version information flows from the gateway to order-service and then through Feign to user-service |
| Unified responses and exception handling | `ResultWrapAdvice`, `GlobalExceptionHandler` | Business objects are wrapped in `Result`; business exceptions return the corresponding business code |
| API rate limiting | `RateLimit`, `RateLimitAspect` | Requests exceeding the configured window limit for a tenant return 429 |
| Pricing strategies and lifecycle enhancement | `PriceStrategyFactory`, `PriceStrategy` implementations, `StrategyTimingBeanPostProcessor` | The correct discount is applied for each scene/version, and strategy execution time is logged |

### Prerequisites

Start Eureka, both user-service versions, both order-service versions, and the gateway using the commands above. Wait about 30 seconds for registration and service discovery. The examples below assume all services are running locally on their default ports.

### Gateway Authentication, Unified Responses, and Exceptions

The token format is `token-{tenant}-{userId}`. User 1001 is on the gray-release allowlist.

```bash
# No token: expect HTTP 401
curl -i -X POST localhost:8080/api/orders/create \
  -H 'Content-Type: application/json' -d '{"productId":1,"amount":100}'

# Valid token: expect a unified Result response with code=0 and order data
curl -s localhost:8080/api/users/1003 -H 'Authorization: token-t1-1003'

# User ID >= 9000: expect a unified Result response with code=404 and a user-not-found message
curl -s localhost:8080/api/users/9001 -H 'Authorization: token-t1-9001'
```

Try including `X-Tenant-Id` or `X-User-Id` with a valid request. Downstream services should use the tenant and user IDs parsed from the token, not the client-supplied values.

### End-to-End Gray Release and Pricing

```bash
# Non-gray user: expect orderServedBy=v1@8081 and userServedBy=v1@8082
# User 1003 is a VIP; v1 applies a 10% discount, so payAmount=90.00
curl -s -X POST localhost:8080/api/orders/create \
  -H 'Authorization: token-t1-1003' -H 'Content-Type: application/json' \
  -d '{"productId":1,"amount":100}'

# Gray user: expect orderServedBy=v2@8084 and userServedBy=v2@8083
# User 1001 is a VIP; v2 applies a 15% discount, so payAmount=85.00
curl -s -X POST localhost:8080/api/orders/create \
  -H 'Authorization: token-t1-1001' -H 'Content-Type: application/json' \
  -d '{"productId":1,"amount":100}'

# Explicit flash-sale scene: expect scene=SECKILL and payAmount=50.00
curl -s -X POST localhost:8080/api/orders/create \
  -H 'Authorization: token-t2-1002' -H 'Content-Type: application/json' \
  -d '{"productId":1,"amount":100,"scene":"SECKILL"}'
```

Use `tenantId` and `userId` in the response to verify identity context. `orderServedBy` and `userServedBy` show whether the gray-release header was preserved across the gateway, load balancer, and Feign call. Service logs should contain strategy timing entries prefixed with `[strategy]`.

### Rate Limiting

The order creation endpoint allows 10 requests per tenant per second; the user lookup endpoint allows 20. Send more requests than the limit for the same tenant in a short period. Excess requests should return a unified error response with `code=429`. Requests should succeed again after the window expires. You can test through the gateway or call a service directly with an explicit `X-Tenant-Id` header to verify per-tenant isolation.

```bash
# Send concurrent user lookups for one tenant; expect both 200 and 429 responses
{
  for i in $(seq 1 25); do
    curl -s -o /dev/null -w '%{http_code}\n' \
      localhost:8080/api/users/1003 -H 'Authorization: token-t1-1003' &
  done
  wait
} | sort | uniq -c
```

Repeat with `token-t2-1003` to verify that a different tenant has a separate limit. After waiting more than one second, requests from the first tenant should succeed again.
# cloud-demo

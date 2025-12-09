# Data Masking Spring Boot Starter

A lightweight Spring Boot utility library for automatically masking sensitive data in logs and toString() outputs.

## Features

- ✅ `@Sensitive` annotation for marking sensitive fields
- ✅ Multiple masking strategies (FULL, FIRST_LAST, LAST_FOUR, CUSTOM)
- ✅ Works automatically when objects are logged
- ✅ Simple `MaskingHelper.mask()` utility for explicit control
- ✅ Configurable mask characters
- ✅ Support for nested objects, collections, arrays, and maps
- ✅ Zero configuration required - just add the dependency

## Installation

### Maven

Add this dependency to your `pom.xml`:

```xml
<dependency>
    <groupId>com.utility</groupId>
    <artifactId>data-masking-spring-boot-starter</artifactId>
    <version>1.0.0</version>
</dependency>
```

### Gradle

```gradle
implementation 'com.utility:data-masking-spring-boot-starter:1.0.0'
```

## Quick Start

### 1. Annotate Sensitive Fields

```java
public class User {
    private String username;
    
    @Sensitive(strategy = MaskStrategy.FULL)
    private String password;
    
    @Sensitive(strategy = MaskStrategy.FIRST_LAST)
    private String email;
    
    @Sensitive(strategy = MaskStrategy.LAST_FOUR)
    private String creditCard;
}
```

### 2. Use MaskingHelper in Logs

```java
@Service
public class UserService {
    private static final Logger log = LoggerFactory.getLogger(UserService.class);
    
    public void createUser(User user) {
        log.info("Creating user: {}", MaskingHelper.mask(user));
        // Output: Creating user: User{username=john, password=********, email=j***m, creditCard=***1234}
    }
}
```

### 3. Or Override toString() for Automatic Masking

```java
public class User {
    @Sensitive(strategy = MaskStrategy.FULL)
    private String password;
    
    // Override toString to automatically mask when logged
    @Override
    public String toString() {
        return MaskingHelper.mask(this);
    }
}

// Now you can just log directly:
log.info("User: {}", user); // Automatically masked!
```

## Masking Strategies

### MaskStrategy.FULL
Masks all characters completely.

**Example:** `"password123"` → `"***********"`

```java
@Sensitive(strategy = MaskStrategy.FULL)
private String password;
```

### MaskStrategy.FIRST_LAST
Keeps first and last characters, masks everything in between.

**Example:** `"password123"` → `"p*********3"`

```java
@Sensitive(strategy = MaskStrategy.FIRST_LAST)
private String email;
```

### MaskStrategy.LAST_FOUR
Keeps last 4 characters, masks everything before.

**Example:** `"1234567890"` → `"******7890"`

```java
@Sensitive(strategy = MaskStrategy.LAST_FOUR)
private String creditCard;
```

### MaskStrategy.CUSTOM
Use custom masking logic by implementing `DataMasker` interface.

```java
public class EmailMasker implements DataMasker {
    @Override
    public String mask(String value, char maskChar) {
        if (value == null || !value.contains("@")) {
            return defaultMask(value, maskChar);
        }
        String[] parts = value.split("@");
        String masked = String.valueOf(maskChar).repeat(parts[0].length());
        return masked + "@" + parts[1];
    }
}

// Usage
@Sensitive(strategy = MaskStrategy.CUSTOM, customMasker = EmailMasker.class)
private String email; // john@example.com → ****@example.com
```

## Usage Examples

### Basic Logging

```java
User user = new User("john", "secret123", "john@example.com");
log.info("User: {}", MaskingHelper.mask(user));
// Output: User{username=john, password=*********, email=j***************m}
```

### Collections and Arrays

```java
List<User> users = Arrays.asList(user1, user2, user3);
log.info("Users: {}", MaskingHelper.mask(users));
// All sensitive fields in all users will be masked
```

### Nested Objects

```java
public class Order {
    private Long orderId;
    private User user; // User's sensitive fields will be automatically masked
    
    @Sensitive(strategy = MaskStrategy.FULL)
    private String orderSecret;
}

log.info("Order: {}", MaskingHelper.mask(order));
// Both Order and nested User sensitive fields are masked
```

### Maps

```java
Map<String, User> userMap = new HashMap<>();
userMap.put("john", user);
log.info("Users: {}", MaskingHelper.mask(userMap));
```

### Exception Logging

```java
try {
    processPayment(payment);
} catch (Exception e) {
    log.error("Payment failed: {}", MaskingHelper.mask(payment), e);
}
```

## Configuration Options

### Custom Mask Character

```java
@Sensitive(strategy = MaskStrategy.FULL, maskChar = '#')
private String secret; // Masks with '#' instead of '*'
```

### Combining with Lombok

```java
import lombok.Data;
import lombok.ToString;

@Data
public class User {
    private String username;
    
    @Sensitive(strategy = MaskStrategy.FULL)
    private String password;
    
    // Use Lombok but exclude toString, then add custom one
    @ToString.Exclude
    private transient String ignored;
    
    @Override
    public String toString() {
        return MaskingHelper.mask(this);
    }
}
```

## Best Practices

### 1. Always Mask PII (Personally Identifiable Information)

Fields to always annotate with `@Sensitive`:
- Passwords and secrets
- Credit card numbers
- Social Security Numbers (SSN)
- Email addresses (use FIRST_LAST or CUSTOM)
- Phone numbers
- API keys and tokens
- Session IDs

### 2. Choose Appropriate Strategies

- **FULL**: Passwords, secrets, API keys
- **LAST_FOUR**: Credit cards, account numbers
- **FIRST_LAST**: Emails, names (when partial visibility is needed)
- **CUSTOM**: Special formats like email (preserve domain)

### 3. Override toString() for Convenience

```java
@Override
public String toString() {
    return MaskingHelper.mask(this);
}
```

This way you can log objects directly without calling `MaskingHelper.mask()` every time.

### 4. Test Your Masking

Always verify sensitive data doesn't leak:

```java
@Test
public void testSensitiveDataMasked() {
    User user = new User("john", "password123", "john@example.com");
    String masked = MaskingHelper.mask(user);
    
    assertThat(masked).doesNotContain("password123");
    assertThat(masked).contains("***");
}
```

## Advanced Features

### Working with Jackson/JSON

```java
// For JSON serialization, you might want a separate DTO
public class UserDTO {
    private String username;
    
    @JsonProperty("password")
    @Sensitive(strategy = MaskStrategy.FULL)
    private String password;
    
    @Override
    public String toString() {
        return MaskingHelper.mask(this);
    }
}
```

### Performance Considerations

- Masking uses reflection, so avoid calling it in tight loops
- For high-throughput scenarios, consider caching masked representations
- The library is thread-safe

### Integration with Logging Frameworks

Works with all SLF4J compatible loggers:
- Logback
- Log4j2
- java.util.logging

## Project Structure

```
data-masking-spring-boot-starter/
├── src/main/java/com/utility/masking/
│   ├── annotation/
│   │   └── Sensitive.java
│   ├── config/
│   │   └── MaskingAutoConfiguration.java
│   ├── converter/
│   │   └── MaskingMessageConverter.java
│   ├── masker/
│   │   └── DataMasker.java
│   ├── service/
│   │   └── MaskingService.java
│   ├── strategy/
│   │   └── MaskStrategy.java
│   └── util/
│       └── MaskingHelper.java
├── src/main/resources/META-INF/
│   └── spring.factories
└── pom.xml
```

## Requirements

- Java 17+
- Spring Boot 3.2.0+
- Maven or Gradle

## Comparison with @LogSensitive Approach

This library takes a simpler approach:
- **No method-level annotation needed** - Just annotate fields with `@Sensitive`
- **More flexible** - Use `MaskingHelper.mask()` anywhere you need it
- **Cleaner** - Override `toString()` once for automatic masking everywhere
- **Less magic** - Explicit control over when masking happens

## License

MIT License

## Contributing

Contributions are welcome! Please submit a Pull Request.

## Support

For issues and questions, please create an issue on GitHub.

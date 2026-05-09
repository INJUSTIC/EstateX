---
applyTo: "**/*Test.java, **/*.test.tsx, **/*.test.ts"
---

# Testing standards
When creating tests, use "shouldDoSomethingWhenCondition" format for test naming, where "shouldDoSomething" describes the expected behavior and "WhenCondition" describes the specific condition under which the behavior is expected to occur. Here is java example of the test method structure:
```java
@Test
public void shouldDoSomethingWhenCondition() {
    /// given
     Set up the necessary preconditions and inputs for the test

    /// when
     Execute the code being tested

    /// then
     Assert the expected outcomes and behaviors
}
```

Here is a TypeScript example of the test method structure:
```typescript
test('should do something when condition', () => {
    // given
    Set up the necessary preconditions and inputs for the test

    // when
    Execute the code being tested

    // then
    Assert the expected outcomes and behaviors
});
```

given when then comments are required.

Use var keyword in java tests where possible

Test body should be as short as possible. If you find that your test method is becoming too long, break it down into smaller, more focused test methods and helper functions.

If you find a test that doesn't follow the rules above, refactor it to adhere to this convention.

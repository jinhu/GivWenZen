package org.givwenzen;

import org.givwenzen.annotations.DomainStep;
import org.givwenzen.annotations.DomainSteps;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import static org.givwenzen.GWZForJUnit.*;
import static org.junit.Assert.*;
import static org.junit.matchers.JUnitMatchers.containsString;

@DomainSteps
public class DomainStepMethodLocatorTest {
    private DomainStepMethodLocator locator;
    private GivWenZenException retrieveMethodsError;
    private MethodAndInvocationTarget methodWithAnnotatedPatternMatching;
    private Exception executeException;

    @Before
    public void setup() {
        setUp(this);
    }

    @Test
    public void stepClassWithValidStepMethodIsFoundSuccessfully() throws Exception {
        given("a valid step class with a valid step method");
        when("the annotated method is retrieved for 'valid step'");
        then("valid step method is found");
    }

    @Test
    public void testGetMethodWithAnnotatedPatternMatching() throws Exception {
        given("a class exists with two methods that have duplicate DomainStep annotations");
        when("the annotated method is retrieved for 'duplicate'");
        then("a GivWenZenException is thrown");
        and("GivWenZenException message contains first definition");
        and("GivWenZenException message contains second definition");
    }

    @Test
    public void testWhenAnInstanceOfTheSameClassIsInTheListOfAnnotatatedObjectsMoreThanOnceThenTheExactDuplicatesAreIgnored() throws Exception {
        given("a method locator is created with a list containing multiple instances of the same class");
        when("the annotated method is retrieved for 'dummy step'");
        then("duplicates for the exact same class are ignored and no duplicate exception is thrown");
    }

    @Test
    public void testAmbiguousStepDefinitionsCauseAnException() throws Exception {
        given("a class exists with two methods that have ambiguous DomainStep annotations");
        when("the annotated method is retrieved for 'the value 5 is passed as a parameter'");
        then("a GivWenZenException is thrown");
        and("GivWenZenException message contains StepsWithAmbiguousAnnotatedMethod.method1");
        and("GivWenZenException message contains StepsWithAmbiguousAnnotatedMethod.method2");
    }

    @Test
    public void whenAMethodSDoesNotExistsAStepNotFoundMethodIsReturned() throws Exception {
        given("no annotated method exists that matches 'step method does not exists'");
        when("the annotated method is retrieved for 'step method does not exists'");
        then("step not found method is found");
    }

    @Test
    public void whenAMethodSDoesNotExistsAExceptionProducingMethodIsReturned() throws Exception {
        given("no annotated method exists that matches 'step method does not exists'");
        when("the annotated method is retrieved for 'step method does not exists'");
        and("the annotated method is executed for 'step method does not exists'");
        then("DomainStepNotFoundException message contains '@DomainStep(\"step method does not exists\")'");
    }

    @DomainStep("no annotated method exists that matches '(:?.*)'")
    public void createStepLocatorWithNoSteps() {
        createStepLocator(new ArrayList<Object>());
    }

    @DomainStep("a method locator is created with a list containing multiple instances of the same class")
    public void createStepLocatorWithMulitpleInstancesOfAStepClass() {
        createStepLocator(createStepObjectsList(new DomainStepMethodLocatorTest(), new DomainStepMethodLocatorTest()));
    }

    @DomainStep("a class exists with two methods that have duplicate DomainStep annotations")
    public void createStepLocatorWithClassContatiningDuplicateAnnotations() {
        createStepLocator(createStepObjectsList(new StepsWithDuplicateAnnotatedMethod()));
    }

    @DomainStep("a class exists with two methods that have ambiguous DomainStep annotations")
    public void createStepLocatorWithClassContatiningAmbiguousAnnotations() {
        createStepLocator(createStepObjectsList(new StepsWithAmbiguousAnnotatedMethod()));
    }

    @DomainStep("a valid step class with a valid step method")
    public void createStepLocatorWithValidStepClass() {
        createStepLocator(createStepObjectsList(new ValidSteps()));
    }

    @DomainStep("the annotated method is retrieved for '(.*)'")
    public void retrieveAnnotatedMethodAndSaveMethodOrError(String annotationString) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        try {
            methodWithAnnotatedPatternMatching = locator.getMethodWithAnnotatedPatternMatching(annotationString);
            retrieveMethodsError = null;
        } catch (GivWenZenException e) {
            retrieveMethodsError = e;
        }
    }

    @DomainStep("the annotated method is executed for '(.*)'")
    public void executeAnnotatedMethodAndSaveMethodOrError(String annotationString) {
        try {
            methodWithAnnotatedPatternMatching.invoke(annotationString);
            executeException = null;
        } catch (Exception e) {
            executeException = e;
        }
    }

    @DomainStep("(.*) method is found")
    public void verifyMethodWasFound(String methodName) {
        String tempMethodName = methodName.replaceAll(" ", "");
        assertThat(tempMethodName, methodWithAnnotatedPatternMatching.getMethodAsString(), containsString("." + tempMethodName));
    }

    @DomainStep("DomainStepNotFoundException message contains '(.*)'")
    public void verifyDomainStepNotFoundException(String messagePart) {
        assertThat(executeException.getMessage(), containsString(messagePart));
    }

    @DomainStep("GivWenZenException message contains (.*)")
    public void verifyGivWenZenExceptionMessageContains(String messagePart) {
        assertThat(retrieveMethodsError.getMessage(), containsString(messagePart));
    }

    @DomainStep("duplicates for the exact same class are ignored and no duplicate exception is thrown")
    public void verifyNoExceptionOccuredRetrievingAnnotatedMethods() {
        assertNull(retrieveMethodsError);
    }

    @DomainStep("a GivWenZenException is thrown")
    public void verifyGivWenZenExceptionWasThrown() {
        assertNotNull(retrieveMethodsError);
    }

    @DomainStep("dummy step")
    public void dummy() {

    }

    private void createStepLocator(List<Object> stepDefinitions) {
        locator = new DomainStepMethodLocator(stepDefinitions, new CustomParserFinder());
    }

    private List<Object> createStepObjectsList(Object... objects) {
        List<Object> stepDefinitions = new ArrayList<Object>();
        for (Object object : objects) {
            stepDefinitions.add(object);
        }
        return stepDefinitions;
    }

    @DomainSteps
    static class ValidSteps {
        @DomainStep("valid step")
        public void validstep() {

        }
    }

    @DomainSteps
    static class StepsWithDuplicateAnnotatedMethod {

        @DomainStep("duplicate")
        public void method1() {
        }

        @DomainStep("duplicate")
        public void method2() {
        }
    }

    @DomainSteps
    static class StepsWithAmbiguousAnnotatedMethod {
        @DomainStep("the value (\\d+) is passed as a parameter")
        public void method1(int value) {
        }

        @DomainStep("the value (.*) is passed as a parameter")
        public void method2(long value) {
        }
    }
}
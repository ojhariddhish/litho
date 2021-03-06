/*
 * Copyright (c) 2017-present, Facebook, Inc.
 * All rights reserved.
 *
 * This source code is licensed under the BSD-style license found in the
 * LICENSE file in the root directory of this source tree. An additional grant
 * of patent rights can be found in the PATENTS file in the same directory.
 */

package com.facebook.litho;

import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import android.graphics.Rect;
import android.support.v4.util.SimpleArrayMap;
import com.facebook.litho.animation.AnimatedProperties;
import com.facebook.litho.animation.AnimatedProperty;
import com.facebook.litho.animation.PropertyAnimation;
import com.facebook.litho.animation.PropertyHandle;
import com.facebook.litho.animation.SpringTransition;
import com.facebook.litho.animation.TransitionAnimationBinding;
import com.facebook.litho.testing.testrunner.ComponentsTestRunner;
import java.util.ArrayList;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

/**
 * Tests for the creation of animations using the targeting API in {@link Transition}.
 */
@RunWith(ComponentsTestRunner.class)
public class TransitionManagerAnimationCreationTest {

  private TransitionManager mTransitionManager;
  private Transition.TransitionAnimator mTestVerificationAnimator;
  private ArrayList<PropertyAnimation> mCreatedAnimations = new ArrayList<>();

  @Before
  public void setup() {
    mTransitionManager = new TransitionManager(new TransitionManager.OnAnimationCompleteListener() {
      @Override
      public void onAnimationComplete(String transitionKey) {
      }
    });
    mTestVerificationAnimator = new Transition.TransitionAnimator() {
      @Override
      public TransitionAnimationBinding createAnimation(PropertyAnimation propertyAnimation) {
        mCreatedAnimations.add(propertyAnimation);
        return new SpringTransition(propertyAnimation);
      }
    };
  }

  @After
  public void tearDown() {
    mTransitionManager = null;
    mCreatedAnimations.clear();
  }

  @Test
  public void testCreateSingleAnimation() {
    final LayoutState current = createMockLayoutState(
        Transition.parallel(),
        createMockLayoutOutput("test", 0, 0));
    final LayoutState next = createMockLayoutState(
        Transition.parallel(
            Transition.create("test")
                .animate(AnimatedProperties.X)
                .animator(mTestVerificationAnimator)),
        createMockLayoutOutput("test", 10, 10));

    mTransitionManager.setupTransitions(current, next);

    assertThat(mCreatedAnimations).containsExactlyInAnyOrder(
        createPropertyAnimation("test", AnimatedProperties.X, 10));
  }

  @Test
  public void testCreateMultiPropAnimation() {
    final LayoutState current = createMockLayoutState(
        Transition.parallel(),
        createMockLayoutOutput("test", 0, 0));
    final LayoutState next = createMockLayoutState(
        Transition.parallel(
            Transition.create("test")
                .animate(Transition.allProperties())
                .animator(mTestVerificationAnimator)),
        createMockLayoutOutput("test", 10, 10));

    mTransitionManager.setupTransitions(current, next);

    assertThat(mCreatedAnimations).containsExactlyInAnyOrder(
        createPropertyAnimation("test", AnimatedProperties.X, 10),
        createPropertyAnimation("test", AnimatedProperties.Y, 10));
  }

  @Test
  public void testCreateMultiPropAnimationWithNonChangingProp() {
    final LayoutState current = createMockLayoutState(
        Transition.parallel(),
        createMockLayoutOutput("test", 0, 0));
    final LayoutState next = createMockLayoutState(
        Transition.parallel(
            Transition.create("test")
                .animate(Transition.allProperties())
                .animator(mTestVerificationAnimator)),
        createMockLayoutOutput("test", 10, 0));

    mTransitionManager.setupTransitions(current, next);

    assertThat(mCreatedAnimations).containsExactlyInAnyOrder(
        createPropertyAnimation("test", AnimatedProperties.X, 10));
  }

  @Test
  public void testCreateMultiComponentAnimation() {
    final LayoutState current = createMockLayoutState(
        Transition.parallel(),
        createMockLayoutOutput("test1", 0, 0),
        createMockLayoutOutput("test2", 0, 0));
    final LayoutState next = createMockLayoutState(
        Transition.parallel(
            Transition.create(Transition.allKeys())
                .animate(AnimatedProperties.X)
                .animator(mTestVerificationAnimator)),
        createMockLayoutOutput("test1", 10, 10),
        createMockLayoutOutput("test2", -10, -10));

    mTransitionManager.setupTransitions(current, next);

    assertThat(mCreatedAnimations).containsExactlyInAnyOrder(
        createPropertyAnimation("test1", AnimatedProperties.X, 10),
        createPropertyAnimation("test2", AnimatedProperties.X, -10));
  }

  @Test
  public void testSetsOfComponentsAndPropertiesAnimation() {
    final LayoutState current = createMockLayoutState(
        Transition.parallel(),
        createMockLayoutOutput("test1", 0, 0),
        createMockLayoutOutput("test2", 0, 0));
    final LayoutState next = createMockLayoutState(
        Transition.parallel(
            Transition.create("test1", "test2")
                .animate(AnimatedProperties.X, AnimatedProperties.Y)
                .animator(mTestVerificationAnimator)),
        createMockLayoutOutput("test1", 10, 10),
        createMockLayoutOutput("test2", -10, -10));

    mTransitionManager.setupTransitions(current, next);

    assertThat(mCreatedAnimations).containsExactlyInAnyOrder(
        createPropertyAnimation("test1", AnimatedProperties.X, 10),
        createPropertyAnimation("test1", AnimatedProperties.Y, 10),
        createPropertyAnimation("test2", AnimatedProperties.X, -10),
        createPropertyAnimation("test2", AnimatedProperties.Y, -10));
  }

  @Test
  public void testAllKeysAllPropertiesAnimation() {
    final LayoutState current = createMockLayoutState(
        Transition.parallel(),
        createMockLayoutOutput("test1", 0, 0),
        createMockLayoutOutput("test2", 0, 0));
    final LayoutState next = createMockLayoutState(
        Transition.parallel(
            Transition.create(Transition.allKeys())
                .animate(Transition.allProperties())
                .animator(mTestVerificationAnimator)),
        createMockLayoutOutput("test1", 10, 10),
        createMockLayoutOutput("test2", -10, -10));

    mTransitionManager.setupTransitions(current, next);

    assertThat(mCreatedAnimations).containsExactlyInAnyOrder(
        createPropertyAnimation("test1", AnimatedProperties.X, 10),
        createPropertyAnimation("test1", AnimatedProperties.Y, 10),
        createPropertyAnimation("test2", AnimatedProperties.X, -10),
        createPropertyAnimation("test2", AnimatedProperties.Y, -10));
  }

  @Test
  public void testKeyDoesntExist() {
    final LayoutState current = createMockLayoutState(
        Transition.parallel(),
        createMockLayoutOutput("test", 0, 0));
    final LayoutState next = createMockLayoutState(
        Transition.parallel(
            Transition.create("test", "keydoesntexist")
                .animate(Transition.allProperties())
                .animator(mTestVerificationAnimator)),
        createMockLayoutOutput("test", 10, 0));

    mTransitionManager.setupTransitions(current, next);

    assertThat(mCreatedAnimations).containsExactlyInAnyOrder(
        createPropertyAnimation("test", AnimatedProperties.X, 10));
  }

  private PropertyAnimation createPropertyAnimation(
      String key,
      AnimatedProperty property,
      float endValue) {
    return new PropertyAnimation(new PropertyHandle(key, property), endValue);
  }

  /**
   * @return a mock LayoutState that only has a transition key -> LayoutOutput mapping.
   */
  private LayoutState createMockLayoutState(
      TransitionSet transitions,
      LayoutOutput... layoutOutputs) {
    final SimpleArrayMap<String, LayoutOutput> transitionKeyMapping = new SimpleArrayMap<>();
    for (int i = 0; i < layoutOutputs.length; i++) {
      final LayoutOutput layoutOutput = layoutOutputs[i];
      transitionKeyMapping.put(layoutOutput.getTransitionKey(), layoutOutput);
    }

    final TransitionContext transitionContext = mock(TransitionContext.class);
    when(transitionContext.getRootTransition()).thenReturn(transitions);

    final LayoutState layoutState = mock(LayoutState.class);
    when(layoutState.getTransitionContext()).thenReturn(transitionContext);
    when(layoutState.getTransitionKeyMapping()).thenReturn(transitionKeyMapping);
    when(layoutState.getLayoutOutputForTransitionKey(anyString())).then(new Answer<LayoutOutput>() {
      @Override
      public LayoutOutput answer(InvocationOnMock invocation) throws Throwable {
        final String transitionKey = (String) invocation.getArguments()[0];
        return transitionKeyMapping.get(transitionKey);
      }
    });
    return layoutState;
  }

  /**
   * @return a mock LayoutOutput with a transition key and dummy bounds.
   */
  private LayoutOutput createMockLayoutOutput(String transitionKey, int x, int y) {
    final LayoutOutput layoutOutput = mock(LayoutOutput.class);
    when(layoutOutput.getTransitionKey()).thenReturn(transitionKey);
    when(layoutOutput.getBounds()).thenReturn(new Rect(x, y, 100, 100));
    return layoutOutput;
  }
}

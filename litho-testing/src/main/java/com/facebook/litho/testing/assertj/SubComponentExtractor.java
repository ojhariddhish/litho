/*
 * Copyright (c) 2017-present, Facebook, Inc.
 * All rights reserved.
 *
 * This source code is licensed under the BSD-style license found in the
 * LICENSE file in the root directory of this source tree. An additional grant
 * of patent rights can be found in the PATENTS file in the same directory.
 */

package com.facebook.litho.testing.assertj;

import com.facebook.litho.Component;
import com.facebook.litho.ComponentContext;
import com.facebook.litho.DebugComponent;
import com.facebook.litho.LithoView;
import com.facebook.litho.testing.ComponentTestHelper;
import com.facebook.litho.testing.InspectableComponent;
import java.util.List;
import org.assertj.core.api.Condition;
import org.assertj.core.api.iterable.Extractor;
import org.assertj.core.util.Preconditions;

public class SubComponentExtractor implements Extractor<Component<?>, List<InspectableComponent>> {

  private final ComponentContext mComponentContext;

  SubComponentExtractor(ComponentContext componentContext) {
    mComponentContext = componentContext;
  }

  @Override
  public List<InspectableComponent> extract(Component<?> input) {
    final LithoView lithoView = ComponentTestHelper.mountComponent(mComponentContext, input);
    final InspectableComponent component = InspectableComponent.getRootInstance(lithoView);

    Preconditions.checkNotNull(component,
        "Could not obtain DebugComponent. "
            + "Please ensure that ComponentsConfiguration.IS_INTERNAL_BUILD is enabled.");

    return component.getChildComponents();
  }

  public static SubComponentExtractor subComponents(ComponentContext c) {
    return new SubComponentExtractor(c);
  }

  public static Condition<? super Component> subComponentWith(final ComponentContext c, final Condition<InspectableComponent> inner) {
    // TODO(T20862132): Provide better error messages.
    return new Condition<Component>() {
      @Override
      public boolean matches(Component value) {
        for (InspectableComponent component : new SubComponentExtractor(c)
            .extract(value)) {
          if (inner.matches(component)) {
            return true;
          }
        }

        return false;
      }
    };
  }
}

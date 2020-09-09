// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.eventualSkills;

import org.terasology.engine.input.BindButtonEvent;
import org.terasology.engine.input.DefaultBinding;
import org.terasology.engine.input.RegisterBindButton;
import org.terasology.nui.input.InputType;
import org.terasology.nui.input.Keyboard;

@RegisterBindButton(id = "eventualSkills", description = "Open Skills", category = "general")
@DefaultBinding(type = InputType.KEY, id = Keyboard.KeyId.L)
public class EventualSkillsButton extends BindButtonEvent {
}

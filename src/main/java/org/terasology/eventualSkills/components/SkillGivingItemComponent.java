// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.eventualSkills.components;

import org.terasology.gestalt.entitysystem.component.Component;

public class SkillGivingItemComponent implements Component<SkillGivingItemComponent> {
    public String skill;
    public Integer level;
}

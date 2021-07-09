// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.eventualSkills.components;

import com.google.common.collect.Maps;
import org.terasology.engine.network.Replicate;
import org.terasology.gestalt.assets.ResourceUrn;
import org.terasology.gestalt.entitysystem.component.Component;

import java.util.Map;

public class EntitySkillsComponent implements Component<EntitySkillsComponent> {

    // a map of the skill and the level to which it has been learned.  These must always be lowercased, otherwise chaos.
    @Replicate
    public Map<String, Integer> learnedSkills = Maps.newHashMap();

    public boolean hasSkill(ResourceUrn skillUrn, int level) {
        return getSkillLevel(skillUrn) >= level;
    }

    public int getSkillLevel(ResourceUrn skillUrn) {
        if (learnedSkills.containsKey(skillUrn.toString().toLowerCase())) {
            return learnedSkills.get(skillUrn.toString().toLowerCase());
        } else {
            return 0;
        }
    }
}

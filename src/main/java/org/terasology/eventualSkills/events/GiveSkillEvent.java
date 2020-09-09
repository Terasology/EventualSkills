// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.eventualSkills.events;

import org.terasology.engine.entitySystem.event.Event;
import org.terasology.gestalt.assets.ResourceUrn;

public class GiveSkillEvent implements Event {
    ResourceUrn skill;
    Integer level;

    public GiveSkillEvent() {
    }

    public GiveSkillEvent(String skill) {
        this.skill = new ResourceUrn(skill);
    }

    public GiveSkillEvent(ResourceUrn skill) {
        this.skill = skill;
    }

    public GiveSkillEvent(String skill, Integer level) {
        this(skill);
        this.level = level;
    }

    public GiveSkillEvent(ResourceUrn skill, Integer level) {
        this(skill);
        this.level = level;
    }

    public ResourceUrn getSkill() {
        return skill;
    }

    public Integer getLevel() {
        return level;
    }
}

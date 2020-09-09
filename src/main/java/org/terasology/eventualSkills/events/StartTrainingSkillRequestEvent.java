// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.eventualSkills.events;

import org.terasology.engine.entitySystem.event.Event;
import org.terasology.engine.network.ServerEvent;
import org.terasology.gestalt.assets.ResourceUrn;

@ServerEvent
public class StartTrainingSkillRequestEvent implements Event {
    public String skill;

    public StartTrainingSkillRequestEvent(ResourceUrn skill) {
        this.skill = skill.toString();
    }

    public StartTrainingSkillRequestEvent() {

    }
}

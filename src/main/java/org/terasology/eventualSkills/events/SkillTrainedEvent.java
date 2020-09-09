// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.eventualSkills.events;

import org.terasology.engine.entitySystem.event.Event;
import org.terasology.gestalt.assets.ResourceUrn;

/**
 * This event is fired when a skill has completed training
 */
public class SkillTrainedEvent implements Event {
    ResourceUrn skillTrained;
    int levelTrained;

    public SkillTrainedEvent(ResourceUrn skillTrained, int levelTrained) {
        this.skillTrained = skillTrained;
        this.levelTrained = levelTrained;
    }

    public ResourceUrn getSkillTrained() {
        return skillTrained;
    }

    public int getLevelTrained() {
        return levelTrained;
    }
}

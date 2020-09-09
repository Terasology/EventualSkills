// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.eventualSkills.events;

import org.terasology.engine.entitySystem.event.Event;
import org.terasology.engine.network.OwnerEvent;
import org.terasology.gestalt.assets.ResourceUrn;

/**
 * This event is fired to the owner when a skill has completed training so that the UI can ping the player
 */
@OwnerEvent
public class SkillTrainedOwnerEvent implements Event {
    ResourceUrn skillTrained;
    int levelTrained;

    public SkillTrainedOwnerEvent() {
    }

    public SkillTrainedOwnerEvent(ResourceUrn skillTrained, int levelTrained) {
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

/*
 * Copyright 2016 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.terasology.eventualSkills.events;

import org.terasology.entitySystem.event.Event;
import org.terasology.gestalt.assets.ResourceUrn;
import org.terasology.network.OwnerEvent;

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
